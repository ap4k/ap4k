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
package io.ap4k.examples.s2i;

import io.ap4k.deps.kubernetes.api.model.Pod;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.ap4k.deps.okhttp3.Request;
import io.ap4k.deps.okhttp3.Response;
import io.ap4k.deps.openshift.api.model.Route;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.testing.annotation.Inject;
import io.ap4k.testing.openshift.annotation.OpenshiftIntegrationTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@OpenshiftIntegrationTest
class SourceToImageIT {

    @Inject
    private KubernetesClient kubernetesClient;

    @Inject
    private Pod pod;

    @Test
    void testRoute() throws IOException {
        String name = pod.getMetadata().getLabels().get("app");
        assertNotNull(name);

        Route route = kubernetesClient.adapt(OpenShiftClient.class).routes().withName(name).get();
        assertNotNull(route);

        String host = route.getSpec().getHost();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get().url("http://" + host).build();

        // we need to poll the Route since it is created long before the pod is actually up and running
        await().pollInterval(5, TimeUnit.SECONDS).atMost(5, TimeUnit.MINUTES).until(() -> {
            try {
                Response response = client.newCall(request).execute();
                return response.body().string().contains("Hello world");
            } catch (Exception ignored) {
                return false;
            }
        });
    }
}
