package com.example.focusplan.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String PREF_NAME = "expense_prefs";
    private static final String KEY_LAST_CATEGORY = "last_category";
    private static final String KEY_LAST_TYPE = "last_type";

    private final SharedPreferences sp;

    public Prefs(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLastCategory(String category) {
        sp.edit().putString(KEY_LAST_CATEGORY, category).apply();
    }

    public String getLastCategory() {
        return sp.getString(KEY_LAST_CATEGORY, "");
    }

    public void saveLastType(String type) {
        sp.edit().putString(KEY_LAST_TYPE, type).apply();
    }

    public String getLastType() {
        return sp.getString(KEY_LAST_TYPE, "支出"); // 默认为支出
    }
}
