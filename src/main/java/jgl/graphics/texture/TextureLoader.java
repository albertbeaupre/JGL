package jgl.graphics.texture;

import jgl.asset.AssetLoader;

public class TextureLoader implements AssetLoader<TextureParameters, TextureData> {
    @Override
    public TextureData load(TextureParameters parameters) {
        return TextureData.load(parameters.key());
    }
}
