package smartsave.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaccion {
    private LocalDate fecha;
    private String descripcion;
    private String categoria;
    private double monto;
    private String tipo; // "Ingreso" o "Gasto"

    public Transaccion(LocalDate fecha, String descripcion, String categoria, double monto, String tipo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.monto = monto;
        this.tipo = tipo;
    }

    // Getters y setters
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Método útil para formatear la fecha
    public String getFechaFormateada() {
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return fecha.format(formateador);
    }

    // Método útil para formatear el monto
    public String getMontoFormateado() {
        return String.format("€%.2f", monto);
    }
}