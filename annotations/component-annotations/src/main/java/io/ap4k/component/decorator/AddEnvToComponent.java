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
package io.ap4k.component.decorator;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.kubernetes.decorator.Decorator;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.doc.Description;

@Description("Add environment variable to component.")
public class AddEnvToComponent extends Decorator<ComponentSpecBuilder> {

  private final Env env;

  public AddEnvToComponent (Env env) {
    this.env = env;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component.addNewEnv()
      .withName(env.getName())
      .withValue(env.getValue())
      .endEnv();
  }
}
