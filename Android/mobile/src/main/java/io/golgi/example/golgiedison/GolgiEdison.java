package io.golgi.example.golgiedison;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;


public class GolgiEdison extends Activity {

    private Switch ledSwitch;
    private Switch PIRSwitch;
    private Switch TouchSwitch;
    private ProgressBar TempProgressBar;
    private ProgressBar PotProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_golgi_edison);
        Intent serviceIntent = new Intent();
        serviceIntent.setClassName("io.golgi.example.golgiedison",
                "io.golgi.example.golgiedison.GEService");
        this.startService(serviceIntent);

        ledSwitch = (Switch)findViewById(R.id.switchLED);
        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    Log.i("DEBUG","I am checked");
                    if(GEService.getInstance() != null){
                        GEService.getInstance().signalLED(1);
                    }
                }
                else{
                    Log.i("DEBUG","I am not checked");
                    GEService.getInstance().signalLED(0);
                }
            }
        });

        PIRSwitch = (Switch)findViewById(R.id.switchPIR);
        PIRSwitch.setClickable(false);

        TouchSwitch = (Switch)findViewById(R.id.switchTouch);
        TouchSwitch.setClickable(false);

        TempProgressBar = (ProgressBar)findViewById(R.id.progressBarTemp);
        TempProgressBar.setMax(1650);
        PotProgressBar = (ProgressBar)findViewById(R.id.progressBarPot);
        PotProgressBar.setMax(1024);
    }

    BroadcastReceiver DigitalReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Switch localSwitch;

//            String sensor = intent.getStringExtra("SENSOR");
//
//            if(sensor.equals("PIR")){
//                localSwitch = PIRSwitch;
//            }
//            else if(sensor.equals("TOUCH")){
//                localSwitch = TouchSwitch;
//            }
            int sensor = intent.getIntExtra("PIN",-1);
            if(sensor == 12){
                localSwitch = PIRSwitch;
            }
            else if(sensor == 2){
                localSwitch = TouchSwitch;
            }
            else{
                return;
            }

            int val = intent.getIntExtra("VALUE",0);

            if(val == 1){
                localSwitch.setChecked(true);
            }
            else{
                localSwitch.setChecked(false);
            }
        }
    };

    BroadcastReceiver AnalogReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ProgressBar localProgressBar;
            int value;
            double resistance;
            double B = 3975.0;
            double temp;

            value = intent.getIntExtra("VALUE",0);

            int sensor = intent.getIntExtra("PIN",-1);
            if(sensor == 0){
                localProgressBar = TempProgressBar;

                // calculate temp
                resistance = (1023-value)*10000.0/(float)value;
                temp = 1/(Math.log(resistance / 10000)/B+1/298.15)-273.15;
                temp += 40; // start at -40C
                localProgressBar.setProgress((int)(temp*10));
            }
            else if(sensor == 1){
                localProgressBar = PotProgressBar;
                localProgressBar.setProgress(value);
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(DigitalReadReceiver,new IntentFilter(GEService.DIGITAL_READ_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(AnalogReadReceiver, new IntentFilter(GEService.ANALOG_READ_INTENT));
    }

    @Override
    public void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DigitalReadReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(AnalogReadReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_golgi_edison, menu);
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
