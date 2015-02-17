package com.estimote.examples.demos;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.List;

/**
 * Shows all available demos.
 *
 */
public class MetaioActivity extends ARViewActivity {

    //Geometries for the models
    private IGeometry mObject;
    private IGeometry mMoviePlane;

    //CallbackHandler
    private MetaioSDKCallbackHandler mCallbackHandler;
    //Chooses the model
    private int mSelectedModel;
    //Handles gestures
    private int mGestureMask;
    private GestureHandlerAndroid mGestureHandler;
    private String name_resource;
    private String trackingData;
    //Possible values for type are "3D" or "Video"
    private String type;
    private String target;
    private String model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // extract all the assets
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(MetaioActivity.this);
        int exponat_id = sp.getInt("exponat_id",0);
        DatabaseHandler db = new DatabaseHandler(MetaioActivity.this);
        List<Exponat> exponats = db.getAllExponats();
        try {
            for (Exponat ex : exponats){
                if (exponat_id==ex.getId()){
                    trackingData = ex.getTrackingData();
                    target = ex.getTarget();
                    type = ex.getType();
                    model = ex.getModel();
                    break;
                }
            }
        }
        catch (Exception e){
            Log.e("Error","Error when extracting from db the assets");
        }
        Log.d("model","TrackingData="+trackingData + " Target=" + target + " Type=" + model + " Model");
        mMoviePlane = null;
        mObject = null;
        mCallbackHandler = new MetaioSDKCallbackHandler();
        //Approve all types of gestures for the models
        mGestureMask = GestureHandler.GESTURE_ALL;
        mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
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


        String trackingConfigFile = AssetsManager.getAssetPath(getApplicationContext(), trackingData);

        Log.v("Assets", "Load " + trackingConfigFile);
        // Assigning tracking configuration
        boolean result1 = metaioSDK.setTrackingConfiguration(trackingConfigFile);
        MetaioDebug.log("Tracking data loaded: " + result1);



        // Load all the geometries
            // Load the Chair (Stuhl)
        if(type.equals("3D")) {

        File filepath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            model);

            if (filepath != null) {
                mObject = metaioSDK.createGeometry(filepath);

                if (mObject != null) {
                    //Sets the scale of the chair and its geometries location and rotation
                    mObject.setScale(50f);
                    mObject.setTranslation(new Vector3d(0f, 0f, 0f));
                    mObject.setRotation(new Rotation((float) Math.PI / 2f, 0.5f, 0f));

                    mGestureHandler.addObject(mObject, 2);
                } else {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
                }
            }
        }
if(type.equals("Video")) {
    // Loading movie
    final File moviePath =
            AssetsManager.getAssetPathAsFile(getApplicationContext(), model);
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
}
        //TODO assign this to the Beacon
        if(type.equals("3D")) {
                setActiveModel(0);
        }
        if(type.equals("Video")){
            setActiveModel(1);
        }
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
        if(type.equals("3D")) {

            mGestureHandler.onTouch(v, event);
        }
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

        if(type.equals("3D")) {
            mObject.setVisible(modelIndex == 0);
        }
        if(type.equals("Video")) {

            mMoviePlane.setVisible(modelIndex == 1);
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
            if(type.equals("Video")) {

            // Play movie if the movie button was selected and we're currently tracking.
            if (trackingValues.isEmpty() || !trackingValues.get(0).isTrackingState()) {
                if (mMoviePlane != null) {
                    mMoviePlane.pauseMovieTexture();
                }
            } else {
                if (mMoviePlane != null && mSelectedModel == 1) {
                    mMoviePlane.startMovieTexture(true);

                }
            }
            }
        }
    }
}