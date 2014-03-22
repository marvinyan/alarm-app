package com.example.myalarm;

public class OtherEvents 
{
	private int code;
	private char [] TPIMessage;
	private String eventMessage;

	public OtherEvents (int responseCode, char [] message)
	{
		code = responseCode;
		TPIMessage = message;
		determineCode (code);
	}
	public void determineCode(int code)
	{
		if (code == 500)
			commandAcknowledge();
		else if (code == 505)
			loginResponse();
		else
			System.out.println("Code does not apply!\n");			
	}

	public void commandAcknowledge ()
	{		
		eventMessage = ("Command has been acknowledged");
	}

	public void loginResponse ()
	{		
		if (TPIMessage [3] == '1')
			eventMessage = ("Login Successful");
		else
			eventMessage = ("Login Failed, Check your password");
	}

	public String getEventMessage()
	{
		return eventMessage;
	}
}
