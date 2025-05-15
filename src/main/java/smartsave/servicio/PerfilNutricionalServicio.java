package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.PerfilNutricional;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio para gestionar operaciones relacionadas con perfiles nutricionales
 * MIGRADO A HIBERNATE - Reemplaza estructuras en memoria
 */
public class PerfilNutricionalServicio {

    /**
     * Crea o actualiza el perfil nutricional de un usuario
     * @param perfil El perfil nutricional a guardar
     * @return El perfil nutricional guardado
     */
    public PerfilNutricional guardarPerfil(PerfilNutricional perfil) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Si el usuario ya tiene un perfil, actualizarlo
            PerfilNutricional perfilExistente = obtenerPerfilPorUsuario(perfil.getUsuarioId());
            if (perfilExistente != null) {
                perfil.setId(perfilExistente.getId());
                session.merge(perfil);
            } else {
                // Si es un nuevo perfil, guardarlo
                session.save(perfil);
            }

            transaction.commit();
            return perfil;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error guardando perfil nutricional: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el perfil nutricional de un usuario
     * @param usuarioId ID del usuario
     * @return El perfil nutricional o null si no existe
     */
    public PerfilNutricional obtenerPerfilPorUsuario(Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<PerfilNutricional> query = session.createQuery(
                    "FROM PerfilNutricional p WHERE p.usuarioId = :usuarioId",
                    PerfilNutricional.class);
            query.setParameter("usuarioId", usuarioId);

            List<PerfilNutricional> resultados = query.getResultList();
            if (!resultados.isEmpty()) {
                PerfilNutricional perfil = resultados.get(0);
                // Forzar carga de restricciones para evitar LazyLoadingException
                perfil.getRestricciones().size();
                return perfil;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo perfil por usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un usuario tiene perfil nutricional
     * @param usuarioId ID del usuario
     * @return true si el usuario tiene perfil nutricional
     */
    public boolean tienePerfil(Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(p) FROM PerfilNutricional p WHERE p.usuarioId = :usuarioId",
                    Long.class);
            query.setParameter("usuarioId", usuarioId);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error verificando existencia de perfil: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina el perfil nutricional de un usuario
     * @param usuarioId ID del usuario
     * @return true si se eliminó correctamente
     */
    public boolean eliminarPerfil(Long usuarioId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<PerfilNutricional> query = session.createQuery(
                    "FROM PerfilNutricional p WHERE p.usuarioId = :usuarioId",
                    PerfilNutricional.class);
            query.setParameter("usuarioId", usuarioId);

            List<PerfilNutricional> perfiles = query.getResultList();
            if (!perfiles.isEmpty()) {
                session.delete(perfiles.get(0));
                transaction.commit();
                return true;
            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error eliminando perfil: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los niveles de actividad física disponibles
     * @return Lista de niveles de actividad
     */
    public List<String> obtenerNivelesActividad() {
        return Arrays.asList(
                "Sedentario", "Ligero", "Moderado", "Intenso", "Muy intenso"
        );
    }

    /**
     * Obtiene las restricciones alimentarias comunes
     * @return Lista de restricciones alimentarias
     */
    public List<String> obtenerRestriccionesAlimentarias() {
        return Arrays.asList(
                "Sin gluten", "Sin lactosa", "Vegano", "Vegetariano",
                "Sin azúcar", "Bajo en sodio", "Sin frutos secos", "Sin mariscos"
        );
    }

    /**
     * Calcula la puntuación nutricional del usuario (0-100)
     * Este método simula una evaluación basada en las restricciones y el IMC
     * @param perfil El perfil nutricional a evaluar
     * @return Puntuación de 0 a 100
     */
    public int calcularPuntuacionNutricional(PerfilNutricional perfil) {
        if (perfil == null) {
            return 0;
        }

        int puntuacion = 75; // Base de 75 puntos

        // Ajuste por IMC
        double imc = perfil.getImc();
        if (imc >= 18.5 && imc < 25) {
            // IMC normal
            puntuacion += 25;
        } else if (imc >= 25 && imc < 30) {
            // Sobrepeso
            puntuacion += 10;
        } else if (imc >= 30) {
            // Obesidad
            puntuacion -= 10;
        } else {
            // Bajo peso
            puntuacion += 0;
        }

        // Ajuste por restricciones (cada restricción reduce ligeramente la puntuación)
        puntuacion -= perfil.getRestricciones().size() * 2;

        // Asegurar que la puntuación esté en el rango 0-100
        return Math.max(0, Math.min(100, puntuacion));
    }

    /**
     * Genera una recomendación alimentaria basada en el perfil nutricional
     * @param perfil El perfil nutricional
     * @return Texto con recomendaciones generales
     */
    public String generarRecomendacionAlimentaria(PerfilNutricional perfil) {
        if (perfil == null) {
            return "No se encontró perfil nutricional. Por favor, crea tu perfil para recibir recomendaciones personalizadas.";
        }

        StringBuilder recomendacion = new StringBuilder();

        // Recomendación basada en calorías
        recomendacion.append("Tu consumo calórico diario recomendado es de ")
                .append(perfil.getCaloriasDiarias())
                .append(" calorías.\n\n");

        // Recomendación basada en macronutrientes
        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
        recomendacion.append("Distribución diaria recomendada de macronutrientes:\n")
                .append("• Proteínas: ").append(macros.getProteinas()).append("g\n")
                .append("• Carbohidratos: ").append(macros.getCarbohidratos()).append("g\n")
                .append("• Grasas: ").append(macros.getGrasas()).append("g\n\n");

        // Recomendación basada en IMC
        String categoriaIMC = perfil.getCategoriaIMC();
        recomendacion.append("Según tu IMC de ").append(String.format("%.1f", perfil.getImc()))
                .append(" (").append(categoriaIMC).append("), ");

        switch (categoriaIMC) {
            case "Bajo peso":
                recomendacion.append("te recomendamos aumentar el consumo de alimentos nutritivos y densos en calorías como frutos secos, aguacate y proteínas magras.");
                break;
            case "Normal":
                recomendacion.append("estás en un peso saludable. Mantén una dieta equilibrada y variada.");
                break;
            case "Sobrepeso":
                recomendacion.append("considera reducir ligeramente tu ingesta calórica y aumentar la actividad física.");
                break;
            case "Obesidad":
                recomendacion.append("te recomendamos consultar con un nutricionista para crear un plan personalizado que te ayude a alcanzar un peso más saludable.");
                break;
        }

        // Recomendaciones basadas en restricciones
        if (!perfil.getRestricciones().isEmpty()) {
            recomendacion.append("\n\nConsiderando tus restricciones alimentarias (");
            recomendacion.append(String.join(", ", perfil.getRestricciones()));
            recomendacion.append("), asegúrate de buscar alternativas adecuadas para mantener una dieta equilibrada.");
        }

        return recomendacion.toString();
    }
}