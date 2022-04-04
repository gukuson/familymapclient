package edu.byu.cs240.familymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import model.Event;
import model.Person;

public class MapsFragment extends Fragment {
    private DataCache dataCache = DataCache.getInstance();
    private GoogleMap myGoogleMap;
    private Settings currSettings;
    private Set<Polyline> polylines = new HashSet<>();
    private Person clickedPerson;
    private Event clickedEvent;
    int FAMILY_TREE_COLOR = 0xFF82AD88;

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

            addMarkers(googleMap);

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    // Draw lines according to settings, fill out bottom info, center marker
                    clickedEvent = (Event) marker.getTag();
                    assert clickedEvent != null;
                    LatLng location = new LatLng(clickedEvent.getLatitude(), clickedEvent.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(location));

                    clickedPerson = dataCache.getPersonById(clickedEvent.getPersonID());

                    // Set datacache current person
                    dataCache.setClickedPerson(clickedPerson);

                    drawLinesWithSettings(clickedPerson, clickedEvent);

                    com.joanzapata.iconify.widget.IconTextView icon = getActivity().findViewById(R.id.event_icon);
                    TextView eventText = getActivity().findViewById(R.id.mapTextView);

                    if (clickedPerson.getGender().equals("m")) {
                        icon.setText(R.string.fa_male);
                        icon.setTextColor(getResources().getColor(R.color.blue));
                    }else {
                        icon.setText(R.string.fa_female);
                        icon.setTextColor(getResources().getColor(R.color.pink));
                    }
                    // Update text bottom info
                    StringBuilder currString = new StringBuilder();

                    currString.append(dataCache.getFullName(clickedPerson));
                    currString.append("\n");
                    currString.append(dataCache.eventToString(clickedEvent));

                    eventText.setText(currString);

                    return true;
                }
            });
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (myGoogleMap != null) {
            if (isSettingsChanged()) {
                addMarkers(myGoogleMap);
                // Check current marker is still there after add markers
                if (dataCache.getFilteredEvents().contains(clickedEvent)) {
                    drawLinesWithSettings(clickedPerson, clickedEvent);
                }else {
                    resetEventDetails();
                }
            }
        }
    }

    private void resetEventDetails() {
        clickedEvent = null;
        clickedPerson = null;

        com.joanzapata.iconify.widget.IconTextView icon = getActivity().findViewById(R.id.event_icon);
        TextView eventText = getActivity().findViewById(R.id.mapTextView);

        icon.setText(R.string.fa_android);
        icon.setTextColor(getResources().getColor(R.color.green));

        eventText.setText(R.string.initial_map_text);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        LinearLayout eventInfo = view.findViewById(R.id.event_info);
        eventInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataCache.getFilteredEvents().contains(clickedEvent)) {
                    Intent switchActivityIntent = new Intent(getActivity(), PersonActivity.class);
                    startActivity(switchActivityIntent);
                }else {
                    Toast.makeText(getContext(),"Please select a marker ",Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
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

    private void drawLinesWithSettings(Person person, Event event) {
        int SPOUSE_LINE_COLOR = 0xFF826E93;
        int LIFE_STORY_COLOR = 0xFFE1A967;
        int BASE_WIDTH = 30;
        Set<Event> filteredEvents = dataCache.getFilteredEvents();
        // Remove previous lines
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();

        // Add lines according to line settings

        if (currSettings.isSpouseLines()) {
            if (person.getSpouseID() != null) {
                // Create start and end points for the line
                assert dataCache.getEventsFor(person.getSpouseID()).size() > 0 : "Didn't get any events for spouse of " + person.getFirstName();
                Event endEvent = dataCache.getEventsFor(person.getSpouseID()).get(0);
                if (filteredEvents.contains(endEvent)) {
                    addLine(event, endEvent, SPOUSE_LINE_COLOR, BASE_WIDTH - 15);
                }
            }
        }

        if (currSettings.isFamilyTreeLines()) {
            // If have parents, recurse through family adding thinner lines
            addFamilyLinesFor(event, BASE_WIDTH, filteredEvents);
        }
        if (currSettings.isLifeStoryLines()) {
            // Get all events for the person of current event, if have more than one event loop through and add lines
            ArrayList<Event> personEvents = (ArrayList<Event>) dataCache.getEventsFor(person.getPersonID());
            if (personEvents.size() > 1) {
                for (int i = 0; i < personEvents.size() - 1; ++i) {
                    addLine(personEvents.get(i), personEvents.get(i + 1), LIFE_STORY_COLOR, BASE_WIDTH - 15);
                }
            }

        }
    }

    private void addLine(Event event, Event endEvent, int color, int width) {
        LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
        LatLng endPoint = new LatLng(endEvent.getLatitude(), endEvent.getLongitude());

        // Add line to map by specifying its endpoints, color, and width
        PolylineOptions options = new PolylineOptions().add(startPoint).add(endPoint).color(color).width(width);
        Polyline line = myGoogleMap.addPolyline(options);
        polylines.add(line);
    }

    private void addFamilyLinesFor(Event startEvent, int width, Set<Event> filteredEvents) {
        Person person = dataCache.getPersonById(startEvent.getPersonID());

        // Check if person/event has parents
        if (person.getMotherID() != null) {
            // If mother has events
            if (dataCache.getEventsFor(person.getMotherID()).size() > 0) {
                Event motherEvent = dataCache.getEventsFor(person.getMotherID()).get(0);
                // If mother event is filtered or not
                if (filteredEvents.contains(motherEvent)) {
                    addLine(startEvent, motherEvent, FAMILY_TREE_COLOR, width);
                    addFamilyLinesFor(motherEvent, width - 10, filteredEvents);
                }
            }
            // If mother has events
            if (dataCache.getEventsFor(person.getFatherID()).size() > 0) {
                Event fatherEvent = dataCache.getEventsFor(person.getFatherID()).get(0);
                // If father event is filtered or not
                if (filteredEvents.contains(fatherEvent)) {
                    addLine(startEvent, fatherEvent, FAMILY_TREE_COLOR, width);
                    addFamilyLinesFor(fatherEvent, width - 10, filteredEvents);
                }
            }
        }
    }

    private boolean isSettingsChanged() {
        Settings settings = getSettings();
        return !settings.equals(currSettings);

    }

    private Settings getSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean lifeStoryLines = preferences.getBoolean(getString(R.string.lifeStoryLines), true);
        boolean familyTreeLines = preferences.getBoolean(getString(R.string.familyTreeLines), true);
        boolean spouseLines = preferences.getBoolean(getString(R.string.spouseLines), true);
        boolean fatherSide = preferences.getBoolean(getString(R.string.filterFatherSide), true);
        boolean motherSide = preferences.getBoolean(getString(R.string.filterMotherSide), true);
        boolean showMaleEvents = preferences.getBoolean(getString(R.string.maleEvents), true);
        boolean showFemaleEvents = preferences.getBoolean(getString(R.string.femaleEvents), true);
        return new Settings(lifeStoryLines, familyTreeLines, spouseLines, fatherSide, motherSide, showMaleEvents, showFemaleEvents);
    }

    private void addMarkers(GoogleMap googleMap) {
        Settings settings = getSettings();
        // If settings were changed
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
                    assert currMarker != null;
                    currMarker.setTag(event);
                }
                System.out.println("Finished loop in addMarkers");
            }
            currSettings = settings;
        }

}