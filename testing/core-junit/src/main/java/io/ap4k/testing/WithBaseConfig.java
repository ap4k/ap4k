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
package io.ap4k.testing;

import io.ap4k.Ap4kException;
import io.ap4k.kubernetes.config.BaseConfig;
import io.ap4k.utils.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface WithBaseConfig {

  String KUBERNETES_CONFIG_PATH = "META-INF/ap4k/.config/base.yml";

  default BaseConfig getBaseConfig() {
    return getBaseConfig(KUBERNETES_CONFIG_PATH);
  }

  default BaseConfig getBaseConfig (String path) {
    URL url = WithBaseConfig.class.getClassLoader().getResource(path);
    if (url != null) {
      try (InputStream is = url.openStream())  {
        return Serialization.unmarshal(is, BaseConfig.class);
      } catch (IOException e) {
        throw Ap4kException.launderThrowable(e);
      }
    }
    throw new IllegalStateException("Expected to find base config at: "+path+"!");
  }
}
