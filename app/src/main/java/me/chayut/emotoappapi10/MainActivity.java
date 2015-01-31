package me.chayut.emotoappapi10;

import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.logging.Handler;


public class MainActivity extends ActionBarActivity {

    eMotoBTService meMotoBTService;
    private static Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void btnClicked(View v)
    {
        if(meMotoBTService.initiateBT()) {
            meMotoBTService = new eMotoBTService(this);

            Log.d("Application", "BtnClicked");

        }


    }

    public void sendClicked(View v)
    {
        Log.d("Application","SendClicked");
        byte[] mBytes = {(byte) 0x89, (byte) 0xfe};
        meMotoBTService.sendBytes(mBytes);
    }

    public void sendClicked2(View v)
    {
        Log.d("Application","SendClicked2");
    }


}
