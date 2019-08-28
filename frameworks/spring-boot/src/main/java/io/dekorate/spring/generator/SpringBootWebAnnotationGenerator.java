/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.spring.generator;

import java.util.Collections;
import java.util.Map;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.kubernetes.configurator.AddPort;
import io.dekorate.spring.SpringPropertiesHolder;

public interface SpringBootWebAnnotationGenerator extends Generator, WithSession, SpringPropertiesHolder {

  Map WEB_ANNOTATIONS = Collections.emptyMap();

  @Override
  default void add(Map map) {
    Session session = getSession();
    Port port = detectHttpPort();
    session.configurators().add(new AddPort(port));
    //TODO add support for detecting actuator and setting the liveness/readiness probes path from the configured path
  }

  default Port detectHttpPort() {
    return new PortBuilder().withContainerPort(extractPortFromProperties()).withName("http").build();
  }

  default Integer extractPortFromProperties() {
    final Object server = getSpringProperties().get("server");
    if (server != null && Map.class.isAssignableFrom(server.getClass())) {
      final Map<String, Object> serverProperties = (Map<String, Object>) server;
      final Object port = serverProperties.get("port");
      if (port != null) {
        return port instanceof Integer ? (Integer) port : Integer.valueOf(port.toString());
      }
    }
    return 8080;
  }
}
