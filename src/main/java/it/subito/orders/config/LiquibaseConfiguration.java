package it.subito.orders.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfiguration {

    @Bean
    @DependsOn("dataSource")
    public SpringLiquibase liquibase(@Autowired @Qualifier("dataSource") DataSource esbDataSource,
                                     @Value("${subito.environment}") String env,
                                     @Value("${spring.liquibase.changelog}") String liquibaseChangelog,
                                     @Value("${spring.liquibase.schema}") String liquibaseSchema) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(esbDataSource);
        liquibase.setChangeLog(liquibaseChangelog);
        liquibase.setContexts(env);
        liquibase.setLiquibaseSchema(liquibaseSchema);
        liquibase.setDefaultSchema(liquibaseSchema);

        return liquibase;
    }

}
