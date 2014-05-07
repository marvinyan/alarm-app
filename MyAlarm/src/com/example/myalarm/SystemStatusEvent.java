package com.example.myalarm;

import android.speech.tts.TextToSpeech;
import android.widget.Button;

public class SystemStatusEvent 
{
	//Button representing System Status Display
	private Button txtDisplay;
	//Button representing Active alarm
	private Button centerBackground;
	//Holds the first three characters parsed into integer of the message received by the server 
	private int code;
	//Holds the message received by the server in an array of characters
	private char [] TPIMessage;
	//Message from the server in a readable format
	private String eventMessage;
	//Holds the status of the system when armed (Stay or Away)
	private String armedMode;
	//Text to Speech
	private TextToSpeech tts;
	
	//Holds images to change system status on the text display area
	Integer [] statusTextDisplay = {
			R.drawable.systemready,
			R.drawable.systemnotready,
			R.drawable.systemarmed,
			R.drawable.alarmactive,
			R.drawable.systemdisarmed,
			R.drawable.exitdelay,
			R.drawable.entrydelay,
			R.drawable.armedstay,
			R.drawable.armedaway,
			R.drawable.menumode   };
	
	//Default Constructor
	//Accepts the button that represents the status of the system
	public SystemStatusEvent (Button textDisplay, Button centerBckgrnd, TextToSpeech TTS)
	{
		txtDisplay = textDisplay;
		centerBackground = centerBckgrnd;
		tts = TTS;
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
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackground);
			break;
		case 651:
			systemNotReady();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackground);
			break;
		case 652:
			systemArmed();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackground);
			break;
		case 654:
			systemAlarmActive();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackgroundred);
			break;
		case 655:
			systemDisarmed();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackground);
			break;
		case 656:
			exitDelay();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackground);
			break;
		case 657:
			entryDelay();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackground);
			break;
		case 900:
			menuMode();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackgroundyellow);
			break;
		case 921:
			menuMode();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackgroundyellow);
			break;
		case 922:
			menuMode();
			centerBackground.setBackgroundResource(R.drawable.centerbuttonbackgroundyellow);
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
		if (armedMode == "Stay Mode")
			txtDisplay.setBackgroundResource(statusTextDisplay[7]);
		else
		{
			if (armedMode == "Away Mode")
				txtDisplay.setBackgroundResource(statusTextDisplay[8]);
			else
				txtDisplay.setBackgroundResource(statusTextDisplay[2]);
		}
		eventMessage = "System has been Armed In " + armedMode;
		tts.speak(eventMessage, TextToSpeech.QUEUE_ADD, null);
	}
	
	//When one of the zones was opened while the system was armed,
	//or when one of the 2 panic buttons was activated (Fire and Police)
	public void systemAlarmActive ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[3]);
		eventMessage = "Active Alarm in Progress";
		tts.speak(eventMessage + "... enter your code to deactivate the system", TextToSpeech.QUEUE_ADD, null);
	}
		
	//System disarmed, after entering the correct code
	public void systemDisarmed ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[4]);
		eventMessage = "System Has Been Disarmed";
		tts.speak(eventMessage, TextToSpeech.QUEUE_ADD, null);
	}
		
	//Delay in progress while system is being armed
	public void exitDelay ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[5]);
		eventMessage = "Exit Delay in Progress... System is being armed";
		tts.speak(eventMessage, TextToSpeech.QUEUE_ADD, null);
	}
		
	//When an Entry/Exit zones was opened while system was armed
	public void entryDelay ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[6]);
		eventMessage = "Entry Delay in Progress";
		tts.speak(eventMessage + "... Please enter your code", TextToSpeech.QUEUE_ADD, null);
	}
	
	//When user has entered in system menu mode
	public void menuMode ()
	{
		txtDisplay.setBackgroundResource(statusTextDisplay[9]);
		eventMessage = "System in Menu Mode";
		tts.speak(eventMessage + "... Press the pound key twice to exit", TextToSpeech.QUEUE_ADD, null);
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
