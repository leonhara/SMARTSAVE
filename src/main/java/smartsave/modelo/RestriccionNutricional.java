package smartsave.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "restricciones_nutricionales")
public class RestriccionNutricional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private PerfilNutricional perfil;

    @Column(nullable = false)
    private String restriccion;

    public RestriccionNutricional() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PerfilNutricional getPerfil() { return perfil; }
    public void setPerfil(PerfilNutricional perfil) { this.perfil = perfil; }

    public String getRestriccion() { return restriccion; }
    public void setRestriccion(String restriccion) { this.restriccion = restriccion; }
}
