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
package io.dekorate.crd.apt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.crd.adapter.CustomResourceConfigAdapter;
import io.dekorate.crd.annotation.CustomResource;
import io.dekorate.crd.confg.Keys;
import io.dekorate.crd.config.CustomResourceConfigBuilder;
import io.dekorate.crd.configurator.AddClassNameConfigurator;
import io.dekorate.crd.generator.CustomResourceGenerator;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.utils.Strings;
import io.sundr.codegen.CodegenContext;
import io.sundr.codegen.functions.ElementTo;
import io.sundr.codegen.functions.Sources;
import io.sundr.codegen.generator.CodeGeneratorBuilder;
import io.sundr.codegen.generator.CodeGeneratorContext;
import io.sundr.codegen.model.TypeDef;
import io.sundr.codegen.utils.ModelUtils;

import static io.dekorate.crd.util.Util.isKubernetesResource;

@SupportedAnnotationTypes({"io.dekorate.crd.annotation.CustomResource"})
public class CustomResourceAnnotationProcessor extends AbstractAnnotationProcessor implements CustomResourceGenerator {

  private final CodeGeneratorContext context = new CodeGeneratorContext();

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      final var session = getSession();
      session.close();
      for (TypeDef def : session.sources()) {
        try {
          if (!isKubernetesResource(def)) {
            generateFromResources(def, "CustomResource.vm");
            generateFromResources(def, "CustomResourceList.vm");
            generateFromResources(def, "CustomResourceCondition.vm");
            generateFromResources(def, "CustomResourceStatus.vm");
          }
          generateFromResources(def, "ResourceHandler.vm");
          generateFromResources(def, "OperationImpl.vm");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return true;
    }
    CodegenContext.create(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        add(mainClass);
      }
    }
    return false;
  }

  @Override
  public void add(Element element) {
    CustomResource customResource = element.getAnnotation(CustomResource.class);
    if (element instanceof TypeElement) {
      TypeDef definition = ElementTo.TYPEDEF.apply((TypeElement) element);
      String className = ModelUtils.getClassName(element);

      on(customResource != null
        ? new AnnotationConfiguration<>(CustomResourceConfigAdapter.newBuilder(customResource)
        .addToAttributes(Keys.TYPE_DEFINITION, definition).accept(new AddClassNameConfigurator(className)))
        : new AnnotationConfiguration<>(
        new CustomResourceConfigBuilder().addToAttributes(Keys.TYPE_DEFINITION, definition)
          .accept(new AddClassNameConfigurator(className))));
    }
  }

  /**
   * Generates a source file from the specified {@link io.sundr.codegen.model.TypeDef}.
   *
   * @param model        The model of the class to generate.
   * @param resourceName The template to use.
   * @throws IOException If it fails to create the source file.
   */
  public void generateFromResources(TypeDef model, String resourceName) throws IOException {
    try {
      TypeDef out = createTypeFromTemplate(model, new String[0], Strings.fromResource(resourceName));
      generateFromResources(model, processingEnv
        .getFiler()
        .createSourceFile(out.getFullyQualifiedName()), resourceName);
    } catch (FilerException e) {
      //TODO: Need to avoid dublicate interfaces here.
    }
  }

  /**
   * Generates a source file from the specified {@link io.sundr.codegen.model.TypeDef}.
   *
   * @param model        The model of the class to generate.
   * @param fileObject   Where to save the generated class.
   * @param resourceName The template to use.
   * @throws IOException If it fails to create the source file.
   */
  public void generateFromResources(TypeDef model, JavaFileObject fileObject, String resourceName) throws IOException {
    if (classExists(model)) {
      System.err.println("Skipping: " + model.getFullyQualifiedName() + ". Class already exists.");
      return;
    }
    System.err.println("Generating: " + model.getFullyQualifiedName());
    new CodeGeneratorBuilder<TypeDef>()
      .withContext(context)
      .withModel(model)
      .withWriter(fileObject.openWriter())
      .withTemplateResource(resourceName)
      .build()
      .generate();
  }

  /**
   * Generate a {@link TypeDef} from the specified model, parameters and template.
   */
  public <T> TypeDef createTypeFromTemplate(T model, String[] parameters, String content) {
    try (StringWriter writer = new StringWriter()) {
      new CodeGeneratorBuilder<T>()
        .withContext(context)
        .withModel(model)
        .withParameters(parameters)
        .withWriter(writer)
        .withTemplateContent(content)
        .build()
        .generate();

      ByteArrayInputStream bis = new ByteArrayInputStream(writer.toString().getBytes());
      return Sources.FROM_INPUTSTEAM_TO_SINGLE_TYPEDEF.apply(bis);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Checks if class already exists.
   *
   * @param typeDef The type definition to check if exists.
   * @return True if class can be found, false otherwise.
   */
  private static boolean classExists(TypeDef typeDef) {
    try {
      Class.forName(typeDef.getFullyQualifiedName());
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
