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
package io.dekorate.kubernetes.handler;

import static io.dekorate.utils.Labels.createLabels;

import java.util.HashMap;
import java.util.Optional;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.BuildServiceFactories;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.ContainerBuilder;
import io.dekorate.kubernetes.config.EditableKubernetesConfig;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.config.KubernetesConfigBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddIngressDecorator;
import io.dekorate.kubernetes.decorator.AddIngressRuleDecorator;
import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.decorator.ApplyApplicationContainerDecorator;
import io.dekorate.kubernetes.decorator.ApplyDeploymentStrategyDecorator;
import io.dekorate.kubernetes.decorator.ApplyHeadlessDecorator;
import io.dekorate.kubernetes.decorator.ApplyImageDecorator;
import io.dekorate.kubernetes.decorator.ApplyReplicasDecorator;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

public class KubernetesHandler extends AbstractKubernetesHandler<KubernetesConfig> implements HandlerFactory, WithProject {

  private static final String KUBERNETES = "kubernetes";
  private static final String DEFAULT_REGISTRY = "docker.io";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  private final Logger LOGGER = LoggerFactory.getLogger();
  private final Configurators configurators;

  public KubernetesHandler() {
    this(new Resources(), new Configurators());
  }

  public KubernetesHandler(Resources resources, Configurators configurators) {
    super(resources);
    this.configurators = configurators;
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new KubernetesHandler(resources, configurators);
  }

  @Override
  public String getKey() {
    return KUBERNETES;
  }

  @Override
  public int order() {
    return 200;
  }

  public void handle(KubernetesConfig config) {
    LOGGER.info("Processing kubernetes configuration.");
    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurators);

    Optional<Deployment> existingDeployment = resources.groups().getOrDefault(KUBERNETES, new KubernetesListBuilder())
        .buildItems().stream()
        .filter(i -> i instanceof Deployment)
        .map(i -> (Deployment) i)
        .filter(i -> i.getMetadata().getName().equals(config.getName()))
        .findAny();

    if (!existingDeployment.isPresent()) {
      resources.add(KUBERNETES, createDeployment(config, imageConfig));
    }

    addDecorators(KUBERNETES, config);

  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
        type.equals(EditableKubernetesConfig.class);
  }

  @Override
  protected void addDecorators(String group, KubernetesConfig config) {
    super.addDecorators(group, config);

    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurators);
    String image = Strings.isNotNullOrEmpty(imageConfig.getImage())
        ? imageConfig.getImage()
        : Images.getImage(imageConfig.isAutoPushEnabled()
            ? (Strings.isNullOrEmpty(imageConfig.getRegistry()) ? DEFAULT_REGISTRY : imageConfig.getRegistry())
            : imageConfig.getRegistry(),
            imageConfig.getGroup(), imageConfig.getName(), imageConfig.getVersion());

    Container appContainer = new ContainerBuilder()
          .withName(config.getName())
          .withImage(image)
          .withImagePullPolicy(ImagePullPolicy.IfNotPresent)
          .addNewEnvVar()
            .withName(KUBERNETES_NAMESPACE)
            .withField(METADATA_NAMESPACE)
          .endEnvVar()
          .build();

    resources.decorate(group, new ApplyApplicationContainerDecorator(config.getName(), appContainer));
    resources.decorate(group, new ApplyImageDecorator(config.getName(), image));

    for (Container container : config.getInitContainers()) {
      resources.decorate(group, new AddInitContainerDecorator(config.getName(), container));
    }

    if (config.getPorts().length > 0) {
      resources.decorate(group, new AddServiceResourceDecorator(config));
    }

    Ports.getHttpPort(config).ifPresent(p -> {
      resources.decorate(group, new AddIngressDecorator(config, Labels.createLabelsAsMap(config, "Ingress")));
      resources.decorate(group, new AddIngressRuleDecorator(config.getName(), config.getHost(), p));
    });

    if (config.isHeadless()) {
      resources.decorate(KUBERNETES, new ApplyHeadlessDecorator(config.getName()));
    }

    if (config.getReplicas() != 1) {
      resources.decorate(KUBERNETES, new ApplyReplicasDecorator(config.getName(), config.getReplicas()));
    }

    resources.decorate(KUBERNETES, new ApplyDeploymentStrategyDecorator(config.getName(), config.getDeploymentStrategy(),
        config.getRollingUpdate()));
  }

  /**
   * Creates a {@link Deployment} for the {@link KubernetesConfig}.
   * 
   * @param appConfig The session.
   * @return The deployment.
   */
  public Deployment createDeployment(KubernetesConfig appConfig, ImageConfiguration imageConfig) {
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(appConfig.getName())
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewSelector() //We need to have at least an empty selector so that the decorator can work with it.
        .withMatchLabels(new HashMap<String, String>())
        .endSelector()
        .withTemplate(createPodTemplateSpec(appConfig, imageConfig))
        .endSpec()
        .build();
  }

  /**
   * Creates a {@link PodTemplateSpec} for the {@link KubernetesConfig}.
   * 
   * @param appConfig The sesssion.
   * @return The pod template specification.
   */
  public static PodTemplateSpec createPodTemplateSpec(KubernetesConfig appConfig, ImageConfiguration imageConfig) {
    return new PodTemplateSpecBuilder()
        .withSpec(createPodSpec(appConfig, imageConfig))
        .withNewMetadata()
        .endMetadata()
        .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link KubernetesConfig}.
   * 
   * @param imageConfig The sesssion.
   * @return The pod specification.
   */
  public static PodSpec createPodSpec(KubernetesConfig appConfig, ImageConfiguration imageConfig) {
    return new PodSpecBuilder()
        .build();
  }

  @Override
  public ConfigurationSupplier<KubernetesConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<KubernetesConfig>(new KubernetesConfigBuilder()
        .accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }

  private static ImageConfiguration getImageConfiguration(Project project, KubernetesConfig appConfig,
      Configurators configurators) {
    return configurators.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(appConfig, i))
        .orElse(ImageConfiguration.from(appConfig));
  }

  private static ImageConfiguration merge(KubernetesConfig appConfig, ImageConfiguration imageConfig) {
    if (appConfig == null) {
      throw new NullPointerException("KubernetesConfig is null.");
    }
    if (imageConfig == null) {
      return ImageConfiguration.from(appConfig);
    }
    return new ImageConfigurationBuilder()
        .withProject(imageConfig.getProject() != null ? imageConfig.getProject() : appConfig.getProject())
        .withImage(imageConfig.getImage() != null ? imageConfig.getImage() : null)
        .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
        .withName(imageConfig.getName() != null ? imageConfig.getName() : appConfig.getName())
        .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : appConfig.getVersion())
        .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : DEFAULT_REGISTRY)
        .withDockerFile(imageConfig.getDockerFile() != null ? imageConfig.getDockerFile() : "Dockerfile")
        .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
        .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false)
        .build();
  }
}
