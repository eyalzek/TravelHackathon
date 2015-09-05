package com.chillhansa.happy.airport.journey.model;

public class Reward {

  private String name;
  private int points;
  private int imageResource;

  public Reward(String name, int points, int imageResource) {
    this.name = name;
    this.points = points;
    this.imageResource = imageResource;
  }

  @Override
  public String toString() {
    return "Reward{" +
        "name='" + name + '\'' +
        ", points=" + points +
        ", imageResource=" + imageResource +
        '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getImageResource() {
    return imageResource;
  }

  public void setImageResource(int imageResource) {
    this.imageResource = imageResource;
  }
}
