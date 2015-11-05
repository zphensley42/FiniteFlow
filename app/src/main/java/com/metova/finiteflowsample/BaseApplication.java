package com.metova.finiteflowsample;

import com.metova.finiteflow.FiniteFlow;
import com.metova.finiteflow.Flow;
import com.metova.finiteflow.FlowInitializationException;
import com.metova.finiteflow.FlowInvalidException;
import com.metova.finiteflow.FlowTransition;

import android.app.Application;

@Flow(  states = {"A", "B", "C", "D"},
        initialState = "A",
        transitions = {
                @FlowTransition(from = "A", to = "B"),
                @FlowTransition(from = "B", to = "C"),
                @FlowTransition(from = "B", to = "D"),
                @FlowTransition(from = "C", to = "D"),
                @FlowTransition(from = "D", to = "D"),
                @FlowTransition(from = "D", to = "C")
        })
public class BaseApplication extends Application {

    public static final String TAG = BaseApplication.class.getSimpleName();

    @Override
    public void onCreate() {

        super.onCreate();

        try {

            FiniteFlow
                    .getInstance(TAG)   // Application-level FSM
                    .flowFor(this)
                    .setEventClasses(MainActivity.class, MainFragment.class);
        } catch (FlowInvalidException | FlowInitializationException e) {

            e.printStackTrace();
        }
    }
}
