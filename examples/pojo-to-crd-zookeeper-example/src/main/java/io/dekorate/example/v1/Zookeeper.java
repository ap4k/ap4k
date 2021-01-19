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

package io.dekorate.example.v1;

import io.dekorate.crd.annotation.Crd;
import io.dekorate.crd.annotation.Status;
import io.dekorate.crd.config.Scope;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("io.zookeeper")
public class Zookeeper implements Namespaced {

  private String kind = "Zookeeper";
  private String apiVersion = "io.zookeeper/v1";
  private ObjectMeta metadata;

  private ZookeeperSpec spec;
  @Status
  private ZookeeperStatus status;
}

