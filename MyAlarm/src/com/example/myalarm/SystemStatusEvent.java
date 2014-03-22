package com.example.myalarm;

import android.widget.Button;

public class SystemStatusEvent 
{
	//Button representing System Status Display
	private Button txtDisplay;
	//Holds the first three characters parsed into integer of the message received by the server 
	private int code;
	//Holds the message received by the server in an array of characters
	private char [] TPIMessage;
	//Message from the server in a readable format
	private String eventMessage;
	//Holds the status of the system when armed (Stay or Away)
	private String armedMode;
	
	//Holds images to change system status on the text display area
	Integer [] statusTextDisplay = {
			R.drawable.systemready,
			R.drawable.systemnotready,
			R.drawable.systemarmed,
			R.drawable.alarmactive,
			R.drawable.systemdisarmed,
			R.drawable.exitdelay,
			R.drawable.entrydelay	};
	
	//Default Constructor
	//Accepts the button that represents the status of the system, the 3-digit code inside the
	//message received from the server and the message itself
	public SystemStatusEvent (Button textDisplay, int responseCode, char [] message)
	{
		txtDisplay = textDisplay;
		code = responseCode;
		TPIMessage = message;
		processCode (code);
	}
		
	//Constructor - For debugging only
	//Accepts the message received by the server and the 3-digit code
	public SystemStatusEvent (int responseCode, char [] message)
	{
		code = responseCode;
		TPIMessage = message;
		processCode (code);
	}
	
	
	public void processCode(int code)
	{
		switch(code)
		{
		case 650:
			systemReady();
			break;
		case 651:
			systemNotReady();
			break;
		case 652:
			systemArmed();
			break;
		case 654:
			systemAlarmActive();
			break;
		case 655:
			systemDisarmed();
			break;
		case 656:
			exitDelay();
			break;
		case 657:
			entryDelay();
			break;
		default:
			eventMessage = "No event for Code# " + code;
		}
						
	}
			
	public void systemReady ()
	{		
		txtDisplay.setBackgroundResource(statusTextDisplay[0]);
		eventMessage = ("System Is Ready To Arm");
	}
		
	public void systemNotReady ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[1]);
		eventMessage = ("System Not Ready");
	}
		
	public void systemArmed ()
	{
		armedMode = getArmedMode();
		txtDisplay.setBackgroundResource(statusTextDisplay[2]);
		eventMessage = ("System Armed In " + armedMode);
	}
		
	public void systemAlarmActive ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[3]);
		eventMessage = ("System Alarm is Active");
	}
		
	public void systemDisarmed ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[4]);
		eventMessage = ("System Has Been Disarmed");
	}
		
	public void exitDelay ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[5]);
		eventMessage = ("Exit Delay in Progress");
	}
		
	public void entryDelay ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[6]);
		eventMessage = ("Entry Delay in Progress");
	}
		
	public String getArmedMode()
	{
		if (TPIMessage[4] == '0')
			return "Away Mode";
		else
			if (TPIMessage[4] == '1')
				return "Stay Mode";
			else
				return "Other Mode";
	}

	//Returns a String containing the event in a readable format
	public String getEventMessage()
	{
		return eventMessage;
	}
}
