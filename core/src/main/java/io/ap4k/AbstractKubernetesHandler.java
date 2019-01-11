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
import io.ap4k.kubernetes.decorator.AddAnnotationDecorator;
import io.ap4k.kubernetes.decorator.AddAwsElasticBlockStoreVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddAzureDiskVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddAzureFileVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddConfigMapVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddEnvVarDecorator;
import io.ap4k.kubernetes.decorator.AddLabelDecorator;
import io.ap4k.kubernetes.decorator.AddLivenessProbeDecorator;
import io.ap4k.kubernetes.decorator.AddMountDecorator;
import io.ap4k.kubernetes.decorator.AddPortDecorator;
import io.ap4k.kubernetes.decorator.AddPvcVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddReadinessProbeDecorator;
import io.ap4k.kubernetes.decorator.AddSecretVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddServiceDecorator;
import io.ap4k.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.ap4k.kubernetes.decorator.ApplyReplicasDecorator;
import io.ap4k.kubernetes.decorator.ApplyServiceAccountDecorator;

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
  protected void addDecorators(String group, C config) {
    resources.decorate(new ApplyServiceAccountDecorator(config.getServiceAccount()));

    resources.decorate(group, new ApplyReplicasDecorator(config.getReplicas()));
    resources.decorate(group, new ApplyImagePullPolicyDecorator(config.getImagePullPolicy()));

    for (Label label : config.getLabels()) {
      resources.decorate(group, new AddLabelDecorator(label));
    }
    for (Annotation annotation : config.getAnnotations()) {
      resources.decorate(group, new AddAnnotationDecorator(annotation));
    }
    for (Env env : config.getEnvVars()) {
      resources.decorate(group, new AddEnvVarDecorator(env));
    }
    for (Port port : config.getPorts()) {
      resources.decorate(group, new AddPortDecorator(port));
    }
    for (Mount mount: config.getMounts()) {
      resources.decorate(group, new AddMountDecorator(mount));
    }

    for (SecretVolume volume : config.getSecretVolumes()) {
      resources.decorate(group, new AddSecretVolumeDecorator(volume));
    }

    for (ConfigMapVolume volume : config.getConfigMapVolumes()) {
      resources.decorate(group, new AddConfigMapVolumeDecorator(volume));
    }

    for (PersistentVolumeClaimVolume volume : config.getPvcVolumes()) {
      resources.decorate(group, new AddPvcVolumeDecorator(volume));
    }

    for (AzureFileVolume volume : config.getAzureFileVolumes()) {
      resources.decorate(group, new AddAzureFileVolumeDecorator(volume));
    }

    for (AzureDiskVolume volume : config.getAzureDiskVolumes()) {
      resources.decorate(group, new AddAzureDiskVolumeDecorator(volume));
    }

    for (AwsElasticBlockStoreVolume volume : config.getAwsElasticBlockStoreVolumes()) {
      resources.decorate(group, new AddAwsElasticBlockStoreVolumeDecorator(volume));
    }

    if (config.getPorts().length > 0) {
      resources.decorate(group, new AddServiceDecorator(config));
    }

    resources.decorate(group, new AddLivenessProbeDecorator(config.getName(), config.getLivenessProbe()));
    resources.decorate(group, new AddReadinessProbeDecorator(config.getName(), config.getReadinessProbe()));
  }
}
