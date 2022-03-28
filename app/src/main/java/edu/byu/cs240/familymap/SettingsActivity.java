package edu.byu.cs240.familymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)  {
            Intent intent = new Intent(this, MainActivity.class);
//            Don't create new activity and put on top of another, other one if main activity but buried in backstack, pop everything in backstack until get to it
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference button = findPreference(getString(R.string.logoutButton));
            if (button != null) {
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //clear datacache and reset preferences
                        DataCache.getInstance().clearData();

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();


//                        clears the stack of activities and creates a new instance of the main activity
                        Intent switchActivityIntent = new Intent(getActivity(), MainActivity.class);
                        switchActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(switchActivityIntent);
                        return true;
                    }
                });
            }
        }

// Get rid of indents, code goes to android developer is his name lol
        @Override
        public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
            super.setPreferenceScreen(preferenceScreen);
            if (preferenceScreen != null) {
                int count = preferenceScreen.getPreferenceCount();
                for (int i = 0; i < count; i++)
                    preferenceScreen.getPreference(i).setIconSpaceReserved(false);
            }
        }

    }

}