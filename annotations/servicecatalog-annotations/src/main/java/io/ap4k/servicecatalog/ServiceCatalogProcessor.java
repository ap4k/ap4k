package io.ap4k.servicecatalog;

import io.ap4k.Processor;
import io.ap4k.Resources;
import io.ap4k.config.Configuration;
import io.ap4k.deps.servicecatalog.api.model.ServiceBindingBuilder;
import io.ap4k.deps.servicecatalog.api.model.ServiceInstanceBuilder;
import io.ap4k.servicecatalog.config.Parameter;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.utils.Strings;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceCatalogProcessor implements Processor<ServiceCatalogConfig> {

  private final Resources resources;

  public ServiceCatalogProcessor() {
    this(new Resources());
  }
  public ServiceCatalogProcessor(Resources resources) {
    this.resources = resources;
  }

  @Override
  public void process(ServiceCatalogConfig config) {
     for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.add(new ServiceInstanceBuilder()
        .withNewMetadata()
        .withName(instance.getName())
        .endMetadata()
        .withNewSpec()
        .withClusterServiceClassExternalName(instance.getServiceClass())
        .withClusterServicePlanExternalName(instance.getServicePlan())
        .withParameters(toMap(instance.getParameters()))
        .endSpec()
        .build());

      if (!Strings.isNullOrEmpty(instance.getBindingSecret())) {
        resources.add(new ServiceBindingBuilder()
          .withNewMetadata()
          .withName(instance.getName())
          .endMetadata()
          .withNewSpec()
          .withNewInstanceRef(instance.getName())
          .withSecretName(instance.getBindingSecret())
          .endSpec()
          .build());
      }
    }
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }


  /**
   * Converts an array of {@link Parameter} to a {@link Map}.
   * @param parameters    The parameters.
   * @return              A map.
   */
  protected static Map<String, Object> toMap(Parameter... parameters) {
    return Arrays.asList(parameters).stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }
}
