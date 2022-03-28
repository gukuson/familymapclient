package edu.byu.cs240.familymap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;

class SortEventsByYear implements Comparator<Event> {

    // Method
    // Sorting in ascending order of year of event
    public int compare(Event a, Event b)
    {
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

    private String authtoken;
    private String userPersonID;
    private Person userPerson;
    // PersonID or eventID for string
    private Map<String, Person> people = new HashMap<>();
    private Map<String, Event> events = new HashMap<>();

    // Keyed by personID gets all events for a person sorted chronologically
    private Map<String, List<Event>> personEvents = new HashMap<>();
    private Set<String> paternalAncestors = new HashSet<>();

    //    Setting settings;

    public Person getPersonById(String personID) {
        return people.get(personID);
    }

    public Event getEventById(String eventID) {
        return events.get(eventID);
    }

    public List<Event> getPersonEvents(String personID) {
        return personEvents.get(personID);
    }


    // Function populate datacache(List events, list persons)

    public void cacheData(Person[] people, Event[] events) {
        cachePeople(people);
        cacheEvents(events);

        // Sort the events for personEvents by year after added all events
        for (List<Event> eventList : personEvents.values()) {
            Collections.sort(eventList, new SortEventsByYear());
        }

        // Set the person for the user after populated events and people
        userPerson = getPersonById(userPersonID);
        setUserPerson(userPerson);
    }

    public void clearData() {
        this.authtoken = null;
        this.userPerson = null;
        this.userPersonID = null;
        this.people.clear();
        this.events.clear();
        this.personEvents.clear();
        paternalAncestors.clear();

    }

    private void cachePeople(Person[] people) {
        for (Person person : people) {
            this.people.put(person.getPersonID(), person);
        }
    }

    private void cacheEvents(Event[] events) {
        for (Event event : events) {
            this.events.put(event.getEventID(), event);

            if (this.personEvents.containsKey(event.getPersonID())) {
                personEvents.get(event.getPersonID()).add(event);
            }else {
                ArrayList<Event> newEventList = new ArrayList<>();
                newEventList.add(event);
                personEvents.put(event.getPersonID(), newEventList);
            }
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
}
