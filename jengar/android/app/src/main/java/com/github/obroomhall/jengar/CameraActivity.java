package com.github.obroomhall.jengar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CameraActivity extends Activity implements CvCameraViewListener2 {

    static {
        System.loadLibrary("native-lib");
    }
    private static final String    TAG = "CameraActivity";

    private Mat                    mRgba;
    private Mat                    mRgb;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        final Button button = findViewById(R.id.settings_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveTaskToBack(true);
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String address = sharedPreferences.getString("ip_address", "");
        String portString = sharedPreferences.getString("port", "");
        int port = Integer.parseInt(portString);

        if (!address.isEmpty() && port > 0) {
            Log.d(TAG, String.format("onCreate: Try to connect on address: '%s', port: '%d'", address, port));

            try {
                connectToServer(address, port);
            }
            catch (Exception e) {
                Log.d(TAG, "onCreate: " + e.getMessage());

                new AlertDialog.Builder(CameraActivity.this)
                        .setTitle("Could not connect to server")
                        .setMessage("Are you sure the IP address and port are set correctly?")
                        .setNeutralButton(android.R.string.yes, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                button.performClick();
                return;
            }

            Set<String> dictNames = sharedPreferences.getStringSet("dictionary_values", null);

            Log.d(TAG, "onCreate: Try to get dictnames");
            String[] newNames = getDictionaryNames();
            for (int i = 0; i < newNames.length; i++){
                Log.d(TAG, String.format("onCreate: Dict: '%s'", newNames[i]));
            }

            Set<String> newNamesSet = new HashSet(Arrays.asList(newNames));
            dictNames.addAll(newNamesSet);

            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putStringSet("dictionary_values", dictNames);
            ed.commit();

            setDictionary("ARUCO_MIP_36h12");
        }
        else {
            button.performClick();
        }

        Log.d(TAG, "onCreate: Done some stuff");
        init();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onDestroy");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        //Log.d(TAG, "Returning to server settings");
        //serverReturnToSettings();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onResume");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        //Log.d(TAG, "Ending server connection");
        //endConnectionToServer();
    }

    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgb = new Mat(height, width, CvType.CV_8UC3);
    }

    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");
        mRgba.release();
        mRgb.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, mRgb, Imgproc.COLOR_RGBA2RGB);
        detectMarkers(mRgb.getNativeObjAddr());
        return mRgb;
    }

    public native void init();
    public native long detectMarkers(long frameAddress);
    public native void connectToServer(String address, int port);
    public native void setDictionary(String jDictName);
    public native String[] getDictionaryNames();
}