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
package io.dekorate.kubernetes.decorator;

import io.dekorate.deps.kubernetes.api.model.ContainerFluent;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Probe;

@Description("Add a readiness probe to all containers.")
public class AddReadinessProbeDecorator extends AbstractAddProbeDecorator {

  public AddReadinessProbeDecorator(String containerName, Probe probe) {
    super(containerName, probe);
  }

  public AddReadinessProbeDecorator(String deploymentName, String containerName, Probe probe) {
    super(deploymentName, containerName, probe);
  }

  @Override
  protected void doCreateProbe(ContainerFluent<?> container, Actions actions) {
    container.withNewReadinessProbe()
      .withExec(actions.execAction)
      .withHttpGet(actions.httpGetAction)
      .withTcpSocket(actions.tcpSocketAction)
      .withInitialDelaySeconds(probe.getInitialDelaySeconds())
      .withPeriodSeconds(probe.getPeriodSeconds())
      .withTimeoutSeconds(probe.getTimeoutSeconds())
      .endReadinessProbe();
  }
}