package org.example.app.model;

public class Payment {

  private String id;
  private double amount;

  // Getters, setters, and constructor
  public Payment() {
  }

  public Payment(String id, double amount) {
    this.id = id;
    this.amount = amount;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  @Override
  public String toString() {
    return "Payment{" +
        "id='" + id + '\'' +
        ", amount=" + amount +
        '}';
  }
}
