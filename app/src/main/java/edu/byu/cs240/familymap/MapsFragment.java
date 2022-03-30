package edu.byu.cs240.familymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

import model.Event;

public class MapsFragment extends Fragment {
    private DataCache dataCache = DataCache.getInstance();
    private GoogleMap myGoogleMap;
    private Settings currSettings;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            myGoogleMap = googleMap;

//            Set<Event> events = dataCache.getEventsWithSettings();
//// Testing for all events
//            for (Event event: dataCache.getEvents().values()) {
//                float googleColor;
//                if (event.getEventType().equals("marriage")) {
//                    googleColor = BitmapDescriptorFactory.HUE_YELLOW;
//                }else if (event.getEventType().equals("birth")) {
//                    googleColor = BitmapDescriptorFactory.HUE_BLUE;
//                }else {
//                    googleColor = BitmapDescriptorFactory.HUE_RED;
//                }
//
//                LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
//                Marker currMarker = googleMap.addMarker(new MarkerOptions()
//                        .title(event.getEventType())
//                        .position(location)
//                        .icon(BitmapDescriptorFactory.defaultMarker(googleColor))
//                );
//
//                currMarker.setTag(event);
//            }

            addMarkers(googleMap);

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Event currEvent = (Event) marker.getTag();
                    LatLng location = new LatLng(currEvent.getLatitude(), currEvent.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                    // Draw lines according to settings, fill out bottom info, center marker
                    System.out.println(currEvent.getPersonID() + " " + currEvent.getEventType() + " " + currEvent.getYear());
//                     Show the stuff below the map fragment here?
                    return true;
                }
            });
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (myGoogleMap != null) {
            addMarkers(myGoogleMap);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

    }

    private void addMarkers(GoogleMap googleMap) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean lifeStoryLines = preferences.getBoolean(getString(R.string.lifeStoryLines), true);
        boolean familyTreeLines = preferences.getBoolean(getString(R.string.familyTreeLines), true);
        boolean spouseLines = preferences.getBoolean(getString(R.string.spouseLines), true);
        boolean fatherSide = preferences.getBoolean(getString(R.string.filterFatherSide), true);
        boolean motherSide = preferences.getBoolean(getString(R.string.filterMotherSide), true);
        boolean showMaleEvents = preferences.getBoolean(getString(R.string.maleEvents), true);
        boolean showFemaleEvents = preferences.getBoolean(getString(R.string.femaleEvents), true);
        Settings settings = new Settings(lifeStoryLines, familyTreeLines, spouseLines, fatherSide, motherSide, showMaleEvents, showFemaleEvents);

        // If settings were changed
        if (!settings.equals(currSettings)) {
            googleMap.clear();

            Set<Event> events = dataCache.getEventsWithSettings(settings);
            if (events != null) {
                for (Event event : events) {
                    float googleColor = dataCache.getColorForEvent(event);
                    LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                    Marker currMarker = googleMap.addMarker(new MarkerOptions()
                            .title(event.getEventType())
                            .snippet(event.getCity())
                            .position(location)
                            .icon(BitmapDescriptorFactory.defaultMarker(googleColor))
                    );
                    currMarker.setTag(event);
                }
                System.out.println("Finished loop in addMarkers");
            }
            currSettings = settings;
        }
    }

}