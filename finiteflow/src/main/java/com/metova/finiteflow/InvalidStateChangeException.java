package com.metova.finiteflow;

public class InvalidStateChangeException extends Exception {

    private String mCurrentState;
    private Transition mTransition;

    public InvalidStateChangeException(String currentState, Transition transition) {
        mCurrentState = currentState;
        mTransition = transition;
    }

    public InvalidStateChangeException(String detailMessage, String currentState, Transition transition) {
        super(detailMessage);
        mCurrentState = currentState;
        mTransition = transition;
    }

    public InvalidStateChangeException(String detailMessage, Throwable throwable, String currentState, Transition transition) {
        super(detailMessage, throwable);
        mCurrentState = currentState;
        mTransition = transition;
    }

    public InvalidStateChangeException(Throwable throwable, String currentState, Transition transition) {
        super(throwable);
        mCurrentState = currentState;
        mTransition = transition;
    }

    @Override
    public String getMessage() {

        if(mTransition == null) {

            return "Transition not found, current state: " + mCurrentState;
        }
        else {
            return "Transition not allowed: " + mTransition.toString() + " from current state: " + mCurrentState;
        }
    }
}
