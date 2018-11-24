package io.ap4k.decorator;

import io.ap4k.config.SecretVolume;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;

public class AddSecretVolume extends Decorator<PodSpecBuilder> {

    private final SecretVolume volume;

    public AddSecretVolume(SecretVolume volume) {
        this.volume = volume;
    }

    @Override
    public void visit(PodSpecBuilder podSpec) {
        podSpec.addNewVolume()
                .withName(volume.getVolumeName())
                .withNewSecret()
                    .withSecretName(volume.getSecretName())
                    .withDefaultMode(volume.getDefaultMode())
                    .withOptional(volume.isOptional())
                .endSecret();

    }
}
