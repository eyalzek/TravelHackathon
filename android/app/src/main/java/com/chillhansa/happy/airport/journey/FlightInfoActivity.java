package com.chillhansa.happy.airport.journey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bitplaces.sdk.android.broadcast.BitplaceEventsBroadcastReceiver;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FlightInfoActivity extends AppCompatActivity {

  private static final String TAG = FlightInfoActivity.class.getSimpleName();

  @Bind(R.id.flight_info_dummy_view) ImageView fligtInfoDummyView;

  private BitplaceEventsBroadcastReceiver bitplaceEventsReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_flight_info);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.flight_info_dummy_view)
  public void onDummyFLightInfoClick(View v) {
    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(mainActivity);
  }

}
