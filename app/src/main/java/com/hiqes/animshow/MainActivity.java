/*
 * Copyright (C) 2014 HIQES LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiqes.animshow;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener,
                                                      AdapterView.OnItemSelectedListener,
                                                      Handler.Callback {

    private static final String         TAG = MainActivity.class.getName();
    private static final int            MODE_SHOW = 0;
    private static final int            MODE_GRAPH = 1;
    private static final int            GRAPH_STEP_TIMEOUT = 10;

    private static final int            INTERP_ACCEL_DECEL_INDEX = 0;
    private static final int            INTERP_ACCEL_INDEX = 1;
    private static final int            INTERP_ANTICIPATE_INDEX = 2;
    private static final int            INTERP_ANTICIP_OVERSHOOT_INDEX = 3;
    private static final int            INTERP_BOUNCE_INDEX = 4;
    private static final int            INTERP_CYCLE_INDEX = 5;
    private static final int            INTERP_DECEL_INDEX = 6;
    private static final int            INTERP_LINEAR_INDEX = 7;
    private static final int            INTERP_OVERSHOOT_INDEX = 8;

    private RadioGroup                  mRadio;
    private TextView                    mText;
    private Animation                   mAnimation;
    private View                        mProgress;
    private AnimationDrawable           mProgAnim;
    private GraphView                   mGraph;
    private Interpolator                mCurInterp;
    private Spinner                     mInterpSpinner;
    private ArrayAdapter<CharSequence>  mInterps;
    private int                         mCurMode = MODE_SHOW;
    private Handler                     mHandler = new Handler(this);

    private float                       mIncrement;
    private float                       mGraphStep;
    private Runnable                    mGraphRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < GRAPH_STEP_TIMEOUT; i++) {
                mGraph.addPoint(mGraphStep, mCurInterp.getInterpolation(mGraphStep));
                mGraphStep += mIncrement;
            }

            if (mGraphStep <= 1.0f) {
                mHandler.postDelayed(this, GRAPH_STEP_TIMEOUT);
            } else {
                mProgAnim.stop();
                mProgress.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRadio = (RadioGroup)findViewById(R.id.radio_interp_mode);
        RadioButton btn = (RadioButton)findViewById(R.id.btn_show_interp);
        btn.setChecked(true);
        mRadio.setOnCheckedChangeListener(this);

        mProgress = findViewById(R.id.prog);
        mProgress.setBackgroundResource(android.R.drawable.progress_indeterminate_horizontal);
        mProgAnim = (AnimationDrawable)mProgress.getBackground();

        mText = (TextView)findViewById(R.id.sample_text);
        mGraph = (GraphView)findViewById(R.id.iterp_graph);

        mInterpSpinner = (Spinner)findViewById(R.id.spn_cur_interp);
        mInterpSpinner.setOnItemSelectedListener(this);
        mInterps =
            ArrayAdapter.createFromResource(this,
                                            R.array.android_interps,
                                            android.R.layout.simple_spinner_item);
        mInterps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInterpSpinner.setAdapter(mInterps);

        mCurInterp = null;

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_small_big);
    }

    @Override
    protected void onPause() {
        stopCurrentWork();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    //  Radio callback for mode of the interpolators
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        stopCurrentWork();

        //  Switch the mode and force an update of the UI
        if (checkedId == R.id.btn_show_interp) {
            mCurMode = MODE_SHOW;
        } else {
            mCurMode = MODE_GRAPH;
        }

        updateUi();
    }

    @Override
    public boolean handleMessage(Message msg) {
        //  No messages used right now
        return false;
    }

    private void stopCurrentWork() {
        mHandler.removeCallbacks(mGraphRunnable);
        mAnimation.cancel();
    }

    private void updateUi() {
        if (mCurMode == MODE_SHOW) {
            mText.setVisibility(View.VISIBLE);
            mGraph.setVisibility(View.INVISIBLE);
            if (mProgAnim.isRunning()) {
                mProgAnim.stop();
            }

            mProgress.setVisibility(View.INVISIBLE);
            mAnimation.setInterpolator(mCurInterp);
            mText.startAnimation(mAnimation);
        } else {
            mText.setVisibility(View.INVISIBLE);
            mGraph.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            mProgAnim.start();
            mGraph.clear();
            mGraphStep = 0.0f;
            mIncrement = 1.0f / (float)mGraph.getWidth();
            mHandler.postDelayed(mGraphRunnable, GRAPH_STEP_TIMEOUT);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //  Create a new interpolator based on the selection then
        //  trigger the UI to update because of the new selection.
        Interpolator interp = null;

        switch (position) {
            case INTERP_ACCEL_DECEL_INDEX:
                interp = new AccelerateDecelerateInterpolator();
                break;

            case INTERP_ACCEL_INDEX:
                interp = new AccelerateInterpolator();
                break;

            case INTERP_ANTICIP_OVERSHOOT_INDEX:
                interp = new AnticipateOvershootInterpolator();
                break;

            case INTERP_ANTICIPATE_INDEX:
                interp = new AnticipateInterpolator();
                break;

            case INTERP_BOUNCE_INDEX:
                interp = new BounceInterpolator();
                break;

            case INTERP_CYCLE_INDEX:
                interp = new CycleInterpolator(1.0f);
                break;

            case INTERP_DECEL_INDEX:
                interp = new DecelerateInterpolator();
                break;

            case INTERP_LINEAR_INDEX:
                interp = new LinearInterpolator();
                break;

            case INTERP_OVERSHOOT_INDEX:
                interp = new OvershootInterpolator();
                break;

            default:
                Log.w(TAG, "Unknown interpolator provide: " + position);
                break;
        }

        if (interp != null) {
            stopCurrentWork();
            mCurInterp = interp;
            updateUi();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //  Force our selection to be the first so our callback
        //  gets things rolling
        mInterpSpinner.setSelection(0);
    }
}
