package edu.byu.cs240.familymap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private Person clickedPerson;
    private Event clickedEvent;

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

    public Set<Event> getFilteredEvents() {
        return filteredEvents;
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
        this.clickedPerson = null;
        this.clickedEvent = null;
        this.people.clear();
        this.events.clear();
        this.markerColors.clear();
        this.maleEvents.clear();
        this.femaleEvents.clear();
        this.filteredEvents.clear();
        this.personEvents.clear();
        this.paternalAncestors.clear();
        this.maternalAncestors.clear();
        this.paternalEvents.clear();
        this.maternalEvents.clear();
        this.personChildren.clear();
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

    public List<Person> getFamilyFor(String personID) {
        Person currPerson = getPersonById(personID);
        List<Person> family = new ArrayList<>();

        Person [] parents = getParents(currPerson);
        if (parents != null) {
            assert parents.length == 2 : "Parent array wasn't 2";
            family.add(parents[0]);
            family.add(parents[1]);
        }


        if (currPerson.getSpouseID() != null) {
            family.add(getPersonById(currPerson.getSpouseID()));
        }

        if (personChildren.containsKey(personID)) {
            family.addAll(getChildrenFor(personID));
        }

        return family;
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
            }
            // Means female event filter is on (false), remove female events
            if (!settings.isFemaleEvents()) {
                filteredEvents.removeAll(femaleEvents);
            }
            // Means father side filter is on (false), remove father's side of events
            if (!settings.isFatherSide()) {
                filteredEvents.removeAll(paternalEvents);
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

    public String getFullName(Person clickedPerson) {
        return clickedPerson.getFirstName() + " " + clickedPerson.getLastName();
    }

    public String eventToString(Event clickedEvent) {
        return clickedEvent.getEventType() + ": " +
                clickedEvent.getCity() +
                ", " + clickedEvent.getCountry() +
                " (" + clickedEvent.getYear() + ")";
    }

    public String getRelationshipBetween(Person clickedPerson, Person person) {
        String personID = person.getPersonID();
        String relationship;
        if (Objects.equals(clickedPerson.getFatherID(), personID)) {
            relationship = "Father";
        }else if (Objects.equals(clickedPerson.getMotherID(), personID)) {
            relationship = "Mother";
        }else if (Objects.equals(clickedPerson.getSpouseID(), personID)) {
            relationship = "Spouse";
        }else {
            relationship = "Child";
        }
        return relationship;
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


    public Person getClickedPerson() {
        return clickedPerson;
    }

    public void setClickedPerson(Person clickedPerson) {
        this.clickedPerson = clickedPerson;
    }

    public Event getClickedEvent() {
        return clickedEvent;
    }

    public void setClickedEvent(Event clickedEvent) {
        this.clickedEvent = clickedEvent;
    }
}
