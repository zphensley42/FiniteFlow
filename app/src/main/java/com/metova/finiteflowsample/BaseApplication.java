package com.metova.finiteflowsample;

import com.metova.finiteflow.FiniteFlow;
import com.metova.finiteflow.FlowInitializationException;
import com.metova.finiteflow.FlowInvalidException;

import android.app.Application;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        try {

            FiniteFlow
                    .getInstance(getApplicationContext())   // Application-level FSM
                    .addState("A")
                    .addState("B")
                    .addState("C")
                    .addState("D")
                    .addTransition("A", "B")
                    .addTransition("B", "C")
                    .addTransition("B", "D")
                    .addTransition("C", "D")
                    .addTransition("D", "D")
                    .addTransition("D", "C")
                    .setInitialState("A")
                    .setEventClasses(MainActivity.class, MainFragment.class);
        }
        catch (FlowInvalidException | FlowInitializationException e) {

            e.printStackTrace();
        }
    }
}
