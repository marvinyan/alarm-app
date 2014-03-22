package com.example.myalarm;


public class ZoneEvent 
{
	private int code;
	private char [] TPIMessage;
	private String eventMessage;

	public ZoneEvent(int responseCode, char [] message) 
	{
		code = responseCode;
		TPIMessage = message;
		determineCode (code, TPIMessage);
	}

	public void determineCode(int code, char [] TPIMessage)
	{
		switch (code)
		{
		case 601:
			zoneAlarm(TPIMessage);
			break;
		case 602:
			zoneAlarmRestored(TPIMessage);
			break;
		case 609:
			zoneOpen(TPIMessage);
			break;
		case 610:
			zoneClosed(TPIMessage);
			break;
		default:
			System.out.println("Code does not apply!\n");
			break;
		}
	}

	public void zoneAlarm (char [] restOfMessage)
	{		
		if (restOfMessage [5] == '0')
			eventMessage = ("Zone Alarm: #" + restOfMessage [6]);
		else
			eventMessage = "This zone is not available";
	}

	public void zoneAlarmRestored (char [] restOfMessage)
	{
		if (restOfMessage [5] == '0')
			eventMessage = ("Zone Restored: #" + restOfMessage [6]);
		else
			eventMessage = "This zone is not available";
	}

	public void zoneOpen (char [] restOfMessage)
	{
		if (restOfMessage [4] == '0')		
			eventMessage = ("Zone Open: #" + restOfMessage [5]);		
		else
			eventMessage = "This zone is not available";
	}

	public void zoneClosed (char [] restOfMessage)
	{
		if (restOfMessage [4] == '0')
			eventMessage = ("Zone Closed: #" + restOfMessage [5]);
		else
			eventMessage = "This zone is not available";
	}

	public String getEventMessage()
	{
		return eventMessage;
	}
}
