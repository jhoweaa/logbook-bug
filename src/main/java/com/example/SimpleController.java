package com.example;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/")
public class SimpleController {
  @Get("dummy")
  public String result() {
    return "Hello";
  }
}