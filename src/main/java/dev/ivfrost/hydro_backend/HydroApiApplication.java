package dev.ivfrost.hydro_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HydroApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(HydroApiApplication.class, args);
  }

}
