package smartsave.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "items_compra")
public class ItemCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaCompra lista;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(nullable = false)
    private boolean comprado = false;

    @Column(columnDefinition = "TEXT")
    private String notas;

    public ItemCompra() {
        this.cantidad = 1;
        this.comprado = false;
    }

    public ItemCompra(Producto producto) {
        this.producto = producto;
        this.cantidad = 1;
        this.comprado = false;
    }

    public ItemCompra(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.comprado = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ListaCompra getLista() { return lista; }
    public void setLista(ListaCompra lista) { this.lista = lista; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public boolean isComprado() { return comprado; }
    public void setComprado(boolean comprado) { this.comprado = comprado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public double getPrecioTotal() {
        return producto.getPrecio() * cantidad;
    }


    public double getCaloriasTotales() {
        return producto.getInfoNutricional().getCalorias() * cantidad;
    }


    public double getProteinasTotales() {
        return producto.getInfoNutricional().getProteinas() * cantidad;
    }

    public double getCarbohidratosTotales() {
        return producto.getInfoNutricional().getCarbohidratos() * cantidad;
    }

    public double getGrasasTotales() {
        return producto.getInfoNutricional().getGrasas() * cantidad;
    }

    public void incrementarCantidad() {
        this.cantidad++;
    }

    public void decrementarCantidad() {
        if (this.cantidad > 1) {
            this.cantidad--;
        }
    }

    @Override
    public String toString() {
        return cantidad + "x " + producto.getNombre() + " - " + String.format("%.2f€", getPrecioTotal());
    }
}