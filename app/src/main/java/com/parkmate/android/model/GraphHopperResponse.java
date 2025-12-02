package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * GraphHopper API Response Model
 */
public class GraphHopperResponse {

    @SerializedName("paths")
    private List<Path> paths;

    @SerializedName("info")
    private Info info;

    public List<Path> getPaths() {
        return paths;
    }

    public Info getInfo() {
        return info;
    }

    public static class Path {
        @SerializedName("distance")
        private double distance; // meters

        @SerializedName("time")
        private long time; // milliseconds

        @SerializedName("points")
        private String points; // Encoded polyline

        @SerializedName("points_encoded")
        private boolean pointsEncoded;

        @SerializedName("instructions")
        private List<Instruction> instructions;

        public double getDistance() {
            return distance;
        }

        public long getTime() {
            return time;
        }

        public String getPoints() {
            return points;
        }

        public boolean isPointsEncoded() {
            return pointsEncoded;
        }

        public List<Instruction> getInstructions() {
            return instructions;
        }
    }

    public static class Instruction {
        @SerializedName("text")
        private String text;

        @SerializedName("distance")
        private double distance;

        @SerializedName("time")
        private long time;

        public String getText() {
            return text;
        }

        public double getDistance() {
            return distance;
        }

        public long getTime() {
            return time;
        }
    }

    public static class Info {
        @SerializedName("copyrights")
        private List<String> copyrights;

        @SerializedName("took")
        private long took; // milliseconds

        public List<String> getCopyrights() {
            return copyrights;
        }

        public long getTook() {
            return took;
        }
    }
}

