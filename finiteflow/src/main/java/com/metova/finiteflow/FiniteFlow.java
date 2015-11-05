package com.metova.finiteflow;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Class that is responsible for keeping state information / moving between states
 * TODO: Either disable ability to modify states / transitions after creation
 * TODO: or provide a method to re-initialize the FSM
 */
public class FiniteFlow implements Serializable {

    private static final String TAG = FiniteFlow.class.getSimpleName();


    /**
     * Defines a mapping of context to FiniteFlow instances, allowing mutliple instances based on context
     */
    private static Map<String, FiniteFlow> mFiniteFlowInstances;


    /**
     * Defines a mapping of class to instance used for calling event method via reflection
     * This is generated during initialization and updated via register / unregister
     */
    private Map<Class, Object> mEventClassInstances;

    /**
     * Defines a mapping of state to event method information
     * This is generated during initialization and updated via register / unregister
     */
    private Map<String, List<StateEventEntry>> mStateEventMap;

    /**
     * Defines a list of states defined by string
     */
    private List<String> mStates;

    /**
     * Defines a list of transitions, defined by start / end states according to the {@link Transition} class
     */
    private List<Transition> mTransitions;

    /**
     * Defines our current state by string name
     */
    private String mCurrentState;

    /**
     * Defines a list of all transitions taken in the past (via Stack)
     */
    private Stack<Transition> mTransitionHistory;

    /**
     * Identifier for the instance
     */
    private String mIdentifier;


    /**
     * Return an instance of FiniteFlow based on the provided context
     * @param identifier String to map the instance to
     * @return Builder instance
     */
    public static FiniteFlow getInstance(@NonNull String identifier) {

        if(mFiniteFlowInstances == null) { mFiniteFlowInstances = new HashMap<>(); }

        if(!mFiniteFlowInstances.containsKey(identifier)) {
            mFiniteFlowInstances.put(identifier, new FiniteFlow(identifier));
        }

        return mFiniteFlowInstances.get(identifier);
    }

    private FiniteFlow(String identifier) {
        mIdentifier = identifier;
    }


    /**
     * Clear / remove an instance mapped to the input context
     * @param identifier identifier of the instance to remove
     */
    public static void clearInstance(@NonNull String identifier) {

        if(mFiniteFlowInstances == null) { return; }

        mFiniteFlowInstances.remove(identifier);
    }

    /**
     * Remove all instance-context mappings of FiniteFlow
     */
    public static void clearAllInstances() {

        if(mFiniteFlowInstances == null) { return; }
        mFiniteFlowInstances.clear();
        mFiniteFlowInstances = null;
    }


    /**
     * Add all classes that provide event method implementations via our Annotations {@link OnEnter} {@link OnExit}
     * @param classes var-args list of classes that contain OnEnter / OnExit events for states
     * @return Builder instance
     * @throws FlowInitializationException If the flow has not been initialized with states / transitions / starting state
     */
    public FiniteFlow setEventClasses(@NonNull Class...classes) throws FlowInitializationException {

        mEventClassInstances = new HashMap<>();
        for(Class cls : classes) {

            mEventClassInstances.put(cls, null);
        }
        initEventMapping();
        return this;
    }


    // TODO: Unit test this
    /**
     * Set an instance of an event class for state event method calls
     * @param object The instance to use when making event calls
     */
    public void register(Object object) {

        for(Map.Entry<Class, Object> entry : mEventClassInstances.entrySet()) {

            if(entry.getKey().equals(object.getClass())) {

                entry.setValue(object);

                // Modify all state event mappings with this instance
                for(Map.Entry<String, List<StateEventEntry>> eventEntry : mStateEventMap.entrySet()) {

                    List<StateEventEntry> entries = eventEntry.getValue();
                    for(StateEventEntry stateEventEntry : entries) {

                        // If the class is the same, set our instance
                        if(stateEventEntry.mStateClass.equals(entry.getKey())) {

                            stateEventEntry.mInstance = object;
                        }
                    }
                }
            }
        }
    }

    // TODO: Unit test this
    /**
     * Remove an instance of an event class for state event method calls
     * @param object The instance to use when making event calls
     */
    public void unregister(Object object) {

        for(Map.Entry<Class, Object> entry : mEventClassInstances.entrySet()) {

            if(entry.getKey().equals(object.getClass())) {

                entry.setValue(null);

                // Modify all state event mappings with this instance
                for(Map.Entry<String, List<StateEventEntry>> eventEntry : mStateEventMap.entrySet()) {

                    List<StateEventEntry> entries = eventEntry.getValue();
                    for(StateEventEntry stateEventEntry : entries) {

                        // If the class is the same, set our instance
                        if(stateEventEntry.mStateClass.equals(entry.getKey())) {

                            stateEventEntry.mInstance = null;
                        }
                    }
                }
            }
        }
    }

    /**
     * Set the starting state for the FSM (requires no transition). This MUST occur as the FSM MUST have a starting state to be valid.
     * @param state The state to begin at. This state MUST be added before making this call.
     * @return Builder instance
     * @throws FlowInitializationException In case states have not been added before making this call or the state provided is invalid
     * @throws FlowInvalidException In case the state doesn't exist
     */
    public synchronized FiniteFlow setInitialState(@NonNull String state) throws FlowInitializationException, FlowInvalidException {

        if(mStates == null) { throw new FlowInitializationException(); }

        for(String s : mStates) {
            if(s.equals(state)) {
                mCurrentState = state;
                return this;
            }
        }

        throw new FlowInvalidException("The state does not exist for moving to the initial state");
    }

    /**
     * Add a state to the FSM's list of states
     * @param name The state to add
     * @return Builder instance
     */
    public synchronized FiniteFlow addState(@NonNull String name) {

        if(mStates == null) { mStates = new ArrayList<String>(); }

        if(!mStates.contains(name)) {
            mStates.add(name);
        }
        else {
            Log.d(TAG, "Duplicate state not added: " + name);
        }
        return this;
    }

    // region Helper methods for @Flow annotation
    public FiniteFlow flowFor(Object object) throws FlowInitializationException, FlowInvalidException {
        addStates(object);
        addTransitions(object);
        addInitialState(object);
        return this;
    }

    public FiniteFlow addInitialState(Object object) throws FlowInvalidException, FlowInitializationException {
        setInitialState(object.getClass().getAnnotation(Flow.class).initialState());
        return this;
    }

    public FiniteFlow addStates(Object object){
        String[] states = object.getClass().getAnnotation(Flow.class).states();
        for (int i = 0; i < states.length; i++) {
            addState(states[i]);
        }
        return this;
    }

    public FiniteFlow addTransitions(Object object) throws FlowInitializationException{
        FlowTransition[] transitions = object.getClass().getAnnotation(Flow.class).transitions();
        for (int i = 0; i < transitions.length; i++) {
            addTransition(transitions[i].from(), transitions[i].to());
        }
        return this;
    }
    // endregion Helper methods

    /**
     * Add a transition to the FSM's list of transitions
     * @param fromState The state to move from
     * @param toState The state to move to
     * @return Builder instance
     * @throws FlowInitializationException If either of the states do not exist
     */
    public synchronized FiniteFlow addTransition(@NonNull String fromState, @NonNull String toState) throws FlowInitializationException {

        if(mStates == null || mStates.isEmpty()) { throw new FlowInitializationException(); }

        if(mTransitions == null) { mTransitions = new ArrayList<Transition>(); }

        Transition transition = new Transition(fromState, toState);

        boolean fromFound = false, toFound = false;
        for(String state : mStates) {
            if(state.equals(fromState)) { fromFound = true; }
            if(state.equals(toState)) { toFound = true; }
        }

        if(!fromFound || !toFound) { throw new FlowInitializationException(); }

        if(!mTransitions.contains(transition)) {
            mTransitions.add(transition);
        }
        else {
            Log.d(TAG, "Duplicate transition not added: " + transition.toString());
        }
        return this;
    }

    /**
     * Move from state to state by applying a specific transitions state changes
     * @param name The state-generated name of the transition to use
     * @return Builder instance
     * @throws InvalidStateChangeException In case moving with the transition is not valid for the current state of the FSM
     * @throws FlowInitializationException If the FSM has not been setup correctly with states / transitions / starting state
     */
    public synchronized FiniteFlow applyTransition(@NonNull String name) throws InvalidStateChangeException, FlowInitializationException {

        if(!isValidForTransitions()) { throw new FlowInitializationException(); }

        for(Transition transition : mTransitions) {

            if(transition.getTransitionName().equals(name)) {

                // If the transition is valid, attempt to apply it
                if(transition.getFromState().equals(mCurrentState)) {

                    mCurrentState = transition.getToState();
                    callEvents(transition, false);
                    addTransitionToHistory(transition);
                    return this;
                }
                else {

                    throw new InvalidStateChangeException(mCurrentState, transition);
                }
            }
        }

        throw new InvalidStateChangeException(mCurrentState, null);
    }

    /**
     * Attempt to move to the provided state from the current state
     * @param name The state to attempt to move to
     * @return Builder instance
     * @throws InvalidStateChangeException In case moving to the supplied state is not valid for the current state of the FSM
     * @throws FlowInitializationException If the FSM has not been setup correctly with states / transitions / starting state
     */
    public synchronized FiniteFlow moveToState(@NonNull String name) throws InvalidStateChangeException, FlowInitializationException {

        if(!isValidForTransitions()) { throw new FlowInitializationException(); }

        for(Transition transition : mTransitions) {

            // If a transition exists from current state to new state, this is valid (use the transition)
            if(transition.getFromState().equals(mCurrentState) && transition.getToState().equals(name)) {

                mCurrentState = name;
                callEvents(transition, false);
                addTransitionToHistory(transition);
                return this;
            }
        }

        throw new InvalidStateChangeException(mCurrentState, new Transition(mCurrentState, name));
    }

    /**
     * Using the transition history, move to the previous state if applicable
     * @return Builder instance
     * @throws FlowInitializationException If the FSM has not been setup correctly with states / transitions / starting state / no transition history (for now)
     * @throws FlowInvalidException If the flow does not have any history to move to.
     */
    public synchronized FiniteFlow moveToPreviousState() throws FlowInvalidException, FlowInitializationException {

        if(!isValidForTransitions()) { throw new FlowInitializationException(); }
        if(mTransitionHistory == null || mTransitionHistory.isEmpty()) { throw new FlowInvalidException("The flow has no history to move to."); }

        Transition transition = removeTransitionFromHistory();
        if(transition != null) {

            mCurrentState = transition.getFromState();
            callEvents(transition, true);
        }

        return this;
    }


    // region Utility
    private boolean isValidForTransitions() {

        return mCurrentState != null
                && mStates != null && !mStates.isEmpty()
                && mTransitions != null && !mTransitions.isEmpty();
    }

    private void addTransitionToHistory(Transition transition) {

        if(mTransitionHistory == null) { mTransitionHistory = new Stack<Transition>(); }

        mTransitionHistory.push(transition);
    }

    private Transition removeTransitionFromHistory() {

        if(mTransitionHistory == null) { mTransitionHistory = new Stack<Transition>(); }

        try {
            return mTransitionHistory.pop();
        }
        catch (EmptyStackException e) {
            return null;
        }
    }

    // region Events
    private void initEventMapping() throws FlowInitializationException {

        if(!isValidForTransitions()) { throw new FlowInitializationException(); }

        mStateEventMap = new HashMap<>();
        for(String state : mStates) {

            for(Map.Entry<Class, Object> entry : mEventClassInstances.entrySet()) {

                StateEventEntry stateEventEntry = getMethodsForState(entry.getKey(), entry.getValue(), state);
                if(stateEventEntry != null) {

                    List<StateEventEntry> entries = mStateEventMap.get(state);
                    if(entries != null) {
                        if(!entries.contains(stateEventEntry)) {
                            entries.add(stateEventEntry);
                        }
                    }
                    else {
                        entries = new ArrayList<>();
                        entries.add(stateEventEntry);
                    }
                    mStateEventMap.put(state, entries);
                }
            }
        }
    }

    /**
     * Method that calls event of annotated methods for classes related to flows
     */
    private void callEvents(@NonNull Transition transition, boolean reverse) {

        if(!isValidForTransitions()) { return; }
        if(mStateEventMap == null || mEventClassInstances == null) { return; }

        Method onExit = null, onEnter = null;
        Object enterInstance = null, exitInstance = null;
        for(Map.Entry<String, List<StateEventEntry>> entry : mStateEventMap.entrySet()) {

            List<StateEventEntry> entries = entry.getValue();
            for(StateEventEntry stateEventEntry : entries) {

                if(entry.getKey().equals(transition.getFromState())) {

                    // Get the OnExit (OnEnter if reversed)
                    if(reverse) {
                        if(stateEventEntry.mStateOnEnterMethod != null) {
                            onEnter = stateEventEntry.mStateOnEnterMethod;
                            enterInstance = stateEventEntry.mInstance;
                        }
                    }
                    else {
                        if(stateEventEntry.mStateOnExitMethod != null) {
                            onExit = stateEventEntry.mStateOnExitMethod;
                            exitInstance = stateEventEntry.mInstance;
                        }
                    }
                }

                if(entry.getKey().equals(transition.getToState())) {

                    // Get the OnEnter (OnExit if reversed)
                    if(reverse) {
                        if(stateEventEntry.mStateOnExitMethod != null) {
                            onExit = stateEventEntry.mStateOnExitMethod;
                            exitInstance = stateEventEntry.mInstance;
                        }
                    }
                    else {
                        if(stateEventEntry.mStateOnEnterMethod != null) {
                            onEnter = stateEventEntry.mStateOnEnterMethod;
                            enterInstance = stateEventEntry.mInstance;
                        }
                    }
                }
            }
        }

        if(onExit != null && exitInstance != null) {

            try {
                onExit.invoke(exitInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if(onEnter != null && enterInstance != null) {

            try {
                onEnter.invoke(enterInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static StateEventEntry getMethodsForState(final Class<?> type, final Object object, String state) {

        final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(type.getDeclaredMethods()));

        StateEventEntry stateEventEntry = new StateEventEntry(type, object);
        for (final Method method : allMethods) {

            if (method.isAnnotationPresent(OnEnter.class) && method.getAnnotation(OnEnter.class).state().equals(state)) {

                if(stateEventEntry.mStateOnEnterMethod != null) {
                    Log.w(TAG, "OnEnter method on class " + type.getSimpleName() + " for state " +  state + " already exists, being overwritten...");
                }
                stateEventEntry.mStateOnEnterMethod = method;
            }

            if(method.isAnnotationPresent(OnExit.class) && method.getAnnotation(OnExit.class).state().equals(state)) {

                if(stateEventEntry.mStateOnExitMethod != null) {
                    Log.w(TAG, "OnExit method on class " + type.getSimpleName() + " for state " + state + " already exists, being overwritten...");
                }
                stateEventEntry.mStateOnExitMethod = method;
            }
        }

        if(stateEventEntry.mStateOnExitMethod != null || stateEventEntry.mStateOnEnterMethod != null) {
            return stateEventEntry;
        }
        else {
            return null;
        }
    }
    // endregion
    // endregion


    // region Classes
    public static class StateEventEntry {

        private Class mStateClass;
        private Object mInstance;
        private Method mStateOnEnterMethod;
        private Method mStateOnExitMethod;

        public StateEventEntry(Class stateClass, Object instance) {
            mStateClass = stateClass;
            mInstance = instance;
        }

        public Class getStateClass() {
            return mStateClass;
        }

        public Object getInstance() {
            return mInstance;
        }

        public Method getStateOnEnterMethod() {
            return mStateOnEnterMethod;
        }

        public Method getStateOnExitMethod() {
            return mStateOnExitMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StateEventEntry that = (StateEventEntry) o;

            if (mStateClass != null ? !mStateClass.equals(that.mStateClass) : that.mStateClass != null) {
                return false;
            }
            if (mInstance != null ? !mInstance.equals(that.mInstance) : that.mInstance != null) {
                return false;
            }
            if (mStateOnEnterMethod != null ? !mStateOnEnterMethod.equals(that.mStateOnEnterMethod) : that.mStateOnEnterMethod != null) {
                return false;
            }
            return !(mStateOnExitMethod != null ? !mStateOnExitMethod.equals(that.mStateOnExitMethod) : that.mStateOnExitMethod != null);

        }

        @Override
        public int hashCode() {
            int result = mStateClass != null ? mStateClass.hashCode() : 0;
            result = 31 * result + (mInstance != null ? mInstance.hashCode() : 0);
            result = 31 * result + (mStateOnEnterMethod != null ? mStateOnEnterMethod.hashCode() : 0);
            result = 31 * result + (mStateOnExitMethod != null ? mStateOnExitMethod.hashCode() : 0);
            return result;
        }
    }
    // endregion


    // region Accessors (for information / to help testing)
    public Map<Class, Object> getEventClassInstances() {
        return mEventClassInstances;
    }

    public Map<String, List<StateEventEntry>> getStateEventMap() {
        return mStateEventMap;
    }

    public List<String> getStates() {
        return mStates;
    }

    public List<Transition> getTransitions() {
        return mTransitions;
    }

    public String getCurrentState() {
        return mCurrentState;
    }

    public Stack<Transition> getTransitionHistory() {
        return mTransitionHistory;
    }

    public static Map<String, FiniteFlow> getFiniteFlowInstances() {
        return mFiniteFlowInstances;
    }

    public String getIdentifier() {
        return mIdentifier;
    }
    // endregion


    // region Serialization
    // TODO: As part of serialization reading (deserialize), we need to somehow store the class names and remake our class / event links
    // TODO: If this is remotely possible for object instances, try that too but it might not be (in which case we might need to enforce
    // TODO: a user-defined method of resetting events after reading from persistence)
    private void writeObject(ObjectOutputStream out) throws IOException {

        out.writeObject(mStates);
        out.writeObject(mTransitions);
        out.writeObject(mCurrentState);
        out.writeObject(mTransitionHistory);
        out.writeObject(mIdentifier);

        // For now, don't write event stuff
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        mStates = (List<String>) in.readObject();
        mTransitions = (List<Transition>) in.readObject();
        mCurrentState = (String) in.readObject();
        mTransitionHistory = (Stack<Transition>) in.readObject();
        mIdentifier = (String) in.readObject();

        // For now, don't read event stuff (and also deal with the unchecked casts above)
    }
    // endregion
}