package au.gov.amsa.detection;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.junit.Test;

import au.gov.amsa.gt.Shapefile;

public class ShapefilesTest {

    @Test
    public void testHolderConstructorIsPrivate() {
        TestingUtil.callConstructorAndCheckIsPrivate(Shapefiles.Holder.class);
    }

    @Test(expected = RuntimeException.class)
    public void testException() {
        Shapefiles.instance().get("abc", new Callable<Shapefile>() {

            @Override
            public Shapefile call() throws Exception {
                throw new IOException();
            }
        });
    }

}
