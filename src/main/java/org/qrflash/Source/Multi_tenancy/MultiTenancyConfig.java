package org.qrflash.Source.Multi_tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MultiTenancyConfig {
    private final DataSource defaultDataSource;

    @Autowired
    public MultiTenancyConfig(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolverImpl();
    }

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new DataSourceBasedMultiTenantConnectionProviderImpl(defaultDataSource);
    }
}

