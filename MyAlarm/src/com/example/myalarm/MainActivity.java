package com.example.myalarm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
	
	final Context context = this;
	private ScrollView sv;
	//Temporary Log
	private TextView log;
	//Socket connection
	private String serverIP, serverPassword;
	private Socket socket;
	//Streams to send and receive commands
	private PrintWriter out;
	private BufferedReader in;
	
	//Status Buttons, three buttons across the top left corner
	public Button [] statusButtons;
	//Numeric Keypad 0-9 	
	public Button [] numericKeypad;
	//Panic Buttons
	public Button [] sideButtons;
	//Display buttons for Zone List and System Status Display
	public Button [] otherStatusButtons;
	// Setup button
	public Button setupButton;
	
	//Temporary list of commands
	String [] listKeypadCommands;
	String [] listSideBtnsCommands;
	
	//Asynctask running in the background thread
	private OpenSocketTask openSocketTask;
	
	//Message received from the server
	private TPIMessage tpiMsg;
	
	private boolean appInForeground;
	
	//Debug
	private String tag;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
		//Hide Action Bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        
        tag = "Testing";
        Log.d(tag, "in the onCreate Method");
        
        appInForeground = true;
        
        //Temporary Log, for debugging 
      	sv = (ScrollView)findViewById(R.id.scroll);
	    log = (TextView)findViewById(R.id.log);
	    log.setTextColor(Color.CYAN);
	  
	    //Initiation of status buttons
	    statusButtons = new Button [4];
	  	int statusIdIndex = R.id.ready;
	  	for (int i=0; i<statusButtons.length; i++)
	  	{
	  		statusButtons[i] = (Button)findViewById(statusIdIndex);
	  		statusIdIndex++;
	  	}
	    
	    
	    //Initiation of numeric keypad, adding a listener to each button
	    numericKeypad = new Button [10];
	    int numKPIndex = R.id.key1;
		for (int i=0; i<numericKeypad.length; i++)
		{
			numericKeypad[i] = (Button)findViewById(numKPIndex);
			numericKeypad[i].setOnClickListener(this);
			numKPIndex++;
		}
		
		//Initiation of side buttons, adding a listener to each one
		sideButtons = new Button [4];
	    int sideIdIndex = R.id.armStay;
		for (int i=0; i<sideButtons.length; i++)
		{
			sideButtons[i] = (Button)findViewById(sideIdIndex);
			sideButtons[i].setOnClickListener(this);
			sideIdIndex++;
		}
		
		//Initiation of buttons that display status of Zones and System
		otherStatusButtons = new Button [9];
		int otherStatusIdIndex = R.id.textDisplay;
		for (int i=0; i<otherStatusButtons.length; i++)
		{
			otherStatusButtons[i] = (Button)findViewById(otherStatusIdIndex);
			otherStatusButtons[i].setClickable(false);
			otherStatusIdIndex++;
		}
		
		// Initiation of setup button
		setupButton = (Button)findViewById(R.id.setup);
		setupButton.setOnClickListener(this);
		
		//Open Socket in background thread, loading saved IP address and server password
		openSocketTask = new OpenSocketTask();			
    	loadSavedPreferences();
        openSocketTask.execute();
		Log.d(tag, "Task starts - onCreate");
		
		//Process message from server
		tpiMsg = new TPIMessage (statusButtons, otherStatusButtons, sideButtons);
		
		//Commands for Key 1, Key 2, Key 3 and Key 4
		listKeypadCommands = new String [] {"0700C7", "0701C8", "0702C9", "0703CA", "0704CB",
				"0705CC", "0706CD", "0707CE", "0708CF", "0709D0"};
		//Commands for Arm Stay, Arm Away, Fire and Police Buttons
		listSideBtnsCommands = new String [] {"0311C5", "0301C4", "0601C7", "0603C9"};
	   
    }
    
    public void onClick(View v)
    {	
    	switch (v.getId())
		{
		case R.id.key0:
			sendNumCommand(0);
			break;
		case R.id.key1:
			sendNumCommand(1);
			break;
		case R.id.key2:
			sendNumCommand(2);
			break;
		case R.id.key3:
			sendNumCommand(3);
			break;
		case R.id.key4:
			sendNumCommand(4);
			break;
		case R.id.key5:
			sendNumCommand(5);
			break;
		case R.id.key6:
			sendNumCommand(6);
			break;
		case R.id.key7:
			sendNumCommand(7);
			break;
		case R.id.key8:
			sendNumCommand(8);
			break;
		case R.id.key9:
			sendNumCommand(9);
			break;
		case R.id.armStay:
			sendSideBtnCommand(0);
			break;
		case R.id.armAway:
			sendSideBtnCommand(1);
			break;
		case R.id.fire:
			sendSideBtnCommand(2);
			break;
		case R.id.police:
			sendSideBtnCommand(3);
			break;
		case R.id.setup:
			openDialog();
			break;
		}
    }
    
    //Socket Connection and Listener. Will run in the background
    private class OpenSocketTask extends AsyncTask<Void, String, Void>
    {
    	
    	@Override
		protected Void doInBackground(Void... task) 
    	{
    			
    			publishProgress("Attempting to connect to device ...");
        		try
        		{
        			//Socket connection
    	        	socket = new Socket(InetAddress.getByName(serverIP), 4025);
    	        	out = new PrintWriter(socket.getOutputStream(), true);    	        	
    				publishProgress("Connection established to " +serverIP);
    				
    				try
    				{    				
    					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    					publishProgress("Server >> " + in.readLine() + ". ");
    				
    					String convertedPassword = "005" + serverPassword + getChecksum("005"+serverPassword);
    					publishProgress("Login << " + convertedPassword);
    					    					
    					//Send password to complete socket connection    					
    					out.println(convertedPassword + "\r");
    					out.flush();
    					
    					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    					publishProgress("Server >> " + in.readLine() + ". ");
    				}
    				catch (IOException e)
    				{
    					try 
    					{
    						publishProgress("Could not access input stream.\n" +
    								"Another user may be using the device.\n" +
    								"Retrying in 5 seconds.");
    				        Thread.sleep(5000);   
    						selfRestart();
    				    } 
    					catch (InterruptedException f) 
    					{
    				       f.printStackTrace();
    				    }
    				}			         				
			        
			        //The input stream must be in a loop to keep listening for any incoming messages
			        String serverMessage = "";
			        
			        while ((serverMessage = in.readLine()) != null)
			        {
			        	publishProgress(serverMessage); 
			        }
			        
        		}
        		catch (UnknownHostException e) 
        		{
        			publishProgress("Invalid IP address.\n");        
        	    } 
        		catch (IOException e)        		
        		{
        			try
        			{
        				publishProgress("Connection timed out.\nRetrying in 5 seconds");
        				Thread.sleep(5000);
    					selfRestart();
    				} 
        			catch (InterruptedException f) 
        			{
    					// TODO Auto-generated catch block
    					f.printStackTrace();
    				}
        	    }
    		    		
			return null;
		}
    	protected void onProgressUpdate(String... response) 
    	{
    		//Check if response[0] is a message from the server
    		if (response[0].matches("[0-9].*"))
            {			
    			updateLog("Server>> " + response[0] + ". ");
    			tpiMsg.processMessage(response[0]);
    			updateLog(tpiMsg.getEventMessage() + "\n");
    			if (tpiMsg.getEventMessage() == "Login Successful")
    			{
    				Toast.makeText(getApplicationContext(), "Login Succesful. Loading System Status",
    						Toast.LENGTH_LONG).show();
    				//Request System Status, after login process is completed
    				out.println("00191\r");
    				out.flush();
    				
    			}
    			else
    				if (tpiMsg.getEventMessage() == "Login Failed")
    					Toast.makeText(getApplicationContext(), "Login Failed. Check Your Password",
    							Toast.LENGTH_LONG).show();
    			
            }
            else
    			updateLog(response[0] + "\n");
    	}
    	
    	//selfRestart code will only execute if the application is in the foreground
    	//preventing self connections after the onPause, onStop or onDestroy methods have been
    	//called
    	public void selfRestart() 
		{
    		if (appInForeground)
    		{	
    			publishProgress("Restarting task.");    		
    			openSocketTask.cancel(true);
    			try 
    			{
    				socket.close();
    			} 
    			catch (IOException e) 
    			{					
    				e.printStackTrace();
    			}
    			openSocketTask = new OpenSocketTask();
    			loadSavedPreferences();
    			openSocketTask.execute();
    			Log.d(tag, "Task starts - selfStart");	    	
    		}
		}
    }
    
    //When the application is restarted, the task running in the background
    //will start again
    @Override
    protected void onRestart()
    {
    	super.onRestart();
    	appInForeground = true;
    	//Restart task in the background if the task was previously terminated
    	if (openSocketTask.getStatus() == AsyncTask.Status.FINISHED)
    	{
    		try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		openSocketTask = new OpenSocketTask();
    		loadSavedPreferences();
        	openSocketTask.execute();
        	Log.d(tag, "Task starts - onRestart");
    	}
    	
    }
    
    //When the application starts a second time, the task running in the background
    //will start again
    @Override
    protected void onStart()
    {
    	super.onStart();
    	appInForeground = true;
    	//Restart task in the background upon application restart
    	if (openSocketTask.getStatus() == AsyncTask.Status.FINISHED)
    	{
    		try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		openSocketTask = new OpenSocketTask();
    		loadSavedPreferences();
        	openSocketTask.execute();
        	Log.d(tag, "Task starts - onStart");
    	}
    	
    }
    
    //When the application is resumed, the task running in the background
    //will start again
    @Override
    protected void onResume()
    {
    	super.onResume();
    	appInForeground = true;
    	//Restart task in the background upon application restart
    	if (openSocketTask.getStatus() == AsyncTask.Status.FINISHED)
    	{
    		try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		openSocketTask = new OpenSocketTask();
    		loadSavedPreferences();
        	openSocketTask.execute();
        	Log.d(tag, "Task starts - onResume");
    	}
    	
    }
    
    //When application is partially visible, the socket if connected will close
    //and the task running in the background will be terminated if running
    @Override
    protected void onPause()
	{
		super.onPause();
		appInForeground = false;
		if (socket.isConnected())
		{
			try 
			{
				//Closing connection to socket
				socket.close();
				Log.d(tag, "Socket Closed - onPause");			
			} 
			catch(IOException e) 
			{
				e.printStackTrace();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		//Terminating task running in the background thread
		if (openSocketTask.getStatus() == AsyncTask.Status.RUNNING || 
				openSocketTask.getStatus() == AsyncTask.Status.PENDING)
		{
			openSocketTask.cancel(true);
			Log.d(tag, "Task killed - onPause");
		}
	}
    
    //When application is not visible, the socket if connected will close
    //and the task running in the background will be terminated if running
    @Override
    protected void onStop()
	{
		super.onStop();
		appInForeground = false;
		if (socket.isConnected())
		{
			try 
			{
				//Closing connection to socket
				socket.close();
				Log.d(tag, "Socket Closed - onStop");
			} 
			catch(IOException e) 
			{
				e.printStackTrace();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		//Terminating task running in the background thread
		if (openSocketTask.getStatus() == AsyncTask.Status.RUNNING || 
				openSocketTask.getStatus() == AsyncTask.Status.PENDING)
		{
			openSocketTask.cancel(true);
			Log.d(tag, "Task killed - onStop");
		}
	}
    
    //When application has been destroyed, the socket if connected will close
    //and the task running in the background will be terminated if running
    @Override
    protected void onDestroy()
	{
		super.onDestroy();
		appInForeground = false;
		if (socket.isConnected())
		{
			try 
			{
				//Closing connection to socket
				socket.close();
				Log.d(tag, "Socket Closed - onStop");
			} 
			catch(IOException e) 
			{
				e.printStackTrace();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		//Terminating task running in the background thread
		if (openSocketTask.getStatus() == AsyncTask.Status.RUNNING || 
				openSocketTask.getStatus() == AsyncTask.Status.PENDING)
		{
			openSocketTask.cancel(true);
			Log.d(tag, "Task killed - onStop");
		}
	}
    
    public void sendNumCommand (int commandNumber)
    {
    	try 
    	{
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(listKeypadCommands [commandNumber] + "\r");
			out.flush();
			updateLog("Sent << " + listKeypadCommands[0] + ". Numeric Keypad Command\n");				
		} 	
    	// If keypad is pressed when not connected to socket (although the NPE may be thrown instead).
    	catch (IOException e) 
    	{	
    		updateLog("Device may not be connected yet.\n"); 
    	}
    	// If app closed when not connected to socket
    	catch (NullPointerException e)
    	{
    		updateLog("Not connected to device yet.\n");
    	}
    }
    
    public void sendSideBtnCommand (int commandNumber)
	{
		try 
		{
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(listSideBtnsCommands[commandNumber] + "\r");
			out.flush();
			updateLog("Sent << " + listSideBtnsCommands[commandNumber] + ". Quick Arm/Panic Buttons\n");				
		} 	
		catch (IOException e) 
		{	
			updateLog("Device may not be connected yet.\n"); 
		}
		catch (NullPointerException e)
		{
			updateLog("Not connected to device yet.\n");
		}
	}
    
    public void updateLog(String msg)
	{
		log.append(msg);
		// Auto-scroll
		// https://groups.google.com/d/msg/android-developers/Gjb2Lfgh13E/3Ea75l6OCvAJ
		sv.post(new Runnable() { 
			public void run() { 
				sv.fullScroll(ScrollView.FOCUS_DOWN); 
			} 
		}); 
	}
    
    private void loadSavedPreferences() 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String ipAddress = sharedPreferences.getString("ip", ""); // default is ""
		String password = sharedPreferences.getString("pw", "");
		if (!ipAddress.equals("") && !password.equals("")) {
			//To initiate connection to socket and keep the listener in the background
			serverIP = ipAddress;
			serverPassword = password;
		} 
		else 
		{
			// 74.101.39.11 005user54
			Toast.makeText(getApplicationContext(), "No saved IP address and password detected.\nBeginning setup.",
					   Toast.LENGTH_LONG).show();
			openDialog();
		}
	}
    
    private void savePreferences(String key, String value) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void openDialog()
	{
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.prompts, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set prompts.xml to alert dialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput1 = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput1);
		final EditText userInput2 = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput2);

		userInput1.setText(serverIP);
		userInput2.setText(serverPassword);

		// set dialog message
		alertDialogBuilder
		.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{	
				String u1 = userInput1.getText().toString().trim();
				String u2 = userInput2.getText().toString().trim();

				if (u1.isEmpty() || u2.isEmpty())
				{
					Toast.makeText(getApplicationContext(), "Fields cannot be empty.",
							   Toast.LENGTH_LONG).show();
					dialog.cancel(); // TODO: keep dialog box open and mark EditText with red border
					openDialog();
				}
				else
				{
					savePreferences("ip", u1);
					savePreferences("pw", u2);
					serverIP = u1;
					serverPassword = u2;
					openSocketTask.selfRestart();
				}
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	// Determines the checksum of a given string and returns it
		private String getChecksum(String s) 
		{
			int decSum = 0;
			for (int i = 0; i < s.length(); i++)
			{
				decSum += (int)s.charAt(i);
			}
			// If checksum contains a letter, that letter must be upper case
			// Checksums with more than 2 chars are truncated to 2 characters
			//
			// TODO: verify checksum process with international letters?
			// http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug
			String hexSum = Integer.toHexString(decSum).toUpperCase(Locale.ENGLISH);
			return hexSum.substring(hexSum.length() - 2, hexSum.length());
		}
}

