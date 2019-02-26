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
 **/
package io.ap4k.kubernetes.config;

import io.ap4k.Coordinates;
import io.ap4k.project.Project;
import io.sundr.builder.annotations.Buildable;

import java.util.Map;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
public class ApplicationConfiguration extends Configuration implements Coordinates {

  private String group;
  private String name;
  private String version;

  public ApplicationConfiguration() {
  }

  public ApplicationConfiguration(Project project, Map<ConfigKey, Object> attributes, String group, String name, String version) {
    super(project, attributes);
    this.group = group;
    this.name = name;
    this.version = version;
  }

  @Override
  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
