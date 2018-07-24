package com.example.anas.zagel.Adaptors;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.example.anas.zagel.Fragments.StepFragment;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

/**
 * Created by Toka on 2018-04-26.
 */
public class StepperAdaptor extends AbstractFragmentStepAdapter {

    public StepperAdaptor(FragmentManager fm, Context context) {
        super(fm, context);
    }

    @Override
    public Step createStep(int position) {
        final StepFragment step = new StepFragment();
        Bundle b = new Bundle();
        //b.putInt(CURRENT_STEP_POSITION_KEY, position);
        step.setArguments(b);
        return step;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        //Override this method to set Step title for the Tabs, not necessary for other stepper types
        return new StepViewModel.Builder(context)
                //.setTitle(R.string.tab_title) //can be a CharSequence instead
                .create();
    }
}