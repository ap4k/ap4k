package io.ap4k.kubernetes.generator;

import io.ap4k.Session;
import io.ap4k.SessionWriter;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.apps.Deployment;
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.processor.SimpleFileWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class KubernetesApplicationGeneratorTest {

  static Path tempDir;

  @BeforeAll
  public static void setup() throws IOException {
    tempDir = Files.createTempDirectory("ap4k");
  }

  @Test
  public void shouldGenerateKubernetes()  {
    SessionWriter writer = new SimpleFileWriter(tempDir);
    Session session = Session.getSession();
    session.setWriter(writer);

    KubernetesApplicationGenerator generator = new KubernetesApplicationGenerator() {};

    Map<String, Object> map = new HashMap<String, Object>() {{
      put(KubernetesApplication.class.getName(), new HashMap<String, Object>() {{
        put("name", "generator-test");
        put("version", "latest");
        put("replicas", 2);
      }});
    }};

    generator.add(map);
    session.close();
    KubernetesList list=session.getGeneratedResources().get("kubernetes");
    assertThat(list).isNotNull();
    assertThat(list.getItems())
      .filteredOn(i -> "Deployment".equals(i.getKind()))
      .filteredOn(i -> ((Deployment)i).getSpec().getReplicas() == 2)
      .isNotEmpty();
  }
}
