package io.ap4k.decorator;

import io.ap4k.config.AwsElasticBlockStoreVolume;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;

public class AddAwsElasticBlockStoreVolume extends Decorator<PodSpecBuilder> {

    private final AwsElasticBlockStoreVolume volume;

    public AddAwsElasticBlockStoreVolume(AwsElasticBlockStoreVolume volume) {
        this.volume = volume;
    }

    @Override
    public void visit(PodSpecBuilder podSpec) {
        podSpec.addNewVolume()
                .withName(volume.getVolumeName())
                .withNewAwsElasticBlockStore()
                .withVolumeID(volume.getVolumeId())
                .withFsType(volume.getFsType())
                .withNewPartition(volume.getPartition())
                .withReadOnly(volume.isReadOnly())
                .endAwsElasticBlockStore();
    }
}
