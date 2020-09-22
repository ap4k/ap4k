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
package io.dekorate.testing.kubernetes;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.testing.adapter.KubernetesIntegrationTestConfigAdapter;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.dekorate.testing.config.KubernetesIntegrationTestConfig;
import io.dekorate.testing.config.KubernetesIntegrationTestConfigBuilder;

public interface WithKubernetesIntegrationTestConfig {

  KubernetesIntegrationTestConfig DEFAULT_KUBERNETES_INTEGRATION_TEST_CONFIG = new KubernetesIntegrationTestConfigBuilder()
      .withReadinessTimeout(500000)
      .build();

  default KubernetesIntegrationTestConfig getKubernetesIntegrationTestConfig(ExtensionContext context) {
    return context.getElement()
        .map(e -> KubernetesIntegrationTestConfigAdapter.adapt(e.getAnnotation(KubernetesIntegrationTest.class)))
        .orElse(DEFAULT_KUBERNETES_INTEGRATION_TEST_CONFIG);
  }
}
