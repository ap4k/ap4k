package io.ap4k.annotationless.annotationlessweb;

import io.ap4k.utils.Serialization;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.Service;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DemoApplicationTests {

  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization.unmarshal(getClass().getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
    assertNotNull(list);
    Service service = findFirst(list, Service.class).orElseThrow(IllegalStateException::new);
    assertNotNull(service);
    assertEquals(1, list.getItems().stream().filter(i -> Service.class.isInstance(i)).count());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
