package com.zizto.medcartandroidroom;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "patients")
public class Patient {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "fio")
    private String fio;

    @ColumnInfo(name = "height")
    private int height;

    @ColumnInfo(name = "weight")
    private float weight;

    @ColumnInfo(name = "card_number")
    private String cardNumber;

    @ColumnInfo(name = "diagnosis")
    private String diagnosis;

    @ColumnInfo(name = "blood_group")
    private String bloodGroup;

    public Patient(String fio, int height, float weight, String cardNumber,
                   String diagnosis, String bloodGroup) {
        this.fio = fio;
        this.height = height;
        this.weight = weight;
        this.cardNumber = cardNumber;
        this.diagnosis = diagnosis;
        this.bloodGroup = bloodGroup;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFio() { return fio; }
    public int getHeight() { return height; }
    public float getWeight() { return weight; }
    public String getCardNumber() { return cardNumber; }
    public String getDiagnosis() { return diagnosis; }
    public String getBloodGroup() { return bloodGroup; }
}
