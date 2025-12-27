package org.qrflash.Repository.v2;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemFindDTO;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Exeption.ResourceNotFoundException;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.springframework.stereotype.Repository;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MenuItemRepository {
    private final DynamicDatabaseService dynamicDatabaseService;

    private final String SAVE_IMAGE = """
            UPDATE menu_items
            SET photo = ?
            WHERE (id = ?);
            """;

    private final String FIND_MENU_BY_ID = """
            SELECT id, name FROM menu_items
            WHERE id = ?;
            """;
    private final String GET_IMAGE_NAME_BY_ID = """
           SELECT photo FROM menu_items
           WHERE id = ?;
           """;

    public String getImageNameByMenuId(String database, Long id) {
        log.info("getImageNameByMenuId id={}",id);
        try(Connection connection = dynamicDatabaseService.getConnection(database);
            PreparedStatement statement = connection.prepareStatement(GET_IMAGE_NAME_BY_ID)){
            statement.setLong(1, id);
            statement.execute();

            try(ResultSet rs = statement.getResultSet()){
                if(rs.next()){
                    return rs.getString(1);
                }
                throw new ResourceNotFoundException("Товар за зазначеним id - відсутній");
            }catch (SQLException sqlException){
                log.error("ProductRepository::getImageNameByProdcutId: ", sqlException);
                throw new ResourceNotFoundException("002");
            }
        }catch (SQLException e){
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    public Optional<MenuItemFindDTO> findMenuItemById(Long id, String database) {
        log.info("Find menu item by id {}", id);
        try(Connection connection = dynamicDatabaseService.getConnection(database);
            PreparedStatement ps = connection.prepareStatement(FIND_MENU_BY_ID)){
            ps.setLong(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapRowToMenuItem(rs));
                }
            }
        }catch (SQLException e){
            //todo exception
            throw new RuntimeException("", e);
        }
        return  Optional.empty();
    }

    public void saveImageName(String fileName, Long productId, String database){
        log.info("Saving image, for DataBase: {}, by id: {}", database, productId);
        try(Connection connection = dynamicDatabaseService.getConnection(database);
            PreparedStatement ps = connection.prepareStatement(SAVE_IMAGE)){
            ps.setString(1, fileName);
            ps.setLong(2, productId);
            ps.execute();
        }catch (SQLException e){
            throw new RuntimeException("", e);
        }
    }

    private MenuItemFindDTO mapRowToMenuItem(ResultSet rs) throws SQLException {
        return new MenuItemFindDTO(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
