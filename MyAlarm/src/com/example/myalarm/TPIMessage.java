package com.example.myalarm;

import android.content.Context;

public class TPIMessage 
{
	protected MainActivity context;

	private char [] originalTPIMessage;
	private int code;
	private ZoneEvent zoneEvent;
	private PanicKeysEvent panicKeys;
	private TroubleEvent troubleEvent;
	private AccessCodeEvent accessCodeEvent;
	private SystemStatusEvent systemStatusEvent;
	private OtherEvents otherEvents;
	private String eventMessage = "No message available for this response";

	public TPIMessage(Context mainContext, String TPIMessage) 
	{
		this.context = (MainActivity) mainContext;
		originalTPIMessage = TPIMessage.toCharArray();
		code = getCode(originalTPIMessage);
		processCode();
	}

	public TPIMessage(String TPIMessage) 
	{		
		originalTPIMessage = TPIMessage.toCharArray();
		code = getCode(originalTPIMessage);
		processCode();
	}

	public int getCode (char [] TPIMessage)
	{		
		char[] temp = new char [3];
		for (int i=0; i<3; i++)
			temp [i] = TPIMessage [i];
		return Integer.parseInt(new String(temp));
	}


	public void processCode()
	{
		if (code >= 500 && code <= 505)
		{
			otherEvents = new OtherEvents (code, originalTPIMessage);
			eventMessage = otherEvents.getEventMessage();
		}
		else if (code >= 600 && code <= 620)
		{ 				
			zoneEvent = new ZoneEvent (code, originalTPIMessage);
			eventMessage = zoneEvent.getEventMessage();
		}
		else if (code >= 621 && code <= 626)
		{
			panicKeys = new PanicKeysEvent (code);
			eventMessage = panicKeys.getEventMessage();
		}
		else if (code >= 650 && code <= 657)
		{
			systemStatusEvent = new SystemStatusEvent (code, originalTPIMessage);
			eventMessage = systemStatusEvent.getEventMessage();
		}
		else if (code == 670)
		{
			accessCodeEvent = new AccessCodeEvent (code);
			eventMessage = accessCodeEvent.getEventMessage();
		}
		else if (code >= 840 && code <= 841)
		{
			troubleEvent = new TroubleEvent (code);
			eventMessage = troubleEvent.getEventMessage();
		}
	}

	public String getEventMessage()
	{
		return eventMessage;
	}
}
