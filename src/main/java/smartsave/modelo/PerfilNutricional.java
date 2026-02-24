package smartsave.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfiles_nutricionales")
public class PerfilNutricional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private int edad;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal altura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    @Column(name = "nivel_actividad", nullable = false)
    private String nivelActividad;

    @Column(name = "calorias_diarias", nullable = false)
    private int caloriasDiarias;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal imc;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RestriccionNutricional> restriccionesEntidades = new ArrayList<>();

    public enum Sexo {
        M, F
    }

    public PerfilNutricional() {}

    public PerfilNutricional(Long usuarioId, int edad, double peso, double altura, String sexo, String nivelActividad) {
        this.usuarioId = usuarioId;
        this.edad = edad;
        this.peso = BigDecimal.valueOf(peso);
        this.altura = BigDecimal.valueOf(altura);
        this.sexo = Sexo.valueOf(sexo);
        this.nivelActividad = nivelActividad;
        calcularIMC();
        calcularCaloriasDiarias();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) {
        this.edad = edad;
        calcularCaloriasDiarias();
    }

    public BigDecimal getPesoBD() { return peso; }
    public void setPesoBD(BigDecimal peso) {
        this.peso = peso;
        calcularIMC();
        calcularCaloriasDiarias();
    }

    public double getPeso() { return peso != null ? peso.doubleValue() : 0.0; }
    public void setPeso(double peso) {
        this.peso = BigDecimal.valueOf(peso);
        calcularIMC();
        calcularCaloriasDiarias();
    }

    public BigDecimal getAlturaBD() { return altura; }
    public void setAlturaBD(BigDecimal altura) {
        this.altura = altura;
        calcularIMC();
        calcularCaloriasDiarias();
    }

    public double getAltura() { return altura != null ? altura.doubleValue() : 0.0; }
    public void setAltura(double altura) {
        this.altura = BigDecimal.valueOf(altura);
        calcularIMC();
        calcularCaloriasDiarias();
    }

    public String getSexo() { return sexo.name(); }
    public void setSexo(String sexo) {
        this.sexo = Sexo.valueOf(sexo);
        calcularCaloriasDiarias();
    }

    public Sexo getSexoEnum() { return sexo; }
    public void setSexoEnum(Sexo sexo) {
        this.sexo = sexo;
        calcularCaloriasDiarias();
    }

    public String getNivelActividad() { return nivelActividad; }
    public void setNivelActividad(String nivelActividad) {
        this.nivelActividad = nivelActividad;
        calcularCaloriasDiarias();
    }

    public List<String> getRestricciones() {
        List<String> restricciones = new ArrayList<>();
        for (RestriccionNutricional entidad : restriccionesEntidades) {
            restricciones.add(entidad.getRestriccion());
        }
        return restricciones;
    }

    public void setRestricciones(List<String> restricciones) {
        this.restriccionesEntidades.clear();

        for (String restriccion : restricciones) {
            agregarRestriccion(restriccion);
        }
    }

    public void agregarRestriccion(String restriccion) {
        boolean existe = restriccionesEntidades.stream()
                .anyMatch(r -> r.getRestriccion().equals(restriccion));

        if (!existe) {
            RestriccionNutricional entidad = new RestriccionNutricional();
            entidad.setPerfil(this);
            entidad.setRestriccion(restriccion);
            restriccionesEntidades.add(entidad);
        }
    }

    public void eliminarRestriccion(String restriccion) {
        restriccionesEntidades.removeIf(r -> r.getRestriccion().equals(restriccion));
    }

    public int getCaloriasDiarias() { return caloriasDiarias; }

    public BigDecimal getImcBD() { return imc; }
    public double getImc() { return imc != null ? imc.doubleValue() : 0.0; }

    private void calcularIMC() {
        if (peso != null && altura != null && altura.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal alturaEnMetros = altura.divide(BigDecimal.valueOf(100));
            this.imc = peso.divide(alturaEnMetros.multiply(alturaEnMetros), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    private void calcularCaloriasDiarias() {
        if (peso == null || altura == null) return;

        double pesoD = peso.doubleValue();
        double alturaD = altura.doubleValue();
        double tmb;

        if (sexo == Sexo.M) {
            tmb = 88.362 + (13.397 * pesoD) + (4.799 * alturaD) - (5.677 * this.edad);
        } else {
            tmb = 447.593 + (9.247 * pesoD) + (3.098 * alturaD) - (4.330 * this.edad);
        }

        double factorActividad;
        switch (this.nivelActividad) {
            case "Sedentario": factorActividad = 1.2; break;
            case "Ligero": factorActividad = 1.375; break;
            case "Moderado": factorActividad = 1.55; break;
            case "Intenso": factorActividad = 1.725; break;
            case "Muy intenso": factorActividad = 1.9; break;
            default: factorActividad = 1.2; break;
        }

        this.caloriasDiarias = (int) Math.round(tmb * factorActividad);
    }

    public String getCategoriaIMC() {
        double imcValue = getImc();
        if (imcValue < 18.5) return "Bajo peso";
        else if (imcValue < 25) return "Normal";
        else if (imcValue < 30) return "Sobrepeso";
        else return "Obesidad";
    }

    public MacronutrientesDiarios getMacronutrientesDiarios() {
        double proteinas = (caloriasDiarias * 0.30) / 4;
        double carbohidratos = (caloriasDiarias * 0.40) / 4;
        double grasas = (caloriasDiarias * 0.30) / 9;

        return new MacronutrientesDiarios(
                Math.round(proteinas),
                Math.round(carbohidratos),
                Math.round(grasas)
        );
    }

    public static class MacronutrientesDiarios {
        private final long proteinas;
        private final long carbohidratos;
        private final long grasas;

        public MacronutrientesDiarios(long proteinas, long carbohidratos, long grasas) {
            this.proteinas = proteinas;
            this.carbohidratos = carbohidratos;
            this.grasas = grasas;
        }

        public long getProteinas() { return proteinas; }
        public long getCarbohidratos() { return carbohidratos; }
        public long getGrasas() { return grasas; }
    }
}