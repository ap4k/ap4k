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

package io.dekorate.tekton.decorator;

import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskRunSpecFluent;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;

public class AddSecretToTaskRunDecorator extends NamedResourceDecorator<TaskRunSpecFluent<?>> {

  private final String workspace;
  private final String secret;

  public AddSecretToTaskRunDecorator(String name, String workspace, String secret) {
    super(name);
    this.workspace = workspace;
    this.secret = secret;
  }

  @Override
  public void andThenVisit(TaskRunSpecFluent<?> spec, ObjectMeta meta) {
     spec.addNewWorkspace()
      .withName(workspace)
       .withSecret(new SecretVolumeSourceBuilder().withSecretName(secret).build())
      .endWorkspace();
  }
}
