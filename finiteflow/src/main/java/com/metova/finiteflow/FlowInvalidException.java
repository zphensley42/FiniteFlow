package com.metova.finiteflow;

public class FlowInvalidException extends Exception {

    public FlowInvalidException() {
    }

    public FlowInvalidException(String detailMessage) {
        super(detailMessage);
    }

    public FlowInvalidException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FlowInvalidException(Throwable throwable) {
        super(throwable);
    }
}
