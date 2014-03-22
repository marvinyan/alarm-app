package com.example.myalarm;

import android.widget.Button;

public class TroubleEvent 
{
	//Buttons representing the system trouble button
	private Button troubleBtn;
	//Holds the first three characters parsed into integer of the message received by the server
	private int code;
	//Holds the message received by the server
	private String eventMessage;
	//Holds the message received by the server in an array of characters
	private char [] TPIMessage;
	
	//Holds Images for trouble indicator ON and OFF
	Integer [] troubleLED = {
			R.drawable.troubleon,
			R.drawable.troubleoff	};
	
	//Default Constructor
	//Accepts the button representing any system troubles and message received by the server
	public TroubleEvent (Button troubleButton, int responseCode, char [] message)
	{
		troubleBtn = troubleButton;
		code = responseCode;
		TPIMessage = message;
		determineCode (code);
	}
	
	//Constructor - For debugging only
	//Accepts the message received by the server
	public TroubleEvent (int responseCode)
	{
		code = responseCode;
		determineCode (code);
	}
	
	//If the code received starts with 840, trouble light is ON
	//If the code received starts with 841, trouble light is OFF
	public void determineCode(int code)
	{
		if (code == 840)
			troubleLightON();
		else
			if (code == 841)
				troubleLightOFF();
			else
				eventMessage = "No event for Code# " + code;
						
	}
		
	//Since the only partition in the alarm system this application controls,
	//the 4th character in the message received from the server represents
	//the partition number
	public void troubleLightON ()
	{		
		if (TPIMessage[3] == '1')
		{
			troubleBtn.setBackgroundResource(troubleLED[0]);		
			eventMessage = ("Part.#1. Trouble LED ON");
		}
		else
			eventMessage = ("Part.#" + TPIMessage[3] + ". Trouble LED ON");
	}
		
	//Since the only partition in the alarm system this application controls,
	//the 4th character in the message received from the server represents
	//the partition number
	public void troubleLightOFF ()
	{
		if (TPIMessage[3] == '1')
		{
			troubleBtn.setBackgroundResource(troubleLED[1]);		
			eventMessage = ("Part.#1. Trouble LED OFF");
		}
		else
			eventMessage = ("Part.#" + TPIMessage[3] + ". Trouble LED OFF");
	}
		
	//Returns a String containing the event in a readable format
	public String getEventMessage()
	{
		return eventMessage;
	}
}



