package io.dekorate.issue276;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dekorate.halkyon.annotation.HalkyonComponent;

@SpringBootApplication
@HalkyonComponent(exposeService = true, name = "customName")
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

}
