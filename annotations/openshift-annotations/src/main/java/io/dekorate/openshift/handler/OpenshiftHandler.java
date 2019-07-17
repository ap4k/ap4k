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
package io.dekorate.openshift.handler;

import java.util.Optional;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.kubernetes.api.model.PodSpec;
import io.dekorate.deps.kubernetes.api.model.PodSpecBuilder;
import io.dekorate.deps.kubernetes.api.model.PodTemplateSpec;
import io.dekorate.deps.kubernetes.api.model.PodTemplateSpecBuilder;
import io.dekorate.deps.openshift.api.model.BuildConfig;
import io.dekorate.deps.openshift.api.model.BuildConfigBuilder;
import io.dekorate.deps.openshift.api.model.DeploymentConfig;
import io.dekorate.deps.openshift.api.model.DeploymentConfigBuilder;
import io.dekorate.deps.openshift.api.model.ImageStream;
import io.dekorate.deps.openshift.api.model.ImageStreamBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.configurator.ApplyAutoBuild;
import io.dekorate.openshift.config.EditableOpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigBuilder;
import io.dekorate.openshift.decorator.AddBuildEnvDecorator;
import io.dekorate.openshift.decorator.AddRouteDecorator;
import io.dekorate.openshift.decorator.ApplyDeploymentTriggerDecorator;
import io.dekorate.openshift.decorator.ApplyReplicasDecorator;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Images;

public class OpenshiftHandler extends AbstractKubernetesHandler<OpenshiftConfig> implements HandlerFactory, WithProject {

  private static final String OPENSHIFT = "openshift";
  private static final String APP = "app";
  private static final String VERSION = "version";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  private static final String IMAGESTREAMTAG = "ImageStreamTag";
  private static final String IMAGECHANGE = "ImageChange";

  private static final String JAVA_APP_JAR = "JAVA_APP_JAR";

  public OpenshiftHandler() {
    super(new Resources());
  }
  public OpenshiftHandler(Resources resources) {
    super(resources);
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new OpenshiftHandler(resources);
  }

  @Override
  public int order() {
    return 300;
  }

  public void handle(OpenshiftConfig config) {
    setApplicationInfo(config);
    Optional<DeploymentConfig> existingDeploymentConfig = resources.groups().getOrDefault(OPENSHIFT, new KubernetesListBuilder()).buildItems().stream()
      .filter(i -> i instanceof DeploymentConfig)
      .map(i -> (DeploymentConfig)i)
      .filter(i -> i.getMetadata().getName().equals(config.getName()))
      .findAny();

    if (!existingDeploymentConfig.isPresent()) {
      resources.add(OPENSHIFT, createDeploymentConfig(config));
    }

    if (config.isBuildResourceGenerationEnabled()) {
      resources.add(OPENSHIFT, createBuilderImageStream(config));
      resources.add(OPENSHIFT, createBuildConfig(config));
      resources.add(OPENSHIFT, createProjectImageStream());

      for (Env env : config.getBuildEnvVars()) {
        resources.decorate(OPENSHIFT, new AddBuildEnvDecorator(env));
      }
    }

    addDecorators(OPENSHIFT, config);
  }

  @Override
  public ConfigurationSupplier<OpenshiftConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<OpenshiftConfig>(new OpenshiftConfigBuilder().accept(new ApplyAutoBuild()).accept(new ApplyProjectInfo(p)));
  }


  @Override
  protected void addDecorators(String group, OpenshiftConfig config) {
    super.addDecorators(group, config);
    resources.decorate(group, new ApplyReplicasDecorator(config.getReplicas()));
    resources.decorate(group, new ApplyDeploymentTriggerDecorator(config.getName(), config.getName() + ":" + config.getVersion()));
    resources.decorate(group, new AddRouteDecorator(config, resources.getLabels()));
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(OpenshiftConfig.class) ||
      type.equals(EditableOpenshiftConfig.class);
  }

  /**
   * Creates a {@link DeploymentConfig} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The deployment config.
   */
  public DeploymentConfig createDeploymentConfig(OpenshiftConfig config)  {
    return new DeploymentConfigBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withReplicas(1)
      .withTemplate(createPodTemplateSpec(config))
      .withSelector(resources.getLabels())
      .addNewTrigger()
      .withType(IMAGECHANGE)
      .withNewImageChangeParams()
      .withAutomatic(true)
      .withContainerNames(config.getName())
      .withNewFrom()
      .withKind(IMAGESTREAMTAG)
      .withName(config.getName() + ":" + config.getVersion())
      .endFrom()
      .endImageChangeParams()
      .endTrigger()
      .endSpec()
      .build();
  }

  /**
   * Creates a {@link PodTemplateSpec} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The pod template specification.
   */
  public PodTemplateSpec createPodTemplateSpec(OpenshiftConfig config) {
    return new PodTemplateSpecBuilder()
      .withSpec(createPodSpec(config))
      .withNewMetadata()
      .withLabels(resources.getLabels())
      .endMetadata()
      .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The pod specification.
   */
  public static PodSpec createPodSpec(OpenshiftConfig config) {
    return new PodSpecBuilder()
      .addNewContainer()
      .withName(config.getName())
      .withImage("")
      .withImagePullPolicy(IF_NOT_PRESENT)
      .addNewEnv()
      .withName(KUBERNETES_NAMESPACE)
      .withNewValueFrom()
      .withNewFieldRef(null, METADATA_NAMESPACE)
      .endValueFrom()
      .endEnv()
      .addNewEnv()
      .withName(JAVA_APP_JAR)
      .withValue("/deployments/" + config.getProject().getBuildInfo().getOutputFile().getFileName().toString())
      .endEnv()
      .endContainer()
      .build();
  }
 
  /**
   * Create an {@link ImageStream} for the {@link OpenshiftConfig}.
   * @param config   The config.
   * @return         The build config.
   */
  public ImageStream createBuilderImageStream(OpenshiftConfig config) {
    String repository = Images.getRepository(config.getBuilderImage());

    String name = !repository.contains("/")
      ? repository
      : repository.substring(repository.lastIndexOf("/") + 1);

    return new ImageStreamBuilder()
      .withNewMetadata()
      .withName(name)
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withDockerImageRepository(repository)
      .endSpec()
      .build();
  }


  /**
   * Create an {@link ImageStream} for the {@link OpenshiftConfig}.
   * @return         The build config.
   */
  public ImageStream createProjectImageStream() {
    return new ImageStreamBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .build();
  }

  /**
   * Create a {@link BuildConfig} for the {@link OpenshiftConfig}.
   * @param config   The config.
   * @return          The build config.
  */
  public BuildConfig createBuildConfig(OpenshiftConfig config) {
    String builderRepository = Images.getRepository(config.getBuilderImage());
    String builderTag = Images.getTag(config.getBuilderImage());

    String builderName = !builderRepository.contains("/")
      ? builderRepository
      : builderRepository.substring(builderRepository.lastIndexOf("/") + 1);


    return new BuildConfigBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withNewOutput()
      .withNewTo()
      .withKind(IMAGESTREAMTAG)
      .withName(resources.getName() + ":" + resources.getVersion())
      .endTo()
      .endOutput()
      .withNewSource()
      .withNewBinary()
      .endBinary()
      .endSource()
      .withNewStrategy()
      .withNewSourceStrategy()
      .withEnv()
      .withNewFrom()
      .withKind(IMAGESTREAMTAG)
      .withName(builderName + ":" + builderTag)
      .endFrom()
      .endSourceStrategy()
      .endStrategy()
      .endSpec()
      .build();
  }

 
}
