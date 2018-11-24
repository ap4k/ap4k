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


package io.ap4k.istio;

import io.ap4k.config.KubernetesConfig;
import io.ap4k.istio.config.EditableIstioConfig;
import io.ap4k.istio.config.IstioConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IstioProcessorTest {

  @Test
  public void shouldAccpetIstioConfig() {
    IstioProcessor generator = new IstioProcessor();
    assertTrue(generator.accepts(IstioConfig.class));
  }

  @Test
  public void shouldAccpetEditableIstioConfig() {
    IstioProcessor generator = new IstioProcessor();
    assertTrue(generator.accepts(EditableIstioConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    IstioProcessor generator = new IstioProcessor();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
