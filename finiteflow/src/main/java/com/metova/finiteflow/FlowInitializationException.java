package com.metova.finiteflow;

public class FlowInitializationException extends Exception {

    // TODO: Make this message more descriptive
    @Override
    public String getMessage() {
        return "The FiniteFlow instance was not initialized properly!";
    }
}
