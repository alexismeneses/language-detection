package com.cybozu.labs.langdetect;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link DetectorFactory} profile loading.
 * @author Alexis Meneses
 *
 */
public class DetectorFactoryTest {

    private DetectorFactory detectorFactory;

    @Before
    public void setUp() throws Exception {
        detectorFactory = DetectorFactory.newInstance();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testDefaultProfiles() throws Exception {
        detectorFactory.loadDefaultProfiles();
        assertTrue(detectorFactory.capacity > 0);
        assertEquals(detectorFactory.capacity, detectorFactory.langlist.size());
    }
}