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
package io.ap4k.testing.kubernetes;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.VersionInfo;
import io.ap4k.hook.OrderedHook;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.kubernetes.hook.DockerBuildHook;
import io.ap4k.kubernetes.hook.DockerPushHook;
import io.ap4k.testing.WithKubernetesClient;
import io.ap4k.testing.WithPod;
import io.ap4k.testing.WithProject;
import io.ap4k.testing.config.KubernetesIntegrationTestConfig;

public class KubernetesExtension implements  ExecutionCondition, BeforeAllCallback, AfterAllCallback,
                                             WithKubernetesIntegrationTestConfig, WithPod, WithKubernetesClient, WithKubernetesResources, WithProject, WithKubernetesConfig {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    try {
      VersionInfo version = getKubernetesClient(context).getVersion();
      return ConditionEvaluationResult.enabled("Found version:" + version);
    } catch (Throwable t) {
      return ConditionEvaluationResult.disabled("Could not communicate with KubernetesExtension API server.");
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    KubernetesIntegrationTestConfig config = getKubernetesIntegrationTestConfig(context);
    KubernetesClient client = getKubernetesClient(context);
    KubernetesList list = getKubernetesResources(context);

    if (hasKubernetesConfig()) {
      KubernetesConfig kubernetesConfig = getKubernetesConfig();

      //
      // We use teh isAutoPushEnabled flag of the @EnableDockerBuild annotation and not @KubernetesIntegrationTest.
      // The reason is that the @EnableDockerBuild.isAutoPushEnabled() affects the generated manifests (adds the registry).
      // and thus the tests MUST follow.
      if (kubernetesConfig.isAutoPushEnabled()) {
        DockerBuildHook buildHook = new DockerBuildHook(getProject(), kubernetesConfig);
        DockerPushHook pushHook = new DockerPushHook(getProject(), kubernetesConfig);
        OrderedHook hook = OrderedHook.create(buildHook, pushHook);
        hook.run();
      } else if (kubernetesConfig.isAutoBuildEnabled()) {
        DockerBuildHook build = new DockerBuildHook(getProject(), kubernetesConfig);
        build.run();
      }
    }

    if (config.isAutoDeployEnabled()) {
      list.getItems().stream()
        .forEach(i -> {
          client.resourceList(i).createOrReplace();
          System.out.println("Created: " + i.getKind() + " name:" + i.getMetadata().getName() + ".");
        });

      client.resourceList(list).waitUntilReady(config.getReadinessTimeout(), TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    Arrays.stream( testInstance.getClass().getDeclaredFields())
      .forEach(f -> {
        injectKubernetesClient(context, testInstance, f);
        injectKubernetesResources(context, testInstance, f);
        injectPod(context, testInstance, f);
      });
  }

  @Override
  public void afterAll(ExtensionContext context) {
    getKubernetesResources(context).getItems().stream().forEach(r -> {
      System.out.println("Deleting: " + r.getKind() + " name:" +r.getMetadata().getName()+ " status:"+ getKubernetesClient(context).resource(r).cascading(true).delete());
    });
  }
}
