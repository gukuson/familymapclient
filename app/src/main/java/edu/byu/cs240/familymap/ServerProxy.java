package edu.byu.cs240.familymap;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import request.LoginRequest;
import request.RegisterRequest;
import result.*;



// Do all HTTP communication details
public class ServerProxy {
    private final String serverHost;
    private final String serverPort;
    private final Gson gson;

    public ServerProxy(String serverHost, String serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        gson = new Gson();
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            Reader respBody = postRequestTry("/user/register", "Registered a new user successfully" , request, true);

               // Parse response body from Json to result
            return gson.fromJson(respBody,RegisterResult.class);
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return new RegisterResult(null, null, null, false, "Error during serverproxy register check server is running");
        }
    }

    public LoginResult login(LoginRequest request) {
        try {
            Reader respBody = postRequestTry("/user/login", "Logged in successfully" , request, true);

            // Parse response body from Json to result
            return gson.fromJson(respBody,LoginResult.class);
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return new LoginResult(null, null, null, false, "Error during serverproxy login, check server is running");
        }
    }

    public ClearResult clear() {
        try {
            Reader respBody = postRequestTry("/clear", "Cleared successfuly", null, false);

            // Parse response body from Json to result
            return gson.fromJson(respBody,ClearResult.class);
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return new ClearResult(false, "Error during serverproxy clear");
        }
    }

    public AllPersonsResult getPeople(String authtoken) {
        try {
            Reader respBody = getRequestTry("/person", "Retrieved all persons for user", authtoken);

            // Parse response body from Json to result
            return gson.fromJson(respBody,AllPersonsResult.class);
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return new AllPersonsResult(null, false, "Error during serverproxy getPeople for a user");
        }
    }

    public AllEventsResult getEvents(String authtoken) {
        try {
            Reader respBody = getRequestTry("/event", "Retrieved all events for user", authtoken);

            // Parse response body from Json to result
            return gson.fromJson(respBody,AllEventsResult.class);
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return new AllEventsResult(null, false, "Error during serverproxy getEvents for a user");
        }
    }

    private URL createUrl(String apiPath) throws MalformedURLException {
        return new URL("http://" + serverHost + ":" + serverPort + apiPath);
    }
    /*
		The readString method shows how to read a String from an InputStream.
	*/
//    private static String readString(InputStream is) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        InputStreamReader sr = new InputStreamReader(is);
//        char[] buf = new char[1024];
//        int len;
//        while ((len = sr.read(buf)) > 0) {
//            sb.append(buf, 0, len);
//        }
//        return sb.toString();
//    }

    private void encode(Object request, OutputStream os) throws IOException {
//        Encode the request object to JSON string then into the request body, close output stream
        Writer reqBody = new OutputStreamWriter(os);
        gson.toJson(request, reqBody);
        reqBody.flush();
        // Close the request body output stream, indicating that the
        // request is complete
        reqBody.close();
    }

    // If doesn't have request body, can pass null into request parameter
    private Reader postRequestTry(String apiPath, String successMessage, Object request, boolean hasReqBody) throws IOException {
        // Create a URL indicating where the server is running, and which
        // web API operation we want to call from apiPath parameter

        URL url = createUrl(apiPath);

        // Start constructing our HTTP request
        HttpURLConnection http = (HttpURLConnection)url.openConnection();

        // Specify that we are sending an HTTP POST request
        http.setRequestMethod("POST");

        // Indicate that this request will or won't contain an HTTP request body
        http.setDoOutput(hasReqBody);	// There is a request body if true

        // Specify that we would like to receive the server's response in JSON
        // format by putting an HTTP "Accept" header on the request
        http.addRequestProperty("Accept", "application/json");

        // Connect to the server and send the HTTP request
        http.connect();

        // If has a request body:
        if (hasReqBody) {
            // Get the output stream containing the HTTP request body
            OutputStream reqBody = http.getOutputStream();

            // Encode the request object to JSON string then into the request body, close output stream
            encode(request,reqBody);
        }

        // By the time we get here, the HTTP response has been received from the server.
        // Check to make sure that the HTTP response from the server contains a 200
        Reader respBody;
        if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Get response body for success
            respBody = new InputStreamReader(http.getInputStream());
            System.out.println(successMessage);
        }
        else {
            // The HTTP response status code indicates an error
            // occurred, so print out the message from the HTTP response
            System.out.println("ERROR: " + http.getResponseMessage());

            // Get the error stream containing the HTTP response body
            respBody = new InputStreamReader(http.getErrorStream());
        }
        return respBody;
    }

    private Reader getRequestTry(String apiPath, String successMessage, String authtoken) throws IOException {
        // Create a URL indicating where the server is running, and which
        // web API operation we want to call from apiPath parameter

        URL url = createUrl(apiPath);

        // Start constructing our HTTP request
        HttpURLConnection http = (HttpURLConnection)url.openConnection();

        // Specify that we are sending an HTTP POST request
        http.setRequestMethod("GET");

        // Indicate that this request will or won't contain an HTTP request body
        http.setDoOutput(false);	// There is a request body if true

        // Specify that we would like to receive the server's response in JSON
        // format by putting an HTTP "Accept" header on the request
        http.addRequestProperty("Accept", "application/json");

        // Add an auth token to the request in the HTTP "Authorization" header
        http.addRequestProperty("Authorization", authtoken);

        // Connect to the server and send the HTTP request
        http.connect();

        // By the time we get here, the HTTP response has been received from the server.
        // Check to make sure that the HTTP response from the server contains a 200
        Reader respBody;
        if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Get response body for success
            respBody = new InputStreamReader(http.getInputStream());
            System.out.println(successMessage);
        }
        else {
            // The HTTP response status code indicates an error
            // occurred, so print out the message from the HTTP response
            System.out.println("ERROR: " + http.getResponseMessage());

            // Get the error stream containing the HTTP response body
            respBody = new InputStreamReader(http.getErrorStream());
        }
        return respBody;
    }

}
