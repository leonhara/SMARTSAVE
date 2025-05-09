package smartsave.modelo;

import java.util.ArrayList;
import java.util.List;

public class PerfilNutricional {
    private Long id;
    private Long usuarioId;
    private int edad;
    private double peso;  // en kg
    private double altura;  // en cm
    private String sexo;  // "M" o "F"
    private String nivelActividad;  // "Sedentario", "Ligero", "Moderado", "Intenso", "Muy intenso"
    private List<String> restricciones;  // Lista de restricciones alimentarias
    private int caloriasDiarias;  // Calculado según los datos biométricos
    private double imc;  // Índice de Masa Corporal

    // Constructor por defecto
    public PerfilNutricional() {
        this.restricciones = new ArrayList<>();
    }

    // Constructor con parámetros básicos
    public PerfilNutricional(Long usuarioId, int edad, double peso, double altura, String sexo, String nivelActividad) {
        this.usuarioId = usuarioId;
        this.edad = edad;
        this.peso = peso;
        this.altura = altura;
        this.sexo = sexo;
        this.nivelActividad = nivelActividad;
        this.restricciones = new ArrayList<>();

        // Calcular IMC y calorías diarias al crear el perfil
        calcularIMC();
        calcularCaloriasDiarias();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
        calcularCaloriasDiarias();  // Recalcular calorías al cambiar la edad
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
        calcularIMC();  // Recalcular IMC al cambiar el peso
        calcularCaloriasDiarias();  // Recalcular calorías al cambiar el peso
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
        calcularIMC();  // Recalcular IMC al cambiar la altura
        calcularCaloriasDiarias();  // Recalcular calorías al cambiar la altura
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
        calcularCaloriasDiarias();  // Recalcular calorías al cambiar el sexo
    }

    public String getNivelActividad() {
        return nivelActividad;
    }

    public void setNivelActividad(String nivelActividad) {
        this.nivelActividad = nivelActividad;
        calcularCaloriasDiarias();  // Recalcular calorías al cambiar el nivel de actividad
    }

    public List<String> getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(List<String> restricciones) {
        this.restricciones = restricciones;
    }

    public void agregarRestriccion(String restriccion) {
        if (!this.restricciones.contains(restriccion)) {
            this.restricciones.add(restriccion);
        }
    }

    public void eliminarRestriccion(String restriccion) {
        this.restricciones.remove(restriccion);
    }

    public int getCaloriasDiarias() {
        return caloriasDiarias;
    }

    public double getImc() {
        return imc;
    }

    // Métodos de cálculo

    /**
     * Calcula el Índice de Masa Corporal (IMC)
     * IMC = peso (kg) / (altura (m))^2
     */
    private void calcularIMC() {
        // Convertir altura de cm a m
        double alturaEnMetros = this.altura / 100.0;
        this.imc = this.peso / (alturaEnMetros * alturaEnMetros);
    }

    /**
     * Calcula las calorías diarias recomendadas basadas en la fórmula de Harris-Benedict
     * https://www.calculator.net/calorie-calculator.html
     */
    private void calcularCaloriasDiarias() {
        double tmb; // Tasa Metabólica Basal

        // Fórmula Harris-Benedict revisada
        if ("M".equals(this.sexo)) {
            // Hombres: TMB = 88.362 + (13.397 × peso en kg) + (4.799 × altura en cm) - (5.677 × edad en años)
            tmb = 88.362 + (13.397 * this.peso) + (4.799 * this.altura) - (5.677 * this.edad);
        } else {
            // Mujeres: TMB = 447.593 + (9.247 × peso en kg) + (3.098 × altura en cm) - (4.330 × edad en años)
            tmb = 447.593 + (9.247 * this.peso) + (3.098 * this.altura) - (4.330 * this.edad);
        }

        // Aplicar factor de actividad
        double factorActividad;
        switch (this.nivelActividad) {
            case "Sedentario":  // Poco o ningún ejercicio
                factorActividad = 1.2;
                break;
            case "Ligero":  // Ejercicio ligero 1-3 días a la semana
                factorActividad = 1.375;
                break;
            case "Moderado":  // Ejercicio moderado 3-5 días a la semana
                factorActividad = 1.55;
                break;
            case "Intenso":  // Ejercicio intenso 6-7 días a la semana
                factorActividad = 1.725;
                break;
            case "Muy intenso":  // Ejercicio muy intenso o trabajo físico
                factorActividad = 1.9;
                break;
            default:
                factorActividad = 1.2;
                break;
        }

        this.caloriasDiarias = (int) Math.round(tmb * factorActividad);
    }

    /**
     * Obtiene la categoría de peso según el IMC
     * @return Categoría de peso
     */
    public String getCategoriaIMC() {
        if (imc < 18.5) {
            return "Bajo peso";
        } else if (imc < 25) {
            return "Normal";
        } else if (imc < 30) {
            return "Sobrepeso";
        } else {
            return "Obesidad";
        }
    }

    /**
     * Obtiene la distribución de macronutrientes recomendada (en gramos)
     * @return Mapa con los gramos diarios recomendados de proteínas, carbohidratos y grasas
     */
    public MacronutrientesDiarios getMacronutrientesDiarios() {
        // Distribución típica: 30% proteínas, 40% carbohidratos, 30% grasas
        double proteinas = (caloriasDiarias * 0.30) / 4; // 4 calorías por gramo de proteína
        double carbohidratos = (caloriasDiarias * 0.40) / 4; // 4 calorías por gramo de carbohidrato
        double grasas = (caloriasDiarias * 0.30) / 9; // 9 calorías por gramo de grasa

        return new MacronutrientesDiarios(
                Math.round(proteinas),
                Math.round(carbohidratos),
                Math.round(grasas)
        );
    }

    // Clase interna para los macronutrientes
    public static class MacronutrientesDiarios {
        private final long proteinas;
        private final long carbohidratos;
        private final long grasas;

        public MacronutrientesDiarios(long proteinas, long carbohidratos, long grasas) {
            this.proteinas = proteinas;
            this.carbohidratos = carbohidratos;
            this.grasas = grasas;
        }

        public long getProteinas() {
            return proteinas;
        }

        public long getCarbohidratos() {
            return carbohidratos;
        }

        public long getGrasas() {
            return grasas;
        }
    }
}