package smartsave.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

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

    @Column(name = "factor_presupuesto", nullable = false, precision = 3, scale = 2)
    private BigDecimal factorPresupuesto; // porcentaje del presupuesto a utilizar

    @Column(name = "prioridad_precio", nullable = false)
    private int prioridadPrecio; // 1-10, qué tanto priorizar precio vs calidad

    @Column(name = "prioridad_nutricion", nullable = false)
    private int prioridadNutricion; // 1-10, qué tanto priorizar nutrición vs precio

    // Constructor vacío
    public ModalidadAhorro() {
    }

    // Constructor con parámetros - actualizado para usar BigDecimal
    public ModalidadAhorro(String nombre, String descripcion, double factorPresupuesto,
                           int prioridadPrecio, int prioridadNutricion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorPresupuesto = BigDecimal.valueOf(factorPresupuesto);
        this.prioridadPrecio = prioridadPrecio;
        this.prioridadNutricion = prioridadNutricion;
    }

    // Constructor con BigDecimal
    public ModalidadAhorro(String nombre, String descripcion, BigDecimal factorPresupuesto,
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

    // Getter para BigDecimal
    public BigDecimal getFactorPresupuesto() {
        return factorPresupuesto;
    }

    // Setter para BigDecimal
    public void setFactorPresupuesto(BigDecimal factorPresupuesto) {
        this.factorPresupuesto = factorPresupuesto;
    }

    // Métodos adicionales para compatibilidad con double
    public double getFactorPresupuestoAsDouble() {
        return factorPresupuesto != null ? factorPresupuesto.doubleValue() : 0.0;
    }

    public void setFactorPresupuesto(double factorPresupuesto) {
        this.factorPresupuesto = BigDecimal.valueOf(factorPresupuesto);
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