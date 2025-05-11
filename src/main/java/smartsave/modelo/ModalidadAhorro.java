package smartsave.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "modalidades_ahorro")
public class ModalidadAhorro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private double factorPresupuesto; // porcentaje del presupuesto a utilizar (0.7 para máximo ahorro, 0.85 para equilibrado, 1.0 para estándar)

    @Column(nullable = false)
    private int prioridadPrecio; // 1-10, qué tanto priorizar precio vs calidad

    @Column(nullable = false)
    private int prioridadNutricion; // 1-10, qué tanto priorizar nutrición vs precio

    // Constructor vacío
    public ModalidadAhorro() {
    }

    // Constructor con parámetros
    public ModalidadAhorro(String nombre, String descripcion, double factorPresupuesto,
                           int prioridadPrecio, int prioridadNutricion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorPresupuesto = factorPresupuesto;
        this.prioridadPrecio = prioridadPrecio;
        this.prioridadNutricion = prioridadNutricion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getFactorPresupuesto() {
        return factorPresupuesto;
    }

    public void setFactorPresupuesto(double factorPresupuesto) {
        this.factorPresupuesto = factorPresupuesto;
    }

    public int getPrioridadPrecio() {
        return prioridadPrecio;
    }

    public void setPrioridadPrecio(int prioridadPrecio) {
        this.prioridadPrecio = prioridadPrecio;
    }

    public int getPrioridadNutricion() {
        return prioridadNutricion;
    }

    public void setPrioridadNutricion(int prioridadNutricion) {
        this.prioridadNutricion = prioridadNutricion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}