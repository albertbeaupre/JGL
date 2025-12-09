package jgl.asset;

import java.util.Collections;
import java.util.List;

public interface AssetParameters {

    String key();

    default List<AssetParameters> dependencies() {
        return Collections.emptyList();
    }
}
