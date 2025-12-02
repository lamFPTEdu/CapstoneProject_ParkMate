package com.parkmate.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ReservationHoldManager {
    private static final String PREFS_NAME = "reservation_hold_prefs";
    private static final String KEY_HOLD_ID = "hold_id";

    private final SharedPreferences prefs;

    public ReservationHoldManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save hold ID
     */
    public void setHoldId(String holdId) {
        prefs.edit().putString(KEY_HOLD_ID, holdId).apply();
    }

    /**
     * Get hold ID
     * @return hold ID or null if no hold exists
     */
    public String getHoldId() {
        return prefs.getString(KEY_HOLD_ID, null);
    }

    /**
     * Clear hold ID
     */
    public void clearHoldId() {
        prefs.edit().remove(KEY_HOLD_ID).apply();
    }

    /**
     * Check if a hold exists
     */
    public boolean hasHold() {
        return getHoldId() != null;
    }
}

