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

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.istio.util.IstioProxy;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.decorator.ContainerDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.VolumeDecorator;
import io.dekorate.kubernetes.decorator.VolumeMountDecorator;
import io.dekorate.istio.config.IstioConfig;
import io.dekorate.deps.kubernetes.api.model.PodSpecBuilder;
import io.dekorate.istio.config.EditableIstioConfig;

public class IstioHandler implements Handler<IstioConfig> {

  private static final String DEV_TERMINATION_LOG = "/dev/termination-log";
  private static final String FILE = "File";

  private static final String ISTIO_PROXY = "istio-proxy";
  private static final Long ISTIO_PROXY_USER = 1337L;

  private static final String ISTIO_SYSTEM = "istio-system";

  private static final String ISTIO_INIT = "istio-init";
  private static final String[] ISTIO_INIT_ARGS = {"-p", "15001", "-u", "1337"};

  private static final String ENABLE_CORE_DUMP = "enable-core-dump";
  private static final String[] CORE_DUMP_ARGS = {"-c", "sysctl -w kernel.core_pattern=/etc/istio/proxy/core.%e.%p.%t && ulimit -c unlimited"};


  private static final String POD_NAME = "POD_NAME";
  private static final String POD_NAMESPACE = "POD_NAMESPACE";
  private static final String INSTANCE_IP = "INSTANCE_IP";

  private static final String METADATA_NAME = "metadata.name";
  private static final String METADATA_NAMESPACE = "metadata.namespace";
  private static final String STATUS_PODIP = "status.podIp";

  private final Resources resources;

  // only used for testing
  IstioHandler() {
    this(new Resources());
  }

  public IstioHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 500;
  }

  @Override
  public void handle(IstioConfig config) {
    //Containers
    resources.decorate(createIstioInit(config));
    resources.decorate(createIstioProxy(config));
    //Volumes
    resources.decorate(VolumeDecorator.createNew()
                     .withName("istio-envoy")
                     .withNewEmptyDir()
                     .withMedium("Memory")
                     .endEmptyDir());
    resources.decorate(VolumeDecorator.createNew()
                     .withName("istio-certs")
                     .withNewSecret()
                     .withSecretName("istio.default")
                     .withDefaultMode(420)
                     .endSecret());
    //Mounts
    resources.decorate(VolumeMountDecorator.createNew()
                     .withName("istio-envoy")
                     .withMountPath("/etc/istio/proxy"));
    resources.decorate(VolumeMountDecorator.createNew()
                     .withName("istio-certs")
                     .withMountPath("/etc/certs"));
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(IstioConfig.class) ||
      type.equals(EditableIstioConfig.class);
  }

  /**
   * Create a decorator that adds an istio proxy container to all pods.
   * @param config    The istio config.
   * @return          A decorator that adds the init container.
   */
  private Decorator<PodSpecBuilder> createIstioProxy(IstioConfig config) {
    return ContainerDecorator.createNew()
      .withName(ISTIO_PROXY)
      .withImage(config.getProxyConfig().getProxyImage())
      .withArgs(IstioProxy.getArguments(config))
      .withTerminationMessagePath(DEV_TERMINATION_LOG)
      .addNewEnv().withName(POD_NAME).withNewValueFrom().withNewFieldRef(null, METADATA_NAME).endValueFrom().endEnv()
      .addNewEnv().withName(POD_NAMESPACE).withNewValueFrom().withNewFieldRef(null, METADATA_NAMESPACE).endValueFrom().endEnv()
      .addNewEnv().withName(INSTANCE_IP).withNewValueFrom().withNewFieldRef(null, STATUS_PODIP).endValueFrom().endEnv()
      .withNewSecurityContext()
      .withPrivileged(true)
      .withRunAsUser(ISTIO_PROXY_USER)
      .withReadOnlyRootFilesystem(false)
      .endSecurityContext()
      .done();
  }

  /**
   * Create a decorator that adds an istio init container to all pods.
   * @param config     The istio config.
   * @return          A decorator that adds the init container.
   */
  private Decorator<PodSpecBuilder> createIstioInit(IstioConfig config) {
    return ContainerDecorator.createNewInit()
      .withName(ISTIO_INIT)
      .withImage(config.getProxyConfig().getInitImage())
      .withImagePullPolicy(ImagePullPolicy.IfNotPresent.name())
      .withTerminationMessagePath(DEV_TERMINATION_LOG)
      .withTerminationMessagePolicy(FILE)
      .withArgs("-p", "15001", "-u", "1337")
      .done();
  }
}
