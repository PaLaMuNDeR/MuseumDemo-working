package com.estimote.examples.demos;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.EPLAYBACK_STATUS;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.MovieTextureStatus;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;


import java.io.File;

/**
 * Shows all available demos.
 *
 */
public class MetaioActivity extends ARViewActivity {

    //Geometries for the models
    private IGeometry mChair;
    private IGeometry mMoviePlane;

    //CallbackHandler
    private MetaioSDKCallbackHandler mCallbackHandler;
    //Chooses the model
    private int mSelectedModel;
    //Handles gestures
    private int mGestureMask;
    private GestureHandlerAndroid mGestureHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMoviePlane = null;
        mCallbackHandler = new MetaioSDKCallbackHandler();
        //Approve all types of gestures for the models
        mGestureMask = GestureHandler.GESTURE_ALL;
        mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
        mChair = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallbackHandler.delete();
        mCallbackHandler = null;
    }

    @Override
    protected int getGUILayout() {
        // Attaching layout to the activity
        return R.layout.tutorial_hello_world;
    }


    public void onButtonClick(View v) {
        finish();
    }


    @Override
    protected void loadContents() {
        // extract all the assets

        String trackingConfigFile = AssetsManager.getAssetPath(getApplicationContext(), "TrackingData_MarkerlessFast.xml");

        Log.v("Assets", "Load " + trackingConfigFile);
        // Assigning tracking configuration
        boolean result1 = metaioSDK.setTrackingConfiguration(trackingConfigFile);
        MetaioDebug.log("Tracking data loaded: " + result1);

        // Load all the geometries. First - Model
        // Load the Chair (Stuhl)
        File filepath =
                AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "stuhl.obj");
        if (filepath != null) {
            mChair = metaioSDK.createGeometry(filepath);

            if (mChair != null) {
                //Sets the scale of the chair and its geometries location and rotation
                mChair.setScale(50f);
                mChair.setTranslation(new Vector3d(0f, 0f, 0f));
                mChair.setRotation(new Rotation((float) Math.PI / 2f, 0.5f, 0f));

                mGestureHandler.addObject(mChair, 2);
            } else {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
            }
        }

        // Loading movie
        final File moviePath =
                AssetsManager.getAssetPathAsFile(getApplicationContext(), "mona_rotated.3gp");
        if (moviePath != null) {
            mMoviePlane = metaioSDK.createGeometryFromMovie(moviePath, false);
            if (mMoviePlane != null) {
                mMoviePlane.setScale(2.0f);
                mMoviePlane.setRotation(new Rotation(0f, 0f, (float) -Math.PI / 2));
                MetaioDebug.log("Loaded geometry " + moviePath);
            } else {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + moviePath);
            }
        }

        //TODO assign this to the Beacon
        setActiveModel(3);
    }

    //Handles what happens when a model is touched
    @Override
    protected void onGeometryTouched(IGeometry geometry) {
        if (geometry.equals(mMoviePlane)) {
            final MovieTextureStatus status = mMoviePlane.getMovieTextureStatus();
            if (status.getPlaybackStatus() == EPLAYBACK_STATUS.EPLAYBACK_STATUS_PLAYING) {
                mMoviePlane.pauseMovieTexture();
            } else {
                mMoviePlane.startMovieTexture(true);
            }
        }

    }

    //Sem Tink
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouch(v, event);

        mGestureHandler.onTouch(v, event);

        return true;
    }

    //Handles the Handler
    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return mCallbackHandler;
    }

    //Chooses the which model to show
    private void setActiveModel(int modelIndex) {
        mSelectedModel = modelIndex;

        mChair.setVisible(modelIndex == 0);
        mMoviePlane.setVisible(modelIndex == 3);

        if (modelIndex != 3) {
            mMoviePlane.stopMovieTexture();
        }

        // Start or pause movie according to tracking state
        mCallbackHandler.onTrackingEvent(metaioSDK.getTrackingValues());
    }

    final private class MetaioSDKCallbackHandler extends IMetaioSDKCallback {
        @Override
        public void onSDKReady() {
            // show GUI after SDK is ready
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGUIView.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onTrackingEvent(TrackingValuesVector trackingValues) {
            super.onTrackingEvent(trackingValues);

            // Play movie if the movie button was selected and we're currently tracking.
            if (trackingValues.isEmpty() || !trackingValues.get(0).isTrackingState()) {
                if (mMoviePlane != null) {
                    mMoviePlane.pauseMovieTexture();
                }
            } else {
                if (mMoviePlane != null && mSelectedModel == 3) {
                    mMoviePlane.startMovieTexture(true);

                }
            }
        }
    }
}