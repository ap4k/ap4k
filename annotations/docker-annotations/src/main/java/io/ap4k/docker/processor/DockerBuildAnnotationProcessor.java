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
package io.ap4k.docker.processor;

import io.ap4k.Session;
import io.ap4k.docker.annotation.EnableDockerBuild;
import io.ap4k.kubernetes.config.ConfigurationSupplier;
import io.ap4k.docker.adapter.DockerBuildConfigAdapter;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.docker.hook.DockerBuildHook;
import io.ap4k.docker.configurator.ApplyDockerBuildHook;
import io.ap4k.docker.configurator.ApplyProjectInfoToDockerBuildConfigDecorator;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

@Description("Register a docker build hook.")
@SupportedAnnotationTypes("io.ap4k.docker.annotation.EnableDockerBuild")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DockerBuildAnnotationProcessor extends AbstractAnnotationProcessor<DockerBuildConfig> {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(this::write);
      Optional<DockerBuildConfig> config = session.configurators().get(DockerBuildConfig.class);
      if (config.isPresent() && config.get().isAutoBuildEnabled()) {
        DockerBuildHook hook = new DockerBuildHook(project, config.get());
        hook.register();
      }
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurators().add(config(mainClass));
      }
    }
    return false;
  }

  public ConfigurationSupplier<DockerBuildConfig> config(Element mainClass) {
    EnableDockerBuild enableDockerBuild = mainClass.getAnnotation(EnableDockerBuild.class);
    return new ConfigurationSupplier<DockerBuildConfig>(DockerBuildConfigAdapter
                                                        .newBuilder(enableDockerBuild)
                                                        .accept(new ApplyProjectInfoToDockerBuildConfigDecorator(project))
                                                        .accept(new ApplyDockerBuildHook()));
  }

}
