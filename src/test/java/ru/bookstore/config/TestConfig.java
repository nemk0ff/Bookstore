package ru.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import ru.bookstore.controllers.impl.importexport.ImportController;
import ru.bookstore.facade.BookFacade;
import ru.bookstore.facade.OrderFacade;
import static org.mockito.Mockito.mock;
import ru.bookstore.facade.RequestFacade;


@Configuration
@PropertySource("classpath:application.properties")
public class TestConfig {
  @Bean
  @Primary
  public BookFacade bookFacade() {
    return mock(BookFacade.class);
  }

  @Bean
  @Primary
  public OrderFacade orderFacade() {
    return mock(OrderFacade.class);
  }

  @Bean
  @Primary
  public RequestFacade requestFacade() {
    return mock(RequestFacade.class);
  }

  @Bean
  @Primary
  public ImportController importController() {
    return mock(ImportController.class);
  }
}