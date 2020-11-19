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
package io.dekorate.tekton.util;

import java.nio.file.Path;

import io.dekorate.WithProject;
import io.dekorate.project.Project;
import io.dekorate.utils.Strings;

public class TektonUtils {

  /**
   * Gets the context path for the detected {@link Project}.
   * @return the context path as {@link String}
   */
  public static final String getContextPath() {
    WithProject p = new WithProject() {};
    return getContextPath(p.getProject());
  }
 
  /**
   * Gets the context path for the specified {@link Project}.
   * @param project the {@link Project}
   * @return the context path as {@link String}
   */
  public static final String getContextPath(Project project) {
    Path root = project != null && project.getScmInfo() != null ? project.getScmInfo().getRoot() : null;
    Path module = project != null ? project.getRoot() : null;

    String result = "";
    if (root != null && module != null) {
      result = module.toAbsolutePath().toString().substring(root.toAbsolutePath().toString().length());
    }
    if (Strings.isNullOrEmpty(result)) {
      result = "./";
    }
    return result;
  }

}
