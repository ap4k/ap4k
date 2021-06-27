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
package io.dekorate.jaeger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.annotation.Port;
import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder", refs = @BuildableReference(Port.class))
@Pojo(name = "JaegerAgentConfig", autobox = true, mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(name = "JaegerAgentConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableJaegerAgent {

  /**
   * Flag to specify if Jaeger operator is available / enabled.
   * 
   * @return True, if operator is available / enabled.
   */
  boolean operatorEnabled() default false;

  /**
   * The jaeger agent version.
   * 
   * @return The version, or default to 1.10
   */
  String version() default "1.10";

  Collector collector() default @Collector();

  Port[] ports() default {};

}
