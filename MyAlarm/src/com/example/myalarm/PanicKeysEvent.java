package com.example.myalarm;


//PanicKeysEvent - Used when either panic button is pressed (Fire and Police)
public class PanicKeysEvent {
    //Holds the first three characters parsed into integer of the message received by the server
    private int code;
    //Message from the server in a readable format
    private String eventMessage;


    //Default Constructor
    public PanicKeysEvent() {
    }

    //Process message from server
    public void processMessage(int responseCode) {
        code = responseCode;
        determineCode(code);
    }

    //Based on the 3-digit code received from the server, we are able to determine
    //if any of the two panic buttons have been used
    public void determineCode(int code) {
        switch (code) {
            case 621:
                fireKeyAlarm();
                break;
            case 622:
                fireKeyRestored();
                break;
            case 625:
                panicAlarm();
                break;
            case 626:
                panicAlarmRestored();
                break;
            default:
                eventMessage = "No event for Code# " + code;
        }

    }

    //Fire panic button Activated
    public void fireKeyAlarm() {
        eventMessage = "Fire Alarm Activated";
    }

    //Fire panic button Restored
    public void fireKeyRestored() {
        eventMessage = "Fire Alarm Key Restored";
    }

    //Police panic button Activated
    public void panicAlarm() {
        eventMessage = "Panic Alarm Activated";
    }

    //Police panic button Restored
    public void panicAlarmRestored() {
        eventMessage = ("Panic Alarm Key Restored");
    }

    //Returns a String containing the event in a readable format
    public String getEventMessage() {
        return eventMessage;
    }
}

