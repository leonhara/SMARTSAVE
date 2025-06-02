package smartsave.api;

import org.junit.jupiter.api.Test;
import smartsave.modelo.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class MercadonaApibusquedasTest {

    @Test
    void ejecutarDiezBusquedas() {
        MercadonaApiServicio servicio = new MercadonaApiServicio("14010");

        if (!servicio.isApiDisponible()) {
            System.err.println("la api del mercadona no está disponible");
            servicio.cerrar();
            return;
        }

        List<String> busquedas = List.of("arroz", "leche", "monster", "tomates", "fresa", "bebidas", "pollo", "papel", "carne", "sal");

        List<CompletableFuture<List<Producto>>> futuras = new ArrayList<>();
        for (String termino : busquedas) {
            futuras.add(servicio.buscarProductos(termino));
        }

        CompletableFuture<Void> esperarTodas = CompletableFuture.allOf(futuras.toArray(new CompletableFuture[0]));

        try {
            esperarTodas.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
        } finally {
            servicio.cerrar();
        }
    }
}