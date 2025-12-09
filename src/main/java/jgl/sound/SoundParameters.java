package jgl.sound;

import jgl.asset.AssetParameters;

public class SoundParameters implements AssetParameters {

    private final String name;

    public SoundParameters(String name) {
        this.name = name;
    }

    @Override
    public String key() {
        return name;
    }
}
