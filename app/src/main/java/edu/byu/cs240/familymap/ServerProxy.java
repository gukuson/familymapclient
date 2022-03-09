package edu.byu.cs240.familymap;

import request.LoginRequest;
import request.RegisterRequest;
import result.AllEventsResult;
import result.AllPersonsResult;
import result.LoginResult;
import result.RegisterResult;


// Do all HTTP communication details
public class ServerProxy {

    public static void main(String[] args) {
        String serverHost = args[0];
        String serverPort = args[1];
    }


    public LoginResult login(LoginRequest request) {
        return null;
    }
    public RegisterResult register(RegisterRequest request) {
        return null;
    }
    public AllPersonsResult getPeople(String authtoken) {
            return null;
    }
    public AllEventsResult getEvents(String authtoken) {
        return null;
    }

}
