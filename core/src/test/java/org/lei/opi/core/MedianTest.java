package org.lei.opi.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MedianTest {
    
    MedianTest() { ; }

    @Test
    public void test1() {
        MedianList ml = new MedianList(3);
            // just ones
        Assertions.assertEquals(1.0, ml.getMedian(1.0));
        Assertions.assertEquals(1.0, ml.getMedian(1.0));
        Assertions.assertEquals(1.0, ml.getMedian(1.0));

            // now start adding 2s
        Assertions.assertEquals(1.0, ml.getMedian(2.0));
        Assertions.assertEquals(2.0, ml.getMedian(2.0));
        Assertions.assertEquals(2.0, ml.getMedian(2.0));

            // add a 3 (so list is 2,2,3)
        Assertions.assertEquals(2.0, ml.getMedian(3));

            // add a 1 (so list is 1,2,3)
        Assertions.assertEquals(2.0, ml.getMedian(1));

            // add a 1 (so list is 1,1,3)
        Assertions.assertEquals(1.0, ml.getMedian(1));

            // test degenerate
        ml = new MedianList(1);
        Assertions.assertEquals(1.0, ml.getMedian(1));
        Assertions.assertEquals(120, ml.getMedian(120));

            // test even length
        ml = new MedianList(2);
        Assertions.assertEquals(1.0, ml.getMedian(1));
        Assertions.assertEquals(1.5, ml.getMedian(2));
        Assertions.assertEquals(1.5, ml.getMedian(1));
        Assertions.assertEquals(1.0, ml.getMedian(1));
    }
}
