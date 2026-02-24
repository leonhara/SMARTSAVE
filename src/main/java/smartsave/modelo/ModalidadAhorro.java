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
    private BigDecimal factorPresupuesto;

    @Column(name = "prioridad_precio", nullable = false)
    private int prioridadPrecio;

    @Column(name = "prioridad_nutricion", nullable = false)
    private int prioridadNutricion;

    public ModalidadAhorro() {
    }

    public ModalidadAhorro(String nombre, String descripcion, double factorPresupuesto,
                           int prioridadPrecio, int prioridadNutricion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorPresupuesto = BigDecimal.valueOf(factorPresupuesto);
        this.prioridadPrecio = prioridadPrecio;
        this.prioridadNutricion = prioridadNutricion;
    }

    public ModalidadAhorro(String nombre, String descripcion, BigDecimal factorPresupuesto,
                           int prioridadPrecio, int prioridadNutricion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorPresupuesto = factorPresupuesto;
        this.prioridadPrecio = prioridadPrecio;
        this.prioridadNutricion = prioridadNutricion;
    }

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

    public BigDecimal getFactorPresupuesto() {
        return factorPresupuesto;
    }

    public void setFactorPresupuesto(BigDecimal factorPresupuesto) {
        this.factorPresupuesto = factorPresupuesto;
    }

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