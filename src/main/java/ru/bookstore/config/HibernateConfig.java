package ru.bookstore.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

  @Value("${db.url}")
  private String dbUrl;

  @Value("${db.user}")
  private String dbUsername;

  @Value("${db.password}")
  private String dbPassword;

  @Bean
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(dbUrl);
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    config.setMaximumPoolSize(5);
    config.setConnectionTimeout(30000);
    config.setInitializationFailTimeout(30000);
    config.setLeakDetectionThreshold(5000);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    return new HikariDataSource(config);
  }

  @Bean
  public LocalSessionFactoryBean sessionFactory() {
    LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
    factory.setDataSource(dataSource());
    factory.setPackagesToScan("ru.bookstore.model");

    Properties props = new Properties();
    props.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
    props.put(Environment.SHOW_SQL, "true");
    props.put(Environment.FORMAT_SQL, "true");
    props.put(Environment.HBM2DDL_AUTO, "update");
    props.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "org.springframework.orm.hibernate5.SpringSessionContext");
    props.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true");
    props.put(Environment.AUTOCOMMIT, "false");

    factory.setHibernateProperties(props);
    return factory;
  }

  @Bean
  public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
    HibernateTransactionManager transactionManager = new HibernateTransactionManager();
    transactionManager.setSessionFactory(sessionFactory);
    transactionManager.setNestedTransactionAllowed(true);
    return transactionManager;
  }
}