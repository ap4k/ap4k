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
package io.ap4k.spring.processor;

import io.ap4k.Session;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.spring.configurator.SetSpringBootRuntime;
import io.ap4k.doc.Description;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Description("Detects Spring Boot and set the runtime attribute to Spring Boot.")
@SupportedAnnotationTypes({"org.springframework.boot.autoconfigure.SpringBootApplication"})
public class SpringBootApplicationProcessor extends AbstractAnnotationProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(r -> write(r));

      session.configurators().add(new SetSpringBootRuntime());
      return true;
    }
    return false;
  }
}
