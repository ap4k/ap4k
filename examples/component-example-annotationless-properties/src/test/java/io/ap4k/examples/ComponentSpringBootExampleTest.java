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
package io.ap4k.examples;

import io.ap4k.component.model.Component;
import io.ap4k.component.model.Link;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentSpringBootExampleTest {

  @Test
  public void shouldContainComponentAndLink() {
    KubernetesList list = Serialization.unmarshal(ComponentSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/component.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    Assertions.assertEquals(2, items.size());
    Component component = (Component) items.get(0);
    Assertions.assertEquals("Component", component.getKind());
    assertEquals("https://github.com/ap4k/ap4k.git", component.getSpec().getBuildConfig().getUri());
    assertEquals("docker", component.getSpec().getBuildConfig().getType());
    assertEquals("component-example-annotationless-properties", component.getSpec().getBuildConfig().getModuleDirName());
    assertNotNull("", component.getSpec().getBuildConfig().getRef());
    Link link = (Link) items.get(1);
    Assertions.assertEquals("Link", link.getKind());
    Assertions.assertEquals(1, link.getSpec().getEnvs().length);
    Assertions.assertEquals("hello-world", link.getMetadata().getName());
    Assertions.assertEquals("target", link.getSpec().getComponentName());
  }

}
