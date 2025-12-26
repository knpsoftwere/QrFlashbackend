package org.qrflash.Service.DataBase;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Service;

import java.sql.DriverManager;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class LiquibaseService {
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${tenant.db-url-template}")
    String rawUrl;

    public void runLiquibaseForTenant(String tenantDbName) throws SQLException, LiquibaseException {
        if(rawUrl == null){
            throw new IllegalStateException("Missing property: tenant.db-url-template");
        }

        String tenantUrl = String.format(rawUrl, tenantDbName);
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                        new JdbcConnection(DriverManager.getConnection(tenantUrl, username, password))
                );
        ResourceAccessor resourceAccessor = new SpringResourceAccessor(new DefaultResourceLoader());
        Liquibase liquibase = new Liquibase(
                "classpath:liquibase/master-tenant.yml",
                resourceAccessor,
                database
        );

        liquibase.update(new Contexts());
    }
}
