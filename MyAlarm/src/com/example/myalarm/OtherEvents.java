package com.example.myalarm;

//OtherEvents - Used when the server has acknowledge the command received and
//when the server password has been accepted
public class OtherEvents {
    //Holds the first three characters parsed into integer of the message received by the server
    private int code;
    //Holds the message received by the server in an array of characters
    private char[] TPIMessage;
    //Message from the server in a readable format
    private String eventMessage;

    //Default Constructor - No parameters needed
    public OtherEvents() {
    }

    //Process message from server
    public void processMessage(int responseCode, char[] message) {
        code = responseCode;
        TPIMessage = message;
        processCode(code);
    }

    //Based on the 3-digit code received from the server, we are able to determine
    //if the message indicates if the command sent was received or if the server
    //password has been accepted
    public void processCode(int code) {
        switch (code) {
            case 500:
                //Message starting with 500, the server has acknowledge the command the app sent
                commandAcknowledge();
                break;
            case 505:
                //Message starting with 505, login process related
                loginResponse();
                break;
            default:
                //If the code received in the message from the server does not apply
                //to this event, a default message containing the code is set
                eventMessage = "No event for Code# " + code;
        }
    }

    //Debugging purpose
    public void commandAcknowledge() {
        eventMessage = "Command acknowledged";
    }

    //Debugging purpose
    public void loginResponse() {
        //The fourth character in the message from the server must be 1, meaning
        //the server password sent was accepted
        if (TPIMessage[3] == '1') {
            eventMessage = "Login Successful";
        } else if (TPIMessage[3] == '3') {
            eventMessage = "Password Request";
        } else {
            eventMessage = "Login Failed";
        }
    }

    //Returns a String containing the event in a readable format
    public String getEventMessage() {
        return eventMessage;
    }
}
