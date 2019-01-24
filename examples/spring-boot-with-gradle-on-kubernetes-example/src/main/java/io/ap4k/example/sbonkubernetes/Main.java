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
package io.ap4k.example.sbonkubernetes;

import io.ap4k.docker.annotation.EnableDockerBuild;
import io.ap4k.kubernetes.annotation.Env;
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.docker.annotation.EnableDockerBuild;
import io.ap4k.kubernetes.annotation.Port;
import io.ap4k.kubernetes.annotation.ServiceType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@KubernetesApplication(envVars = @Env(name = "MESSAGE", value = "hello world"))
@EnableDockerBuild
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}

