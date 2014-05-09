package com.sea_monster.core.resource.compress;

import android.graphics.Bitmap;

/**
 * Created by DragonJ on 13-7-3.
 */
public interface IResourceCompressHandler {
    public Bitmap compressResourceWithCrop(AbstractCompressRequest request);
    public Bitmap compressResource(AbstractCompressRequest request);
}
