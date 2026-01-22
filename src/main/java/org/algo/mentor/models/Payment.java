package org.algo.mentor.models;

public class Payment {
    private int id;
    private int studentId;
    private double amount;
    private String paymentFromDate;
    private String paymentToDate;
    private String createdDate;

    public Payment(int id, int studentId, double amount, String paymentFromDate, 
                   String paymentToDate, String createdDate) {
        this.id = id;
        this.studentId = studentId;
        this.amount = amount;
        this.paymentFromDate = paymentFromDate;
        this.paymentToDate = paymentToDate;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentFromDate() {
        return paymentFromDate;
    }

    public void setPaymentFromDate(String paymentFromDate) {
        this.paymentFromDate = paymentFromDate;
    }

    public String getPaymentToDate() {
        return paymentToDate;
    }

    public void setPaymentToDate(String paymentToDate) {
        this.paymentToDate = paymentToDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
