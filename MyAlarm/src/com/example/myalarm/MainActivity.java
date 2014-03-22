package com.example.myalarm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
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
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
		//Hide Action Bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Force application to run in Landscape mode only
      	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
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
		
		//Open Socket in background thread
		openSocketTask = new OpenSocketTask();		
		openSocketTask.execute();
		
		// Load saved IP and pw
		serverIP = "";
		serverPassword = "";
		loadSavedPreferences();
		
		//Commands for Key 1, Key 2, Key 3 and Key 4
		listKeypadCommands = new String [] {"0700C7", "0701C8", "0702C9", "0703CA", "0704CB",
				"0705CC", "0706CD", "0707CE", "0708CF", "0709D0"};
		//Commands for Arm Stay, Arm Away, Fire and Police Butttons
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
    								"Retrying in 5 seconds.\n");
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
    			tpiMsg = new TPIMessage (statusButtons, otherStatusButtons, sideButtons, response [0]);
    			updateLog(tpiMsg.getEventMessage() + "\n");
    			if (tpiMsg.getEventMessage() == "Login Successful")
					Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
            }
            else
    			updateLog(response[0] + "\n");
    	}
    	
    	public void selfRestart() 
		{
			publishProgress("Restarting task.");
			try 
			{
				socket.close();
			} 
			catch(IOException e) 
			{
				e.printStackTrace();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
			openSocketTask = new OpenSocketTask();
			openSocketTask.execute();
		}
    }
    
    @Override
    protected void onRestart()
    {
    	super.onRestart();
    	//Restart task in the background upon application restart
    	openSocketTask = new OpenSocketTask(); 			
    	openSocketTask.execute();
    }
    
    @Override
    protected void onStop()
	{
		super.onStop();
		try 
		{
			//Closing connection to socket
			socket.close();
			//Terminating task running in the background thread
			openSocketTask.cancel(true);
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
		String hexSum = Integer.toHexString(decSum);
		return hexSum.substring(hexSum.length()-2, hexSum.length());
	}
}

