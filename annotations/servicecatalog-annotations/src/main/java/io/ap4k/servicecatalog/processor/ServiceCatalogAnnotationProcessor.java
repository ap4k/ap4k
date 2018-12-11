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
package io.ap4k.servicecatalog.processor;


import io.ap4k.Session;
import io.ap4k.kubernetes.config.ConfigurationSupplier;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogConfigAdapter;
import io.ap4k.servicecatalog.handler.ServiceCatalogHandler;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"io.ap4k.servicecatalog.annotation.ServiceCatalog", "io.ap4k.servicecatalog.annotation.ServiceCatalogInstance"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ServiceCatalogAnnotationProcessor extends AbstractAnnotationProcessor<ServiceCatalogConfig> {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(this::write);
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurators().add(config(mainClass));
        session.handlers().add(new ServiceCatalogHandler(session.resources()));
      }
    }
    return false;
  }

  public ConfigurationSupplier<ServiceCatalogConfig> config(Element mainClass) {
    ServiceCatalog serviceCatalog = mainClass.getAnnotation(ServiceCatalog.class);
    return serviceCatalog != null
      ? new ConfigurationSupplier<>(ServiceCatalogConfigAdapter.newBuilder(serviceCatalog))
      : new ConfigurationSupplier<>(ServiceCatalogConfigAdapter.newServiceCatalogBuilder());
  }
}
