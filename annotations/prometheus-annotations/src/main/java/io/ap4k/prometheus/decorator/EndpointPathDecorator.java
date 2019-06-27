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
package io.ap4k.prometheus.decorator;

import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.kubernetes.decorator.Decorator;
import io.ap4k.prometheus.model.EndpointBuilder;
import io.ap4k.prometheus.model.ServiceMonitorBuilder;

/**
 * A {@link Decorator} that will set the endpoint path on the {@link io.ap4k.prometheus.model.Endpoint} that matches the port,
 * inside the {@link io.ap4k.prometheus.model.ServiceMonitor} that matches the name.
 */
public class EndpointPathDecorator extends Decorator<ServiceMonitorBuilder> {

  private final String name;
  private final String port;
  private final String path;

  public EndpointPathDecorator(String name, String port, String path) {
    this.name = name;
    this.port = port;
    this.path = path;
  }

  @Override
  public void visit(ServiceMonitorBuilder serviceMonitor) {
    if (name.equals(serviceMonitor.getMetadata().getName())) {
      serviceMonitor.accept(new TypedVisitor<EndpointBuilder>() {
        @Override
        public void visit(EndpointBuilder endpoint) {
         if (port.equals(endpoint.getPort())) {
           endpoint.withPath(path);
         }
        }
      });
    }
  }
}
