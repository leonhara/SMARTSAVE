package smartsave.modelo;

public class Producto {
    private Long id;
    private String nombre;
    private String marca;
    private String categoria;
    private double precio;
    private String supermercado;
    private boolean disponible;
    private NutricionProducto infoNutricional;

    // Clase anidada para la información nutricional
    public static class NutricionProducto {
        private double calorias; // Kcal por 100g/ml
        private double proteinas; // g por 100g/ml
        private double carbohidratos; // g por 100g/ml
        private double grasas; // g por 100g/ml
        private double fibra; // g por 100g/ml
        private double sodio; // mg por 100g/ml
        private double azucares; // g por 100g/ml

        public NutricionProducto() {
        }

        public NutricionProducto(double calorias, double proteinas, double carbohidratos, double grasas) {
            this.calorias = calorias;
            this.proteinas = proteinas;
            this.carbohidratos = carbohidratos;
            this.grasas = grasas;
        }

        public NutricionProducto(double calorias, double proteinas, double carbohidratos, double grasas,
                                 double fibra, double sodio, double azucares) {
            this.calorias = calorias;
            this.proteinas = proteinas;
            this.carbohidratos = carbohidratos;
            this.grasas = grasas;
            this.fibra = fibra;
            this.sodio = sodio;
            this.azucares = azucares;
        }

        // Getters y setters
        public double getCalorias() { return calorias; }
        public void setCalorias(double calorias) { this.calorias = calorias; }

        public double getProteinas() { return proteinas; }
        public void setProteinas(double proteinas) { this.proteinas = proteinas; }

        public double getCarbohidratos() { return carbohidratos; }
        public void setCarbohidratos(double carbohidratos) { this.carbohidratos = carbohidratos; }

        public double getGrasas() { return grasas; }
        public void setGrasas(double grasas) { this.grasas = grasas; }

        public double getFibra() { return fibra; }
        public void setFibra(double fibra) { this.fibra = fibra; }

        public double getSodio() { return sodio; }
        public void setSodio(double sodio) { this.sodio = sodio; }

        public double getAzucares() { return azucares; }
        public void setAzucares(double azucares) { this.azucares = azucares; }
    }

    // Constructores
    public Producto() {
        this.infoNutricional = new NutricionProducto();
        this.disponible = true;
    }

    public Producto(String nombre, String marca, String categoria, double precio, String supermercado) {
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.supermercado = supermercado;
        this.disponible = true;
        this.infoNutricional = new NutricionProducto();
    }

    public Producto(String nombre, String marca, String categoria, double precio, String supermercado,
                    double calorias, double proteinas, double carbohidratos, double grasas) {
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.supermercado = supermercado;
        this.disponible = true;
        this.infoNutricional = new NutricionProducto(calorias, proteinas, carbohidratos, grasas);
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getSupermercado() { return supermercado; }
    public void setSupermercado(String supermercado) { this.supermercado = supermercado; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public NutricionProducto getInfoNutricional() { return infoNutricional; }
    public void setInfoNutricional(NutricionProducto infoNutricional) { this.infoNutricional = infoNutricional; }

    // Métodos adicionales

    /**
     * Calcula la relación proteína/precio (gramos de proteína por euro)
     * @return Cantidad de proteína por euro
     */
    public double getRelacionProteinaPrecio() {
        if (precio <= 0) return 0;
        return infoNutricional.getProteinas() / precio;
    }

    /**
     * Calcula la relación calorías/precio (kcal por euro)
     * @return Cantidad de calorías por euro
     */
    public double getRelacionCaloriasPrecio() {
        if (precio <= 0) return 0;
        return infoNutricional.getCalorias() / precio;
    }

    /**
     * Verifica si el producto cumple con ciertas restricciones alimentarias
     * @param restricciones Lista de restricciones a verificar
     * @return true si el producto cumple con todas las restricciones
     */
    public boolean cumpleRestricciones(java.util.List<String> restricciones) {
        // Implementación simplificada - en una versión real esto debería consultar una base de datos
        // de ingredientes y alérgenos

        // Simulamos algunas verificaciones básicas
        if (restricciones.contains("Sin gluten") && categoria.toLowerCase().contains("pan")) {
            return false;
        }

        if (restricciones.contains("Sin lactosa") &&
                (categoria.toLowerCase().contains("lácteo") || categoria.toLowerCase().contains("lacteo"))) {
            return false;
        }

        if (restricciones.contains("Vegano") &&
                (categoria.toLowerCase().contains("carne") || categoria.toLowerCase().contains("pescado") ||
                        categoria.toLowerCase().contains("lácteo") || categoria.toLowerCase().contains("lacteo"))) {
            return false;
        }

        // Si pasa todas las verificaciones, cumple con las restricciones
        return true;
    }

    @Override
    public String toString() {
        return nombre + " - " + marca + " (" + String.format("%.2f€", precio) + ")";
    }
}