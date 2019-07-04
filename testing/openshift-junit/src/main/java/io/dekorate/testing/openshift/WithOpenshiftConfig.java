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
package io.dekorate.testing.openshift;

import io.dekorate.DekorateException;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.utils.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface WithOpenshiftConfig {

  String OPENSHIFT_CONFIG_PATH = "META-INF/dekorate/.config/openshift.yml";

  default OpenshiftConfig getOpenshiftConfig() {
    return getOpenshiftConfig(OPENSHIFT_CONFIG_PATH);
  }

  default OpenshiftConfig getOpenshiftConfig(String path) {
    URL url = WithOpenshiftConfig.class.getClassLoader().getResource(path);
    if (url != null) {
      try (InputStream is = url.openStream())  {
        return Serialization.unmarshal(is, OpenshiftConfig.class);
      } catch (IOException e) {
        throw DekorateException.launderThrowable(e);
      }
    }
    throw new IllegalStateException("Expected to find openshift config at: "+path+"!");
  }
}
