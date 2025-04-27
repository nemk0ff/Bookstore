package ru.bookstore.config;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.support.OpenSessionInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class HibernateWebConfig implements WebMvcConfigurer {

  @Autowired
  private SessionFactory sessionFactory;

  @Bean
  public OpenSessionInViewInterceptor openSessionInViewInterceptor() {
    OpenSessionInViewInterceptor interceptor = new OpenSessionInViewInterceptor();
    interceptor.setSessionFactory(sessionFactory);
    return interceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addWebRequestInterceptor(openSessionInViewInterceptor());
  }
}