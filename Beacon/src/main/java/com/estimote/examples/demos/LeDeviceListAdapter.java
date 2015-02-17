package com.estimote.examples.demos;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.metaio.tools.io.AssetsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.provider.Settings.Global.getString;


/**
 * Displays basic information about beacon.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<Beacon> beacons;
    private LayoutInflater inflater;
    Context mContext;

    private ArrayList<HashMap<String, String>> mExponatsList;
    private JSONArray mExponats = null;

    private static final String TAG_NAME = "name";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_BEACON_MAC = "beaconMac";
    public String image_value;

    public LeDeviceListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.beacons = new ArrayList<Beacon>();
        mContext = this.inflater.getContext();

    Parser();
    }

    public void replaceWith(Collection<Beacon> newBeacons) {
        this.beacons.clear();
        this.beacons.addAll(newBeacons);
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        DatabaseHandler db = new DatabaseHandler(mContext);

        bind(getItem(position), view);

        return view;
    }

    private void bind(Beacon beacon, View view) {

        ViewHolder holder = (ViewHolder) view.getTag();
        DatabaseHandler db = new DatabaseHandler(mContext);
        List<Exponat> exponats = db.getAllExponats();

//    mbeaconMac = "";
        //mContext.getString(R.string.exponat_1_name);
        Log.d("Beacon Mac", beacon.getMacAddress());
/*
    while(beacon.getMacAddress().equals(mbeaconMac))
*/


        for (Exponat ex : exponats) {
           // String log = "Id: " + ex.getId() + " ,Name: " + ex.getName() + " ,MAC: " + ex.getBeaconMac();
            // Writing Exponats to log
            //Log.d("Database ", log);
            String b_mac_db = ex.getBeaconMac();


            if(beacon.getMacAddress().equals(b_mac_db)){
                String name_value = ex.getName();
                holder.macTextView.setText(String.format("%s (%.2fm)", name_value, Utils.computeAccuracy(beacon)));
                int id = ex.getId();
                holder.macId = id;
                image_value = ex.getImage();
                holder.macImageResource = image_value;
                int image_source = mContext.getResources().getIdentifier("com.estimote.examples.demos:drawable/" + image_value, null, null);
                holder.macImageView.setImageResource(image_source);
                break;
            }
            //if(!beacon.getMacAddress().equals(b_mac_db))
            else{
                holder.macTextView.setText(String.format("%s (%.2fm)",
                        "MAC:" + beacon.getMacAddress(),
                        Utils.computeAccuracy(beacon)));
                int image_source = mContext.getResources().getIdentifier("com.estimote.examples.demos:drawable/beacon_gray", null, null);
                holder.macImageView.setImageResource(image_source);
            }


            }
db.close();

/*


            for (HashMap<String, String> hashMap : mExponatsList) {
                Log.d("hashche", " KeySet(): " + hashMap.keySet().toString());


                    String beacon_value = hashMap.get("beaconMac");
                if (beacon_value.equals(beacon.getMacAddress()) && holder.macTextResource.equals("Unknown")) {
                    String name_value = hashMap.get("name");
                    holder.macTextView.setText(String.format("%s (%.2fm)", name_value, Utils.computeAccuracy(beacon)));
                    holder.macTextResource = name_value;
                    image_value = hashMap.get("image");
                    holder.macImageResource = image_value;
                    int id = mContext.getResources().getIdentifier("com.estimote.examples.demos:drawable/" + image_value, null, null);
                    holder.macImageView.setImageResource(id);
                } else {
                    if(beacon_value.equals(beacon.getMacAddress()) && !holder.macTextResource.equals("Unknown"))
                    {
                        String name_value = hashMap.get("name");
                        holder.macTextView.setText(String.format("%s (%.2fm)", name_value, Utils.computeAccuracy(beacon)));

                    }
                }
*/




    }
    public String getImage(View view){
        ViewHolder holder = (ViewHolder) view.getTag();
       // String text = holder.macTextView.getText().toString();
        String image_resource = holder.macImageResource;
        return image_resource;
    }

    public Integer getExponatId(View view){
        ViewHolder holder = (ViewHolder) view.getTag();
        Integer exponat_id = holder.macId;
        return exponat_id;
    }



    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.device_item, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    public class ViewHolder {
        final TextView macTextView;
        final ImageView macImageView;
        public String macImageResource;
        public int macId;

        ViewHolder(View view) {
            macTextView = (TextView) view.findViewWithTag("mac");
            macImageView = (ImageView) view.findViewWithTag("image");
            macId = 0;
        }
    }

    void Parser() {

        mExponatsList = new ArrayList<HashMap<String, String>>();

        String string = loadJSONFromAsset();
        try {
            JSONObject json = new JSONObject(string);
            mExponats = json.getJSONArray("exponats");

            for (int i = 0; i < mExponats.length(); i++) {
                JSONObject c = mExponats.getJSONObject(i);

                // gets the content of each tag
                String name = c.getString(TAG_NAME);
                String image = c.getString(TAG_IMAGE);
                String beaconMac = c.getString(TAG_BEACON_MAC);


                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // map.put(TAG_POI_ID, poi_id);
                map.put(TAG_NAME, name);
                map.put(TAG_IMAGE, image);
                map.put(TAG_BEACON_MAC, beaconMac);

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

            InputStream is = mContext.getAssets().open("exponats.json");

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
