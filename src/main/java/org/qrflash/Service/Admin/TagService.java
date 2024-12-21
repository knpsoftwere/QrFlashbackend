package org.qrflash.Service.Admin;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.TagEntity;
import org.qrflash.Exeption.DuplicateTagException;
import org.qrflash.Repository.TagRepository;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final DynamicDatabaseService dynamicDatabaseService;

    public List<TagEntity> getTags(String databaseName) {
        String query = "SELECT * FROM tags";
        List<TagEntity> tags = new ArrayList<>();

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                TagEntity tag = new TagEntity();
                tag.setId(resultSet.getLong("id"));
                tag.setName(resultSet.getString("name"));
                tag.setDescription(resultSet.getString("description"));
                tag.setEmoji(resultSet.getString("emoji"));
                tags.add(tag);
            }

            System.out.println("Tags retrieved from database: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tags from database: " + databaseName, e);
        }

        return tags;
    }

    public String addTag(String databaseName, TagEntity tag) {
        if (isTagExists(databaseName, tag)) {
            throw new DuplicateTagException("Тег з такою назвою або емоджі вже існує");
        }

        String insertQuery = "INSERT INTO tags (name, description, emoji) VALUES (?, ?, ?)";
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            insertStatement.setString(1, tag.getName());
            insertStatement.setString(2, tag.getDescription());
            insertStatement.setString(3, tag.getEmoji());

            insertStatement.executeUpdate();
            return "Тег успішно додано";

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при додаванні тега в базу", e);
        }
    }

    private boolean isTagExists(String databaseName, TagEntity tag) {
        String query = "SELECT COUNT(*) FROM tags WHERE name = ? OR emoji = ?";
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getEmoji());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Помилка під час перевірки існування тегу", e);
        }
        return false;
    }


    public void updateTag(String databaseName, Long tagId, TagEntity updatedTag) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE tags SET ");
        List<String> updates = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (updatedTag.getName() != null) {
            updates.add("name = ?");
            parameters.add(updatedTag.getName());
        }
        if (updatedTag.getDescription() != null) {
            updates.add("description = ?");
            parameters.add(updatedTag.getDescription());
        }
        if (updatedTag.getEmoji() != null) {
            updates.add("emoji = ?");
            parameters.add(updatedTag.getEmoji());
        }

        // Додаємо WHERE умову
        queryBuilder.append(String.join(", ", updates)).append(" WHERE id = ?");
        parameters.add(tagId);

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Тег з ID " + tagId + " не знайдено.");
            }

            System.out.println("Тег оновлено у базі: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка оновлення тегу у базі: " + databaseName, e);
        }
    }

    public void deleteTag(String databaseName, Long tagId) {
        String query = "DELETE FROM tags WHERE id = ?";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, tagId);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted == 0) {
                throw new RuntimeException("Тег з ID " + tagId + " не знайдено.");
            }

            System.out.println("Тег видалено з бази: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка видалення тегу у базі: " + databaseName, e);
        }
    }

}
