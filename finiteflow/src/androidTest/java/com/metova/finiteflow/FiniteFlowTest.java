package com.metova.finiteflow;

import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.lang.reflect.Method;

// TODO: Test events where OnExit and OnEnter are in different classes
public class FiniteFlowTest extends AndroidTestCase {

    // region Test Events
    private boolean onEnterAHit = false, onExitAHit = false;

    @OnEnter(state = "A")
    public void onEnter() {
        onEnterAHit = true;
    }

    @OnExit(state = "A")
    public void onExit() {
        onExitAHit = true;
    }
    // endregion

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FiniteFlow.clearAllInstances(); // Be sure to clear the instances between test execution
    }

    public void testInstance() throws Throwable {

        assertNull(FiniteFlow.getFiniteFlowInstances());

        FiniteFlow finiteFlow = FiniteFlow.getInstance(getContext());
        assertNotNull(finiteFlow);

        assertNotNull(FiniteFlow.getFiniteFlowInstances());
        assertEquals(1, FiniteFlow.getFiniteFlowInstances().size());

        FiniteFlow finiteFlow2 = FiniteFlow.getInstance(getContext());
        assertNotNull(finiteFlow2);

        assertEquals(1, FiniteFlow.getFiniteFlowInstances().size());

        assertTrue(finiteFlow.equals(finiteFlow2));

        MockContext mockContext = new MockContext();
        FiniteFlow finiteFlow3 = FiniteFlow.getInstance(mockContext);
        assertNotNull(finiteFlow3);

        assertEquals(2, FiniteFlow.getFiniteFlowInstances().size());

        assertFalse(finiteFlow.equals(finiteFlow3));
        assertFalse(finiteFlow2.equals(finiteFlow3));

        FiniteFlow.clearInstance(mockContext);
        assertEquals(1, FiniteFlow.getFiniteFlowInstances().size());

        FiniteFlow.clearAllInstances();
        assertNull(FiniteFlow.getFiniteFlowInstances());
    }

    public void testAddStates() throws Throwable {

        assertNull(FiniteFlow.getInstance(getContext()).getStates());

        FiniteFlow.getInstance(getContext()).addState("A").addState("B");

        assertNotNull(FiniteFlow.getInstance(getContext()).getStates());

        assertEquals("A", FiniteFlow.getInstance(getContext()).getStates().get(0));
        assertEquals("B", FiniteFlow.getInstance(getContext()).getStates().get(1));
    }

    public void testAddTransitions() throws Throwable {

        assertNull(FiniteFlow.getInstance(getContext()).getTransitions());

        FlowInitializationException e = null;
        try {
            FiniteFlow.getInstance(getContext()).addTransition("A", "B");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);

        FiniteFlow.getInstance(getContext()).addState("A");
        FiniteFlow.getInstance(getContext()).addState("B");
        FiniteFlow.getInstance(getContext()).addTransition("A", "B");

        assertNotNull(FiniteFlow.getInstance(getContext()).getTransitions());
        assertEquals("A", FiniteFlow.getInstance(getContext()).getTransitions().get(0).getFromState());
        assertEquals("B", FiniteFlow.getInstance(getContext()).getTransitions().get(0).getToState());
        assertEquals("A_B", FiniteFlow.getInstance(getContext()).getTransitions().get(0).getTransitionName());
    }

    public void testSetInitialState() throws Throwable {

        assertNull(FiniteFlow.getInstance(getContext()).getCurrentState());

        Exception e = null;
        try {
            FiniteFlow.getInstance(getContext()).setInitialState("A");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        FiniteFlow.getInstance(getContext()).addState("A");

        try {
            FiniteFlow.getInstance(getContext()).setInitialState("B");
        }
        catch (FlowInvalidException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        FiniteFlow.getInstance(getContext()).setInitialState("A");
        assertEquals("A", FiniteFlow.getInstance(getContext()).getCurrentState());
    }

    public void testSetEventClasses() throws Throwable {

        assertNull(FiniteFlow.getInstance(getContext()).getEventClassInstances());
        assertNull(FiniteFlow.getInstance(getContext()).getStateEventMap());

        Exception e = null;

        try {
            FiniteFlow.getInstance(getContext()).setEventClasses(FiniteFlowTest.class);
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        FiniteFlow.getInstance(getContext())
                .addState("A")
                .addState("B")
                .addTransition("A", "B")
                .setInitialState("A");

        FiniteFlow.getInstance(getContext()).setEventClasses(FiniteFlowTest.class);
        assertNotNull(FiniteFlow.getInstance(getContext()).getEventClassInstances());
        assertNotNull(FiniteFlow.getInstance(getContext()).getStateEventMap());

        assertTrue(FiniteFlow.getInstance(getContext()).getEventClassInstances().containsKey(FiniteFlowTest.class));
        assertNull(FiniteFlow.getInstance(getContext()).getEventClassInstances().get(FiniteFlowTest.class));

        // We only provided annotations for "A" in this class, not "B"
        assertTrue(FiniteFlow.getInstance(getContext()).getStateEventMap().containsKey("A"));
        assertFalse(FiniteFlow.getInstance(getContext()).getStateEventMap().containsKey("B"));

        // Ensure we have no object yet (we haven't registered yet)
        assertNull(FiniteFlow.getInstance(getContext()).getStateEventMap().get("A").getInstance());
        assertEquals(FiniteFlowTest.class, FiniteFlow.getInstance(getContext()).getStateEventMap().get("A").getStateClass());

        Method onEnterMethod = FiniteFlowTest.class.getMethod("onEnter");
        Method onExitMethod = FiniteFlowTest.class.getMethod("onExit");
        assertEquals(onEnterMethod, FiniteFlow.getInstance(getContext()).getStateEventMap().get("A").getStateOnEnterMethod());
        assertEquals(onExitMethod, FiniteFlow.getInstance(getContext()).getStateEventMap().get("A").getStateOnExitMethod());

        // Test register / unregister affecting the event mapping
        FiniteFlow.getInstance(getContext()).register(this);
        assertEquals(this, FiniteFlow.getInstance(getContext()).getStateEventMap().get("A").getInstance());
        assertEquals(this, FiniteFlow.getInstance(getContext()).getEventClassInstances().get(FiniteFlowTest.class));

        FiniteFlow.getInstance(getContext()).unregister(this);
        assertNull(FiniteFlow.getInstance(getContext()).getStateEventMap().get("A").getInstance());
        assertNull(FiniteFlow.getInstance(getContext()).getEventClassInstances().get(FiniteFlowTest.class));
    }

    public void testApplyTransition() throws Throwable {

        Exception e = null;

        // Before proper setup
        try {
            FiniteFlow.getInstance(getContext()).applyTransition("A_B");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        // Setup
        FiniteFlow.getInstance(getContext())
                .addState("A")
                .addState("B")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .setInitialState("A");


        // Invalid move (not at state B)
        try {
            FiniteFlow.getInstance(getContext()).applyTransition("B_B");
        }
        catch (InvalidStateChangeException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Invalid move (transition doesn't exist)
        try {
            FiniteFlow.getInstance(getContext()).applyTransition("A_A");
        }
        catch (InvalidStateChangeException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;

        assertEquals("A", FiniteFlow.getInstance(getContext()).getCurrentState());
        FiniteFlow.getInstance(getContext()).applyTransition("A_B");
        assertEquals("B", FiniteFlow.getInstance(getContext()).getCurrentState());
    }

    public void testMoveToState() throws Throwable {

        Exception e = null;

        // Before proper setup
        try {
            FiniteFlow.getInstance(getContext()).moveToState("A");
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Setup
        FiniteFlow.getInstance(getContext())
                .addState("A")
                .addState("B")
                .addState("C")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .addTransition("B", "C")
                .setInitialState("A");

        // Invalid move (no transition from A to C)
        try {
            FiniteFlow.getInstance(getContext()).moveToState("C");
        }
        catch (InvalidStateChangeException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Proper move
        assertEquals("A", FiniteFlow.getInstance(getContext()).getCurrentState());
        FiniteFlow.getInstance(getContext()).moveToState("B");
        assertEquals("B", FiniteFlow.getInstance(getContext()).getCurrentState());
        FiniteFlow.getInstance(getContext()).moveToState("C");
        assertEquals("C", FiniteFlow.getInstance(getContext()).getCurrentState());
    }

    public void testTransitionHistory() throws Throwable {

        Exception e = null;

        // Without setup
        try {
            FiniteFlow.getInstance(getContext()).moveToPreviousState();
        }
        catch (FlowInitializationException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;


        // Setup
        FiniteFlow.getInstance(getContext())
                .addState("A")
                .addState("B")
                .addState("C")
                .addTransition("A", "B")
                .addTransition("B", "B")
                .addTransition("B", "C")
                .setInitialState("A");

        assertNull(FiniteFlow.getInstance(getContext()).getTransitionHistory());

        // Add to history
        FiniteFlow.getInstance(getContext()).moveToState("B");

        assertNotNull(FiniteFlow.getInstance(getContext()).getTransitionHistory());
        assertEquals("A", FiniteFlow.getInstance(getContext()).getTransitionHistory().peek().getFromState());
        assertEquals("B", FiniteFlow.getInstance(getContext()).getTransitionHistory().peek().getToState());

        // Add to history
        FiniteFlow.getInstance(getContext()).moveToState("C");

        assertEquals("B", FiniteFlow.getInstance(getContext()).getTransitionHistory().peek().getFromState());
        assertEquals("C", FiniteFlow.getInstance(getContext()).getTransitionHistory().peek().getToState());

        // Check current state
        assertEquals("C", FiniteFlow.getInstance(getContext()).getCurrentState());

        // Remove from history / move back
        FiniteFlow.getInstance(getContext()).moveToPreviousState();

        assertEquals("A", FiniteFlow.getInstance(getContext()).getTransitionHistory().peek().getFromState());
        assertEquals("B", FiniteFlow.getInstance(getContext()).getTransitionHistory().peek().getToState());

        // Check current state
        assertEquals("B", FiniteFlow.getInstance(getContext()).getCurrentState());

        // Remove from history / move back
        FiniteFlow.getInstance(getContext()).moveToPreviousState();

        assertEquals("A", FiniteFlow.getInstance(getContext()).getCurrentState());
        assertTrue(FiniteFlow.getInstance(getContext()).getTransitionHistory().isEmpty());

        // Ensure we can no longer move backwards once emptying out the history
        try {
            FiniteFlow.getInstance(getContext()).moveToPreviousState();
        }
        catch (FlowInvalidException ex) {
            e = ex;
        }

        assertNotNull(e);
        e = null;
    }

    public void testEventsCalled() throws Throwable {

        // Setup
        FiniteFlow.getInstance(getContext())
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
        FiniteFlow.getInstance(getContext()).moveToState("B");

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);

        // Reset to state A
        FiniteFlow.getInstance(getContext()).moveToState("A");

        FiniteFlow.getInstance(getContext()).register(this);

        FiniteFlow.getInstance(getContext()).moveToState("B");

        assertFalse(onEnterAHit);
        assertTrue(onExitAHit);

        onEnterAHit = false;
        onExitAHit = false;

        FiniteFlow.getInstance(getContext()).moveToState("A");

        assertTrue(onEnterAHit);
        assertFalse(onExitAHit);

        // Unregister and try again, ensuring events are not hit
        FiniteFlow.getInstance(getContext()).unregister(this);

        onEnterAHit = false;
        onExitAHit = false;

        FiniteFlow.getInstance(getContext()).moveToState("B");
        FiniteFlow.getInstance(getContext()).moveToState("A");

        assertFalse(onEnterAHit);
        assertFalse(onExitAHit);
    }
}
