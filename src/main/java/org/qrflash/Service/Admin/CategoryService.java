package org.qrflash.Service.Admin;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.Admin.CategoryDTO;
import org.qrflash.Entity.CategoryEntity;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Exeption.DuplicateTagException;
import org.qrflash.Repository.CategoryRepository;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DynamicDatabaseService dynamicDatabaseService;

    public List<CategoryDTO> getAllCategories(String databaseName) {
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName)) {
            String query = "SELECT id, name, description, image_url FROM categories";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                CategoryDTO dto = new CategoryDTO();
                dto.setId(resultSet.getLong("id"));
                dto.setName(resultSet.getString("name"));
                dto.setDescription(resultSet.getString("description"));
                dto.setImage_url(resultSet.getString("image_url"));
                categoryDTOList.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching categories from database " + databaseName, e);
        }
        return categoryDTOList;
    }

    public String addCategory(String databaseName, CategoryEntity categoryEntity) {
        if (isCategoryExists(databaseName, categoryEntity.getName())) {
            return "Категорія з такою назвою вже існує";
        }

        String insertQuery = "INSERT INTO categories (name, description, image_url) VALUES (?, ?, ?)";
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, categoryEntity.getName());
            insertStatement.setString(2, categoryEntity.getDescription());
            insertStatement.setString(3, categoryEntity.getImage_url());
            insertStatement.executeUpdate();
            return "Категорія успішно додана";
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при додаванні категорії в базу", e);
        }
    }


    private Boolean isCategoryExists(String databaseName, String categoryName) {
        String query = "SELECT COUNT(*) FROM categories WHERE name = ?";
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка під час перевірки існування категорії", e);
        }
        return false;
    }

    public void updateCategory(String databaseName, Long categoryId, CategoryEntity categoryEntity) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE categories SET ");
        List<String> updates = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        // Перевіряємо, які поля не є null
        if (categoryEntity.getName() != null) {
            updates.add("name = ?");
            parameters.add(categoryEntity.getName());
        }
        if (categoryEntity.getDescription() != null) {
            updates.add("description = ?");
            parameters.add(categoryEntity.getDescription());
        }
        if (categoryEntity.getImage_url() != null) {
            updates.add("image_url = ?");
            parameters.add(categoryEntity.getImage_url());
        }

        // Якщо немає жодних оновлень
        if (updates.isEmpty()) {
            throw new RuntimeException("Не передано жодних полів для оновлення.");
        }

        // Формуємо SQL-запит
        queryBuilder.append(String.join(", ", updates)).append(" WHERE id = ?");
        parameters.add(categoryId);

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Категорію з ID " + categoryId + " не знайдено.");
            }
            System.out.println("Категорію оновлено у базі: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка оновлення категорії у базі: " + databaseName, e);
        }
    }

    public void deleteCategory(String databaseName, Long categoryId) {
        String query = "DELETE FROM categories WHERE id = ?";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, categoryId);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted == 0) {
                throw new RuntimeException("Категорію з ID " + categoryId + " не знайдено.");
            }
            System.out.println("Категорію з ID " + categoryId + " успішно видалено з бази " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("deleteCategory: Помилка видалення категорії у базі: " + databaseName, e);
        }
    }
}
