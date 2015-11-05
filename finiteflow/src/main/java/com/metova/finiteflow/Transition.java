package com.metova.finiteflow;

import java.io.Serializable;

/**
 * Transitions have an implied name base on the fromState / toState
 */
public class Transition implements Serializable {

    private String fromState;
    private String toState;

    public Transition(String fromState, String toState) {
        this.fromState = fromState;
        this.toState = toState;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }

    public String getTransitionName() {

        return fromState + "_" + toState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Transition that = (Transition) o;

        if (fromState != null ? !fromState.equals(that.fromState) : that.fromState != null) {
            return false;
        }
        return !(toState != null ? !toState.equals(that.toState) : that.toState != null);

    }

    @Override
    public int hashCode() {
        int result = fromState != null ? fromState.hashCode() : 0;
        result = 31 * result + (toState != null ? toState.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "fromState='" + fromState + '\'' +
                ", toState='" + toState + '\'' +
                '}';
    }
}
