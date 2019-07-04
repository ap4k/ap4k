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

package io.dekorate.istio.handler;

import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.istio.config.EditableIstioConfig;
import io.dekorate.istio.config.IstioConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IstioHandlerTest {

  @Test
  public void shouldAcceptIstioConfig() {
    IstioHandler generator = new IstioHandler();
    assertTrue(generator.canHandle(IstioConfig.class));
  }

  @Test
  public void shouldAcceptEditableIstioConfig() {
    IstioHandler generator = new IstioHandler();
    assertTrue(generator.canHandle(EditableIstioConfig.class));
  }

  @Test
  public void shouldNotAcceptKubernetesConfig() {
    IstioHandler generator = new IstioHandler();
    assertFalse(generator.canHandle(KubernetesConfig.class));
  }
}
