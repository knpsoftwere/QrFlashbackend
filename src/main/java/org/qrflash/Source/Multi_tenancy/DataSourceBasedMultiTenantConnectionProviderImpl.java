package org.qrflash.Source.Multi_tenancy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private DataSource defaultDataSource;
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    // Безаргументний конструктор, потрібний Hibernate
    public DataSourceBasedMultiTenantConnectionProviderImpl() {
        // Можна залишити порожнім
        // defaultDataSource можна ініціалізувати, наприклад, вручну або взагалі не використовувати.
    }

    // Якщо потрібно, можна мати інший конструктор, але Hibernate його не викликатиме
    public DataSourceBasedMultiTenantConnectionProviderImpl(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    private static final String DB_URL_TEMPLATE = "jdbc:postgresql://138.201.118.129:5432/%s";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "R3cv77m6F3Ys6MfV";

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        // Відмова від дефолтного tenant
        throw new RuntimeException("Tenant ID is not defined. Unable to establish a connection.");
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        if (!dataSourceMap.containsKey(tenantIdentifier)) {
            // Якщо база даних із вказаним tenantId не існує
            throw new RuntimeException("Database for tenant " + tenantIdentifier + " does not exist.");
        }
        return dataSourceMap.get(tenantIdentifier).getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    private DataSource selectDataSource(String tenantIdentifier) {
        return dataSourceMap.computeIfAbsent(tenantIdentifier, this::createDataSourceForDB);
    }

    private DataSource createDataSourceForDB(String tenantId) {
        String jdbcUrl = String.format(DB_URL_TEMPLATE, tenantId);
        System.out.println("Creating DataSource for tenant: " + tenantId + " | URL: " + jdbcUrl);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);

        return new HikariDataSource(config);
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) { return false; }

    @Override
    public <T> T unwrap(Class<T> unwrapType) { return null; }
}



