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

	protected static final int ENABLED = 1;
	protected static final int DISABLED = 0;
	
	protected static int sSensorDelay;
	protected static int sMaxDuration;
	protected static String sAlarm;
	protected static int sAlarmCheck;
	protected static int sNotificationCheck;
	protected static String sEmail;
	protected static String sPassword;
	protected static ArrayList<String> sDest;
	
	protected static void setDefault() {
		sSensorDelay = SensorManager.SENSOR_DELAY_GAME;
		sMaxDuration = 8;
		sAlarm = "8:00";
		sAlarmCheck = DISABLED;
		sNotificationCheck = DISABLED;
		sEmail = null;
		sPassword = null;
		sDest = null;
	}
	
	protected static void readSettings(Context context) {
		try {
			FileInputStream input = context.openFileInput("settings.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			if ((line = br.readLine()) == null) {
				return;
			}
			else {
				sSensorDelay = Integer.parseInt(line);
			}
			sMaxDuration = Integer.parseInt(br.readLine());
			sAlarm = br.readLine();
			sAlarmCheck = Integer.parseInt(br.readLine());
			sNotificationCheck = Integer.parseInt(br.readLine());
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
			while ((line = br.readLine()) != null) {
				sDest.add(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sDest;
	}
	
}
