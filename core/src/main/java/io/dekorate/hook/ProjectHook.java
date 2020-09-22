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
package io.dekorate.hook;

import io.dekorate.project.Project;
import io.dekorate.utils.Exec;

public abstract class ProjectHook implements Runnable {

  protected final Project project;
  //This is added as a class variable to ensure that the class is loaded when the hook is executed.
  private final Exec.ProjectExec exec;

  public ProjectHook(Project project) {
    this.project = project;
    this.exec = Exec.inProject(project);
  }

  /**
   * Things to execute when initializing.
   */
  public abstract void init();

  /**
   * Call an operation that will preload all required classes.
   * A shutdown hook cannot load new classes (at least it seems so).
   * So we need to preload all classes beforehand.
   */
  public abstract void warmup();

  /**
   * Register the hook for execution on shutdown.
   */
  public void register() {
    init();
    warmup();
    Runtime.getRuntime().addShutdownHook(new Thread(this));
  }

  public boolean exec(String... commands) {
    return exec.commands(commands);
  }
}
