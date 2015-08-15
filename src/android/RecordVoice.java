package com.androidexample.noisealert;

 
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import com.androidexample.noisealert.R;
import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class NoiseAlert extends CordovaPlugin  {
public static final String ACTION_ADD_RECORD_ENTRY = "startrecording";
    /* constants */
        private static final int POLL_INTERVAL = 300;
       
        /** running state **/
        private boolean mRunning = false;
        
        /** config state **/
        private int mThreshold;
        
        private PowerManager.WakeLock mWakeLock;

        private Handler mHandler = new Handler();

        /* References to view elements */
        private TextView mStatusView;
        private SoundLevelView mDisplay;

        /* sound data source */

    private  boolean thesholdReached =false;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (ACTION_ADD_RECORD_ENTRY.equals(action)) { 
                JSONObject arg_object = args.getJSONObject(0);
                
             cordova.getActivity().runOnUiThread(new Runnable() {
                 public void run() {
                      double amp = getAmplitude();

                        //Log.i("Noise", "runnable mPollTask");
                        updateDisplay(amp, amp);

                        if ((amp > mThreshold)) {
                              //start();

                            if(thesholdReached==false) {
                                stop();
                                startrecordingvoice();
                                // Show alert when noise thersold crossed
                                Toast.makeText(getApplicationContext(), "Noise Thersold Crossed, do here your stuff.",
                                        Toast.LENGTH_LONG).show();
                                //callForHelp();
                                //Log.i("Noise", "==== onCreate ===");
                                thesholdReached=true;
                            }
                        }
                        
                        // Runnable(mPollTask) will again execute after POLL_INTERVAL
                        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
                     
                     callbackContext.success(); 
                }
            }
               
               return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        } 
    }
    
        
       /****************** Define runnable thread again and again detect noise *********/     
        
        
        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                
                // Defined SoundLevelView in main.xml file
                setContentView(R.layout.main);
                mStatusView = (TextView) findViewById(R.id.status);
               
                // Used to record voice
               mDisplay = (SoundLevelView) findViewById(R.id.volume);
                
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
        }

        
        @Override
        public void onResume() {
                super.onResume();
                //Log.i("Noise", "==== onResume ===");
                
                initializeApplicationConstants();
                mDisplay.setLevel(0, mThreshold);
                
                if (!mRunning) {
                    mRunning = true;
                    start();
                }
        }

        @Override
        public void onStop() {
                super.onStop();
               // Log.i("Noise", "==== onStop ===");
               
                //Stop noise monitoring
                stop();
               
        }

        private void start() {
                //Log.i("Noise", "==== start ===");
            
                startrecordingnoise();
                if (!mWakeLock.isHeld()) {
                        mWakeLock.acquire();
                }

                //Noise monitoring start
                // Runnable(mPollTask) will execute after POLL_INTERVAL
                mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }

        private void stop() {
            Log.i("Noise", "==== Stop Noise Monitoring===");
                if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                }
                mHandler.removeCallbacks(mSleepTask);
                mHandler.removeCallbacks(mPollTask);

            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
                mDisplay.setLevel(0,0);
                
                mRunning = false;
               
        }

       
        private void initializeApplicationConstants() {
                // Set Noise Threshold
                mThreshold = 8;
                
        }

        private void updateDisplay(double ampl, double signalEMA) {
                mStatusView.setText(Double.toString(ampl));
                // 
                mDisplay.setLevel((int)signalEMA, mThreshold);
        }
        
        
        private void callForHelp() {
              
            stop();
            startrecordingvoice();
             // Show alert when noise thersold crossed
              Toast.makeText(getApplicationContext(), "Noise Thersold Crossed, do here your stuff.",
                      Toast.LENGTH_LONG).show();

        }
    // This file is used to record voice
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    public void startrecordingnoise() {

        if (mRecorder == null) {
            String outputFile = null;
            outputFile = Environment.getExternalStorageDirectory() + "/NoiseRecording.amr";

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //mRecorder.setOutputFile("/dev/null");
            mRecorder.setOutputFile(outputFile);

            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mRecorder.start();
            mEMA = 0.0;
        }
    }
    public void startrecordingvoice() {

        if (mRecorder == null) {
            String outputFile = null;
            outputFile = Environment.getExternalStorageDirectory() + "/VoiceRecording.amr";

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //mRecorder.setOutputFile("/dev/null");
            mRecorder.setOutputFile(outputFile);

            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mRecorder.start();
            mEMA = 0.0;
        }
    }


    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude()/2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

};
