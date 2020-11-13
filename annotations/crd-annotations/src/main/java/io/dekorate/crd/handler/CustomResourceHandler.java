/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.crd.handler;

import java.util.List;

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.crd.confg.Keys;
import io.dekorate.crd.config.CustomResourceConfig;
import io.dekorate.crd.config.EditableCustomResourceConfig;
import io.dekorate.crd.util.JsonSchema;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.sundr.codegen.generator.CodeGeneratorContext;
import io.sundr.codegen.model.TypeDef;
import io.sundr.codegen.model.TypeDefBuilder;

import static io.dekorate.crd.util.Util.isKubernetesResource;

public class CustomResourceHandler implements Handler<CustomResourceConfig> {

  private final Resources resources;
  private final List<TypeDef> sources;

  private final CodeGeneratorContext context = new CodeGeneratorContext();

  public CustomResourceHandler(Resources resources, List<TypeDef> sources) {
    this.resources = resources;
    this.sources = sources;
  }

  @Override
  public int order() {
    return 400;
  }

  @Override
  public String getKey() {
    return "crd";
  }

  @Override
  public void handle(CustomResourceConfig config) {
    TypeDef typeDef = new TypeDefBuilder(config.getAttribute(Keys.TYPE_DEFINITION))
      .addToAttributes(Keys.CUSTOM_RESOURCE_CONFIG, config)
      .build();

    JSONSchemaProps schema = null;
    //Check if we need to also generate the Kubernetes resource.
    if (!isKubernetesResource(typeDef)) {
      if (Strings.isNullOrEmpty(config.getKind())) {
        throw new IllegalStateException("No kind has been specified and annotated class is not a Kubernetes resource (does not implement HasMetadata).");
      }

      sources.add(typeDef);
      schema = JsonSchema.newSpec(typeDef);
    } else {
      schema = JsonSchema.from(typeDef);
    }

    resources.add(new CustomResourceDefinitionBuilder()
      .withApiVersion("apiextensions.k8s.io/v1")
      .withNewMetadata()
      .withName(config.getPlural() + "." + config.getGroup())
      .endMetadata()
      .withNewSpec()
      .withScope(config.getScope().name())
      .withGroup(config.getGroup())
      .addNewVersion().withName(config.getVersion())
      .withNewSchema().withOpenAPIV3Schema(schema).endSchema()
      .endVersion()
      .withNewNames()
      .withKind(config.getKind())
      .withShortNames(config.getShortName())
      .withPlural(config.getPlural())
      .withSingular(config.getKind().toLowerCase())
      .endNames()
      .endSpec()
      .build());
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> config) {
    return CustomResourceConfig.class.equals(config) || EditableCustomResourceConfig.class.equals(config);
  }
}
