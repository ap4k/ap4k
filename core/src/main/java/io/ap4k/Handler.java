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
package io.ap4k;

import io.ap4k.kubernetes.config.Configuration;

public interface Handler<C extends Configuration> {

  /**
   * A number indicating the order each handler will be invoked.
   * Lower number will be invoked first.
   * @return  The number used for ordering.
   */
  int order();

  /**
   * Generate / populate the resources.
   * @param config The config to handle.
   */
  void handle(C config);

  /**
   * Generate / populate the resources, asusming default configuration.
   */
  default void handleDefault() {
    //by default do noting
  }

  /**
   * Check if config is accepted.
   * A generator can choose to which config it should react.
   * @param config The specified config class;
   * @returns True if config type is accepted, false otherwise.
   */
  boolean canHandle(Class<? extends Configuration> config);
}
