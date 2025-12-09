package jgl.asset;

public interface AssetLoader<P extends AssetParameters, T> {

    T load(P parameters);
}
