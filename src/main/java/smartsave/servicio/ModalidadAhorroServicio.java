// src/main/java/smartsave/servicio/ModalidadAhorroServicio.java
package smartsave.servicio;

import smartsave.modelo.ModalidadAhorro;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar las modalidades de ahorro
 */
public class ModalidadAhorroServicio {

    // Simulación de base de datos (se reemplazará por Hibernate)
    private static final Map<Long, ModalidadAhorro> MODALIDADES = new HashMap<>();
    private static Long ultimoId = 0L;

    // Inicializar modalidades predefinidas
    static {
        ModalidadAhorro maximoAhorro = new ModalidadAhorro(
                "Máximo",
                "Prioriza el ahorro por encima de todo. Busca las opciones más económicas aunque sacrifique algo de calidad nutricional.",
                0.7, // Utiliza solo el 70% del presupuesto para máximo ahorro
                9,   // Alta prioridad al precio (9/10)
                4    // Baja-media prioridad a la nutrición (4/10)
        );
        maximoAhorro.setId(++ultimoId);
        MODALIDADES.put(maximoAhorro.getId(), maximoAhorro);

        ModalidadAhorro equilibrado = new ModalidadAhorro(
                "Equilibrado",
                "Busca un balance entre ahorro y nutrición. Recomendado para la mayoría de usuarios.",
                0.85, // Utiliza el 85% del presupuesto
                6,    // Media-alta prioridad al precio (6/10)
                7     // Media-alta prioridad a la nutrición (7/10)
        );
        equilibrado.setId(++ultimoId);
        MODALIDADES.put(equilibrado.getId(), equilibrado);

        ModalidadAhorro estandar = new ModalidadAhorro(
                "Estándar",
                "Enfocado en la calidad nutricional manteniendo un presupuesto razonable.",
                1.0,  // Utiliza el 100% del presupuesto disponible
                3,    // Baja prioridad al precio (3/10)
                9     // Alta prioridad a la nutrición (9/10)
        );
        estandar.setId(++ultimoId);
        MODALIDADES.put(estandar.getId(), estandar);
    }

    /**
     * Obtiene todas las modalidades de ahorro disponibles
     * @return Lista de modalidades de ahorro
     */
    public List<ModalidadAhorro> obtenerTodasModalidades() {
        return new ArrayList<>(MODALIDADES.values());
    }

    /**
     * Obtiene una modalidad de ahorro por su ID
     * @param id ID de la modalidad
     * @return La modalidad de ahorro o null si no existe
     */
    public ModalidadAhorro obtenerModalidadPorId(Long id) {
        return MODALIDADES.get(id);
    }

    /**
     * Obtiene una modalidad de ahorro por su nombre
     * @param nombre Nombre de la modalidad (Máximo, Equilibrado, Estándar)
     * @return La modalidad de ahorro o null si no existe
     */
    public ModalidadAhorro obtenerModalidadPorNombre(String nombre) {
        return MODALIDADES.values().stream()
                .filter(m -> m.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcula el presupuesto ajustado según la modalidad de ahorro
     * @param presupuestoOriginal Presupuesto original
     * @param modalidad Modalidad de ahorro
     * @return Presupuesto ajustado
     */
    public double calcularPresupuestoAjustado(double presupuestoOriginal, ModalidadAhorro modalidad) {
        return presupuestoOriginal * modalidad.getFactorPresupuesto();
    }

    /**
     * Obtiene consejos de ahorro específicos para una modalidad
     * @param modalidad Modalidad de ahorro
     * @return Lista de consejos de ahorro
     */
    public List<String> obtenerConsejosAhorro(ModalidadAhorro modalidad) {
        List<String> consejos = new ArrayList<>();

        switch (modalidad.getNombre()) {
            case "Máximo":
                consejos.add("Compra productos de marca blanca o genéricos");
                consejos.add("Aprovecha ofertas y descuentos");
                consejos.add("Planifica las compras con antelación para evitar compras impulsivas");
                consejos.add("Compra alimentos de temporada");
                consejos.add("Considera comprar a granel para productos no perecederos");
                break;
            case "Equilibrado":
                consejos.add("Balancea calidad y precio en tus compras");
                consejos.add("Prioriza proteínas de buena calidad");
                consejos.add("Compra frutas y verduras de temporada");
                consejos.add("Busca ofertas en productos de calidad");
                consejos.add("Cocina en casa en lugar de comer fuera");
                break;
            case "Estándar":
                consejos.add("Invierte en alimentos de alta calidad nutricional");
                consejos.add("Prioriza alimentos frescos y orgánicos cuando sea posible");
                consejos.add("Compra productos locales para mejor frescura");
                consejos.add("Considera servicios de suscripción de cajas de productos frescos");
                consejos.add("Compara la relación calidad-precio entre marcas");
                break;
            default:
                consejos.add("Planifica tus compras con antelación");
                consejos.add("Compara precios entre supermercados");
                consejos.add("Evita el desperdicio de alimentos");
        }

        return consejos;
    }
}