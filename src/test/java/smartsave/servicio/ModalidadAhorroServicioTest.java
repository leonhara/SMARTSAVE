package smartsave.servicio;

import org.junit.jupiter.api.Test;
import smartsave.modelo.ModalidadAhorro;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModalidadAhorroServicioTest {

    @Test
    void calcularPresupuestoAjustado_modmaximo_da70porciento() {

        ModalidadAhorroServicio servicio = new ModalidadAhorroServicio();
        double presupuestoOriginal = 100; //este es el presupuesto original que vamos a ajustar

        ModalidadAhorro modalidadPrueba = new ModalidadAhorro(
                "Máximo Test",
                "prueba",
                0.70,
                9,
                4
        );
        double presupuestoEsperado = 100.0 * 0.70; //el resultado esperado es el 70% del presupuesto original

        //llamamos al metodo que queremos probar de la instancia del servicio
        double presupuestoAjustado = servicio.calcularPresupuestoAjustado(presupuestoOriginal, modalidadPrueba);

        //vamos a compar el resultado esperado con el resultado obtenido
        assertEquals(presupuestoEsperado, presupuestoAjustado, 0.001,
                "El presupuesto que esperamos en modalidad Equilibrado deberia ser el 70% del original");
    }

    @Test
    void calcularPresupuestoAjustado_modequilibrado_da85Porciento() {
        // 1. Preparar los datos
        ModalidadAhorroServicio servicio = new ModalidadAhorroServicio();
        double presupuestoOriginal = 100;
        ModalidadAhorro modalidadPrueba = new ModalidadAhorro(
                "Equilibrado Test",
                "prueba",
                0.85,
                6,
                7
        );
        double presupuestoEsperado = 100.0 * 0.85; //resltado esperado 85.0
        double presupuestoAjustado = servicio.calcularPresupuestoAjustado(presupuestoOriginal, modalidadPrueba);

        assertEquals(presupuestoEsperado, presupuestoAjustado, 0.001,
                "El presupuesto que esperamos en modalidad Equilibrado deberia ser el 85% del original");
    }

    @Test
    void calcularPresupuestoAjustado_modalidadNull_sincambios() {
        ModalidadAhorroServicio servicio = new ModalidadAhorroServicio();
        double presupuestoOriginal = 100.0;
        ModalidadAhorro modalidadNull = null;
        double presupuestoEsperado = 100.0;
        double presupuestoAjustado = servicio.calcularPresupuestoAjustado(presupuestoOriginal, modalidadNull);

        assertEquals(presupuestoEsperado, presupuestoAjustado, 0.001,
                "Si la modalidad es null, el presupuesto no debe cambiar.");
    }
}