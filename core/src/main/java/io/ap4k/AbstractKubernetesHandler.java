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
package io.ap4k;

import io.ap4k.kubernetes.config.Annotation;
import io.ap4k.kubernetes.config.AwsElasticBlockStoreVolume;
import io.ap4k.kubernetes.config.AzureDiskVolume;
import io.ap4k.kubernetes.config.AzureFileVolume;
import io.ap4k.kubernetes.config.ConfigMapVolume;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.kubernetes.config.Label;
import io.ap4k.kubernetes.config.Mount;
import io.ap4k.kubernetes.config.PersistentVolumeClaimVolume;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.config.SecretVolume;
import io.ap4k.kubernetes.decorator.AddAnnotation;
import io.ap4k.kubernetes.decorator.AddAwsElasticBlockStoreVolume;
import io.ap4k.kubernetes.decorator.AddAzureDiskVolume;
import io.ap4k.kubernetes.decorator.AddAzureFileVolume;
import io.ap4k.kubernetes.decorator.AddConfigMapVolume;
import io.ap4k.kubernetes.decorator.AddEnvVar;
import io.ap4k.kubernetes.decorator.AddLabel;
import io.ap4k.kubernetes.decorator.AddLivenessProbe;
import io.ap4k.kubernetes.decorator.AddMount;
import io.ap4k.kubernetes.decorator.AddPort;
import io.ap4k.kubernetes.decorator.AddPvcVolume;
import io.ap4k.kubernetes.decorator.AddReadinessProbe;
import io.ap4k.kubernetes.decorator.AddSecretVolume;
import io.ap4k.kubernetes.decorator.AddService;

/**
 * An abstract generator.
 * A generator is meant to popullate the initial resources to the {@link Session} as well as adding decorator etc.
 * @param <C>   The config type (its expected to vary between processors).
 */
public abstract class AbstractKubernetesHandler<C extends KubernetesConfig> implements Handler<C> {

  protected final Resources resources;

  public AbstractKubernetesHandler(Resources resources) {
    this.resources = resources;
  }

  /**
   * Generate / populate the resources.
   * @param config
   */
  public abstract void handle(C config);


  /**
   * Add all decorator to the resources.
   * This method will read the config and then add all the required decorator to the resources.
   * The method is intended to be called from the generate method and thus marked as protected.
   * @param group     The group..
   * @param config    The config.
   */
  protected void addVisitors(String group, C config) {
    for (Label label : config.getLabels()) {
      resources.decorate(group, new AddLabel(label));
    }
    for (Annotation annotation : config.getAnnotations()) {
      resources.decorate(group, new AddAnnotation(annotation));
    }
    for (Env env : config.getEnvVars()) {
      resources.decorate(group, new AddEnvVar(env));
    }
    for (Port port : config.getPorts()) {
      resources.decorate(group, new AddPort(port));
    }
    for (Mount mount: config.getMounts()) {
      resources.decorate(group, new AddMount(mount));
    }

    for (SecretVolume volume : config.getSecretVolumes()) {
      resources.decorate(group, new AddSecretVolume(volume));
    }

    for (ConfigMapVolume volume : config.getConfigMapVolumes()) {
      resources.decorate(group, new AddConfigMapVolume(volume));
    }

    for (PersistentVolumeClaimVolume volume : config.getPvcVolumes()) {
      resources.decorate(group, new AddPvcVolume(volume));
    }

    for (AzureFileVolume volume : config.getAzureFileVolumes()) {
      resources.decorate(group, new AddAzureFileVolume(volume));
    }

    for (AzureDiskVolume volume : config.getAzureDiskVolumes()) {
      resources.decorate(group, new AddAzureDiskVolume(volume));
    }

    for (AwsElasticBlockStoreVolume volume : config.getAwsElasticBlockStoreVolumes()) {
      resources.decorate(group, new AddAwsElasticBlockStoreVolume(volume));
    }

    if (config.getPorts().length > 0) {
      resources.decorate(group, new AddService(config));
    }

    resources.decorate(group, new AddLivenessProbe(config.getLivenessProbe()));
    resources.decorate(group, new AddReadinessProbe(config.getReadinessProbe()));
  }
}
