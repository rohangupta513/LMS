package com.lms.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LmsBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(LmsBackendApplication.class, args);
  }
}
