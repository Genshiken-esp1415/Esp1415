package it.unipd.dei.esp1415;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.hardware.SensorManager;

public class SettingValues {

	protected static final boolean ENABLED = true;
	protected static final boolean DISABLED = false;
	
	protected static int sSensorDelay;
	protected static int sMaxDuration;
	protected static int sAlarmHour;
	protected static int sAlarmMinute;
	protected static String sAlarm;
	protected static boolean sAlarmCheck;
	protected static boolean sNotificationCheck;
	protected static String sEmail;
	protected static String sPassword;
	protected static ArrayList<String> sDest = new ArrayList<String>();
	protected static ArrayList<String> sName = new ArrayList<String>();
	
	protected static void setDefault() {
		sSensorDelay = SensorManager.SENSOR_DELAY_GAME;
		sMaxDuration = 8;
		sAlarmHour = 8;
		sAlarmMinute = 0;
		sAlarm = "8:00";
		sAlarmCheck = DISABLED;
		sNotificationCheck = DISABLED;
		sEmail = "";
		sPassword = "";
	}
	
	protected static void readSettings(Context context) {
		try {
			FileInputStream input = context.openFileInput("settings.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			if ((line = br.readLine()) == null) {
				return;
			} else {
				//sSensorDelay = Integer.parseInt(line);
				setSensorDelay(line);
			}			
			sMaxDuration = Integer.parseInt(br.readLine());
			sAlarmHour = Integer.parseInt(br.readLine());
			sAlarmMinute = Integer.parseInt(br.readLine());
			sAlarmCheck = Boolean.parseBoolean(br.readLine());
			sNotificationCheck = Boolean.parseBoolean(br.readLine());
			if( (line = br.readLine()) != null){
				sEmail = line;
				sPassword = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static ArrayList<String> setSelectedContacts(Context context) {
		try {
			FileInputStream input = context.openFileInput("contactlist.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			int i;
			while ((line = br.readLine()) != null) {
				for(i=0;line.charAt(i)!=':';i++); //nome: indirizzo@email.com
				sName.add(line.substring(0, i));
				sDest.add(line.substring(i+2,line.length()));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sDest;
	}
	
	protected static void setSensorDelay(String sampleRate){
		if (sampleRate.equals("Molto alta")) {
			sSensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
		} else if (sampleRate.equals("Alta")) {
			sSensorDelay = SensorManager.SENSOR_DELAY_GAME;
		} else if (sampleRate.equals("Normale")) {
			sSensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
		} else {
			sSensorDelay = SensorManager.SENSOR_DELAY_UI;
		}
	}
	
}
