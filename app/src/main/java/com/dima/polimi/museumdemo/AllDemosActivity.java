package com.dima.polimi.museumdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.gc.materialdesign.views.ButtonRectangle;

/**
 * Shows all available demos.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class AllDemosActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.all_demos);
      final ButtonRectangle button = (ButtonRectangle) findViewById(R.id.button_demos);
      button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
//        Intent intent = new Intent(AllDemosActivity.this, ListBeaconsActivity.class);
    //    intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, DistanceBeaconActivity.class.getName());
//        startActivity(intent);
      }
    });
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
}