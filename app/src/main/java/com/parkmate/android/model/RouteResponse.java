package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model cho OSRM Route Response
 */
public class RouteResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("routes")
    private List<Route> routes;

    @SerializedName("waypoints")
    private List<Waypoint> waypoints;

    public String getCode() { return code; }
    public List<Route> getRoutes() { return routes; }
    public List<Waypoint> getWaypoints() { return waypoints; }

    public static class Route {
        @SerializedName("geometry")
        private String geometry; // Polyline encoded string

        @SerializedName("legs")
        private List<Leg> legs;

        @SerializedName("distance")
        private double distance; // meters

        @SerializedName("duration")
        private double duration; // seconds

        public String getGeometry() { return geometry; }
        public List<Leg> getLegs() { return legs; }
        public double getDistance() { return distance; }
        public double getDuration() { return duration; }
    }

    public static class Leg {
        @SerializedName("steps")
        private List<Step> steps;

        @SerializedName("distance")
        private double distance;

        @SerializedName("duration")
        private double duration;

        public List<Step> getSteps() { return steps; }
        public double getDistance() { return distance; }
        public double getDuration() { return duration; }
    }

    public static class Step {
        @SerializedName("geometry")
        private String geometry;

        @SerializedName("maneuver")
        private Maneuver maneuver;

        @SerializedName("name")
        private String name;

        @SerializedName("distance")
        private double distance;

        @SerializedName("duration")
        private double duration;

        public String getGeometry() { return geometry; }
        public Maneuver getManeuver() { return maneuver; }
        public String getName() { return name; }
        public double getDistance() { return distance; }
        public double getDuration() { return duration; }
    }

    public static class Maneuver {
        @SerializedName("location")
        private List<Double> location;

        @SerializedName("type")
        private String type;

        @SerializedName("instruction")
        private String instruction;

        public List<Double> getLocation() { return location; }
        public String getType() { return type; }
        public String getInstruction() { return instruction; }
    }

    public static class Waypoint {
        @SerializedName("location")
        private List<Double> location;

        @SerializedName("name")
        private String name;

        public List<Double> getLocation() { return location; }
        public String getName() { return name; }
    }
}

