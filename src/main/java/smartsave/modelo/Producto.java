package smartsave.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private String supermercado;

    @Column(nullable = false)
    private boolean disponible = true;

    @Embedded
    private NutricionProducto infoNutricional;

    @Embeddable
    public static class NutricionProducto {
        @Column(precision = 8, scale = 2)
        private BigDecimal calorias = BigDecimal.ZERO;

        @Column(precision = 8, scale = 2)
        private BigDecimal proteinas = BigDecimal.ZERO;

        @Column(precision = 8, scale = 2)
        private BigDecimal carbohidratos = BigDecimal.ZERO;

        @Column(precision = 8, scale = 2)
        private BigDecimal grasas = BigDecimal.ZERO;

        @Column(precision = 8, scale = 2)
        private BigDecimal fibra = BigDecimal.ZERO;

        @Column(precision = 8, scale = 2)
        private BigDecimal sodio = BigDecimal.ZERO;

        @Column(precision = 8, scale = 2)
        private BigDecimal azucares = BigDecimal.ZERO;

        public NutricionProducto() {}

        public NutricionProducto(double calorias, double proteinas, double carbohidratos, double grasas) {
            this.calorias = BigDecimal.valueOf(calorias);
            this.proteinas = BigDecimal.valueOf(proteinas);
            this.carbohidratos = BigDecimal.valueOf(carbohidratos);
            this.grasas = BigDecimal.valueOf(grasas);
        }

        public NutricionProducto(double calorias, double proteinas, double carbohidratos, double grasas,
                                 double fibra, double sodio, double azucares) {
            this.calorias = BigDecimal.valueOf(calorias);
            this.proteinas = BigDecimal.valueOf(proteinas);
            this.carbohidratos = BigDecimal.valueOf(carbohidratos);
            this.grasas = BigDecimal.valueOf(grasas);
            this.fibra = BigDecimal.valueOf(fibra);
            this.sodio = BigDecimal.valueOf(sodio);
            this.azucares = BigDecimal.valueOf(azucares);
        }

        public BigDecimal getCaloriasBD() { return calorias; }
        public void setCaloriasBD(BigDecimal calorias) { this.calorias = calorias; }
        public BigDecimal getProteinasBD() { return proteinas; }
        public void setProteinasBD(BigDecimal proteinas) { this.proteinas = proteinas; }
        public BigDecimal getCarbohidratosBD() { return carbohidratos; }
        public void setCarbohidratosBD(BigDecimal carbohidratos) { this.carbohidratos = carbohidratos; }
        public BigDecimal getGrasasBD() { return grasas; }
        public void setGrasasBD(BigDecimal grasas) { this.grasas = grasas; }
        public BigDecimal getFibraBD() { return fibra; }
        public void setFibraBD(BigDecimal fibra) { this.fibra = fibra; }
        public BigDecimal getSodioBD() { return sodio; }
        public void setSodioBD(BigDecimal sodio) { this.sodio = sodio; }
        public BigDecimal getAzucaresBD() { return azucares; }
        public void setAzucaresBD(BigDecimal azucares) { this.azucares = azucares; }

        public double getCalorias() { return calorias != null ? calorias.doubleValue() : 0.0; }
        public void setCalorias(double calorias) { this.calorias = BigDecimal.valueOf(calorias); }
        public double getProteinas() { return proteinas != null ? proteinas.doubleValue() : 0.0; }
        public void setProteinas(double proteinas) { this.proteinas = BigDecimal.valueOf(proteinas); }
        public double getCarbohidratos() { return carbohidratos != null ? carbohidratos.doubleValue() : 0.0; }
        public void setCarbohidratos(double carbohidratos) { this.carbohidratos = BigDecimal.valueOf(carbohidratos); }
        public double getGrasas() { return grasas != null ? grasas.doubleValue() : 0.0; }
        public void setGrasas(double grasas) { this.grasas = BigDecimal.valueOf(grasas); }
        public double getFibra() { return fibra != null ? fibra.doubleValue() : 0.0; }
        public void setFibra(double fibra) { this.fibra = BigDecimal.valueOf(fibra); }
        public double getSodio() { return sodio != null ? sodio.doubleValue() : 0.0; }
        public void setSodio(double sodio) { this.sodio = BigDecimal.valueOf(sodio); }
        public double getAzucares() { return azucares != null ? azucares.doubleValue() : 0.0; }
        public void setAzucares(double azucares) { this.azucares = BigDecimal.valueOf(azucares); }
    }

    public Producto() {
        this.infoNutricional = new NutricionProducto();
        this.disponible = true;
        this.precio = BigDecimal.ZERO;
    }

    public Producto(String nombre, String marca, String categoria, double precio, String supermercado) {
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = BigDecimal.valueOf(precio);
        this.supermercado = supermercado;
        this.disponible = true;
        this.infoNutricional = new NutricionProducto();
    }

    public Producto(String nombre, String marca, String categoria, double precio, String supermercado,
                    double calorias, double proteinas, double carbohidratos, double grasas) {
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = BigDecimal.valueOf(precio);
        this.supermercado = supermercado;
        this.disponible = true;
        this.infoNutricional = new NutricionProducto(calorias, proteinas, carbohidratos, grasas);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecioBD() { return precio; }
    public void setPrecioBD(BigDecimal precio) { this.precio = precio; }
    public double getPrecio() { return precio != null ? precio.doubleValue() : 0.0; }
    public void setPrecio(double precio) { this.precio = BigDecimal.valueOf(precio); }

    public String getSupermercado() { return supermercado; }
    public void setSupermercado(String supermercado) { this.supermercado = supermercado; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public NutricionProducto getInfoNutricional() { return infoNutricional; }
    public void setInfoNutricional(NutricionProducto infoNutricional) { this.infoNutricional = infoNutricional; }

    public double getRelacionProteinaPrecio() {
        double precioValue = getPrecio();
        if (precioValue <= 0) return 0;
        return infoNutricional.getProteinas() / precioValue;
    }

    public double getRelacionCaloriasPrecio() {
        double precioValue = getPrecio();
        if (precioValue <= 0) return 0;
        return infoNutricional.getCalorias() / precioValue;
    }

    public boolean cumpleRestricciones(List<String> restricciones) {
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
        return true;
    }

    @Override
    public String toString() {
        return nombre + " - " + marca + " (" + String.format("%.2f€", getPrecio()) + ")";
    }
}