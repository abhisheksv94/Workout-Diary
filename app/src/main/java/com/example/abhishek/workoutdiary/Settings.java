package com.example.abhishek.workoutdiary;

import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;


/**
 * Created by Abhishek on 05/12/17.
 */

public class Settings extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        //set the preference change listeners
        Preference pref1=findPreference("imperial"),pref2=findPreference("metric");
        pref1.setOnPreferenceChangeListener(this);pref2.setOnPreferenceChangeListener(this);
        Preference cal=findPreference("calories");
        cal.setOnPreferenceChangeListener(this);
        getActivity().setTitle("Settings");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key=preference.getKey();
        //if settings had imperial units selected
        if(key.equalsIgnoreCase("imperial")){
            boolean f=(boolean)newValue;
            if(f)((CheckBoxPreference)findPreference("metric")).setChecked(false);
            else ((CheckBoxPreference)findPreference("metric")).setChecked(true);
            return true;
        }
        //metric units selected
        else if(key.equalsIgnoreCase("metric")){
            boolean f=(boolean)newValue;
            if(f)((CheckBoxPreference)findPreference("imperial")).setChecked(false);
            else ((CheckBoxPreference)findPreference("imperial")).setChecked(true);
            return true;
        }
        //whether calories is required
        else if (key.equalsIgnoreCase("calories")){
            boolean f=(boolean)newValue;
            if(!f){
                ((CheckBoxPreference)findPreference("graph_calories")).setChecked(false);
                ((CheckBoxPreference)preference).setChecked(false);
            }
            if(f)((CheckBoxPreference)preference).setChecked(true);
        }
        return false;
    }

    //save the state of the app
    @Override
    public void onPause() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(getString(R.string.saveState),"Settings");
        editor.apply();
        super.onPause();
    }
}
