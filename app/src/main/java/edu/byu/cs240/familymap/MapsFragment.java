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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import model.Event;

public class MapsFragment extends Fragment {
    private DataCache dataCache = DataCache.getInstance();

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {

// Testing for all events
            for (Event event: dataCache.getEvents().values()) {
                float googleColor;
                if (event.getEventType().equals("marriage")) {
                    googleColor = BitmapDescriptorFactory.HUE_YELLOW;
                }else if (event.getEventType().equals("birth")) {
                    googleColor = BitmapDescriptorFactory.HUE_BLUE;
                }else {
                    googleColor = BitmapDescriptorFactory.HUE_RED;
                }

                LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                Marker currMarker = googleMap.addMarker(new MarkerOptions()
                        .title(event.getEventType())
                        .position(location)
                        .icon(BitmapDescriptorFactory.defaultMarker(googleColor))
                );

                currMarker.setTag(event);
            }

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Event currEvent = (Event) marker.getTag();
                    System.out.println(currEvent.getEventType() + " " + currEvent.getYear());
//                     Show the stuff below the map fragment here?
                    return true;
                }
            });

        }
    };

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
}