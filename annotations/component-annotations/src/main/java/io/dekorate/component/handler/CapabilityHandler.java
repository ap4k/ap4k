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

package io.dekorate.component.handler;

import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.component.config.CapabilityConfig;
import io.dekorate.component.config.EditableCapabilityConfig;
import io.dekorate.component.config.CapabilityConfigBuilder;
import io.dekorate.component.model.Capability;
import io.dekorate.component.model.CapabilityBuilder;
import io.dekorate.component.model.CapabilityFluent;
import io.dekorate.component.model.Parameter;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.utils.Strings;

import java.util.stream.Collectors;
import java.util.Arrays;

public class CapabilityHandler implements HandlerFactory, Handler<CapabilityConfig> {

  private final Resources resources;

  public Handler create(Resources resources) {
    return new CapabilityHandler(resources);
  }
 
  public CapabilityHandler() {
    this(new Resources());
         
  }
  public CapabilityHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 1200;
  }

  @Override
  public void handle(CapabilityConfig config) {
    if (Strings.isNullOrEmpty(resources.getName())) {
      resources.setName(config.getName());
    }
    if (!Strings.isNullOrEmpty(config.getCategory()) && !Strings.isNullOrEmpty(config.getKind())) {
      resources.addCustom(ResourceGroup.NAME, createCapability(config));
    }
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(CapabilityConfig.class) ||
      type.equals(EditableCapabilityConfig.class);
  }

  /**
   * Create a {@link Capability} from a {@link CapabilityConfig}.
   *
   * @param config The config.
   * @return The link.
   */
  private Capability createCapability(CapabilityConfig config) {
    return new CapabilityBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withCategory(config.getCategory())
      .withKind(config.getKind())
      .withVersion(config.getVersion())
      .addAllToParameters(Arrays.stream(config.getParameters())
                                 .map(p->new Parameter(p.getName(),p.getValue()))
                                 .collect(Collectors.toList()))

      .endSpec()
      .build();
  }

  @Override
  public ConfigurationSupplier<CapabilityConfig> getFallbackConfig() {
    return new ConfigurationSupplier<CapabilityConfig>(new CapabilityConfigBuilder());
  }

}
