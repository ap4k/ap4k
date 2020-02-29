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

package io.dekorate.utils;

import java.util.HashMap;
import java.util.Map;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Label;


public class Labels {

  public static final String NAME = "app.kubernetes.io/name";
  public static final String VERSION = "app.kubernetes.io/version";
  public static final String PART_OF = "app.kubernetes.io/part-of";
  public static final String UNKNOWN = "<<unknown>>";

  /**
   * Creates a {@link Map} with the labels for the {@link BaseConfig}.
   * @param config   The config.
   * @return          A map containing the lables.
   */
  public static Map<String, String> createLabels(BaseConfig config) {
    Map<String,String> result =  new HashMap<String, String >() {{
        put(NAME, config.getName());
        put(VERSION, config.getVersion());
        if (Strings.isNotNullOrEmpty(config.getPartOf())) {
          put(PART_OF, config.getPartOf());
        }
      }};

    for (Label label : config.getLabels()) {
      result.put(label.getKey(), label.getValue());
    }
    return result; 
  }
}
