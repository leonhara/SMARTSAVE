package smartsave.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacciones")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion tipo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    public enum TipoTransaccion {
        Ingreso,
        Gasto
    }

    public Transaccion() {
        this.fecha = LocalDate.now();
        this.monto = BigDecimal.ZERO;
    }

    public Transaccion(LocalDate fecha, String descripcion, String categoria, double monto, String tipo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.monto = BigDecimal.valueOf(monto);
        this.tipo = TipoTransaccion.valueOf(tipo);
    }

    public Transaccion(Long id, LocalDate fecha, String descripcion, String categoria, double monto, String tipo, Long usuarioId) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.monto = BigDecimal.valueOf(monto);
        this.tipo = TipoTransaccion.valueOf(tipo);
        this.usuarioId = usuarioId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getMontoBD() { return monto; }
    public void setMontoBD(BigDecimal monto) { this.monto = monto; }
    public double getMonto() { return monto != null ? monto.doubleValue() : 0.0; }
    public void setMonto(double monto) { this.monto = BigDecimal.valueOf(monto); }

    public String getTipo() { return tipo.name(); }
    public void setTipo(String tipo) { this.tipo = TipoTransaccion.valueOf(tipo); }

    public TipoTransaccion getTipoEnum() { return tipo; }
    public void setTipoEnum(TipoTransaccion tipo) { this.tipo = tipo; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getFechaFormateada() {
        return fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getMontoFormateado() {
        return String.format("€%.2f", getMonto());
    }

    public double getMontoConSigno() {
        return tipo == TipoTransaccion.Gasto ? -getMonto() : getMonto();
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