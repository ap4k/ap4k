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

package io.dekorate.utils;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import io.dekorate.utils.Exec.ProjectExec;

public class Maven {

  public static String MVN = "mvn";
  public static String MVNW = "mvnw";
  public static String DASH_VERSION = "-version";

  public static String DOT_MVNW = "./" + MVNW;

  public static String NEW_LINE = "[\\n\\r]+";
  public static String SPACE = " ";

  public static String getVersion(Path modulePath) {
    Path moduleMvnw = modulePath.resolve(MVNW);
    Path rootMvnw = Git.getRoot(modulePath).orElse(modulePath).resolve(MVNW);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ProjectExec exec = Exec.inPath(modulePath).redirectingOutput(out);

    boolean success = false;
    if  (moduleMvnw.toFile().exists()) {
      success = exec.commands(moduleMvnw.toAbsolutePath().toString(), DASH_VERSION);
    } else if  (rootMvnw.toFile().exists()) {
       success = exec.commands(rootMvnw.toAbsolutePath().toString(), DASH_VERSION);
    } else {
      success = exec.commands(MVN, DASH_VERSION);
    }

    if (!success) {
      throw new IllegalStateException("Maven version check failed!");
    }

    return getVersionFromOutput(new String(out.toByteArray()));
  }


  private static String getVersionFromOutput(String output) {
    if (Strings.isNullOrEmpty(output)) {
      throw new IllegalArgumentException("Maven version output should not be empty!");
    }

    Optional<String> versionLine = Arrays.stream(output.split(NEW_LINE))
//      .map(l ->  {System.out.println(l); return l;})
      .filter(l -> l.startsWith("Apache Maven"))
      .findFirst();

    if (!versionLine.isPresent()) {
      throw new IllegalStateException("Unknown maven version output format. Expected at least one line!");
    }
    String[] parts = versionLine.map(l -> l.split(SPACE)).get();
    if (parts.length < 3) {
        throw new IllegalStateException("Unknown maven version output format. Expected 'Apache Maven x.y.z ...'");
     }
    return parts[2];
  }
}
