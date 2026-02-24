package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.PerfilNutricional;

import java.util.Arrays;
import java.util.List;

public class PerfilNutricionalServicio {

    public PerfilNutricional guardarPerfil(PerfilNutricional perfil) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            PerfilNutricional perfilExistente = obtenerPerfilPorUsuario(perfil.getUsuarioId());
            if (perfilExistente != null) {
                perfil.setId(perfilExistente.getId());
                session.merge(perfil);
            } else {
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

    public PerfilNutricional obtenerPerfilPorUsuario(Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<PerfilNutricional> query = session.createQuery(
                    "FROM PerfilNutricional p WHERE p.usuarioId = :usuarioId",
                    PerfilNutricional.class);
            query.setParameter("usuarioId", usuarioId);

            List<PerfilNutricional> resultados = query.getResultList();
            if (!resultados.isEmpty()) {
                PerfilNutricional perfil = resultados.get(0);
                perfil.getRestricciones().size();
                return perfil;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo perfil por usuario: " + e.getMessage(), e);
        }
    }

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

    public List<String> obtenerNivelesActividad() {
        return Arrays.asList(
                "Sedentario", "Ligero", "Moderado", "Intenso", "Muy intenso"
        );
    }

    public List<String> obtenerRestriccionesAlimentarias() {
        return Arrays.asList(
                "Sin gluten", "Sin lactosa", "Vegano", "Vegetariano",
                "Sin azúcar", "Bajo en sodio", "Sin frutos secos", "Sin mariscos"
        );
    }

    public int calcularPuntuacionNutricional(PerfilNutricional perfil) {
        if (perfil == null) {
            return 0;
        }

        int puntuacion = 75;

        double imc = perfil.getImc();
        if (imc >= 18.5 && imc < 25) {
            puntuacion += 25;
        } else if (imc >= 25 && imc < 30) {
            puntuacion += 10;
        } else if (imc >= 30) {
            puntuacion -= 10;
        } else {
            puntuacion += 0;
        }

        puntuacion -= perfil.getRestricciones().size() * 2;

        return Math.max(0, Math.min(100, puntuacion));
    }

    public String generarRecomendacionAlimentaria(PerfilNutricional perfil) {
        if (perfil == null) {
            return "No se encontró perfil nutricional. Por favor, crea tu perfil para recibir recomendaciones personalizadas.";
        }

        StringBuilder recomendacion = new StringBuilder();

        recomendacion.append("Tu consumo calórico diario recomendado es de ")
                .append(perfil.getCaloriasDiarias())
                .append(" calorías.\n\n");

        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
        recomendacion.append("Distribución diaria recomendada de macronutrientes:\n")
                .append("• Proteínas: ").append(macros.getProteinas()).append("g\n")
                .append("• Carbohidratos: ").append(macros.getCarbohidratos()).append("g\n")
                .append("• Grasas: ").append(macros.getGrasas()).append("g\n\n");

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

        if (!perfil.getRestricciones().isEmpty()) {
            recomendacion.append("\n\nConsiderando tus restricciones alimentarias (");
            recomendacion.append(String.join(", ", perfil.getRestricciones()));
            recomendacion.append("), asegúrate de buscar alternativas adecuadas para mantener una dieta equilibrada.");
        }

        return recomendacion.toString();
    }
}