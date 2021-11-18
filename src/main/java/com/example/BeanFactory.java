package com.example;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.zalando.logbook.Logbook;

@Factory
public class BeanFactory {
  @Singleton
  Logbook logbook() {
    return Logbook.builder()
        .build();
  }
}
