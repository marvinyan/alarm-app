package com.example.myalarm;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnClickListener {

    final Context context = this;
    private ScrollView sv;
    //Temporary Log
    private TextView log;
    //Socket connection
    private String serverIP, serverPassword;
    private Socket socket;
    //Streams to send and receive commands
    private PrintWriter out;
    private BufferedReader in;

    //Status Buttons, three buttons across the top left corner
    public Button[] statusButtons;
    //Numeric Keypad 0-9
    public Button[] numericKeypad;
    //Panic Buttons
    public Button[] sideButtons;
    //Display buttons for Zone List and System Status Display
    public Button[] otherStatusButtons;
    // Setup button
    public Button setupButton;
    //Log Background
    public Button logBackground;
    //Log / Zones List switch
    public Button logSwitch;
    //Center Button to Hide or display quick access buttons
    public Button numsSwitch;
    //Center Background used to display active alarms
    public Button centerBackground;

    //List of commands
    String[] listKeypadCommands;
    String[] listSideBtnsCommands;

    //Asynctask running in the background thread
    private OpenSocketTask openSocketTask;

    //Message received from the server
    private TPIMessage tpiMsg;

    private boolean appInForeground;

    private boolean logOrZones;
    private boolean numsOrSideBtns;

    //Debug
    private String tag;

    //Button Animation
    public Animation scaleAnimation;
    public Animation translateAnimation;
    public Animation rotateAnimation;
    public Animation alphaAnimation;

    //Text to Speech
    public TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Hide Action Bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tag = "Testing";
        Log.d(tag, "in the onCreate Method");

        //Buttons Animation
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        translateAnimation = AnimationUtils.loadAnimation(this, R.anim.translate);
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);

        //Text to Speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }

                Log.d(tag, "in the TTS onInit method");
            }
        });


        appInForeground = true;

        //App Log
        sv = (ScrollView) findViewById(R.id.scroll);
        log = (TextView) findViewById(R.id.log);
        log.setTextSize(8);
        log.setTextColor(Color.WHITE);
        logBackground = (Button) findViewById(R.id.logBackground);
        logSwitch = (Button) findViewById(R.id.switchBtn);
        logSwitch.setOnClickListener(this);
        logOrZones = true;

        //This button hides and display numeric KP and Side Buttons
        numsSwitch = (Button) findViewById(R.id.centerBtn);
        numsSwitch.setOnClickListener(this);
        numsOrSideBtns = true;

        //This image changes when an active alarm is in progress
        centerBackground = (Button) findViewById(R.id.centerBtnBackground);

        //Initiation of status buttons
        statusButtons = new Button[4];
        int statusIdIndex = R.id.ready;
        for (int i = 0; i < statusButtons.length; i++) {
            statusButtons[i] = (Button) findViewById(statusIdIndex);
            statusIdIndex++;
        }


        //Initiation of numeric keypad, adding a listener to each button
        numericKeypad = new Button[12];
        int numKPIndex = R.id.key1;
        for (int i = 0; i < numericKeypad.length; i++) {
            numericKeypad[i] = (Button) findViewById(numKPIndex);
            numericKeypad[i].setOnClickListener(this);
            numKPIndex++;
        }

        //Initiation of side buttons, adding a listener to each one
        sideButtons = new Button[4];
        int sideIdIndex = R.id.armStay;
        for (int i = 0; i < sideButtons.length; i++) {
            sideButtons[i] = (Button) findViewById(sideIdIndex);
            sideButtons[i].setOnClickListener(this);
            sideButtons[i].setVisibility(View.INVISIBLE);
            sideIdIndex++;
        }

        //Initiation of buttons that display status of Zones and System
        otherStatusButtons = new Button[9];
        int otherStatusIdIndex = R.id.textDisplay;
        for (int i = 0; i < otherStatusButtons.length; i++) {
            if (i == 0) {
                otherStatusButtons[i] = (Button) findViewById(otherStatusIdIndex);
                otherStatusButtons[i].setClickable(false);
                otherStatusIdIndex++;
            } else {
                otherStatusButtons[i] = (Button) findViewById(otherStatusIdIndex);
                otherStatusButtons[i].setClickable(false);
                otherStatusButtons[i].setVisibility(View.INVISIBLE);
                otherStatusIdIndex++;
            }
        }

        // Initiation of setup button
        setupButton = (Button) findViewById(R.id.setup);
        setupButton.setOnClickListener(this);

        //Open Socket in background thread, loading saved IP address and server password
        openSocketTask = new OpenSocketTask();
        loadSavedPreferences();
        openSocketTask.execute();
        Log.d(tag, "Task starts - onCreate");

        //Process message from server
        tpiMsg = new TPIMessage(statusButtons, otherStatusButtons, centerBackground, tts);

        //Commands for numeric keypad
        listKeypadCommands = new String[]{"0700C7", "0701C8", "0702C9", "0703CA", "0704CB",
                                          "0705CC", "0706CD", "0707CE", "0708CF", "0709D0", "070*C1", "070#BA"};
        //Commands for Arm Stay, Arm Away, Fire and Police Buttons
        listSideBtnsCommands = new String[]{"0311C5", "0301C4", "0601C7", "0603C9"};


    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.key0:
                sendNumCommand(0);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key1:
                sendNumCommand(1);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key2:
                sendNumCommand(2);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key3:
                sendNumCommand(3);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key4:
                sendNumCommand(4);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key5:
                sendNumCommand(5);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key6:
                sendNumCommand(6);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key7:
                sendNumCommand(7);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key8:
                sendNumCommand(8);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.key9:
                sendNumCommand(9);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.keystar:
                sendNumCommand(10);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.keypound:
                sendNumCommand(11);
                v.startAnimation(scaleAnimation);
                break;
            case R.id.armStay:
                sendSideBtnCommand(0);
                v.startAnimation(scaleAnimation);
                hideNumsOrSideButtons();
                break;
            case R.id.armAway:
                sendSideBtnCommand(1);
                v.startAnimation(scaleAnimation);
                hideNumsOrSideButtons();
                break;
            case R.id.fire:
                sendSideBtnCommand(2);
                v.startAnimation(scaleAnimation);
                hideNumsOrSideButtons();
                break;
            case R.id.police:
                sendSideBtnCommand(3);
                v.startAnimation(scaleAnimation);
                hideNumsOrSideButtons();
                break;
            case R.id.setup:
                openDialog();
                v.startAnimation(rotateAnimation);
                break;
            case R.id.switchBtn:
                hideLogOrZones();
                v.startAnimation(scaleAnimation);
                break;
            case R.id.centerBtn:
                hideNumsOrSideButtons();
                v.startAnimation(scaleAnimation);
                break;
        }
    }

    //Socket Connection and Listener. Will run in the background
    private class OpenSocketTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... task) {

            publishProgress("Attempting to connect to device ...");
            try {
                //Socket connection
                socket = new Socket(InetAddress.getByName(serverIP), 4025);
                out = new PrintWriter(socket.getOutputStream(), true);
                publishProgress("Connection established to " + serverIP);

                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    publishProgress("Server >> " + in.readLine() + ". ");

                    String convertedPassword = "005" + serverPassword + getChecksum("005" + serverPassword);
                    publishProgress("Login << " + convertedPassword);

                    //Send password to complete socket connection
                    out.println(convertedPassword + "\r");
                    out.flush();

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    publishProgress("Server >> " + in.readLine() + ". ");
                } catch (IOException e) {
                    try {
                        publishProgress("Could not access input stream.\n" +
                                        "Another user may be using the device.\n" +
                                        "Retrying in 5 seconds.");
                        Thread.sleep(5000);
                        selfRestart();
                    } catch (InterruptedException f) {
                        f.printStackTrace();
                    }
                }

                //The input stream must be in a loop to keep listening for any incoming messages
                String serverMessage = "";

                while ((serverMessage = in.readLine()) != null) {
                    publishProgress(serverMessage);
                }

            } catch (UnknownHostException e) {
                publishProgress("Invalid IP address.\n");
            } catch (IOException e) {
                try {
                    publishProgress("Connection timed out.\nRetrying in 5 seconds");
                    Thread.sleep(5000);
                    selfRestart();
                } catch (InterruptedException f) {
                    f.printStackTrace();
                }
            }

            return null;
        }

        protected void onProgressUpdate(String... response) {
            logSwitch.startAnimation(rotateAnimation);
            //Check if response[0] is a message from the server
            if (response[0].matches("[0-9].*")) {
                updateLog("Server>> " + response[0] + ". ");
                tpiMsg.processMessage(response[0]);
                updateLog(tpiMsg.getEventMessage() + "\n");
                if (tpiMsg.getEventMessage().equals("Login Successful")) {
                    Toast.makeText(getApplicationContext(), "Login Succesful. Loading System Status",
                                   Toast.LENGTH_LONG).show();

                    //Request System Status, after login process is completed
                    out.println("00191\r");
                    out.flush();

                    //After a login, hide Log and display zones list
                    hideLogOrZones();
                    hideNumsOrSideButtons();
                } else if (tpiMsg.getEventMessage().equals("Login Failed. Check Your Password")) {
                    Toast.makeText(getApplicationContext(), "Login Failed. Check Your Password",
                                   Toast.LENGTH_LONG).show();
                }

            } else {
                updateLog(response[0] + "\n");
            }
        }

        //selfRestart code will only execute if the application is in the foreground
        //preventing self connections after the onPause, onStop or onDestroy methods have been
        //called
        public void selfRestart() {
            if (appInForeground) {
                publishProgress("Restarting task.");
                openSocketTask.cancel(true);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                openSocketTask = new OpenSocketTask();
                loadSavedPreferences();
                openSocketTask.execute();
                Log.d(tag, "Task starts - selfRestart");
            }
        }
    }

    //When the application is resumed, the task running in the background
    //will start again
    @Override
    protected void onResume() {
        super.onResume();
        connectionContinue();
    }

    //When the application is restarted, the task running in the background
    //will start again
    @Override
    protected void onRestart() {
        super.onRestart();
        connectionContinue();
    }

    //When application is not visible, the socket if connected will close
    //and the task running in the background will be terminated if running
    @Override
    protected void onStop() {
        connectionCleanup();
        super.onStop();
    }

    //When application has been destroyed, the socket if connected will close
    //and the task running in the background will be terminated if running
    @Override
    protected void onDestroy() {
        connectionCleanup();
        super.onDestroy();
    }

    // Closes socket and ends task.
    private void connectionCleanup() {
        //Shutdown Text to Speech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(tag, "TTS Stopped and Shutdown");
        }

        appInForeground = false;

        // Get life cycle process name
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[3].getMethodName();

        try {
            //Closing connection to socket
            socket.close();
            Log.d(tag, "Socket Closed - " + methodName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        //Terminating task running in the background thread
        if (openSocketTask.getStatus() == AsyncTask.Status.RUNNING ||
            openSocketTask.getStatus() == AsyncTask.Status.PENDING) {
            openSocketTask.cancel(true);
            Log.d(tag, "Task killed - " + methodName);
        }

        logOrZones = true;
        numsOrSideBtns = true;
        otherStatusButtons[0].setBackgroundResource(R.drawable.connecting);
    }

    // Restarts task if task was terminated.
    private void connectionContinue() {
        appInForeground = true;
        otherStatusButtons[0].setBackgroundResource(R.drawable.connecting);

        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[3].getMethodName();

        if (openSocketTask.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            openSocketTask = new OpenSocketTask();
            loadSavedPreferences();
            openSocketTask.execute();
            Log.d(tag, "Task starts - " + methodName);
        }
    }

    public void sendNumCommand(int commandNumber) {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(listKeypadCommands[commandNumber] + "\r");
            out.flush();
            updateLog("Sent << " + listKeypadCommands[0] + ". Numeric Keypad Command\n");
        }
        // If keypad is pressed when not connected to socket (although the NPE may be thrown instead).
        catch (IOException e) {
            updateLog("Device may not be connected yet.\n");
        }
        // If app closed when not connected to socket
        catch (NullPointerException e) {
            updateLog("Not connected to device yet.\n");
        }
    }

    public void sendSideBtnCommand(int commandNumber) {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(listSideBtnsCommands[commandNumber] + "\r");
            out.flush();
            updateLog("Sent << " + listSideBtnsCommands[commandNumber] + ". Quick Arm/Panic Buttons\n");
        } catch (IOException e) {
            updateLog("Device may not be connected yet.\n");
        } catch (NullPointerException e) {
            updateLog("Not connected to device yet.\n");
        }
    }

    public void updateLog(String msg) {
        log.append(msg);
        // Auto-scroll
        // https://groups.google.com/d/msg/android-developers/Gjb2Lfgh13E/3Ea75l6OCvAJ
        sv.post(new Runnable() {
            public void run() {
                sv.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ipAddress = sharedPreferences.getString("ip", ""); // default is ""
        String password = sharedPreferences.getString("pw", "");
        if (!ipAddress.equals("") && !password.equals("")) {
            //To initiate connection to socket and keep the listener in the background
            serverIP = ipAddress;
            serverPassword = password;
        } else {
            // 74.101.39.11 005user54
            Toast.makeText(getApplicationContext(), "No saved IP address and password detected.\nBeginning setup.",
                           Toast.LENGTH_LONG).show();
            openDialog();
        }
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void openDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput1 = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput1);
        final EditText userInput2 = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput2);

        userInput1.setText(serverIP);
        userInput2.setText(serverPassword);

        // set dialog message
        alertDialogBuilder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String u1 = userInput1.getText().toString().trim();
                        String u2 = userInput2.getText().toString().trim();

                        if (u1.isEmpty() || u2.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Fields cannot be empty.",
                                           Toast.LENGTH_LONG).show();
                            dialog.cancel(); // TODO: keep dialog box open and mark EditText with red border
                            openDialog();
                        } else {
                            savePreferences("ip", u1);
                            savePreferences("pw", u2);
                            serverIP = u1;
                            serverPassword = u2;
                            openSocketTask.selfRestart();
                        }
                    }
                })
                .setNegativeButton("Cancel",
                                   new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int id) {
                                           dialog.cancel();
                                       }
                                   }
                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    // Determines the checksum of a given string and returns it
    private String getChecksum(String s) {
        int decSum = 0;
        for (int i = 0; i < s.length(); i++) {
            decSum += (int) s.charAt(i);
        }
        // If checksum contains a letter, that letter must be upper case
        // Checksums with more than 2 chars are truncated to 2 characters
        //
        // TODO: verify checksum process with international letters?
        // http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug
        String hexSum = Integer.toHexString(decSum).toUpperCase(Locale.ENGLISH);
        return hexSum.substring(hexSum.length() - 2, hexSum.length());
    }

    //Hide App log and display Zones List
    private void hideLogOrZones() {

        if (logOrZones) {
            logOrZones = false;
            log.setVisibility(View.INVISIBLE);
            logBackground.setVisibility(View.INVISIBLE);
            for (int i = 1; i < otherStatusButtons.length; i++) {
                otherStatusButtons[i].setVisibility(View.VISIBLE);
            }
        } else {
            logOrZones = true;
            log.setVisibility(View.VISIBLE);
            logBackground.setVisibility(View.VISIBLE);
            for (int i = 1; i < otherStatusButtons.length; i++) {
                otherStatusButtons[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    //Hide App log and display Zones List
    private void hideNumsOrSideButtons() {
        if (numsOrSideBtns) {
            numsOrSideBtns = false;
            for (int i = 0; i < sideButtons.length; i++) {
                sideButtons[i].setVisibility(View.INVISIBLE);
            }

            for (int i = 0; i < numericKeypad.length; i++) {
                numericKeypad[i].setVisibility(View.VISIBLE);
            }

            numsSwitch.setBackgroundResource(R.drawable.centerbuttonwhite);
        } else {
            numsOrSideBtns = true;
            for (int i = 0; i < numericKeypad.length; i++) {
                numericKeypad[i].setVisibility(View.INVISIBLE);
            }

            for (int i = 0; i < sideButtons.length; i++) {
                sideButtons[i].setVisibility(View.VISIBLE);
            }

            numsSwitch.setBackgroundResource(R.drawable.centerbuttonblue);
        }
    }
}
