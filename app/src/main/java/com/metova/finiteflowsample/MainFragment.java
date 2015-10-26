package com.metova.finiteflowsample;

import com.metova.finiteflow.FiniteFlow;
import com.metova.finiteflow.FlowInitializationException;
import com.metova.finiteflow.InvalidStateChangeException;
import com.metova.finiteflow.OnEnter;
import com.metova.finiteflow.OnExit;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();
    private FiniteFlow mFiniteFlow;

    @OnEnter(state = "D")
    public void onEnterD() {

        Log.i("StateEvent", "onEnterD");
        Toast.makeText(getActivity(), "onEnterD", Toast.LENGTH_SHORT).show();
        setActiveStateButton("D");
    }

    @OnExit(state = "D")
    public void onExitD() {

        Log.i("StateEvent", "onExitD");
        Toast.makeText(getActivity(), "onExitD", Toast.LENGTH_SHORT).show();
    }

    @OnEnter(state = "E")
    public void onEnterE() {

        Log.i("StateEvent", "onEnterE");
        Toast.makeText(getActivity(), "onEnterE", Toast.LENGTH_SHORT).show();
        setActiveStateButton("E");
    }

    @OnExit(state = "E")
    public void onExitE() {

        Log.i("StateEvent", "onExitE");
        Toast.makeText(getActivity(), "onExitE", Toast.LENGTH_SHORT).show();
    }

    @OnEnter(state = "F")
    public void onEnterF() {

        Log.i("StateEvent", "onEnterF");
        Toast.makeText(getActivity(), "onEnterF", Toast.LENGTH_SHORT).show();
        setActiveStateButton("F");
    }

    @OnExit(state = "F")
    public void onExitF() {

        Log.i("StateEvent", "onExitF");
        Toast.makeText(getActivity(), "onExitF", Toast.LENGTH_SHORT).show();
    }


    private TextView currentState, lastError;
    private Button stateButtonA;
    private Button stateButtonB;
    private Button stateButtonC;
    private Button stateButtonD;
    private Button stateButtonE;
    private Button stateButtonF;
    public void setActiveStateButton(@NonNull String state) {

        List<Button> buttons = Arrays.asList(stateButtonA, stateButtonB, stateButtonC, stateButtonD, stateButtonE, stateButtonF);
        for(Button button : buttons) {

            button.setBackgroundColor(getResources().getColor(android.R.color.black));
            button.setTextColor(getResources().getColor(android.R.color.white));
        }

        if(state.equals("A")) {

            stateButtonA.setBackgroundColor(getResources().getColor(android.R.color.white));
            stateButtonA.setTextColor(getResources().getColor(android.R.color.black));
        }
        else if(state.equals("B")) {

            stateButtonB.setBackgroundColor(getResources().getColor(android.R.color.white));
            stateButtonB.setTextColor(getResources().getColor(android.R.color.black));
        }
        else if(state.equals("C")) {

            stateButtonC.setBackgroundColor(getResources().getColor(android.R.color.white));
            stateButtonC.setTextColor(getResources().getColor(android.R.color.black));
        }
        else if(state.equals("D")) {

            stateButtonD.setBackgroundColor(getResources().getColor(android.R.color.white));
            stateButtonD.setTextColor(getResources().getColor(android.R.color.black));
        }
        else if(state.equals("E")) {

            stateButtonE.setBackgroundColor(getResources().getColor(android.R.color.white));
            stateButtonE.setTextColor(getResources().getColor(android.R.color.black));
        }
        else if(state.equals("F")) {

            stateButtonF.setBackgroundColor(getResources().getColor(android.R.color.white));
            stateButtonF.setTextColor(getResources().getColor(android.R.color.black));
        }

        currentState.setText("State: " + mFiniteFlow.getCurrentState());
    }

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFiniteFlow = FiniteFlow.getInstance(getActivity().getApplicationContext());
        setActiveStateButton(mFiniteFlow.getCurrentState());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_main, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View root) {

        currentState = (TextView) root.findViewById(R.id.current_state);
        lastError = (TextView) root.findViewById(R.id.last_error);

        stateButtonA = (Button) root.findViewById(R.id.state_a_button);
        stateButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mFiniteFlow.moveToState("A");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                    lastError.setText(e.getMessage());
                }
            }
        });

        stateButtonB = (Button) root.findViewById(R.id.state_b_button);
        stateButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mFiniteFlow.moveToState("B");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                    lastError.setText(e.getMessage());
                }
            }
        });

        stateButtonC = (Button) root.findViewById(R.id.state_c_button);
        stateButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mFiniteFlow.moveToState("C");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                    lastError.setText(e.getMessage());
                }
            }
        });

        stateButtonD = (Button) root.findViewById(R.id.state_d_button);
        stateButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mFiniteFlow.moveToState("D");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                    lastError.setText(e.getMessage());
                }
            }
        });

        stateButtonE = (Button) root.findViewById(R.id.state_e_button);
        stateButtonE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mFiniteFlow.moveToState("E");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                    lastError.setText(e.getMessage());
                }
            }
        });

        stateButtonF = (Button) root.findViewById(R.id.state_f_button);
        stateButtonF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mFiniteFlow.moveToState("F");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                    lastError.setText(e.getMessage());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FiniteFlow.getInstance(getActivity().getApplicationContext()).register(this);
    }

    @Override
    public void onStop() {
        FiniteFlow.getInstance(getActivity().getApplicationContext()).unregister(this);
        super.onStop();
    }

    public void setErrorMessage(String msg) {

        lastError.setText(msg);
    }
}
