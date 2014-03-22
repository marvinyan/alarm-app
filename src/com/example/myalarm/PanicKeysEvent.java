package com.example.myalarm;

public class PanicKeysEvent 
{
	private int code;
	private String eventMessage;

	public PanicKeysEvent (int responseCode)
	{
		code = responseCode;
		determineCode (code);
	}
	public void determineCode(int code)
	{
		switch (code)
		{
		case 621:
			fireKeyAlarm();
			break;
		case 622:
			fireKeyRestored();
			break;
		case 625:
			panicAlarm();
			break;
		case 626:
			panicAlarmRestored();
			break;
		default:
			System.out.println("Code does not apply!\n");
		}
	}

	public void fireKeyAlarm ()
	{		
		eventMessage = ("Fire Key Active");
	}

	public void fireKeyRestored ()
	{
		eventMessage = ("Fire Key Restored");
	}

	public void panicAlarm ()
	{
		eventMessage = ("Panic Alarm Active");
	}

	public void panicAlarmRestored ()
	{
		eventMessage = ("Panic Alarm Restored");
	}

	public String getEventMessage()
	{
		return eventMessage;
	}
}

