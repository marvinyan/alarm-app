package com.example.myalarm;

import android.widget.Button;

public class StatusLEDsEvent {
    private Button[] statusLEDs;
    private int code;
    private char[] TPIMessage;
    private String eventMessage;

    //Holds images to change system status LEDs
    Integer[] statusLEDsImages = {
            R.drawable.readyon,
            R.drawable.readyoff,
            R.drawable.armedon,
            R.drawable.armedoff};


    //Default Constructor
    //Accepts the buttons that represent the general status of the system
    public StatusLEDsEvent(Button[] statusButtons) {
        statusLEDs = statusButtons;
    }

    //Process message from server
    public void processMessage(int responseCode, char[] message) {
        code = responseCode;
        TPIMessage = message;
        processCode(code);
    }

    //Check if the code received by the constructor does match the code
    //indicating that any of the two system status LEDs has changed
    public void processCode(int code) {
        //Message starting with 510, system status LED has changed
        if (code == 510) {
            changeLEDstatus();
        } else {
            //If the code received in the message from the server does not apply
            //to this event, a default message containing the code is set
            eventMessage = "No event for Code# " + code;
        }
    }

    //Determine the state of two lights representing the status of the system in general
    public void changeLEDstatus() {
        switch (TPIMessage[4]) {
            case '0':
                //Check Mark OFF
                statusLEDs[0].setBackgroundResource(statusLEDsImages[1]);
                //Lock Symbol OFF
                statusLEDs[1].setBackgroundResource(statusLEDsImages[3]);
                eventMessage = "Green OFF, Red OFF";
                break;
            case '1':
                //Check Mark ON
                statusLEDs[0].setBackgroundResource(statusLEDsImages[0]);
                //Lock Symbol OFF
                statusLEDs[1].setBackgroundResource(statusLEDsImages[3]);
                eventMessage = "Green ON, Red OFF";
                break;
            case '2':
                //Check Mark OFF
                statusLEDs[0].setBackgroundResource(statusLEDsImages[1]);
                //Lock Symbol ON
                statusLEDs[1].setBackgroundResource(statusLEDsImages[2]);
                eventMessage = "Green OFF, Red ON";
                break;
            case '3':
                //Check Mark ON
                statusLEDs[0].setBackgroundResource(statusLEDsImages[0]);
                //Lock Symbol ON
                statusLEDs[1].setBackgroundResource(statusLEDsImages[2]);
                eventMessage = "Green ON, Red ON";
                break;
            case '5':
                //Check Mark ON
                statusLEDs[0].setBackgroundResource(statusLEDsImages[0]);
                //Lock Symbol OFF
                statusLEDs[1].setBackgroundResource(statusLEDsImages[3]);
                eventMessage = "Green ON, Red OFF";
                break;
            case 'A':
                //Check Mark OFF
                statusLEDs[0].setBackgroundResource(statusLEDsImages[1]);
                //Lock Symbol ON
                statusLEDs[1].setBackgroundResource(statusLEDsImages[2]);
                eventMessage = "Green OFF, Red ON";
                break;
            case 'B':
                //Check Mark ON
                statusLEDs[0].setBackgroundResource(statusLEDsImages[0]);
                //Lock Symbol ON
                statusLEDs[1].setBackgroundResource(statusLEDsImages[2]);
                eventMessage = "Green ON, Red ON";
                break;
            default:
                eventMessage = "No event for Code# " + code;
        }
    }

    //Returns a String containing the event in a readable format
    public String getEventMessage() {
        return eventMessage;
    }

}
