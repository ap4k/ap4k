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
package io.dekorate.examples.s2i;

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.openshift.api.model.BuildConfig;
import io.dekorate.deps.openshift.api.model.DeploymentConfig;
import io.dekorate.deps.openshift.api.model.ImageStream;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SourceToImageExampleTest {

  @Test
  public void shouldContainDeploymentConfig() {
    KubernetesList list = Serialization.unmarshalAsList(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig dc = findFirst(list, DeploymentConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(dc);
  }

  @Test
  public void shouldContainBuildConfig() {
    KubernetesList list = Serialization.unmarshalAsList(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/openshift.yml"));
    assertNotNull(list);
    BuildConfig bc = findFirst(list, BuildConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(bc);
  }

  @Test
  public void shouldContainImageStream() {
    KubernetesList list = Serialization.unmarshalAsList(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/openshift.yml"));
    assertNotNull(list);
    ImageStream is = findFirst(list, ImageStream.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(is);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }

}
