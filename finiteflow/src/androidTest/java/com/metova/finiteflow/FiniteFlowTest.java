package com.metova.finiteflow;

import junit.framework.TestCase;

import java.lang.reflect.Method;

public class FiniteFlowTest extends TestCase {

    // region Test Events
    private boolean onEnterAHit = false, onExitAHit = false, onExitTestStateHit = false;

    public static final String TEST_INSTANCE_NAME = "test_instance";
    public static final String TEST_INSTANCE_NAME_TWO = "test_instance2";

    // Util for other class testing
    TestUtil mTestUtil = TestUtil.getInstance();

    @OnEnter(state = "A")
    public void onEnter() {
        onEnterAHit = true;
    }

    @OnExit(state = "A")
    public void onExit() {
        onExitAHit = true;
    }

    @OnExit(state = "TestState")
    public void onExitTestState() {
        onExitTestStateHit = true;
    }
    // endregion

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();

        // Be sure to clear the instances between test execution
        FiniteFlow.clearAllInstances();

        // Reset the TestUtil instance
        mTestUtil.setTestStateHit(false);
    }

    public void testInstance() throws Throwable {

        assertNull(FiniteFlow.getFiniteFlowInstances());

        FiniteFlow finiteFlow = FiniteFlow.getInstance(TEST_INSTANCE_NAME);
        assertNotNull(finiteFlow);

        assertNotNull(FiniteFlow.getFiniteFlowInstances());
        assertEquals(1, FiniteFlow.getFiniteFlowInstances().size());

        FiniteFlow finiteFlow2 = FiniteFlow.getInstance(TEST_INSTANCE_NAME);
        assertNotNull(finiteFlow2);

        assertEquals(1, FiniteFlow.getFiniteFlowInstances().size());

        assertTrue(finiteFlow.equals(finiteFlow2));

        FiniteFlow finiteFlow3 = FiniteFlow.getInstance(TEST_INSTANCE_NAME_TWO);
        assertNotNull(finiteFlow3);

        assertEquals(2, FiniteFlow.getFiniteFlowInstances().size());

        assertFalse(finiteFlow.equals(finiteFlow3));
        assertFalse(finiteFlow2.equals(finiteFlow3));

        FiniteFlow.clearInstance(TEST_INSTANCE_NAME);
        assertEquals(1, FiniteFlow.getFiniteFlowInstances().size());

        FiniteFlow.clearAllInstances();
        assertNull(FiniteFlow.getFiniteFlowInstances());
    }

    public void testAddStates() throws Throwable {

        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStates());

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).addState("A").addState("B");

        assertNotNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStates());

        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStates().get(0));
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStates().get(1));
    }

    public void testAddTransitions() throws Throwable {

        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitions());

        FlowInitializationException e = null;
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).addTransition("A", "B");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).addState("A");
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).addState("B");
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).addTransition("A", "B");

        assertNotNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitions());
        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitions().get(0).getFromState());
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitions().get(0).getToState());
        assertEquals("A_B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitions().get(0).getTransitionName());
    }

    public void testSetInitialState() throws Throwable {

        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());

        Exception e = null;
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).setInitialState("A");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).addState("A");

        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).setInitialState("B");
        }
        catch (FlowInvalidException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).setInitialState("A");
        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
    }

    public void testSetEventClasses() throws Throwable {

        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getEventClassInstances());
        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap());

        Exception e = null;

        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).setEventClasses(FiniteFlowTest.class);
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("B")
                .addTransition("A", "B")
                .setInitialState("A");

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).setEventClasses(FiniteFlowTest.class);
        assertNotNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getEventClassInstances());
        assertNotNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap());

        assertTrue(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getEventClassInstances().containsKey(FiniteFlowTest.class));
        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getEventClassInstances().get(FiniteFlowTest.class));

        // We only provided annotations for "A" in this class, not "B"
        assertTrue(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().containsKey("A"));
        assertFalse(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().containsKey("B"));

        // Ensure we have no object yet (we haven't registered yet)
        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().get("A").get(0).getInstance());
        assertEquals(FiniteFlowTest.class, FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().get("A").get(0).getStateClass());

        Method onEnterMethod = FiniteFlowTest.class.getMethod("onEnter");
        Method onExitMethod = FiniteFlowTest.class.getMethod("onExit");
        assertEquals(onEnterMethod, FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().get("A").get(0).getStateOnEnterMethod());
        assertEquals(onExitMethod, FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().get("A").get(0).getStateOnExitMethod());

        // Test register / unregister affecting the event mapping
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).register(this);
        assertEquals(this, FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().get("A").get(0).getInstance());
        assertEquals(this, FiniteFlow.getInstance(TEST_INSTANCE_NAME).getEventClassInstances().get(FiniteFlowTest.class));

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).unregister(this);
        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getStateEventMap().get("A").get(0).getInstance());
        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getEventClassInstances().get(FiniteFlowTest.class));
    }

    public void testApplyTransition() throws Throwable {

        Exception e = null;

        // Before proper setup
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).applyTransition("A_B");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        // Setup
        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("B")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .setInitialState("A");


        // Invalid move (not at state B)
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).applyTransition("B_B");
        }
        catch (InvalidStateChangeException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Invalid move (transition doesn't exist)
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).applyTransition("A_A");
        }
        catch (InvalidStateChangeException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).applyTransition("A_B");
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
    }

    public void testMoveToState() throws Throwable {

        Exception e = null;

        // Before proper setup
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("A");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Setup
        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("B")
                .addState("C")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .addTransition("B", "C")
                .setInitialState("A");

        // Invalid move (no transition from A to C)
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("C");
        }
        catch (InvalidStateChangeException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Proper move
        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("B");
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("C");
        assertEquals("C", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
    }

    public void testTransitionHistory() throws Throwable {

        Exception e = null;

        // Without setup
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToPreviousState();
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Setup
        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("B")
                .addState("C")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .addTransition("B", "C")
                .setInitialState("A");

        assertNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory());

        // Add to history
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("B");

        assertNotNull(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory());
        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().peek().getFromState());
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().peek().getToState());

        // Add to history
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("C");

        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().peek().getFromState());
        assertEquals("C", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().peek().getToState());

        // Check current state
        assertEquals("C", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());

        // Remove from history / move back
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToPreviousState();

        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().peek().getFromState());
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().peek().getToState());

        // Check current state
        assertEquals("B", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());

        // Remove from history / move back
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToPreviousState();

        assertEquals("A", FiniteFlow.getInstance(TEST_INSTANCE_NAME).getCurrentState());
        assertTrue(FiniteFlow.getInstance(TEST_INSTANCE_NAME).getTransitionHistory().isEmpty());

        // Ensure we can no longer move backwards once emptying out the history
        try {
            FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToPreviousState();
        }
        catch (FlowInvalidException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;
    }

    public void testEventsCalled() throws Throwable {

        // Setup
        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("B")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .addTransition("B", "A")
                .setInitialState("A")
                .setEventClasses(FiniteFlowTest.class);

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);

        // Should fail to call event without an instance registered
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("B");

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);

        // Reset to state A
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("A");

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).register(this);

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("B");

        assertFalse(onEnterAHit);
        assertTrue(onExitAHit);

        onEnterAHit = false;
        onExitAHit = false;

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("A");

        assertTrue(onEnterAHit);
        assertFalse(onExitAHit);

        // Unregister and try again, ensuring events are not hit
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).unregister(this);

        onEnterAHit = false;
        onExitAHit = false;

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("B");
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("A");

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);
    }

    public void testMultiClassEvents() throws Throwable {

        // Setup
        FiniteFlow.getInstance(TEST_INSTANCE_NAME)
                .addState("A")
                .addState("TestState")
                .addTransition("A", "TestState")
                .addTransition("TestState", "A")
                .setInitialState("A")
                .setEventClasses(FiniteFlowTest.class, TestUtil.class);

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);
        assertFalse(mTestUtil.isTestStateHit());
        assertFalse(onExitTestStateHit);

        // Should fail to call event without an instance registered
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("TestState");

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);
        assertFalse(mTestUtil.isTestStateHit());
        assertFalse(onExitTestStateHit);

        // Reset to A
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToPreviousState();

        // Register for events
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).register(this);
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).register(mTestUtil);

        // Now, move to states and check for events
        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("TestState");

        assertFalse(onEnterAHit);
        assertTrue(onExitAHit);
        assertTrue(mTestUtil.isTestStateHit());
        assertFalse(onExitTestStateHit);

        // Reset and move back
        onEnterAHit = false;
        onExitAHit = false;
        mTestUtil.setTestStateHit(false);

        FiniteFlow.getInstance(TEST_INSTANCE_NAME).moveToState("A");

        assertTrue(onEnterAHit);
        assertFalse(onExitAHit);
        assertFalse(mTestUtil.isTestStateHit());
        assertTrue(onExitTestStateHit);
    }
}
