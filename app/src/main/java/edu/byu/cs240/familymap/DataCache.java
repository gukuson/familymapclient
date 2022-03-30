package edu.byu.cs240.familymap;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.common.collect.Sets;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;

class SortEventsByYear implements Comparator<Event> {

    // Method
    // Sorting in ascending order of year of event
    public int compare(Event a, Event b)
    {

//        if (a.getYear())
        return a.getYear() - b.getYear();
    }
}


public class DataCache {
//    Creating singleton
    private static DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {
    }


    private final float[] MARKER_COLORS = new float[]{BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};
    private String authtoken;
    private String userPersonID;
    private Person userPerson;
    // PersonID or eventID for string
    private Map<String, Person> people = new HashMap<>();
    private Map<String, Event> events = new HashMap<>();
    // Key by event type, value is float for color of marker for that event type
    private Map<String, Float> markerColors = new HashMap<>();
    private Set<Event> maleEvents = new HashSet<>();
    private Set<Event> femaleEvents = new HashSet<>();
    // Events that should be displayed everywhere, should be adjusted everytime filters are applied
    private Set<Event> filteredEvents = new HashSet<>();
    // Keyed by personID gets all events for a person sorted chronologically
    private Map<String, List<Event>> personEvents = new HashMap<>();
    // Keyed by personID gets all children for a person
    private Map<String, List<Person>> personChildren = new HashMap<>();
    // Set of personID string for father or mother side
    private Set<String> paternalAncestors = new HashSet<>();
    private Set<String> maternalAncestors = new HashSet<>();
    // Contains set of events for father or mother's sides
    private Set<Event> paternalEvents = new HashSet<>();
    private Set<Event> maternalEvents = new HashSet<>();

    //    Setting settings;

    public Person getPersonById(String personID) {
        return people.get(personID);
    }

    public Event getEventById(String eventID) {
        return events.get(eventID);
    }

    public List<Event> getEventsFor(String personID) {
        return personEvents.get(personID);
    }

    public List<Person> getChildrenFor(String personID) {
        return personChildren.get(personID);
    }

    public void clearData() {
        this.authtoken = null;
        this.userPersonID = null;
        this.userPerson = null;
        this.people.clear();
        this.events.clear();
        markerColors.clear();
        this.maleEvents.clear();
        this.femaleEvents.clear();
        filteredEvents.clear();
        this.personEvents.clear();
        paternalAncestors.clear();
        maternalAncestors.clear();
        paternalEvents.clear();
        maternalEvents.clear();
        personChildren.clear();
    }

    public float getColorForEvent(Event event) {
        return markerColors.get(event.getEventType().toLowerCase());
    }

    // Function populate datacache(List events, list persons)

    Comparator<Event> SortEventsByYearThenEventType
            = Comparator.comparingInt(Event::getYear)
            .thenComparing(Event::getEventType);

    public void cacheData(Person[] people, Event[] events) throws InvalidParameterException {
        if (people != null && events != null) {
            cachePeople(people);

            // Set the person for the user after populated people
            this.userPerson = getPersonById(userPersonID);

            // Get maternal paternal personIDs
            calculateSides();

            calculateChildren();

            cacheEvents(events);

            // Sort the events for personEvents by year and type after added all events
            for (List<Event> eventList : personEvents.values()) {
                Collections.sort(eventList, SortEventsByYearThenEventType);
            }
        } else {
            throw new InvalidParameterException("Must have non null people and event arrays to cache");
        }
    }



    private void cachePeople(Person[] people) {
        for (Person person : people) {
            this.people.put(person.getPersonID(), person);
        }
    }

    private void cacheEvents(Event[] events) {
        int colorNum = 0;
        for (Event event : events) {
            this.events.put(event.getEventID(), event);

            // Sort by male and female events
            if (getPersonById(event.getPersonID()).getGender().equals("m")) {
                maleEvents.add(event);
            }else {
                femaleEvents.add(event);
            }

            // Group maternal and paternal events
            if (maternalAncestors.contains(event.getPersonID())) {
                maternalEvents.add(event);
            }else if (paternalAncestors.contains(event.getPersonID())){
                paternalEvents.add(event);
            }

            // If hasn't had this event type yet, assign a color
            if (!markerColors.containsKey(event.getEventType().toLowerCase())) {
                if (colorNum == MARKER_COLORS.length) {
                    colorNum = 0;
                }
                    markerColors.put(event.getEventType().toLowerCase(), MARKER_COLORS[colorNum]);
                    ++colorNum;
            }

            // Add events to personEvents, list of events for each person
            if (this.personEvents.containsKey(event.getPersonID())) {
                personEvents.get(event.getPersonID()).add(event);
            }else {
                ArrayList<Event> newEventList = new ArrayList<>();
                newEventList.add(event);
                personEvents.put(event.getPersonID(), newEventList);
            }
        }
    }

    public Person getSpouseFor(String personID) {
        Person currPerson = getPersonById(personID);
        return getPersonById(currPerson.getSpouseID());
    }

    /**
     * Returns mom[0] first then dad[1]
     * */
    public Person[] getParents(Person currPerson) {
        if (currPerson.getMotherID() != null) {
            Person[] parents = new Person[2];
            Person mother = getPersonById(currPerson.getMotherID());
            Person father = getPersonById(currPerson.getFatherID());
            parents[0] = mother;
            parents[1] = father;
            return parents;
        } else {
            return null;
        }

    }

    private void calculateChildren() {
        for (Person person : people.values()) {
            if (person.getMotherID() != null) {
                if (personChildren.containsKey(person.getMotherID())) {
                    personChildren.get(person.getMotherID()).add(person);
                    personChildren.get(person.getFatherID()).add(person);
                }else {
                    ArrayList<Person> children = new ArrayList<>();
                    children.add(person);
                    personChildren.put(person.getMotherID(), children);
                    personChildren.put(person.getFatherID(), children);
                }
            }
        }
    }

    private void calculateSides() {
        if (userPerson.getMotherID() != null) {
            addSides(userPerson.getMotherID(), maternalAncestors);
            addSides(userPerson.getFatherID(), paternalAncestors);
        }
    }

    private void addSides(String currPersonID, Set<String> ancestors) {
        Person currPerson = getPersonById(currPersonID);
        ancestors.add(currPersonID);
        if (currPerson.getMotherID() != null) {
            addSides(currPerson.getMotherID(), ancestors);
            addSides(currPerson.getFatherID(), ancestors);
        }
    }

    public Set<Event> getEventsWithSettings(Settings settings) {
        filteredEvents.clear();

        // Starting with extreme cases first
        // if both genders are off, no events
        if (!settings.isMaleEvents() && !settings.isFemaleEvents()) {
            return filteredEvents;
        }
        // if both sides are off and genders are on, only show user and spouse events
        if ((!settings.isFatherSide() && !settings.isMotherSide()) && settings.isMaleEvents() && settings.isFemaleEvents()) {
            filteredEvents.addAll(getEventsFor(userPersonID));
            if (userPerson.getSpouseID() != null) {
                filteredEvents.addAll(getEventsFor(userPerson.getSpouseID()));
            }
            return filteredEvents;
        }
        // If any of the filters are on (false)
        // Add all events
        filteredEvents.addAll(events.values());
        if (!settings.isMaleEvents() || !settings.isFemaleEvents() || !settings.isFatherSide() || !settings.isMotherSide()) {

            // Means male event filter is on (false), remove male events
            if (!settings.isMaleEvents()) {
                filteredEvents.removeAll(maleEvents);
//                for (Event event : filteredEvents) {
//                    if (maleEvents.contains(event)) {
//                        filteredEvents.remove(event);
//                    }
//                }
            }
            // Means female event filter is on (false), remove female events
            if (!settings.isFemaleEvents()) {
                filteredEvents.removeAll(femaleEvents);
//                for (Event event : filteredEvents) {
//                    if (femaleEvents.contains(event)) {
//                        filteredEvents.remove(event);
//                    }
//                }
            }
            // Means father side filter is on (false), remove father's side of events
            if (!settings.isFatherSide()) {
                filteredEvents.removeAll(paternalEvents);
//                for (Event event : filteredEvents) {
//                    if (paternalEvents.contains(event)) {
//                        filteredEvents.remove(event);
//                    }
//                }
            }
            // Means mother side filter is on (false), remove mother's side of events
            if (!settings.isMotherSide()) {
                filteredEvents.removeAll(maternalEvents);
            }
            return filteredEvents;
        } else {
            // No filters are on (all are true)
            return filteredEvents;
        }
    }



    public void setAuthtoken(String authtoken) {
        this.authtoken = authtoken;
    }

    public void setUserPersonID(String userPersonID) {
        this.userPersonID = userPersonID;
    }

    public String getAuthtoken() {
        return authtoken;
    }

    public String getUserPersonID() {
        return userPersonID;
    }

    private void setUserPerson(Person userPerson) {
        this.userPerson = userPerson;
    }

    public Map<String, List<Event>> getAllPersonEvents() {
        return personEvents;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public Person getUserPerson() {
        return userPerson;
    }

    public Set<Event> getMaleEvents() {
        return maleEvents;
    }

    public Set<Event> getFemaleEvents() {
        return femaleEvents;
    }

    public Set<Event> getPaternalEvents() {
        return paternalEvents;
    }

    public Set<Event> getMaternalEvents() {
        return maternalEvents;
    }
}
