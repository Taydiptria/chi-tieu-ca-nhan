package com.example.quanlichitieu;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String type; // "Chi tiêu" hoặc "Thu nhập"

    public CategoryEntity(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
