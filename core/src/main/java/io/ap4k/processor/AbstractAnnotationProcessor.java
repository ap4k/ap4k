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
 */
package io.ap4k.processor;

import io.ap4k.Ap4kException;
import io.ap4k.SessionWriter;
import io.ap4k.Session;
import io.ap4k.WithProject;
import io.ap4k.deps.jackson.core.type.TypeReference;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.project.Project;
import io.ap4k.project.AptProjectFactory;
import io.ap4k.utils.Maps;
import io.ap4k.utils.Serialization;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.utils.Urls;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor implements WithProject  {

  protected static final String PACKAGE = "";
  protected static final String PROJECT = "META-INF/ap4k/.project.%s";
  protected static final String JSON = "json";
  protected static final String YML = "yml";
  protected static final String TMP = "tmp";
  protected static final String DOT = ".";

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    if (!projectExists()) {
      setProject(AptProjectFactory.create(processingEnv));
    }

    Session session = Session.getSession();
    if (!session.hasWriter()) {
      session.setWriter(new AptWriter(processingEnv));
    }
  }

  protected List<HasMetadata> read(String path) {
    try {
      FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path);
      try (InputStream is = fileObject.openInputStream()) {
        KubernetesResource resource = Serialization.unmarshal(is, KubernetesResource.class);
        if (resource instanceof KubernetesList) {
          return ((KubernetesList) resource).getItems();
        } else if (resource instanceof HasMetadata) {
          return Arrays.asList((HasMetadata)resource);
        } else {
          return Collections.emptyList();
        }
      }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, path + " JSON not found.");
    }
    return null;
  }

  /**
   * @return the application properties
   */
  protected Map<String, Object> readApplicationConfig(String... resourceNames) {
    Map<String, Object> result = new HashMap<>();
    Filer filer = this.processingEnv.getFiler();
    for (String resourceName : resourceNames) {
      try {
        FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        if (resourceName.endsWith(".properties")) {
          return Maps.fromProperties(f.openInputStream());
        } else if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
          return Maps.fromYaml(f.openInputStream());
        } else {
          throw new IllegalArgumentException("Illegal resource name:" + resourceName + ". It needs to be properties or yaml file.");
        }
      } catch (FileNotFoundException e) {
        continue;
      } catch (Exception e) {
        throw Ap4kException.launderThrowable(e);
      } 
    }
    return Collections.emptyMap();
  }

  /**
   * Get the output directory of the processor.
   * @return  The directroy.
   */
  public Path getOutputDirectory() {
    try {
      FileObject project = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, PACKAGE, String.format(PROJECT, TMP));
      return Paths.get(Urls.toFile(project.toUri().toURL()).getParentFile().getAbsolutePath());
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }
}
