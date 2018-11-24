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
package io.ap4k.component.model;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;


/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "kind",
      "name",
      "targetComponentName",
      "refs",
      "envs",
      })
      @JsonDeserialize(using = io.ap4k.deps.jackson.databind.JsonDeserializer.None.class)
      @Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
      public class Link {

        enum Kind {
          Secret,
          ConfigMap,
          Env
        }

        private Kind kind;
        private String name;
        private String targetComponentName;
        private String ref;
        private Env[] envs;

        public Link() {
        }

        public Link(Kind kind, String name, String targetComponentName, String ref, Env[] envs) {
          this.kind = kind;
          this.name = name;
          this.targetComponentName = targetComponentName;
          this.ref = ref;
          this.envs = envs;
        }

        public Kind getKind() {
          return kind;
        }

        public void setKind(Kind kind) {
          this.kind = kind;
        }

        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public String getTargetComponentName() {
          return targetComponentName;
        }

        public void setTargetComponentName(String targetComponentName) {
          this.targetComponentName = targetComponentName;
        }

        public String getRef() {
          return ref;
        }

        public void setRef(String ref) {
          this.ref = ref;
        }

        public Env[] getEnvs() {
          return envs;
        }

        public void setEnvs(Env[] envs) {
          this.envs = envs;
        }
      }
