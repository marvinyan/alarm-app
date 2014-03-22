package com.example.myalarm;

public class SystemStatusEvent 
{
	private int code;
	private char [] TPIMessage;
	private String eventMessage;
	private String armedMode;

	public SystemStatusEvent (int responseCode, char [] message)
	{
		code = responseCode;
		TPIMessage = message;
		determineCode (code);
	}
	public void determineCode(int code)
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
			System.out.println("Code does not apply!\n");
		}
	}

	public void systemReady ()
	{		
		eventMessage = ("System Is Ready To Arm");
	}

	public void systemNotReady ()
	{
		eventMessage = ("System Not Ready");
	}

	public void systemArmed ()
	{
		armedMode = getArmedMode();
		eventMessage = ("System Armed In " + armedMode);
	}

	public void systemAlarmActive ()
	{
		eventMessage = ("System Alarm is Active");
	}

	public void systemDisarmed ()
	{
		eventMessage = ("System Has Been Disarmed");
	}

	public void exitDelay ()
	{
		eventMessage = ("Exit Delay in Progress");
	}

	public void entryDelay ()
	{
		eventMessage = ("Entry Delay in Progress");
	}

	public String getArmedMode()
	{
		if (TPIMessage[4] == '0')
			return "Away Mode";
		else if (TPIMessage[4] == '1')
			return "Stay Mode";
		else
			return "Other Mode";
	}

	public String getEventMessage()
	{
		return eventMessage;
	}		
}
