package edu.byu.cs240.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For menu fonts on map
        Iconify.with(new FontAwesomeModule());

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        // Get a pointer to a fragment that is currently in the frame layout in main
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentFrameLayout);
        if(fragment == null) {
            fragment = createFirstFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.fragmentFrameLayout, fragment)
                    .commit();
        } else {
            // If the fragment is not null, the MainActivity was destroyed and recreated
            // so we need to reset the listener to the new instance of the fragment
            if(fragment instanceof LoginFragment) {
                ((LoginFragment) fragment).registerListener(this);
            }
        }
    }

    private Fragment createFirstFragment() {
        LoginFragment fragment = new LoginFragment();
        // Attach main activity to login fragment as a listener
        fragment.registerListener(this);
        return fragment;
    }

    @Override
    public void notifyLogin() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapsFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentFrameLayout, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.searchMenuItem);
        MenuItem settingMenuItem = menu.findItem(R.id.settingMenuItem);

        System.out.println("Before icons");
        searchMenuItem.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_search)
                .colorRes(R.color.white)
                .actionBarSize());

        settingMenuItem.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_gear)
                .colorRes(R.color.white)
                .actionBarSize());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch(menu.getItemId()) {
            case R.id.searchMenuItem:
                Toast.makeText(this, "Clicked search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settingMenuItem:
                Intent switchActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(switchActivityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(menu);
        }
    }
}