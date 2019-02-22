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

package io.ap4k.generatoroptions.processor;

import io.ap4k.WithSession;
import io.ap4k.doc.Description;
import io.ap4k.generatoroptions.annotation.GeneratorOptions;
import io.ap4k.generatoroptions.handler.GeneratorOptionsHandler;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.utils.Strings;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;

@Description("Processing generator options, which are used for customizing the generation process")
@SupportedAnnotationTypes("io.ap4k.generatoroptions.annotation.GeneratorOptions")
public class GeneratorOptionsProcessor extends AbstractAnnotationProcessor implements WithSession {

  private static final String INPUT_DIR = "ap4k.input.dir";
  private static final String OUTPUT_DIR = "ap4k.output.dir";

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      session.close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        GeneratorOptions options = mainClass.getAnnotation(GeneratorOptions.class);
        if (options == null) {
          continue;
        }
        String inputPath = System.getProperty(INPUT_DIR, options.inputPath());
        String outputPath = System.getProperty(OUTPUT_DIR, options.outputPath());

        if (Strings.isNotNullOrEmpty(inputPath)) {

          applyToProject(p -> p.withAp4kInputDir(inputPath));
          session.handlers().add(new GeneratorOptionsHandler(session.resources(), new ResourceReader(inputPath)));
        }
        if (Strings.isNotNullOrEmpty(outputPath)) {
          applyToProject(p -> p.withAp4kOutputDir(outputPath));
        }
        return false;
       }
    }
    return false;
  }

  /**
   * A Simple function for reading resources from class output.
   */
  private class ResourceReader implements Function<String, InputStream>  {
    private final String path;
    private ResourceReader(String path) {
      this.path = path;
    }

    @Override
    public InputStream apply(String resource) {
      try {
        FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path + "/" + resource + DOT + YML);
        if (fileObject == null) {
          fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path + "/" + resource + DOT + JSON);
        }
        return fileObject != null ? fileObject.openInputStream() : null;
      } catch (IOException e) {
        return null;
      }
    }
  }
}
