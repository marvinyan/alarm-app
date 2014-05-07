package com.example.myalarm;

import android.speech.tts.TextToSpeech;
import android.widget.Button;

//AcessCodeEvent - Used when an invalid code has been entered
public class AccessCodeEvent {
    //Holds the button used to display the System Status
    private Button txtDisplay;
    //Holds the first three characters parsed into integer of the message received by the server
    private int code;
    //Message from the server in a readable format
    private String eventMessage;
    //Text to Speech
    private TextToSpeech tts;

    //Default Constructor
    //Accepts button that display the system status
    public AccessCodeEvent(Button textDisplay, TextToSpeech TTS) {
        txtDisplay = textDisplay;
        tts = TTS;
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
        switch (code) {
            case 670:
                invalidAccessCode();
                break;
            case 672:
                failureToArm();
                break;
            //If the code received in the message from the server does not apply
            //to this event, a default message containing the code is set
            default:
                eventMessage = "No event for Code# " + code;
        }
    }

    //Changes the system status text to Invalid Code
    public void invalidAccessCode() {
        txtDisplay.setBackgroundResource(R.drawable.invalidcode);
        eventMessage = ("Invalid Access Code");
        tts.speak(eventMessage + "... Please try again", TextToSpeech.QUEUE_ADD, null);
    }

    //System can not be armed
    public void failureToArm() {
        txtDisplay.setBackgroundResource(R.drawable.failuretoarm);
        eventMessage = ("Failure to Arm");
        tts.speak(eventMessage + "... Please secure the system and try again", TextToSpeech.QUEUE_ADD, null);
    }

    //Returns a String containing the event in a readable format
    public String getEventMessage() {
        return eventMessage;
    }
}
