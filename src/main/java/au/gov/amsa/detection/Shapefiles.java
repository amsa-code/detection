package au.gov.amsa.detection;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import au.gov.amsa.gt.Shapefile;

public class Shapefiles {

    public static class Holder {
        private static final Shapefiles INSTANCE = new Shapefiles();
    }

    public static Shapefiles instance() {
        return Holder.INSTANCE;
    }

    private final Cache<String, Shapefile> cache = CacheBuilder.<String, Shapefile> newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES).build();

    public Shapefile get(String key, Callable<Shapefile> creator) {
        try {
            return cache.get(key, creator);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
