package edu.byu.cs240.familymap;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import request.LoginRequest;
import result.AllEventsResult;
import result.AllPersonsResult;
import result.LoginResult;

public class LoginFragment extends Fragment {

    private Listener listener;
    private EditText serverHostField,serverPortField, usernameField, passwordField, firstnameField, lastnameField, emailField;
    private RadioButton femaleButton, maleButton;
    private Button loginButton, registerButton;
    private static final String LOG_TAG = "LoginFragment";
    private static final String LOGIN_RESULT_KEY = "LoginResultKey";
    //  create a textWatcher member
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // check Fields For Empty Values
            checkFieldsForEmptyValues();
        }
    };

    void checkFieldsForEmptyValues(){
        String host = serverHostField.getText().toString();
        String port = serverPortField.getText().toString();
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String firstname = firstnameField.getText().toString();
        String lastname = lastnameField.getText().toString();
        String email = emailField.getText().toString();

        if(host.equals("")|| port.equals("") || username.equals("") || password.equals("")){
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
        } else {
            loginButton.setEnabled(true);
            // Enable or disable register button
            if(firstname.equals("") || lastname.equals("") || email.equals("") || (!femaleButton.isChecked() && !maleButton.isChecked())){
                registerButton.setEnabled(false);
            } else {
                registerButton.setEnabled(true);
            }
        }

    }

    public interface Listener {
        void notifyLogin();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    // Background thread classes
    private static class LoginTask implements Runnable {

        private final Handler messageHandler;
        private LoginRequest loginRequest;
        private String serverHost;
        private String serverPort;
        private DataCache dataCache = DataCache.getInstance();

        public LoginTask(Handler messageHandler, LoginRequest request, String host, String port) {
            this.messageHandler = messageHandler;
            this.loginRequest = request;
            this.serverHost = host;
            this.serverPort = port;
        }

        @Override
        public void run() {
            Gson gson = new Gson();
            ServerProxy serverProxy = new ServerProxy(serverHost, serverPort);

            LoginResult result = serverProxy.login(loginRequest);

            if (result.isSuccess()) {
                dataCache.setAuthtoken(result.getAuthtoken());
                dataCache.setUserPersonID(result.getPersonID());

                GetDataTask task = new GetDataTask(serverProxy);
                String loginOutput = task.fetchData();
//                ExecutorService executor = Executors.newSingleThreadExecutor();
//                executor.submit(task);

                sendMessage(loginOutput);
            }else {
                dataCache.setAuthtoken(null);
                dataCache.setUserPersonID(null);
                Log.e(LOG_TAG, result.getMessage());
                sendMessage(null);
            }

//            sendMessage(gson.toJson(result));

//              if success get Data

        }

        private void sendMessage(String result) {
            Message message = Message.obtain();

            Bundle messageBundle = new Bundle();
            messageBundle.putString(LOGIN_RESULT_KEY, result);
            message.setData(messageBundle);

            messageHandler.sendMessage(message);
        }
    }

    private static class RegisterTask implements Runnable {

        @Override
        public void run() {

        }
    }

//    private static class GetDataTask implements Runnable {
//        private DataCache dataCache = DataCache.getInstance();
//
//        @Override
//        public void run() {
//
//        }
//    }

    private static class GetDataTask {
        private DataCache dataCache = DataCache.getInstance();
        private ServerProxy serverProxy;

        public GetDataTask(ServerProxy serverProxy) {
            this.serverProxy = serverProxy;
        }

        public String fetchData() {
            AllEventsResult events = serverProxy.getEvents(dataCache.getAuthtoken());
            AllPersonsResult people = serverProxy.getPeople(dataCache.getAuthtoken());

            dataCache.cacheData(people.getData(), events.getData());
            return dataCache.getUserPerson().getFirstName() + " " + dataCache.getUserPerson().getLastName();
        }

    }


//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Set buttons to private members
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        femaleButton = view.findViewById(R.id.femaleButton);
        maleButton = view.findViewById(R.id.maleButton);

        // Set text fields to private members
        serverHostField = (EditText) view.findViewById(R.id.serverHostText);
        serverPortField = (EditText) view.findViewById(R.id.serverPortText);
        usernameField = (EditText) view.findViewById(R.id.usernameText);
        passwordField = (EditText) view.findViewById(R.id.passwordText);
        firstnameField = (EditText) view.findViewById(R.id.firstnameText);
        lastnameField = (EditText) view.findViewById(R.id.lastnameText);
        emailField = (EditText) view.findViewById(R.id.emailAddressText);

        // Set listeners
        serverHostField.addTextChangedListener(mTextWatcher);
        serverPortField.addTextChangedListener(mTextWatcher);
        usernameField.addTextChangedListener(mTextWatcher);
        passwordField.addTextChangedListener(mTextWatcher);
        firstnameField.addTextChangedListener(mTextWatcher);
        lastnameField.addTextChangedListener(mTextWatcher);
        emailField.addTextChangedListener(mTextWatcher);

//        Run check field for empty values for onclick
//        femaleButton.setOnClickListener();
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.genders);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkFieldsForEmptyValues();
            }
        });

        // run once to disable if empty
        checkFieldsForEmptyValues();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Notifies mainactivity sign in button was clicked
                if(listener != null) {
                    // Create login background task
                        // Set up a handler that will process messages from the task and make updates on the UI thread
                        Handler uiThreadMessageHandler = new Handler() {
                            @Override
                            public void handleMessage(Message message) {
                                Bundle bundle = message.getData();

                                Gson gson = new Gson();
                                String loginOutput = bundle.getString(LOGIN_RESULT_KEY);
//                                LoginResult loginResult = gson.fromJson(loginResultString, LoginResult.class);
                                if (loginOutput != null) {
                                    Toast.makeText(getContext(),"Welcome " + loginOutput,Toast.LENGTH_LONG).show();
//                                    listener.notifyLogin();
                                }else {
                                    Toast.makeText(getContext(),"Login Failed: Check inputs",Toast.LENGTH_LONG).show();
                                }
                            }
                        };

                        // Create and execute the login task on a separate thread
                        String username = usernameField.getText().toString();
                        String password = passwordField.getText().toString();
                        String host = serverHostField.getText().toString();
                        String port = serverPortField.getText().toString();

                        LoginRequest request = new LoginRequest(username, password);

                        LoginTask task = new LoginTask(uiThreadMessageHandler, request, host, port);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.submit(task);

                }else {
                    System.out.println("Listener is null for login button");
                }
            }
        });
        return view;
    }


}