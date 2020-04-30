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

package io.dekorate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import io.dekorate.utils.Strings;

public class BuildImage {

  private static final String DOT = "[\\._\\-]+";
  private static final String EMPTY = "";
  private static final String NON_DIGIT = "\\D+";

  private final String image;
  private final String[] command;

  private final String tool;
  private final String toolVersion;
  private final int jdkVersion;
  private final String jdkFlavor;

  private static final List<BuildImage> IMAGES = Arrays.asList(
                                                                   new BuildImage("docker.io/maven:3.6.3-jdk-8", "maven", "3.6.3", 8, "openjdk", "mvn", "clean", "install"),
                                                                   new BuildImage("docker.io/maven:3.6.3-jdk-11", "maven", "3.6.3", 11, "openjdk", "mvn", "clean", "install" ),
                                                                   new BuildImage("docker.io/maven:3.6.3-jdk-13", "maven", "3.6.3", 14, "openjdk", "mvn", "clean", "install"),

                                                                   new BuildImage("docker.io/maven:3.6.3-amazoncorretto-8", "maven", "3.6.3", 8, "amazoncorretto", "mvn", "clean", "install"),
                                                                   new BuildImage("docker.io/maven:3.6.3-amazoncorretto-11", "maven", "3.6.3", 11, "amazoncorreto", "mvn", "clean", "install"),

                                                                   new BuildImage("docker.io/maven:3.6.3-openj9-11", "maven", "3.6.3", 11, "openj9", "mvn", "clean", "install"),
                                                                   new BuildImage("docker.io/maven:3.6.3-ibmjava-8", "maven", "3.6.3", 8, "ibmjava", "mvn", "clean", "install"),

                                                                   new BuildImage("docker.io/gradle:6.3.0-jdk8", "gradle", "6.3.0", 8, "openjdk", "gradle", "clean", "build"),
                                                                   new BuildImage("docker.io/gradle:6.3.0-jdk11", "gradle", "6.3.0", 11, "openjdk", "gradle", "clean", "build"),
                                                                   new BuildImage("docker.io/gradle:6.3.0-jdk13", "gradle", "6.3.0", 13, "openjdk", "gradle", "clean", "build")
  );

  public static Optional<BuildImage> find(String tool, String toolVersion, int jdkVersion, String jdkFlavor) {
    ToIntFunction<BuildImage> dist = i -> i.distance(tool, toolVersion, jdkVersion, jdkFlavor);
    return IMAGES.stream().filter(i -> dist.applyAsInt(i) >= 0).sorted(Comparator.comparingInt(dist)).findFirst();
  }

  public BuildImage(String image, String tool, String toolVersion, int jdkVersion, String jdkFlavor, String... command) {
    this.image = image;
    this.command = command;
    this.tool = tool;
    this.toolVersion = toolVersion;
    this.jdkVersion = jdkVersion;
    this.jdkFlavor = jdkFlavor;
  }

  public String getImage() {
    return this.image;
  }

  public String[] getCommand() {
    return this.command;
  }

  public String getJdkFlavor() {
    return jdkFlavor;
  }

  public int getJdkVersion() {
    return jdkVersion;
  }

  public String getToolVersion() {
    return toolVersion;
  }

  public String getTool() {
    return tool;
  }

  public int distance(BuildImage other) {
    return distance(other.tool, other.toolVersion, other.jdkVersion, other.jdkFlavor);
  }

  public int distance(String tool, String toolVersion, int jdkVersion, String jdkFlavor) {
    int distance = 0;

    if (!this.tool.equals(tool)) {
      return -1;
    }

    distance += 1000 * Math.abs(this.jdkVersion - jdkVersion);
    distance += 100 * Math.abs(major(this.toolVersion) - major(toolVersion));
    distance += 10 * Math.abs(minor(this.toolVersion) - minor(toolVersion));
    distance += Math.abs(micro(this.toolVersion) - micro(toolVersion));

    if (!Strings.isNullOrEmpty(this.jdkFlavor) && !this.jdkFlavor.equals(jdkFlavor)) {
      distance += 99;
    }
    return distance;
  }

  private static int major(String version) {
    if (Strings.isNullOrEmpty(version)) {
      return 0;
    }
    String[] parts = version.split(DOT);
    return Integer.parseInt(parts[0].trim().replaceAll(NON_DIGIT, EMPTY));
  }

  private static int minor(String version) {
    if (Strings.isNullOrEmpty(version)) {
      return 0;
    }
    String[] parts = version.split(Pattern.quote(DOT));
    if (parts.length < 2) {
      return 0;
    }
    return Integer.parseInt(parts[1].trim().replaceAll(NON_DIGIT, EMPTY));
  }

  private static int micro(String version) {
    if (Strings.isNullOrEmpty(version)) {
      return 0;
    }
    String[] parts = version.split(Pattern.quote(DOT));
    if (parts.length < 3) {
      return 0;
    }
    return Integer.parseInt(parts[2].trim().replaceAll(NON_DIGIT, EMPTY));
  }

}
