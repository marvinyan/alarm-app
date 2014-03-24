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
	//Accepts the button that represents the status of the system
	public SystemStatusEvent (Button textDisplay)
	{
		txtDisplay = textDisplay;
	}
		
	//Process message from server
	public void processMessage (int responseCode, char [] message)
	{
		code = responseCode;
		TPIMessage = message;
		processCode (code);
	}
	
	//Based on the 3-digit code received from the server, we are able to determine
	//the state the system is in
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
	
	//When the system is ready to be armed
	public void systemReady ()
	{		
		txtDisplay.setBackgroundResource(statusTextDisplay[0]);
		eventMessage = ("System Is Ready To Arm");
	}

	//When one of the zones is open. The system can not be armed at this point
	public void systemNotReady ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[1]);
		eventMessage = ("System Not Ready");
	}
		
	//System is Armed
	public void systemArmed ()
	{
		armedMode = getArmedMode();
		txtDisplay.setBackgroundResource(statusTextDisplay[2]);
		eventMessage = ("System Armed In " + armedMode);
	}
	
	//When one of the zones was opened while the system was armed,
	//or when one of the 2 panic buttons was activated (Fire and Police)
	public void systemAlarmActive ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[3]);
		eventMessage = ("System Alarm is Active");
	}
		
	//System disarmed, after entering the correct code
	public void systemDisarmed ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[4]);
		eventMessage = ("System Has Been Disarmed");
	}
		
	//Delay in progress while system is being armed
	public void exitDelay ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[5]);
		eventMessage = ("Exit Delay in Progress");
	}
		
	//When an Entry/Exit zones was opened while system was armed
	public void entryDelay ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[6]);
		eventMessage = ("Entry Delay in Progress");
	}
		
	//Specifies the way in which the System was armed
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
