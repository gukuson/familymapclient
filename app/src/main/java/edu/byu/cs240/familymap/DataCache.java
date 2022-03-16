package edu.byu.cs240.familymap;

import java.util.HashMap;
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
    private Map<String, Person> people = new HashMap<>();
    private Map<String, Event> events = new HashMap<>();
    // Keyed by personID gets all events for a person sorted chronologically
    private Map<String, List<Event>> personEvents;
    private Set<String> paternalAncestors;
    private String authtoken;
    private String userPersonID;
    private Person userPerson;

    //    Setting settings;

    public Person getPersonById(String personID) {
        return people.get(personID);
    }

    public Event getEventById(String eventID) {
        return events.get(eventID);
    }

    public List<Event> getPersonEvents(String personID) {
        return null;
    }


    // Function populate datacache(List events, list persons)

    public void cacheData(Person[] people, Event[] events) {
        cachePeople(people);
        cacheEvents(events);
        // Set the person for the user after populated events and people
        userPerson = getPersonById(userPersonID);
        setUserPerson(userPerson);
    }

    public void cachePeople(Person[] people) {
        for (Person person : people) {
            this.people.put(person.getPersonID(), person);
        }
    }

    public void cacheEvents(Event[] events) {
        for (Event event : events) {
            this.events.put(event.getEventID(), event);
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

    public void setUserPerson(Person userPerson) {
        this.userPerson = userPerson;
    }

    public Person getUserPerson() {
        return userPerson;
    }
}
