package com.example.myalarm;

public class AccessCodeEvent 
{
	private int code;
	private String eventMessage;

	public AccessCodeEvent (int responseCode)
	{
		code = responseCode;
		determineCode (code);
	}
	public void determineCode(int code)
	{
		if (code == 670)
			invalidAccessCode();
		else
			System.out.println("Code does not apply!\n");			
	}

	public void invalidAccessCode ()
	{		
		eventMessage = ("Invalid Access Code");
	}

	public String getEventMessage()
	{
		return eventMessage;
	}
}
