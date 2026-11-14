package com.example.quanlichitieu;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<CategoryEntity> getAllCategories();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id ASC")
    List<CategoryEntity> getCategoriesByType(String type);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();
}
