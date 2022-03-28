package edu.byu.cs240.familymap;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Event;
import request.RegisterRequest;
import result.AllEventsResult;
import result.AllPersonsResult;
import result.RegisterResult;

// Make sure server is running and cachedataPass runs first

public class DataCacheTest {
    private ServerProxy serverProxy;
    private DataCache dataCache;
    private RegisterResult registerResult;

    @Before
    public void setUp() {
        serverProxy = new ServerProxy("localhost","8080");
        dataCache = DataCache.getInstance();
        // Clear database before each test
        serverProxy.clear();
        // Register one user before each test with username and password for login and clear tests
        RegisterRequest registerRequest  = new RegisterRequest("username", "password", "email", "stanton", "anthony", "m");
        registerResult = serverProxy.register(registerRequest);
    }

    @Test
    public void cacheDataPass() {
        assertNull(dataCache.getAuthtoken());
        assertNull(dataCache.getUserPersonID());
        dataCache.setAuthtoken(registerResult.getAuthtoken());
        dataCache.setUserPersonID(registerResult.getPersonID());

        AllPersonsResult people = serverProxy.getPeople(dataCache.getAuthtoken());
        AllEventsResult events = serverProxy.getEvents(dataCache.getAuthtoken());

        dataCache.cacheData(people.getData(), events.getData());

        assertEquals(registerResult.getPersonID(),dataCache.getUserPerson().getPersonID());

        // Checks if events are sorted chronologically for each person
        for (List<Event> eventList : dataCache.getAllPersonEvents().values()) {
            for (int i = 0; i < eventList.size() - 1; ++i) {
                assertTrue(eventList.get(i).getYear() < eventList.get(i + 1).getYear());
            }
        }
    }

    @Test
    public void dataCache() {
        assertNotNull(dataCache.getAuthtoken());
        assertNotNull(dataCache.getUserPersonID());
        assertEquals("username", dataCache.getUserPerson().getAssociatedUsername());
        assertEquals("stanton", dataCache.getUserPerson().getFirstName());
        assertEquals("anthony", dataCache.getUserPerson().getLastName());
        assertEquals("m", dataCache.getUserPerson().getGender());
    }
}
