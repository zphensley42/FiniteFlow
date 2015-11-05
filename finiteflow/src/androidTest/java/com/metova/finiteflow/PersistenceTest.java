package com.metova.finiteflow;

import android.test.AndroidTestCase;

public class PersistenceTest extends AndroidTestCase {

    public static final String TEST_INSTANCE_NAME = "test_instance";

    public void testDiskPersistence() throws Throwable {

        // Setup
        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("B")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .addTransition("B", "A")
                .setInitialState("A")
                .setEventClasses(FiniteFlowTest.class);

        // Attempt persistence
        assertTrue(PersistenceUtil.persistFlow(getContext(), PersistenceUtil.PERSIST_TYPE.TYPE_DISK, FiniteFlow.getInstance(TEST_INSTANCE_NAME)));

        // Attempt read
        FiniteFlow finiteFlow = PersistenceUtil.readFlow(getContext(), PersistenceUtil.PERSIST_TYPE.TYPE_DISK, TEST_INSTANCE_NAME);
        assertNotNull(finiteFlow);
    }
}
