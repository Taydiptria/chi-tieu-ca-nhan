
package com.example.quanlichitieu;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public double amount;
    public String type;
    public String category;
    public String note;
    public String date;

    public TransactionEntity(
            double amount,
            String type,
            String category,
            String note,
            String date) {

        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.date = date;
    }
}
