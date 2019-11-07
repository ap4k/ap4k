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
package io.dekorate.examples;

import io.dekorate.halkyon.model.Component;
import io.dekorate.halkyon.model.Link;
import io.dekorate.halkyon.model.Env;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.dekorate.halkyon.model.DeploymentMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentSpringBootExampleTest {

  @Test
  public void shouldContainComponentAndLink() {
    KubernetesList list = Serialization.unmarshalAsList(ComponentSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/halkyon.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    Assertions.assertEquals(2, items.size());
    Component component = (Component) items.get(0);
    Assertions.assertEquals("Component", component.getKind());
    // This doesn't work during release.
    //assertEquals("https://github.com/dekorateio/dekorate.git", component.getSpec().getBuildConfig().getUrl());
    assertEquals("docker", component.getSpec().getBuildConfig().getType());
    assertEquals("feat-229-override-annotationbased-config", component.getSpec().getBuildConfig().getModuleDirName());
    // This may be null during the release process where HEAD point to a commit instead of a branch.
    //assertNotNull("", component.getSpec().getBuildConfig().getRef());
    assertEquals(DeploymentMode.build, component.getSpec().getDeploymentMode());
    Link link = (Link) items.get(1);
    Assertions.assertEquals("Link", link.getKind());
    Env[] envs = link.getSpec().getEnvs();
    Assertions.assertEquals(1, envs.length);
    Assertions.assertEquals("key1-from-properties", envs[0].getName());
    Assertions.assertEquals("val1-from-properties", envs[0].getValue());
    Assertions.assertEquals("hello-world", link.getMetadata().getName());
    Assertions.assertEquals("target", link.getSpec().getComponentName());
  }

}
