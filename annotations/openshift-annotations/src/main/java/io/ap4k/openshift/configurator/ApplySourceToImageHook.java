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
package io.ap4k.openshift.configurator;

import io.ap4k.kubernetes.config.Configurator;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.OpenshiftConfigFluent;
import io.ap4k.doc.Description;

@Description("Apply source to image build hook.")
public class ApplySourceToImageHook extends Configurator<OpenshiftConfigFluent> {

  private static final String AP4K_BUILD = "ap4k.build";
  private static final String AP4K_DEPLOY = "ap4k.deploy";

  private final OpenshiftConfig openshiftConfig;

  public ApplySourceToImageHook(OpenshiftConfig openshiftConfig) {
    this.openshiftConfig = openshiftConfig;
  }

  @Override
  public void visit(OpenshiftConfigFluent config) {
    config
      .withAutoBuildEnabled(Boolean.parseBoolean(System.getProperty(AP4K_BUILD, String.valueOf(config.isAutoBuildEnabled()))))
      .withAutoDeployEnabled(Boolean.parseBoolean(System.getProperty(AP4K_DEPLOY, String.valueOf(config.isAutoDeployEnabled() || openshiftConfig.isAutoDeployEnabled()))));
  }
}
