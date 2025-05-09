package smartsave.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaccion {
    private Long id;
    private LocalDate fecha;
    private String descripcion;
    private String categoria;
    private double monto;
    private String tipo; // "Ingreso" o "Gasto"
    private Long usuarioId; // Para asociar la transacción con un usuario

    // Constructor sin argumentos
    public Transaccion() {
        this.fecha = LocalDate.now();
    }

    // Constructor con argumentos básicos
    public Transaccion(LocalDate fecha, String descripcion, String categoria, double monto, String tipo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.monto = monto;
        this.tipo = tipo;
    }

    // Constructor completo
    public Transaccion(Long id, LocalDate fecha, String descripcion, String categoria, double monto, String tipo, Long usuarioId) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.monto = monto;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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

    // Método para obtener el monto con signo según el tipo
    public double getMontoConSigno() {
        return "Gasto".equals(tipo) ? -monto : monto;
    }

    @Override
    public String toString() {
        return "Transaccion{" +
                "fecha=" + getFechaFormateada() +
                ", descripcion='" + descripcion + '\'' +
                ", categoria='" + categoria + '\'' +
                ", monto=" + getMontoFormateado() +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}