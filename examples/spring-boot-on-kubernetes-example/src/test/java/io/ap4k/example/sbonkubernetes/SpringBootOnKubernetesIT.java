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
package io.ap4k.example.sbonkubernetes;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Pod;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.LocalPortForward;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.ap4k.deps.okhttp3.Request;
import io.ap4k.deps.okhttp3.Response;
import io.ap4k.testing.annotation.Inject;
import io.ap4k.testing.annotation.KubernetesIntegrationTest;
import io.ap4k.testing.annotation.Named;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@KubernetesIntegrationTest
public class SpringBootOnKubernetesIT {

  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList list;

  @Inject
  @Named("spring-boot-on-kubernetes-example")
  Pod pod;

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    Assertions.assertNotNull(client);
    Assertions.assertNotNull(list);
    System.out.println("Forwarding port");
    try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(9090)) { //port matches what is configured in properties file
      assertTrue(p.isAlive());
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/");

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().get().url(url).build();
      Response response = client.newCall(request).execute();
      assertEquals(response.body().string(), "Hello world");
    } catch (Exception e)  {
      e.printStackTrace();
    }
  }
}
