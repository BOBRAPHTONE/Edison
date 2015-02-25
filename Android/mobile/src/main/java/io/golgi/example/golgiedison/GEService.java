package io.golgi.example.golgiedison;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiAPIHandler;
import com.openmindnetworks.golgi.api.GolgiException;
import com.openmindnetworks.golgi.api.GolgiTransportOptions;

import io.golgi.apiimpl.android.GolgiService;
import io.golgi.example.golgiedison.gen.Analog;
import io.golgi.example.golgiedison.gen.Digital;
import io.golgi.example.golgiedison.gen.GolgiEdisonService;
import io.golgi.example.golgiedison.gen.GolgiKeys;

public class GEService extends GolgiService {

    private boolean isStarted;
    private boolean isRegistered;

    public static GEService geService = null;

    public final static String DIGITAL_READ_INTENT = "DIGITAL_READ_INTENT";
    public final static String ANALOG_READ_INTENT = "ANALOG_READ_INTENT";

    GolgiEdisonService.digitalRead.RequestReceiver DigitalReadReceiver = new GolgiEdisonService.digitalRead.RequestReceiver() {
        @Override
        public void receiveFrom(GolgiEdisonService.digitalRead.ResultSender resultSender, Digital digital) {
            // send the success indication
            resultSender.success();

            // broadcast the update to the UI thread
            Intent intent = new Intent(DIGITAL_READ_INTENT);
            intent.putExtra("VALUE",digital.getValue());
            intent.putExtra("PIN",digital.getPin());
//            if(digital.getPin() == 12){
//                intent.putExtra("SENSOR","PIR");
//            }
//            else if(digital.getPin() == 2){
//                intent.putExtra("SENSOR","TOUCH");
//            }
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        }
    };

    GolgiEdisonService.analogRead.RequestReceiver AnalogReadReceiver = new GolgiEdisonService.analogRead.RequestReceiver() {
        @Override
        public void receiveFrom(GolgiEdisonService.analogRead.ResultSender resultSender, Analog analog) {
            // send the success indication
            resultSender.success();

            // broadcast the update to the UI thread
            Intent intent = new Intent(ANALOG_READ_INTENT);
            intent.putExtra("VALUE",analog.getValue());
            intent.putExtra("PIN",analog.getPin());
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        }
    };

    private void golgiRegister(){
        if(isStarted) {

            GolgiEdisonService.digitalRead.registerReceiver(DigitalReadReceiver);
            GolgiEdisonService.analogRead.registerReceiver(AnalogReadReceiver);
            Log.i("DEBUG", "Attempting to register with DevKey: " + GolgiKeys.DEV_KEY + "\n and AppKey: " + GolgiKeys.APP_KEY);
            GolgiAPI.getInstance().register(GolgiKeys.DEV_KEY,
                                            GolgiKeys.APP_KEY,
                                            "mobile-address",
                    new GolgiAPIHandler() {
                        @Override
                        public void registerSuccess() {
                            Log.i("DEBUG", "Registration Success");
                            isRegistered = true;
                        }

                        @Override
                        public void registerFailure() {
                            Log.i("DEBUG", "Registration Failure");
                        }
                    }
            );
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent,flags,startId);
        isStarted = true;
        geService = this;
        golgiRegister();
        return START_STICKY;
    }

    public static GEService getInstance(){
        return geService;
    }

    public void signalLED(int value){
        GolgiTransportOptions golgiTransportOptions = new GolgiTransportOptions();
        golgiTransportOptions.setValidityPeriod(60);

        Digital digital = new Digital();
        digital.setPin(4);
        digital.setValue(value);

        GolgiEdisonService.digitalWrite.sendTo(new GolgiEdisonService.digitalWrite.ResultReceiver() {
            @Override
            public void failure(GolgiException ex) {
                Log.i("DEBUG","Failed: "+ex.getErrText());
            }

            @Override
            public void success() {
                Log.i("DEBUG","Successfully set pin");
            }
        },
        golgiTransportOptions,
        "edison-board",
        digital);
    }
}
