package com.example.myalarm;

import android.widget.Button;

//AcessCodeEvent - Used when an invalid code has been entered
public class AccessCodeEvent {
    //Holds the button used to display the System Status
    private Button txtDisplay;
    //Holds the first three characters parsed into integer of the message received by the server
    private int code;
    //Message from the server in a readable format
    private String eventMessage;

    //Default Constructor
    //Accepts button that display the system status
    public AccessCodeEvent(Button textDisplay) {
        txtDisplay = textDisplay;
    }

    //Process message from server
    public void processMessage(int responseCode) {
        code = responseCode;
        processCode(code);
    }

    //Check if the code received by the constructor does match the code
    //indicating that an invalid code has been used
    public void processCode(int code) {
        //Message starting with 670, an invalid code was entered
        if (code == 670) {
            invalidAccessCode();
        } else {
            //If the code received in the message from the server does not apply
            //to this event, a default message containing the code is set
            eventMessage = "No event for Code# " + code;
        }
    }

    //Changes the system status text to Invalid Code
    public void invalidAccessCode() {
        txtDisplay.setBackgroundResource(R.drawable.invalidcode);
        eventMessage = ("Invalid Access Code");
    }

    //Returns a String containing the event in a readable format
    public String getEventMessage() {
        return eventMessage;
    }
}
