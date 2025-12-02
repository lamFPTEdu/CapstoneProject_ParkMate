package com.parkmate.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SubscriptionHoldManager {
    private static final String PREFS_NAME = "subscription_hold_prefs";
    private static final String KEY_HELD_SPOT_ID = "held_spot_id";

    private final SharedPreferences prefs;

    public SubscriptionHoldManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save held spot ID
     */
    public void setHeldSpot(long spotId) {
        prefs.edit().putLong(KEY_HELD_SPOT_ID, spotId).apply();
    }

    /**
     * Get held spot ID
     * @return spot ID or -1 if no spot is held
     */
    public long getHeldSpot() {
        return prefs.getLong(KEY_HELD_SPOT_ID, -1);
    }

    /**
     * Clear held spot
     */
    public void clearHeldSpot() {
        prefs.edit().remove(KEY_HELD_SPOT_ID).apply();
    }

    /**
     * Check if a spot is currently held
     */
    public boolean hasHeldSpot() {
        return getHeldSpot() != -1;
    }
}

