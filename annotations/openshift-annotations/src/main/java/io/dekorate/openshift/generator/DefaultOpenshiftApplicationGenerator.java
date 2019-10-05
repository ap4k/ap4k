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
package io.dekorate.openshift.generator;

import io.dekorate.config.DefaultConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyDeployToImageConfiguration;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigBuilder;
import io.dekorate.project.ApplyProjectInfo;

public class DefaultOpenshiftApplicationGenerator implements OpenshiftApplicationGenerator {

    public DefaultOpenshiftApplicationGenerator () {
      on(new DefaultConfiguration<OpenshiftConfig>(new OpenshiftConfigBuilder()
                                                   .accept(new ApplyProjectInfo(getProject()))
                                                   .accept(new ApplyBuildToImageConfiguration())
                                                   .accept(new ApplyDeployToImageConfiguration())));
    }
}
