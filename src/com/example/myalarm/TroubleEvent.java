package com.example.myalarm;

public class TroubleEvent 
{
	private int code;
	private String eventMessage;
	
	public TroubleEvent (int responseCode)
	{
		code = responseCode;
		determineCode (code);
	}
	public void determineCode(int code)
	{
		if (code == 840)
			troubleLightON();
		else if (code == 841)
			troubleLightOFF();
		else
			System.out.println("Code does not apply!\n");
						
		}
			
		public void troubleLightON ()
		{		
			eventMessage = ("Trouble Light ON");
		}
		
		public void troubleLightOFF ()
		{
			eventMessage = ("Trouble Light OFF");
		}
		
		public String getEventMessage()
		{
			return eventMessage;
		}
}



