package com.example.myalarm;

import android.speech.tts.TextToSpeech;
import android.widget.Button;

//TPIMessage - Sorts out the message received by the server and process the message
//based on the first three characters of the string (3-digit number)
public class TPIMessage {
    //Buttons representing System Status Display and Zones List
    private Button[] zonesAndSystemBtns;
    //Buttons representing general System Status
    private Button[] statusBtns;
    //Holds the message received by the server in an array of characters
    private char[] originalTPIMessage;
    //Holds the first three characters parsed into integer of the message received by the server
    private int code;
    //Creates a new event for any of the messages regarding a zone
    private ZoneEvent zoneEvent;
    //Creates a new event for the Quick Arm and Panic Buttons
    private PanicKeysEvent panicKeys;
    //Creates a new event for the button that represents a trouble in the system
    private TroubleEvent troubleEvent;
    //Creates a new event when the wrong code has been entered
    private AccessCodeEvent accessCodeEvent;
    //Creates a new event when the System Status changes
    private SystemStatusEvent systemStatusEvent;
    //Creates a new event when the status LEDs indicator change
    private StatusLEDsEvent statusLEDsEvent;
    //Creates a new event when the server acknowledges a command and when login has been requested
    private OtherEvents otherEvents;
    //Message from the server in a readable format
    private String eventMessage;

    //Default Constructor
    //Accepts all buttons on the UI and message received by the server
    public TPIMessage(Button[] statusButtons, Button[] zonesButtons,
                      Button centerBckgrnd, TextToSpeech tts) {
        statusBtns = statusButtons;
        zonesAndSystemBtns = zonesButtons;
        otherEvents = new OtherEvents();
        statusLEDsEvent = new StatusLEDsEvent(statusBtns);
        zoneEvent = new ZoneEvent(zonesAndSystemBtns, tts);
        panicKeys = new PanicKeysEvent();
        systemStatusEvent = new SystemStatusEvent(zonesAndSystemBtns[0], centerBckgrnd, tts);
        accessCodeEvent = new AccessCodeEvent(zonesAndSystemBtns[0], tts);
        troubleEvent = new TroubleEvent(statusBtns[2]);
    }

    //Constructor - For debugging only
    //Accepts the message received by the server
    public TPIMessage(String TPIMessage) {
        originalTPIMessage = TPIMessage.toCharArray();
        code = getCode(originalTPIMessage);
        processCode();
    }

    //Process message from server
    public void processMessage(String TPIMessage) {
        originalTPIMessage = TPIMessage.toCharArray();
        code = getCode(originalTPIMessage);
        processCode();
    }

    //Parse the first three characters of the message received by the server into an integer
    //That integer represents the code
    public int getCode(char[] TPIMessage) {
        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = TPIMessage[i];
        }
        return Integer.parseInt(new String(temp));
    }

    //Depending on which code the server has sent, an event will be created accordingly
    public void processCode() {
        //Command acknowledge and Login messages
        if (code >= 500 && code <= 505) {
            otherEvents.processMessage(code, originalTPIMessage);
            eventMessage = otherEvents.getEventMessage();
        }
        //System Status LEDs
        else if (code == 510) {
            statusLEDsEvent.processMessage(code, originalTPIMessage);
            eventMessage = statusLEDsEvent.getEventMessage();
        }
        //Zones related events (Opening, Closing, Alarm and Restored)

        else if (code >= 600 && code <= 620) {
            zoneEvent.processMessage(code, originalTPIMessage);
            eventMessage = zoneEvent.getEventMessage();
        }
        //Arming System and using panic buttons
        else if (code >= 621 && code <= 626) {
            panicKeys.processMessage(code);
            eventMessage = panicKeys.getEventMessage();
        }
        //System Status text display
        else if (code >= 650 && code <= 657) {
            systemStatusEvent.processMessage(code, originalTPIMessage);
            eventMessage = systemStatusEvent.getEventMessage();
        }
        //Invalid access code being detected
        else if (code == 670) {
            accessCodeEvent.processMessage(code);
            eventMessage = accessCodeEvent.getEventMessage();
        }
        //General system trouble detected (Power Lost and Low System Battery)
        else if (code >= 840 && code <= 841) {
            troubleEvent.processMessage(code, originalTPIMessage);
            eventMessage = troubleEvent.getEventMessage();
        } else
        //If the code received in the message from the server does not apply
        //then, a default message containing the code is set
        {
            setDefaultEventMessage();
        }
    }

    //Returns a String containing the event in a readable format
    public String getEventMessage() {
        return eventMessage;
    }

    //If the message received does not apply to this application, a default message is set
    //containing the code received in the message
    public String setDefaultEventMessage() {
        eventMessage = "No event for Code# " + code;
        return eventMessage;
    }

}
