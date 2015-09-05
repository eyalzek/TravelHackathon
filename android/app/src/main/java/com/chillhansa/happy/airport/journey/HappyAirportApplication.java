package com.chillhansa.happy.airport.journey;

import android.app.Application;

import com.chillhansa.happy.airport.journey.model.Reward;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class HappyAirportApplication extends Application {

  public List<Reward> rewards;
  private ParseUser user;
  private List<ParseObject> beacons;


  @Override
  public void onCreate() {
    super.onCreate();

    initRewards();
    initParse();
    initCalligraphy();
  }

  private void initParse() {
    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, "WG8xtdl7uHL3XOJZJaHDf6ADYlPyDogYpMLTvD3e", "KMvMIwd1DqrxmJmZInh7OsAxyYqLn4ibtyUuIe2k");

    ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);
  }

  private void initCalligraphy() {
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
            .setDefaultFontPath("fonts/HelveticaNeueLTPro-Md.otf")
            .setFontAttrId(R.attr.fontPath)
            .build()
    );
  }

  private void initRewards() {
    rewards = new ArrayList<Reward>();

    rewards.add(new Reward("Cold Beer", 150, 0));
    rewards.add(new Reward("1000 Miles", 2000, 0));
    rewards.add(new Reward("Free Telekom WiFi", 230, 0));
    rewards.add(new Reward("Free Movie", 300, 0));
    rewards.add(new Reward("15% off at Duty Free", 400, 0));
  }

  public ParseUser getUser() {
    return user;
  }

  public void setUser(ParseUser user) {
    this.user = user;
  }

  public List<ParseObject> getBeacons() {
    return beacons;
  }

  public void setBeacons(List<ParseObject> beacons) {
    this.beacons = beacons;
  }

}
