package com.example.myalarm;

import android.widget.Button;


public class ZoneEvent 
{
	//Holds the buttons representing the zones on the UI
	private Button [] zonesButtons;
	//Holds the first three characters parsed into integer of the message received by the server
	private int code;
	//Holds the message received by the server in an array of characters
	private char [] TPIMessage;
	//Message from the server in a readable format
	private String eventMessage;
	
	//Holds images for Zones when open
	private Integer [] zonesOpen = {
			R.drawable.zone1open,
			R.drawable.zone2open,
			R.drawable.zone3open,
			R.drawable.zone4open,
			R.drawable.zone5open,
			R.drawable.zone6open,
			R.drawable.zone7open,
			R.drawable.zone8open	};
	
	//Holds images for Zones when restored
	private Integer [] zonesClosed = {
			R.drawable.zone1closed,
			R.drawable.zone2closed,
			R.drawable.zone3closed,
			R.drawable.zone4closed,
			R.drawable.zone5closed,
			R.drawable.zone6closed,
			R.drawable.zone7closed,
			R.drawable.zone8closed	};
	
	
	//Default Constructor accepting the array of buttons representing the zones list, 3-digit response code and
	//the complete message that came out of the server
	public ZoneEvent (Button [] zonesBtns, int responseCode, char [] message)
	{
		zonesButtons = zonesBtns;
		code = responseCode;
		TPIMessage = message;
		determineCode (code, TPIMessage);
	}
	
	//Constructor - For debugging only
	//Accepts the message received by the server and first 3-digit code
	public ZoneEvent(int responseCode, char [] message) 
	{
		code = responseCode;
		TPIMessage = message;
		determineCode (code, TPIMessage);
	}
	
	//Depending on the code received from the server, that will determine the status of
	//any of the zones
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
			eventMessage = "No event for Code# " + code;
		}
					
	}
	
	//If the message from the server started with 601, it means one of the
	//zones caused the alarm to go off
	public void zoneAlarm (char [] restOfMessage)
	{		
		if (restOfMessage [5] == '0')
			eventMessage = ("Zone #" + restOfMessage [6] + " in Alarm");
		else
			eventMessage = "This zone is not available";
	}
	
	//If the message from the server started with 602, it means that the zone
	//that made the alarm go off has been restored
	public void zoneAlarmRestored (char [] restOfMessage)
	{
		if (restOfMessage [5] == '0')
			eventMessage = ("Zone #" + restOfMessage [6] + " Restored");
		else
			eventMessage = "This zone is not available";
	}
	
	//If the message received from the server started with 609, it means that
	//one of the zones is open
	public void zoneOpen (char [] restOfMessage)
	{
		if (restOfMessage [4] == '0')
		{
			eventMessage = ("Zone #" + restOfMessage [6] + " Open");
			changeZoneToOpen (getZoneNumber(restOfMessage[5]));
		}
		else
			eventMessage = "This zone is not available";
	}
	
	//If the message received from the server started with 610, it means that
	//one zone has been closed
	public void zoneClosed (char [] restOfMessage)
	{
		if (restOfMessage [4] == '0')
		{
			eventMessage = ("Zone #" + restOfMessage [6] + " Closed");
			changeZoneToClosed (getZoneNumber(restOfMessage[5]));
		}
		else
			eventMessage = "This zone is not available";
	}
	
	//Sets the image of the opened zone to red
	public void changeZoneToOpen(int zoneNumber)
	{
		zonesButtons[zoneNumber].setBackgroundResource(zonesOpen[zoneNumber-1]);
	}
	
	//Sets the image of the closed zone back to its default color
	public void changeZoneToClosed(int zoneNumber)
	{
		zonesButtons[zoneNumber].setBackgroundResource(zonesClosed[zoneNumber-1]);
	}
	
	//Parse a single character into an integer. The integer represents the zone number
	//within the message received by the server
	public int getZoneNumber (char zoneNum)
	{
		return Character.getNumericValue(zoneNum);
	}
	
	//Returns a String containing the event in a readable format
	public String getEventMessage()
	{
		return eventMessage;
	}
	
	
}
