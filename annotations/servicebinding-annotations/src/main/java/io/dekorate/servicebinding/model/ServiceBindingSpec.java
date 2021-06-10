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
package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.annotations.Buildable;

/**
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "application", "services", "envVarPrefix", "detectBindingResources", "bindAsFiles", "mountPath",
    "customEnvVar" })
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class ServiceBindingSpec {

  private Application application;
  private Service[] services;
  private String envVarPrefix;
  private boolean detectBindingResources;
  private boolean bindAsFiles;
  private String mountPath;
  private CustomEnvVar[] customEnvVar;

  public ServiceBindingSpec() {
  }

  public ServiceBindingSpec(Application application, Service[] services, String envVarPrefix,
      boolean detectBindingResources, boolean bindAsFiles, String mountPath, CustomEnvVar[] customEnvVar) {
    super();
    this.application = application;
    this.services = services;
    this.envVarPrefix = envVarPrefix;
    this.detectBindingResources = detectBindingResources;
    this.bindAsFiles = bindAsFiles;
    this.mountPath = mountPath;
    this.customEnvVar = customEnvVar;
  }

  public Service[] getServices() {
    return services;
  }

  public void setServices(Service[] services) {
    this.services = services;
  }

  public String getEnvVarPrefix() {
    return envVarPrefix;
  }

  public void setEnvVarPrefix(String envVarPrefix) {
    this.envVarPrefix = envVarPrefix;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public boolean getDetectBindingResources() {
    return detectBindingResources;
  }

  public void setDetectBindingResources(boolean detectBindingResources) {
    this.detectBindingResources = detectBindingResources;
  }

  public boolean isBindAsFiles() {
    return bindAsFiles;
  }

  public void setBindAsFiles(boolean bindAsFiles) {
    this.bindAsFiles = bindAsFiles;
  }

  public String getMountPath() {
    return mountPath;
  }

  public void setMountPath(String mountPath) {
    this.mountPath = mountPath;
  }

  public CustomEnvVar[] getCustomEnvVar() {
    return customEnvVar;
  }

  public void setCustomEnvVar(CustomEnvVar[] customEnvVar) {
    this.customEnvVar = customEnvVar;
  }
}
