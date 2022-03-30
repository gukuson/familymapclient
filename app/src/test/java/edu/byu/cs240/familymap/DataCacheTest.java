package edu.byu.cs240.familymap;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;

import org.junit.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Event;
import model.Person;
import request.LoginRequest;
import request.RegisterRequest;
import result.AllEventsResult;
import result.AllPersonsResult;
import result.LoginResult;
import result.RegisterResult;

// Make sure server is running, make sure Sheila Parker data loaded into server via LocalHost,
// since registering user auto generates only 3 event types
//        Calculates family relationships (i.e., spouses, parents, children)
//        Filters events according to the current filter settings
//        Chronologically sorts a person’s individual events (birth first, death last, etc.)
//        Correctly searches for people and events (for your Search Activity)


public class DataCacheTest {
    private ServerProxy serverProxy;
    private DataCache dataCache;
//    private RegisterResult registerResult;
    private LoginResult loginResult;
    AllPersonsResult people;
    AllEventsResult events;
    private Context context;

    @Before
    public void setUp() {
        // Make sure passoff data is loaded into server (Sheila Parker)
        serverProxy = new ServerProxy("localhost","8080");
        dataCache = DataCache.getInstance();
        // Fresh dataCache for each test
        dataCache.clearData();

        // Login to passoff data
        LoginRequest loginRequest  = new LoginRequest("sheila", "parker");
        loginResult = serverProxy.login(loginRequest);

        // Get data from server
        people = serverProxy.getPeople(loginResult.getAuthtoken());
        events = serverProxy.getEvents(loginResult.getAuthtoken());

        // Set authtoken and personID for cache
        dataCache.setAuthtoken(loginResult.getAuthtoken());
        dataCache.setUserPersonID(loginResult.getPersonID());

    }

    @Test
    public void sortedEventsPass() {
        dataCache.cacheData(people.getData(), events.getData());

        assertEquals(loginResult.getPersonID(),dataCache.getUserPerson().getPersonID());

        // Checks if events are sorted chronologically for each person, and by eventtype
        for (List<Event> eventList : dataCache.getAllPersonEvents().values()) {
            for (int i = 0; i < eventList.size() - 1; ++i) {
                assertTrue(eventList.get(i).getYear() < eventList.get(i + 1).getYear()
                        || eventList.get(i).getEventType().compareTo(eventList.get(i + 1).getEventType()) <= 0);
            }
        }
    }

    @Test
    public void sortedEventsFail() {

        assertEquals(loginResult.getPersonID(),dataCache.getUserPersonID());
        // Invalid input to cacheData
        assertThrows(InvalidParameterException.class, ()-> dataCache.cacheData(null, null));
    }

    @Test
    public void relationshipPass() {
        dataCache.cacheData(people.getData(), events.getData());

        // Manually checking children are correct
        Person sheila = dataCache.getPersonById("Sheila_Parker");
        Person blaine = dataCache.getPersonById("Blaine_McGary");
        Person betty = dataCache.getPersonById("Betty_White");

        assertEquals(sheila, dataCache.getChildrenFor(blaine.getPersonID()).get(0));
        assertEquals(sheila, dataCache.getChildrenFor(betty.getPersonID()).get(0));

        Person ken = dataCache.getPersonById("Ken_Rodham");
        Person mrsRodham = dataCache.getPersonById("Mrs_Rodham");

        assertEquals(blaine, dataCache.getChildrenFor(ken.getPersonID()).get(0));
        assertEquals(blaine, dataCache.getChildrenFor(mrsRodham.getPersonID()).get(0));

        Person mrJones = dataCache.getPersonById("Frank_Jones");
        Person mrsJones = dataCache.getPersonById("Mrs_Jones");

        assertEquals(betty, dataCache.getChildrenFor(mrJones.getPersonID()).get(0));
        assertEquals(betty, dataCache.getChildrenFor(mrsJones.getPersonID()).get(0));

        // Manually checking spouse relationships
        assertEquals(sheila,dataCache.getSpouseFor("Davis_Hyer"));
        assertEquals(blaine,dataCache.getSpouseFor("Betty_White"));
        assertEquals(mrsRodham,dataCache.getSpouseFor("Ken_Rodham"));
        assertEquals(mrJones,dataCache.getSpouseFor("Mrs_Jones"));

        // Manually checking parent relationships
        assertEquals(betty, dataCache.getParents(sheila)[0]);
        assertEquals(blaine, dataCache.getParents(sheila)[1]);

        assertEquals(mrsRodham, dataCache.getParents(blaine)[0]);
        assertEquals(ken, dataCache.getParents(blaine)[1]);

        assertEquals(mrsJones, dataCache.getParents(betty)[0]);
        assertEquals(mrJones, dataCache.getParents(betty)[1]);

    }

    @Test
    public void filterPass() {
// Test Data Notes for Sheila:
//        Total Events: 16
//        Female Events: 10 (Sheila: 5, Betty: 1, Mrs. Jones: 2, Mrs. Rodham: 2)
//        Male Events: 6 (Davis: 1, Blaine: 1, Frank Jones: 2,  Ken Rodham: 2)
//        Maternal Events (includes Sheila and Davis (spouse) events): 11  (Betty and Jones’s)
//        Paternal Events (includes Sheila and Davis (spouse) events): 11 (Blaine and Rodhams)
//        Neither paternal nor maternal (Sheila and Davis (spouse) events): 6

        dataCache.cacheData(people.getData(), events.getData());
        // Show all events
        Settings settings = new Settings(true,true,true,true,true,true, true);
        Set<Event> eventsExpect = new HashSet<>();
        eventsExpect.addAll(dataCache.getEvents().values());

        assertEquals(eventsExpect, dataCache.getEventsWithSettings(settings));
        assertEquals(16, dataCache.getEventsWithSettings(settings).size());

        // Female filter turned on, only male events
        settings.setFemaleEvents(false);
        assertEquals(dataCache.getMaleEvents(), dataCache.getEventsWithSettings(settings));
        assertEquals(6, dataCache.getEventsWithSettings(settings).size());

        // Male only turn on, only female events
        settings.setFemaleEvents(true);
        settings.setMaleEvents(false);
        assertEquals(dataCache.getFemaleEvents(), dataCache.getEventsWithSettings(settings));
        assertEquals(10, dataCache.getEventsWithSettings(settings).size());

        // Female and male filter turned on, no returned events
        settings.setFemaleEvents(false);
        eventsExpect.clear();
        assertEquals(eventsExpect, dataCache.getEventsWithSettings(settings));

        // Father side filter turned on, only see mother's side and Sheila and spouse events
        settings.setMaleEvents(true);
        settings.setFemaleEvents(true);
        settings.setFatherSide(false);
        eventsExpect.addAll(dataCache.getMaternalEvents());
        eventsExpect.addAll(dataCache.getEventsFor("Sheila_Parker"));
        eventsExpect.addAll(dataCache.getEventsFor("Davis_Hyer"));
        assertEquals(eventsExpect, dataCache.getEventsWithSettings(settings));
        assertEquals(11, dataCache.getEventsWithSettings(settings).size());

        // Mother side turned on, father's side only and Sheila and spouse events
        settings.setFatherSide(true);
        settings.setMotherSide(false);
        eventsExpect.clear();
        eventsExpect.addAll(dataCache.getPaternalEvents());
        eventsExpect.addAll(dataCache.getEventsFor("Sheila_Parker"));
        eventsExpect.addAll(dataCache.getEventsFor("Davis_Hyer"));
        assertEquals(eventsExpect,dataCache.getEventsWithSettings(settings));
        assertEquals(11, dataCache.getEventsWithSettings(settings).size());

        // Both mother and father side on, only and Sheila and spouse events
        settings.setFatherSide(false);
        eventsExpect.clear();
        eventsExpect.addAll(dataCache.getEventsFor("Sheila_Parker"));
        eventsExpect.addAll(dataCache.getEventsFor("Davis_Hyer"));
        assertEquals(eventsExpect,dataCache.getEventsWithSettings(settings));
        assertEquals(6, dataCache.getEventsWithSettings(settings).size());

        // Test if mother's side filter and female on, only show male event's on father side not sheila
        // Show 4: (Davis: 1, Blaine: 1, Ken Rodham: 2)
        settings.setFemaleEvents(false);
        settings.setFatherSide(true);
        eventsExpect.clear();
        eventsExpect.addAll(dataCache.getEventsFor("Davis_Hyer"));
        eventsExpect.addAll(dataCache.getEventsFor("Blaine_McGary"));
        eventsExpect.addAll(dataCache.getEventsFor("Ken_Rodham"));
        assertEquals(eventsExpect,dataCache.getEventsWithSettings(settings));
        assertEquals(4, dataCache.getEventsWithSettings(settings).size());

    }

        /*
Sheila "personID": "Sheila_Parker",
                 daughter of :
                "fatherID": "Blaine_McGary", parents = Rodhams
                "motherID": "Betty_White", parents = Jones'

       "personID": "Blaine_McGary", son of
                "fatherID": "Ken_Rodham",
                "motherID": "Mrs_Rodham",

"personID": "Betty_White", daughter of
            "fatherID": "Frank_Jones",
            "motherID": "Mrs_Jones",

         */


//    @Test
//    public void dataCachePersistent() {
//        assertNotNull(dataCache.getAuthtoken());
//        assertNotNull(dataCache.getUserPersonID());
//        assertEquals("sheila", dataCache.getUserPerson().getAssociatedUsername());
//        assertEquals("Sheila", dataCache.getUserPerson().getFirstName());
//        assertEquals("Parker", dataCache.getUserPerson().getLastName());
//        assertEquals("f", dataCache.getUserPerson().getGender());
//    }
}
