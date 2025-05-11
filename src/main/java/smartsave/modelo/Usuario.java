package smartsave.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String contrasenaHash;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(name = "ultimo_login")
    private LocalDate ultimoLogin;

    // Nueva columna para la modalidad de ahorro
    @Column(name = "modalidad_ahorro")
    private String modalidadAhorroSeleccionada; // "Máximo", "Equilibrado" o "Estándar"

    // Constructor sin argumentos para JPA
    public Usuario() {
        this.modalidadAhorroSeleccionada = "Equilibrado"; // Valor por defecto
    }

    // Constructor con argumentos
    public Usuario(String email, String nombre, String apellidos, String contrasenaHash) {
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasenaHash = contrasenaHash;
        this.fechaRegistro = LocalDate.now();
        this.modalidadAhorroSeleccionada = "Equilibrado"; // Valor por defecto
    }

    // Constructor completo
    public Usuario(String email, String nombre, String apellidos, String contrasenaHash, String modalidadAhorro) {
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasenaHash = contrasenaHash;
        this.fechaRegistro = LocalDate.now();
        this.modalidadAhorroSeleccionada = modalidadAhorro;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDate getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDate ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public String getModalidadAhorroSeleccionada() {
        return modalidadAhorroSeleccionada;
    }

    public void setModalidadAhorroSeleccionada(String modalidadAhorroSeleccionada) {
        this.modalidadAhorroSeleccionada = modalidadAhorroSeleccionada;
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public void actualizarUltimoLogin() {
        this.ultimoLogin = LocalDate.now();
    }

    // Método para verificar si ha seleccionado una modalidad de ahorro
    public boolean tieneModalidadAhorro() {
        return modalidadAhorroSeleccionada != null && !modalidadAhorroSeleccionada.isEmpty();
    }

    // Método para obtener el factor de presupuesto según la modalidad
    public double getFactorPresupuesto() {
        switch (modalidadAhorroSeleccionada) {
            case "Máximo":
                return 0.7;
            case "Equilibrado":
                return 0.85;
            case "Estándar":
                return 1.0;
            default:
                return 0.85; // Equilibrado por defecto
        }
    }
}