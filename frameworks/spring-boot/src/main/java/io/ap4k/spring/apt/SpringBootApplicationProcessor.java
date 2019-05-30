/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
package io.ap4k.spring.apt;

import io.ap4k.Ap4kException;
import io.ap4k.Generator;
import io.ap4k.Session;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;
import io.ap4k.spring.generator.SpringBootApplicationGenerator;
import io.ap4k.utils.Maps;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Description("Detects Spring Boot and set the runtime attribute to Spring Boot.")
@SupportedAnnotationTypes({"org.springframework.boot.autoconfigure.SpringBootApplication"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SpringBootApplicationProcessor extends AbstractAnnotationProcessor implements SpringBootApplicationGenerator {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.close();
    return true;
    }

    Generator.init(readApplicationProperties());
    add(SPRING_BOOT_APPLICATION);
    return false;
  }

  /**
   * @return the application properties
   */
  Map<String, Object> readApplicationProperties() {
    Map<String, Object> result = new HashMap<>();
    Filer filer = this.processingEnv.getFiler();
    try {
      FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "application.properties"); 
      return Maps.fromProperties(f.openInputStream());
    } catch (FileNotFoundException e) {
      return Collections.emptyMap();
    } catch (Exception e) {
      throw Ap4kException.launderThrowable(e);
    } 
  }
}
