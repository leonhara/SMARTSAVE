package smartsave.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ListaCompra {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private LocalDate fechaCreacion;
    private LocalDate fechaPlanificada;
    private String modalidadAhorro; // "Máximo", "Equilibrado", "Estándar"
    private double presupuestoMaximo;
    private List<ItemCompra> items;
    private boolean completada;

    // Constructores
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

    public ListaCompra(Long usuarioId, String nombre, String modalidadAhorro, double presupuestoMaximo) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.modalidadAhorro = modalidadAhorro;
        this.presupuestoMaximo = presupuestoMaximo;
        this.fechaCreacion = LocalDate.now();
        this.items = new ArrayList<>();
        this.completada = false;
    }

    // Getters y setters
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

    public double getPresupuestoMaximo() { return presupuestoMaximo; }
    public void setPresupuestoMaximo(double presupuestoMaximo) { this.presupuestoMaximo = presupuestoMaximo; }

    public List<ItemCompra> getItems() { return items; }
    public void setItems(List<ItemCompra> items) { this.items = items; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    // Métodos adicionales

    /**
     * Añade un item a la lista de compra
     * @param item Item a añadir
     */
    public void agregarItem(ItemCompra item) {
        items.add(item);
    }

    /**
     * Elimina un item de la lista de compra
     * @param item Item a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarItem(ItemCompra item) {
        return items.remove(item);
    }

    /**
     * Calcula el coste total de la lista de compra
     * @return Suma de los precios de todos los items
     */
    public double getCosteTotal() {
        return items.stream()
                .mapToDouble(ItemCompra::getPrecioTotal)
                .sum();
    }

    /**
     * Calcula el total de calorías de la lista de compra
     * @return Suma de calorías de todos los items
     */
    public double getCaloriasTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getCaloriasTotales)
                .sum();
    }

    /**
     * Calcula el total de proteínas de la lista de compra
     * @return Suma de proteínas de todos los items
     */
    public double getProteinasTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getProteinasTotales)
                .sum();
    }

    /**
     * Calcula el total de carbohidratos de la lista de compra
     * @return Suma de carbohidratos de todos los items
     */
    public double getCarbohidratosTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getCarbohidratosTotales)
                .sum();
    }

    /**
     * Calcula el total de grasas de la lista de compra
     * @return Suma de grasas de todos los items
     */
    public double getGrasasTotales() {
        return items.stream()
                .mapToDouble(ItemCompra::getGrasasTotales)
                .sum();
    }

    /**
     * Verifica si la lista de compra está dentro del presupuesto
     * @return true si el coste total es menor o igual al presupuesto máximo
     */
    public boolean estaDentroPresupuesto() {
        return getCosteTotal() <= presupuestoMaximo;
    }

    /**
     * Calcula el número de items en la lista
     * @return Número de items
     */
    public int getNumeroItems() {
        return items.size();
    }

    /**
     * Calcula el número de items marcados como comprados
     * @return Número de items comprados
     */
    public int getNumeroItemsComprados() {
        return (int) items.stream()
                .filter(ItemCompra::isComprado)
                .count();
    }

    /**
     * Calcula el porcentaje de progreso de la compra
     * @return Porcentaje de items comprados (0-100)
     */
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