package com.zizto.medcarts;

public class Patient {
    private int id;
    private String fio;
    private int height;
    private float weight;
    private String cardNumber;
    private String diagnosis;
    private String bloodGroup;

    public Patient(int id, String fio, int height, float weight,
                   String cardNumber, String diagnosis, String bloodGroup) {
        this.id = id;
        this.fio = fio;
        this.height = height;
        this.weight = weight;
        this.cardNumber = cardNumber;
        this.diagnosis = diagnosis;
        this.bloodGroup = bloodGroup;
    }

    public int getId() { return id; }
    public String getFio() { return fio; }
    public int getHeight() { return height; }
    public float getWeight() { return weight; }
    public String getCardNumber() { return cardNumber; }
    public String getDiagnosis() { return diagnosis; }
    public String getBloodGroup() { return bloodGroup; }
}