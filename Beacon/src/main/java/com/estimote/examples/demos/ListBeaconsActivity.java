package com.estimote.examples.demos;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;
import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Displays list of found beacons sorted by RSSI.
 * Starts new activity with selected beacon if activity was provided.
 *
 * @author wiktorgworek@google.com (Wiktor Gworek)
 */
public class ListBeaconsActivity extends Activity  {

    private static final String TAG = ListBeaconsActivity.class.getSimpleName();

    public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
    public static final String EXTRAS_BEACON = "extrasBeacon";
    public static final String EXTRAS_IMAGE_RESOURCE = "imageResource";

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    private BeaconManager beaconManager;
    private LeDeviceListAdapter adapter;
    AssetsExtracter mTask;

    private ArrayList<HashMap<String, String>> mExponatsList;
    private JSONArray mExponats = null;
    private HashMap<String,Integer> mVersion;


    private static final String TAG_NAME = "name";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_BEACON_MAC = "beaconMac";
    private static final String TAG_TRACKING_DATA = "trackingData";
    private static final String TAG_TARGET = "target";
    private static final String TAG_TYPE = "type";
    private static final String TAG_MODEL = "model";
    SharedPreferences prefs = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

         prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);


        // extract all the assets
        mTask = new AssetsExtracter();
        mTask.execute(0);


        // Configure device list.
        adapter = new LeDeviceListAdapter(ListBeaconsActivity.this);
        ListView list = (ListView) findViewById(R.id.device_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(createOnItemClickListener());

        // Configure verbose debug logging.
        L.enableDebugLogging(true);


        // Configure BeaconManager.
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        if (beacons.size() == 1)
                            getActionBar().setSubtitle("Found " + beacons.size() + " exponat");
                        if (beacons.size() > 1)
                            getActionBar().setSubtitle("Found " + beacons.size() + " exponats");
                        if (beacons.size() == 0)
                            getActionBar().setSubtitle(R.string.scanning);
                        adapter.replaceWith(beacons);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        MenuItem refreshItem = menu.findItem(R.id.refresh);
        refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            Log.d("run","first run");


            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }
 /* @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }*/


    @Override
    protected void onDestroy() {
        beaconManager.disconnect();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }

    @Override
    protected void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        super.onStop();
    }

    //Assets Extraction
    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {

        //   @Override
        //  protected void onPreExecute()
        // {
        //Create a new progress dialog
        //      }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                // Extract all assets except Menu. Overwrite existing files for debug build only.
                AssetsManager.extractAllAssets(getApplicationContext(), "", BuildConfig.DEBUG);
            } catch (IOException e) {
                MetaioDebug.printStackTrace(Log.ERROR, e);
                return false;
            }
            try {
                JSONParserToDB();
            } catch (Exception e) {
                Log.e("Error with the JSON Parser",
                        "Error when parsing the JSON, may be it is not formatted properly.");
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
//            mProgress.setVisibility(View.GONE);

            if (!result) {
                MetaioDebug.log(Log.ERROR, "Error extracting assets, closing the application...");
                showToast("Error extracting assets, closing the application...");
                finish();
            }
        }
    }

    /**
     * Display a short toast message
     *
     * @param message Message to display
     */
    private void showToast(final String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                getActionBar().setSubtitle("Bluetooth not enabled");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        getActionBar().setTitle(R.string.welcome);
        getActionBar().setSubtitle(R.string.scanning);
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(ListBeaconsActivity.this, "Cannot start ranging, something terrible happened",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }

    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if (getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY) != null) {
                //try {
                //Class<?> clazz = Class.forName(getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));
                //Intent intent = new Intent(ListBeaconsActivity.this, clazz);
                Intent intent = new Intent(ListBeaconsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(EXTRAS_BEACON, adapter.getItem(position));
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(ListBeaconsActivity.this);
                SharedPreferences.Editor edit = sp.edit();
                Log.d("image",adapter.getImage(view));
                edit.putString("image_resource", adapter.getImage(view));
                edit.putInt("exponat_id", adapter.getExponatId(view));
                Log.d("database","exponat_id="+adapter.getExponatId(view));
              //  edit.putString("name_resource", adapter.getName(view));
                edit.commit();

                startActivity(intent);
//          } catch (ClassNotFoundException e) {
//            Log.e(TAG, "Finding class by name failed", e);
//          }
//        }
            }
        };
    }

    private void JSONParserToDB(){
        DatabaseHandler db = new DatabaseHandler(ListBeaconsActivity.this);
        // Hashmap for ListView

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ListBeaconsActivity.this);

        //       SharedPreferences.Editor edit = sp.edit();
        //     edit.putInt("dbVersion",);

       /* Log.d("Database","Dropping...");
        int oldv = sp.getInt("dbVersion",1);
        int newv = oldv+1;
        sp.edit().putInt("dbVersion",newv);

        db.onUpgrade(db.getWritableDatabase(),1,2);

        Log.d("Database","Creating new db...");*/


        mExponatsList = new ArrayList<HashMap<String, String>>();
        //   mVersion = new HashMap<String, Integer>();

        //  ArrayList<HashMap<String, String>> exponatList = new ArrayList<HashMap<String, String>>();
        String string = loadJSONFromAsset();
        try {

            JSONObject json = new JSONObject(string);

            int version = json.getInt("version");


                Log.d("Database","Dropping database...");
                db.onUpgrade(db.getWritableDatabase(),db.getDATABASE_VERSION(),version);
                Log.d("Database","Creating database...");

                mExponats = json.getJSONArray("exponats");
                for (int i = 0; i < mExponats.length(); i++) {
                JSONObject c = mExponats.getJSONObject(i);

                // gets the content of each tag
                String name = c.getString(TAG_NAME);
                String image = c.getString(TAG_IMAGE);
                String beaconMac = c.getString(TAG_BEACON_MAC);
                String trackingData = c.getString(TAG_TRACKING_DATA);
                String target = c.getString(TAG_TARGET);
                String type = c.getString(TAG_TYPE);
                String model = c.getString(TAG_MODEL);


                Log.d("Database", "Inserting...");
                db.addExponat(new Exponat(name, image, beaconMac, trackingData, target, type, model));
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // map.put(TAG_POI_ID, poi_id);
                map.put(TAG_NAME, name);
                map.put(TAG_IMAGE, image);
                map.put(TAG_BEACON_MAC, beaconMac);
                map.put(TAG_TRACKING_DATA, trackingData);
                map.put(TAG_TARGET, target);
                map.put(TAG_TYPE, type);
                map.put(TAG_MODEL, model);

                // adding HashList to ArrayList
                mExponatsList.add(map);

                // annndddd, our JSON data is up to date same with our array
                // list
                //TODO Remove
                Log.d("hashmap", "One more added");


            }

        } catch (JSONException e) {
            e.printStackTrace();

        }



        Log.d("hashmap", mExponatsList.toString());
// Reading all contacts
        Log.d("Database", "Reading all exponats..");
        List<Exponat> exponats = db.getAllExponats();
        for (Exponat ex : exponats) {
            String log = "Id: " + ex.getId() + " ,Name: " + ex.getName() + " ,MAC: " + ex.getBeaconMac();
            // Writing Exponats to log
            Log.d("Database ", log);
            db.close();
        }



// now we save the strings for the poi in the sharedresources
        // to pass it to the adapter
      /*  SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext.this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("jsonString", string);
*/

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("exponats.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");

            //TODO remove
            Log.d("Hashmap", "I read it");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }






}
