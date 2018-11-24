package io.ap4k.component.decorator;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.decorator.Decorator;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;

import java.util.Arrays;

public class AddServiceInstanceToComponent extends Decorator<ComponentSpecBuilder> {

  private final ServiceCatalogInstance instance;

  public AddServiceInstanceToComponent(ServiceCatalogInstance instance) {
    this.instance = instance;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    if (hasService(component)) {
      return;
    }

    component.addNewService()
      .withName(instance.getName())
      .withServiceClass(instance.getServiceClass())
      .withServicePlan(instance.getServicePlan())
      .withSecretName(instance.getBindingSecret())
      .endService();
  }

  private boolean hasService(ComponentSpecBuilder componentSpec) {
    return Arrays.asList(componentSpec.getService()).stream().filter(s -> s.getName().equals(instance.getName())).count() > 0;
  }
}
