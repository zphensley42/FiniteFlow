package com.metova.finiteflow;

/**
 * Util to help test certain things (like different classes for events)
 */
public class TestUtil {

    private boolean mTestStateHit = false;

    public static TestUtil getInstance() {
        return new TestUtil();
    }

    private TestUtil() {}

    @OnEnter(state = "TestState")
    public void onEnterTestState() {
        mTestStateHit = true;
    }

    public boolean isTestStateHit() {
        return mTestStateHit;
    }

    public void setTestStateHit(boolean testStateHit) {
        mTestStateHit = testStateHit;
    }
}
