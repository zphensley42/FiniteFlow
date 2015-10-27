package com.metova.finiteflowsample;

import com.metova.finiteflow.FiniteFlow;
import com.metova.finiteflow.FlowInitializationException;
import com.metova.finiteflow.FlowInvalidException;
import com.metova.finiteflow.InvalidStateChangeException;
import com.metova.finiteflow.OnEnter;
import com.metova.finiteflow.OnExit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainFragment mMainFragment;

    @OnEnter(state = "A")
    public void onEnterA() {

        Log.i("StateEvent", "onEnterA");
        Toast.makeText(this, "onEnterA", Toast.LENGTH_SHORT).show();
        if(mMainFragment != null) {
            mMainFragment.setActiveStateButton("A");
        }
    }

    @OnExit(state = "A")
    public void onExitA() {

        Log.i("StateEvent", "onExitA");
        Toast.makeText(this, "onExitA", Toast.LENGTH_SHORT).show();
    }

    @OnEnter(state = "B")
    public void onEnterB() {

        Log.i("StateEvent", "onEnterB");
        Toast.makeText(this, "onEnterB", Toast.LENGTH_SHORT).show();
        if(mMainFragment != null) {
            mMainFragment.setActiveStateButton("B");
        }
    }

    @OnExit(state = "B")
    public void onExitB() {

        Log.i("StateEvent", "onExitB");
        Toast.makeText(this, "onExitB", Toast.LENGTH_SHORT).show();
    }

    @OnEnter(state = "C")
    public void onEnterC() {

        Log.i("StateEvent", "onEnterC");
        Toast.makeText(this, "onEnterC", Toast.LENGTH_SHORT).show();
        if(mMainFragment != null) {
            mMainFragment.setActiveStateButton("C");
        }
    }

    @OnExit(state = "C")
    public void onExitC() {

        Log.i("StateEvent", "onExitC");
        Toast.makeText(this, "onExitC", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFAB();
        setupFragment();
    }

    private void setupFAB() {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Snackbar
                        .make(view, "FAB Clicked", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();

                try {
                    FiniteFlow.getInstance(BaseApplication.TAG).moveToState("B");
                } catch (InvalidStateChangeException | FlowInitializationException e) {
                    e.printStackTrace();
                }
            }
        });

        // For now
        fab.setVisibility(View.GONE);
    }

    private void setupFragment() {

        if(mMainFragment == null) {

            mMainFragment = new MainFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment, mMainFragment, MainFragment.TAG)
                    .commit();
        }
        else {

            mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        FiniteFlow.getInstance(BaseApplication.TAG).register(this);
    }

    @Override
    protected void onStop() {

        FiniteFlow.getInstance(BaseApplication.TAG).unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        try {
            FiniteFlow.getInstance(BaseApplication.TAG).moveToPreviousState();
            if(mMainFragment != null) {
                mMainFragment.setActiveStateButton(FiniteFlow.getInstance(BaseApplication.TAG).getCurrentState());
            }
        } catch (FlowInvalidException | FlowInitializationException e) {
            e.printStackTrace();
            if(mMainFragment != null) {
                mMainFragment.setErrorMessage(e.getMessage());
            }
        }
    }
}
