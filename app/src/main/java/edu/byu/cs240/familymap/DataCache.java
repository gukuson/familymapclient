package edu.byu.cs240.familymap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;

public class DataCache {
//    Creating singleton
    private static DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {
    }

    // PersonID or eventID for string
    private Map<String, Person> people;
    private Map<String, Event> events;
    // Keyed by personID gets all events for a person sorted chronologically
    private Map<String, List<Event>> personEvents;
    private Set<String> paternalAncestors;

//    Setting settings;
    public Person getPersonById(String personID) {
        return null;
    }
    public Event getEventById(String eventID) {
        return null;
    }
    public List<Event> getPersonEvents(String personID) {
        return null;
    }


}
