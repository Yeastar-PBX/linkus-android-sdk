package com.yxf.clippathlayout;

public class PathRegionGenerators {

    public static PathRegionGenerator createBitmapPathRegionGenerator() {

        return (path, clipType, width, height) -> new BitmapPathRegion(path, clipType, width, height);
    }

    public static PathRegionGenerator createBitmapPathRegionGenerator(final int inSampleSize) {
        return (path, clipType, width, height) -> new BitmapPathRegion(path, width, height, inSampleSize);
    }

    public static PathRegionGenerator createNativePathRegionGenerator() {
        return (path, clipType, width, height) -> new NativePathRegion(path, clipType);
    }

}
