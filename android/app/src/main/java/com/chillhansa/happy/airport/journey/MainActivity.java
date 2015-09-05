package com.chillhansa.happy.airport.journey;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitplaces.sdk.android.BitplacesSDK;
import com.bitplaces.sdk.android.broadcast.BitplaceEventsBroadcastReceiver;
import com.bitplaces.sdk.android.broadcast.BitplacesBroadcastHandler;
import com.bitplaces.sdk.android.datatypes.BitplaceEvent;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Bind(R.id.toolbar) Toolbar toolbar;
  @Bind(R.id.toolbar_title_text_view) TextView toolbarTitleTextView;
  @Bind(R.id.goal_reached_text_view) TextView goalReachedTextView;
  @Bind(R.id.points_achieved_text_view) TextView pointsAchievedTextView;
  @Bind(R.id.my_points_text_view) TextView myPointsTextView;
  @Bind(R.id.goal_container) LinearLayout goalContainer;


  private List<ParseObject> myNextGoals;
  private Long lastBeaconId = null;
  private List<ParseObject> beacons;

  private BitplaceEventsBroadcastReceiver bitplaceEventsReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    initToolbar();
    initBeacons();
    initUI();
  }

  private void initUI() {
    ParseUser currentUser = ParseUser.getCurrentUser();
    Integer points = currentUser.getInt("points");
    myPointsTextView.setText(points + " Points");
    goalContainer.setVisibility(View.INVISIBLE);
  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
  }

  private void initBeacons() {
    beacons = ((HappyAirportApplication) getApplication()).getBeacons();
  }


  private void registerBitplaceEventsHandler() {
    if (bitplaceEventsReceiver != null) {
      return;
    }
    bitplaceEventsReceiver =
        BitplaceEventsBroadcastReceiver.registerWithHandler(getApplicationContext(),
            new BitplacesBroadcastHandler<BitplaceEvent>() {
              @Override
              public void onReceivedBroadcast(BitplaceEvent event) {
                handleBitplaceEvent(event);
              }
            });
  }

  private void unregisterBitplaceEventsHandler() {
    if (bitplaceEventsReceiver == null) {
      return;
    }
    bitplaceEventsReceiver.unregister(getApplicationContext());
    bitplaceEventsReceiver = null;
  }

  private void handleBitplaceEvent(BitplaceEvent event) {
    BitplaceEvent.EventType eventType = event.getEventType();

//    logTextView.append("Got event from Beacon: " + event.getBitplaceName() + " - ID: " + event.getBitplaceId());
    // Only care about passing through a beacon
//    if (!isInOrOutEvent(eventType)) {
//      return;
//    }

    String eventFormatString = getString(eventType == BitplaceEvent.EventType.BitplaceIN ?
        R.string.bitplace_in : R.string.bitplace_out);
    String eventText = String.format(eventFormatString, event.getBitplaceName()) + " ID: " + event.getBitplaceId() + "\n";


    if (isTrustedBeacon(event.getBitplaceId())
        && (lastBeaconId == null || lastBeaconId != event.getBitplaceId())) {
      Toast.makeText(getApplicationContext(), eventText, Toast.LENGTH_SHORT).show();
      appendUserStats(event);
      lastBeaconId = event.getBitplaceId();
    } else {
      Log.d(TAG, "Untrusted beacon: " + eventText);
    }
  }

  private void appendUserStats(BitplaceEvent event) {
    onBeaconReached(event);
    getBeaconGoals(event.getBitplaceId());
  }

  private void onBeaconReached(BitplaceEvent event) {
    long beaconId = event.getBitplaceId();
    updateWhereIam(event);
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("beaconId", beaconId);
    ParseCloud.callFunctionInBackground("onBeaconReached", params, new FunctionCallback<Map<String, Object>>() {
      @Override
      public void done(Map<String, Object> result, ParseException e) {
        if (e == null) {
          Log.d(TAG, "Beacon Reached result: " + result);
          if (result != null && !result.isEmpty()) {
            ParseObject goal = (ParseObject) result.get("goal");
            pointsAchievedTextView.setText("You gained " + result.get("points") + "/" + goal.getLong("maxPoints"));
            goalContainer.setVisibility(View.VISIBLE);
            updateUser();
          }
        } else {
          Log.e(TAG, "Error ", e);
        }
      }
    });
  }

  private void updateWhereIam(BitplaceEvent event) {
    goalReachedTextView.setText(event.getBitplaceName());
    // Get beacon from cloud
  }

  private void getBeaconGoals(long beaconId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("beaconId", beaconId);
    ParseCloud.callFunctionInBackground("getBeaconGoals", params, new FunctionCallback<List<ParseObject>>() {
      @Override
      public void done(List<ParseObject> goals, ParseException e) {
        if (e == null) {
          myNextGoals = goals;
          Log.d(TAG, "myNextGoals: " + myNextGoals);
          if (goals != null && !goals.isEmpty()) {
            updateUi(goals.get(0));
          }
          updateUser();
        } else {
          Log.e(TAG, "Error ", e);
        }
      }
    });
  }

  private void updateUi(ParseObject goal) {
    toolbarTitleTextView.setText(goal.getString("description"));
  }

  private void updateUser() {
    ParseUser user = ParseUser.getCurrentUser();
    user.fetchInBackground(new GetCallback<ParseUser>() {
      @Override
      public void done(ParseUser updatedUser, ParseException e) {
        ((HappyAirportApplication) getApplication()).setUser(updatedUser);
        Log.d(TAG, "Got updated user: " + updatedUser);
      }
    });
    ParseUser currentUser = ParseUser.getCurrentUser();
    Integer points = currentUser.getInt("points");
    myPointsTextView.setText(points + " Points");
  }

  private boolean isTrustedBeacon(Long bitplaceId) {
    Log.d(TAG, "Got beacon id: " + bitplaceId);
    if (beacons != null) {
      for (ParseObject beaconObject : beacons) {
        long id = beaconObject.getLong("beaconId");
        if (id == bitplaceId) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isInOrOutEvent(BitplaceEvent.EventType eventType) {
    if (eventType == BitplaceEvent.EventType.BitplaceIN) {
      return true;
    }
    if (eventType == BitplaceEvent.EventType.BitplaceOUT) {
      return true;
    }
    return false;
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerBitplaceEventsHandler();
    BitplacesSDK.getInstance().startMonitoringBitplaces();
  }

  @Override
  protected void onPause() {
    unregisterBitplaceEventsHandler();
    BitplacesSDK.getInstance().stopMonitoringBitplaces();
    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //TODO implement corresponding actions
    switch (item.getItemId()) {
      case R.id.action_directions:
        break;
      case R.id.action_user_preferences:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

}
