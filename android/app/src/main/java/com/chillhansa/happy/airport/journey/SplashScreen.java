package com.chillhansa.happy.airport.journey;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bitplaces.sdk.android.BitplacesOperationCompletionHandler;
import com.bitplaces.sdk.android.BitplacesSDK;
import com.bitplaces.sdk.android.MonitoringPreferences;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashScreen extends AppCompatActivity {

  private final static String TAG = SplashScreen.class.getSimpleName();
  private static final int SPLASH_TIME_OUT = 1000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_splash_screen);

    new Handler().postDelayed(new Runnable() {

      @Override
      public void run() {
        startBitplacesSDK();
      }
    }, SPLASH_TIME_OUT);
  }



  private void startBitplacesSDK() {
    Context appContext = getApplicationContext();
    // TODO: set up your credentials!
    String customerID = "LHSEP";
    String customerSecret = "LHSEP";
    String uaReleaseID = "179.15.3";

    BitplacesSDK.start(appContext, customerID, customerSecret, uaReleaseID,
        new BitplacesOperationCompletionHandler<Void>() {
          @Override
          public void onOperationComplete(int resultCode, Void resultData) {
            if (BitplacesSDK.ResultCode.fromCode(resultCode).isSuccessful()) {
              // From this point on you know that the SDK initialization was successful.

              authenticateParseUser();

              MonitoringPreferences preferences = new MonitoringPreferences.Builder()
                  .setBeaconScanLengthMillis(1000)
                  .setPeriodBetweenBeaconScansMillis(0)
                  .build();
              BitplacesSDK.getInstance().setMonitoringPreferences(preferences);

              // You could also monitor and listen to bitplace events from a service.
            } else {
              Log.e(TAG, "Failed to start bitplaces SDK. ");
              // The SDK initialization failed. If you know the customer credentials were correct,
              // try again later. Don't call any other SDK methods until the initialization is
              // successfully completed.
            }
          }
        });
  }

  private void authenticateParseUser() {
    ParseUser.logInInBackground("user", "1234", new LogInCallback() {
      public void done(ParseUser user, ParseException e) {
        if (user != null) {
          ((HappyAirportApplication) getApplication()).setUser(user);
          Log.i(TAG, "Got user: " + user);
          getUsableBeacons();
          clearUserJourney();
        } else {
          Log.e(TAG, "Could not authenticate User", e);
        }
      }
    });
  }

  private void clearUserJourney() {
    ParseCloud.callFunctionInBackground("clearUserJourney", new HashMap<String, Object>(), new FunctionCallback<String>() {
      @Override
      public void done(String result, ParseException e) {
        if (e == null) {
          Log.d(TAG, "clear user journey result " + result);
        } else {
          Log.e(TAG, "Error ", e);
        }
      }
    });
  }

  private void getUsableBeacons() {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Beacon");
    query.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> beaconObjects, ParseException e) {
        if (e == null) {
          ((HappyAirportApplication) getApplication()).setBeacons(beaconObjects);
          Log.d(TAG, "Beacons: " + beaconObjects);
          startNextActivity();
        } else {
          Log.e(TAG, "Could not find beacons", e);
        }
      }
    });
  }

  private void startNextActivity() {
    Intent next = new Intent(getApplicationContext(), FlightInfoActivity.class);
    startActivity(next);
    finish();
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }
}
