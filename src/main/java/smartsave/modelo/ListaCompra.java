package smartsave.modelo;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listas_compra")
public class ListaCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fecha_planificada")
    private LocalDate fechaPlanificada;

    @Column(name = "modalidad_ahorro", nullable = false)
    private String modalidadAhorro;

    @Column(name = "presupuesto_maximo", nullable = false, precision = 10, scale = 2)
    private BigDecimal presupuestoMaximo;

    @Column(nullable = false)
    private boolean completada = false;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItemCompra> items = new ArrayList<>();

    @Column(name = "transaccion_id_asociada")
    private Long transaccionIdAsociada;

    public ListaCompra() {
        this.fechaCreacion = LocalDate.now();
        this.items = new ArrayList<>();
        this.completada = false;
    }

    public ListaCompra(Long usuarioId, String nombre) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.fechaCreacion = LocalDate.now();
        this.items = new ArrayList<>();
        this.completada = false;
    }

    public ListaCompra(Long usuarioId, String nombre, String modalidadAhorro, BigDecimal presupuestoMaximo) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.modalidadAhorro = modalidadAhorro;
        this.presupuestoMaximo = presupuestoMaximo;
        this.fechaCreacion = LocalDate.now();
        this.items = new ArrayList<>();
        this.completada = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDate getFechaPlanificada() { return fechaPlanificada; }
    public void setFechaPlanificada(LocalDate fechaPlanificada) { this.fechaPlanificada = fechaPlanificada; }

    public String getModalidadAhorro() { return modalidadAhorro; }
    public void setModalidadAhorro(String modalidadAhorro) { this.modalidadAhorro = modalidadAhorro; }

    public BigDecimal getPresupuestoMaximo() {
        return presupuestoMaximo;
    }

    public void setPresupuestoMaximo(BigDecimal presupuestoMaximo) {
        this.presupuestoMaximo = presupuestoMaximo;
    }

    public double getPresupuestoMaximoAsDouble() {
        return presupuestoMaximo != null ? presupuestoMaximo.doubleValue() : 0.0;
    }

    public void setPresupuestoMaximo(double presupuesto) {
        this.presupuestoMaximo = BigDecimal.valueOf(presupuesto);
    }

    public List<ItemCompra> getItems() { return items; }
    public void setItems(List<ItemCompra> items) { this.items = items; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public Long getTransaccionIdAsociada() {
        return transaccionIdAsociada;
    }

    public void setTransaccionIdAsociada(Long transaccionIdAsociada) {
        this.transaccionIdAsociada = transaccionIdAsociada;
    }

    public void agregarItem(ItemCompra item) {
        item.setLista(this);
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public boolean eliminarItem(ItemCompra item) {
        return items.remove(item);
    }

    public double getCosteTotal() {
        return items.stream()
                .mapToDouble(ItemCompra::getPrecioTotal)
                .sum();
    }

    public double getCaloriasTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getCaloriasTotales)
                .sum();
    }

    public double getProteinasTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getProteinasTotales)
                .sum();
    }

    public double getCarbohidratosTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getCarbohidratosTotales)
                .sum();
    }

    public double getGrasasTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getGrasasTotales)
                .sum();
    }

    public boolean estaDentroPresupuesto() {
        if (presupuestoMaximo == null) return false;
        return BigDecimal.valueOf(getCosteTotal()).compareTo(presupuestoMaximo) <= 0;
    }

    public int getNumeroItems() {
        return items.size();
    }

    public int getNumeroItemsComprados() {
        return (int) items.stream()
                .filter(ItemCompra::isComprado)
                .count();
    }

    public int getPorcentajeProgreso() {
        if (items.isEmpty()) return 0;
        return (getNumeroItemsComprados() * 100) / getNumeroItems();
    }

    @Override
    public String toString() {
        return nombre + " - " + String.format("%.2f€", getCosteTotal()) +
                " (" + getPorcentajeProgreso() + "% completado)";
    }
}