package edu.byu.cs240.familymap;

import org.junit.*;
//import org.junit.jupiter.api.*;

import static org.junit.Assert.*;

import request.LoginRequest;
import request.RegisterRequest;
import result.AllEventsResult;
import result.AllPersonsResult;
import result.ClearResult;
import result.LoginResult;
import result.RegisterResult;


// Other Unit Tests:
//        Calculates family relationships (i.e., spouses, parents, children)
//        Filters events according to the current filter settings
//        Chronologically sorts a person’s individual events (birth first, death last, etc.)
//        Correctly searches for people and events (for your Search Activity)


public class ServerProxyTest {
    private ServerProxy serverProxy;
    private RegisterResult registerResult;

    @Before
    public void setUp() {
        serverProxy = new ServerProxy("localhost","8080");

        // Clear database before each test
        serverProxy.clear();

        // Register one user before each test with username and password for login and clear tests
        RegisterRequest registerRequest  = new RegisterRequest("username", "password", "email", "stanton", "anthony", "m");
        registerResult = serverProxy.register(registerRequest);
    }

    @Test
    public void clear() {

        // Login with already setup  user
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = serverProxy.login(loginRequest);

        // Check if login username and personID matches the new registered user
        assertEquals(registerResult.getPersonID(), loginResult.getPersonID());
        assertEquals(registerResult.getUsername(), loginResult.getUsername());
        assertNull(loginResult.getMessage());
        assertNotNull(loginResult.getAuthtoken());

        // Clear database
        ClearResult result = serverProxy.clear();

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("succeeded"));

        // Try logging in again with same credentials
        loginResult = serverProxy.login(loginRequest);

        assertNull(loginResult.getAuthtoken());
        assertNull(loginResult.getPersonID());
        assertTrue(loginResult.getMessage().contains("Error"));
        assertNull(loginResult.getUsername());

    }

    @Test
    public void clear2() {
        // Create login request for user that was registered in setup
        LoginRequest loginRequest = new LoginRequest("username", "password");

        // Clear database 2 times to check works on empty database
        serverProxy.clear();
        ClearResult result = serverProxy.clear();

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("succeeded"));

        // Try logging in again with same credentials
        LoginResult loginResult = serverProxy.login(loginRequest);

        assertNull(loginResult.getAuthtoken());
        assertNull(loginResult.getPersonID());
        assertTrue(loginResult.getMessage().contains("Error"));
        assertNull(loginResult.getUsername());
    }

    @Test
    public void register() {
        RegisterRequest request = new RegisterRequest("stanton227", "123", "email", "stanton", "anthony", "m");
        RegisterResult testResult = serverProxy.register(request);

        assertEquals(testResult.getUsername(),"stanton227");
        assertNotNull(testResult.getAuthtoken());
        assertNotNull(testResult.getPersonID());
        assertNull(testResult.getMessage());
        assertTrue(testResult.isSuccess());

        System.out.println("Authtoken: " + testResult.getAuthtoken());
        System.out.println("PersonID: " + testResult.getPersonID());
        System.out.println(testResult.getUsername());
        System.out.println(testResult.isSuccess());
    }

    @Test
    public void registerFail() {
        RegisterRequest request = new RegisterRequest("stanton227", "123", "email", "stanton", "anthony", "m");
        serverProxy.register(request);

        // Try registering with same username
        request = new RegisterRequest("stanton227", "q23rerw", "email", "stanton", "anthony", "m");
        RegisterResult testResult = serverProxy.register(request);

        assertNull(testResult.getUsername());
        assertNull(testResult.getAuthtoken());
        assertNull(testResult.getPersonID());
        assertTrue(testResult.getMessage().contains("Error"));
        assertFalse(testResult.isSuccess());

        System.out.println(testResult.getMessage());
        System.out.println(testResult.isSuccess());
    }

    @Test
    public void login() {
        LoginRequest request = new LoginRequest("username", "password");
        LoginResult testResult = serverProxy.login(request);

        assertEquals(testResult.getUsername(),"username");
        assertNotNull(testResult.getAuthtoken());
        assertNotNull(testResult.getPersonID());
        assertNull(testResult.getMessage());
        assertTrue(testResult.isSuccess());

        System.out.println("Authtoken: " + testResult.getAuthtoken());
        System.out.println("PersonID: " + testResult.getPersonID());
        System.out.println(testResult.getUsername());
        System.out.println(testResult.isSuccess());
    }

    @Test
    public void loginFail() {
        LoginRequest request = new LoginRequest("username", "pasword");
        LoginResult testResult = serverProxy.login(request);

        assertNull(testResult.getUsername());
        assertNull(testResult.getAuthtoken());
        assertNull(testResult.getPersonID());
        assertTrue(testResult.getMessage().contains("Error"));
        assertFalse(testResult.isSuccess());

        System.out.println(testResult.getMessage());
        System.out.println(testResult.isSuccess());
    }

    @Test
    public void getPeople() {
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = serverProxy.login(loginRequest);

        AllPersonsResult result = serverProxy.getPeople(loginResult.getAuthtoken());

        assertTrue(result.isSuccess());
        assertNull(result.getMessage());
        assertEquals(31,result.getData().length);
        assertNotNull(result.getData()[0].getGender());
        assertEquals("username", result.getData()[0].getAssociatedUsername());
    }

    @Test
    public void getPeopleFail() {
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = serverProxy.login(loginRequest);

        // Try getting people where authtoken is wrong
        AllPersonsResult result = serverProxy.getPeople(loginResult.getAuthtoken() + "tehee");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Error"));
        assertNull(result.getData());
    }

    @Test
    public void getEvents() {
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = serverProxy.login(loginRequest);

        AllEventsResult result = serverProxy.getEvents(loginResult.getAuthtoken());

        assertTrue(result.isSuccess());
        assertNull(result.getMessage());
        // 91 because 3 events per person generated for 4 generations, except the user which is 1 event (birth)
        assertEquals(91,result.getData().length);
        assertNotNull(result.getData()[0].getCity());
        assertEquals("username", result.getData()[0].getUsername());
    }

    @Test
    public void getEventsFail() {
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = serverProxy.login(loginRequest);

        // Try getting people where authtoken is wrong
        AllEventsResult result = serverProxy.getEvents(loginResult.getAuthtoken() + ";)");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Error"));
        assertNull(result.getData());
    }
}