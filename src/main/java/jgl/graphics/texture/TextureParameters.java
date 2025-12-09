package jgl.graphics.texture;

import jgl.asset.AssetParameters;

public class TextureParameters implements AssetParameters {

    private final String name;

    public TextureParameters(String path) {
        this.name = path;
    }

    @Override
    public String key() {
        return name;
    }
}
