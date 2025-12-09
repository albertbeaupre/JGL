package jgl.sound;

import jgl.asset.AssetLoader;

public class SoundLoader implements AssetLoader<SoundParameters, SoundData> {
    @Override
    public SoundData load(SoundParameters parameters) {
        return SoundData.load(parameters.key());
    }
}
