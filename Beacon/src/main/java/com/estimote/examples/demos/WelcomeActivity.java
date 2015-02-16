package com.estimote.examples.demos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

import java.io.IOException;
import java.util.Timer;

/**
 * Shows all available demos.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
@SuppressLint("SetJavaScriptEnabled")
public class WelcomeActivity extends Activity {
    /**
     * Task that will extract all the assets
     */
    AssetsExtracter mTask;
    Handler myhandler;
    private ProgressDialog progressDialog;

    @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.welcome_activity);
// run a thread to start the home screen



      myhandler = new Handler();
        progressDialog = ProgressDialog.show(WelcomeActivity.this,"Loading...",
                "Loading application View, please wait...", false, false);
      // run a thread to start the home screen
      myhandler.postDelayed(new Runnable()
      {
          @Override
          public void run()
          {

              // extract all the assets
              mTask = new AssetsExtracter();
              mTask.execute(0);

              Intent intent = new Intent(WelcomeActivity.this, ListBeaconsActivity.class);
//        intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, NotifyDemoActivity.class.getName());
              startActivity(intent);
          }

      }, 3000);

//    findViewById(R.id.notify_demo_button).setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        Intent intent = new Intent(AllDemosActivity.this, ListBeaconsActivity.class);
//        intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, NotifyDemoActivity.class.getName());
//        startActivity(intent);
//      }
//    });
//    findViewById(R.id.characteristics_demo_button).setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        Intent intent = new Intent(AllDemosActivity.this, ListBeaconsActivity.class);
//        intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, CharacteristicsDemoActivity.class.getName());
//        startActivity(intent);
//      }
//    });



  }

    @Override
    protected void onStop() {

        super.onStop();
    }
    /**
     * This task extracts all the assets to an external or internal location
     * to make them accessible to Metaio SDK
     */
    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
    {

     //   @Override
      //  protected void onPreExecute()
       // {
            //Create a new progress dialog
         //      }

        @Override
        protected Boolean doInBackground(Integer... params)
        {
            try
            {
                // Extract all assets except Menu. Overwrite existing files for debug build only.
                AssetsManager.extractAllAssets(getApplicationContext(), "",  BuildConfig.DEBUG);
            }
            catch (IOException e)
            {
                MetaioDebug.printStackTrace(Log.ERROR, e);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
//            mProgress.setVisibility(View.GONE);

            if (result)
            {
                showToast("Assets are extracted successfully :"+ result);

            }
            else
            {
                MetaioDebug.log(Log.ERROR, "Error extracting assets, closing the application...");
               showToast("Error extracting assets, closing the application...");
                finish();
            }
        }
    }

    /**
     * Display a short toast message
     * @param message Message to display
     */
    private void showToast(final String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
