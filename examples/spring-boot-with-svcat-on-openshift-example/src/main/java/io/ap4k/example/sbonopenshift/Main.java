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
 **/

package io.ap4k.example.sbonopenshift;

import io.ap4k.kubernetes.annotation.Env;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;
import io.ap4k.servicecatalog.annotation.ServiceCatalogInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenshiftApplication(buildEnvVars = @Env(name="foo", value = "bar"))
@SpringBootApplication
@EnableAutoConfiguration
@ServiceCatalog(instances = @ServiceCatalogInstance(
  name = "postgresql-instance",
  serviceClass = "postgresql-ephemeral",
  servicePlan = "default",
  bindingSecret = "postgresql-binding")
)
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}
