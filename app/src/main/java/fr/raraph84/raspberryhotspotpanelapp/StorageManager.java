package fr.raraph84.raspberryhotspotpanelapp;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageManager {

    private static final String SHARED_PREFERENCES_FILE = "fr.raraph84.raspberryhotspotpanelapp.StorageManager.SHARED_PREFERENCES_FILE";
    private static final String TOKEN_KEY = "fr.raraph84.raspberryhotspotpanelapp.StorageManager.TOKEN_KEY";
    private static final String LAST_IP_KEY = "fr.raraph84.raspberryhotspotpanelapp.StorageManager.LAST_IP_KEY";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public StorageManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void setToken(String token) {
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public String getLastIp() {
        return sharedPreferences.getString(LAST_IP_KEY, null);
    }

    public void setLastIp(String lastIp) {
        editor.putString(LAST_IP_KEY, lastIp);
        editor.apply();
    }
}
