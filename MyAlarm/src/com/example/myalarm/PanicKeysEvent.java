package com.example.myalarm;

import android.widget.Button;

//PanicKeysEvent - Used when either panic button is pressed (Fire and Police)
public class PanicKeysEvent 
{
	//Buttons representing the panic functions (Fire and Police)
	private Button [] panicKeys;
	//Holds the first three characters parsed into integer of the message received by the server
	private int code;
	//Message from the server in a readable format
	private String eventMessage;
	
	//Holds images for panic buttons when activated and when deactivated
	private Integer [] panicOn = {
			R.drawable.fireon,
			R.drawable.policeon,
			R.drawable.fireoff,
			R.drawable.policeoff	};
	
	//Default Constructor
	//Accepts Quick Arm and Panic Buttons 
	//Notice, panic buttons include "Arm Stay" and "Arm Away". So Fire and Police are
	//panicButtons[2] and [3]
	public PanicKeysEvent (Button [] panicButtons)
	{
		panicKeys = panicButtons;
	}
	
	//Process message from server
	public void processMessage (int responseCode)
	{
		code = responseCode;
		determineCode (code);
	}
	
	//Based on the 3-digit code received from the server, we are able to determine
	//if any of the two panic buttons have been used
	public void determineCode(int code)
	{
		switch (code)
		{
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
	public void fireKeyAlarm ()
	{
		panicKeys[2].setBackgroundResource(panicOn[0]);
		eventMessage = ("Fire Key Active");
	}
	
	//Fire panic button Restored
	public void fireKeyRestored ()
	{
		panicKeys[2].setBackgroundResource(panicOn[2]);
		eventMessage = ("Fire Key Restored");
	}
		
	//Police panic button Activated
	public void panicAlarm ()
	{
		panicKeys[3].setBackgroundResource(panicOn[1]);
		eventMessage = ("Panic Alarm Active");
	}
		
	//Police panic button Restored
	public void panicAlarmRestored ()
	{
		panicKeys[3].setBackgroundResource(panicOn[3]);
		eventMessage = ("Panic Alarm Restored");
	}
		
	//Returns a String containing the event in a readable format
	public String getEventMessage()
	{
		return eventMessage;
	}
}

