package org.qrflash.Service.Admin;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.CategoryEntity;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Repository.CategoryRepository;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final DynamicDatabaseService dynamicDatabaseService;

    public List<CategoryEntity> getAllCategories(String databaseName) {
        String query = "SELECT id, name, description, image_url FROM categories";
        List<CategoryEntity> categories = new ArrayList<>();

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                CategoryEntity category = new CategoryEntity();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                category.setImageUrl(rs.getString("image_url"));
                categories.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка отримання категорій з бази даних " + databaseName, e);
        }
        return categories;
    }

    public void addCategory(String databaseName, CategoryEntity category) {
        String query = "INSERT INTO categories (name, description, image_url) VALUES (?, ?, ?)";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getImageUrl());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка додавання категорії до бази даних " + databaseName, e);
        }
    }

    public void deleteCategory(String databaseName, Long categoryId) {
        String query = "DELETE FROM categories WHERE id = ?";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, categoryId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка видалення категорії з бази даних " + databaseName, e);
        }
    }
}
