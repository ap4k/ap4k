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
package io.dekorate.kubernetes.annotation;

public @interface Mount {

  /**
   * The name of the volumeName to mount.
   * @return  The name.
   */
  String name();

  /**
   * The path to mount.
   * @return  The path.
   */
  String path();

  /**
   * Path within the volumeName from which the container's volumeName should be mounted.
   * @return  The subPath.
   */
  String subPath() default "";

  /**
   * ReadOnly
   * @return  True if mount is readonly, False otherwise.
   */
  boolean readOnly() default false;
}
