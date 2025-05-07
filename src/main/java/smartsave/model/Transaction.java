package smartsave.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDate date;
    private String description;
    private String category;
    private double amount;
    private String type; // "Ingreso" o "Gasto"

    public Transaction(LocalDate date, String description, String category, double amount, String type) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.type = type;
    }

    // Getters y setters

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Método útil para formatear la fecha
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    // Método útil para formatear el monto
    public String getFormattedAmount() {
        return String.format("€%.2f", amount);
    }
}