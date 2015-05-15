package it.unipd.dei.esp1415;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Collezione di metodi di uso generale per ottenere dati casuali per il testing
 * dell'applicazione
 * 
 * @author Andrea
 *
 */
public class Randomizer {
	public static final int MILLISINHOUR = 3600000;
	/**
	 * This method returns a list of random sessions with random falls (0-5 falls).
	 * 
	 * @param size 
	 * @return
	 */
	public static ArrayList<Session> randomSession(int size){
		ArrayList<Session> sessions = new ArrayList<Session>();
		ArrayList<Fall> falls = new ArrayList<Fall>();
		ArrayList<AccelerometerData> accData = new ArrayList<AccelerometerData>();
		Random randNumber = new Random();
		Session randomSession = null;
		
		//month in Calendar goes from 0 to 11
		int randMonth = randNumber.nextInt(11);
		Calendar cal = new GregorianCalendar();
		cal.set(2014, randMonth, 1);
		
		Fall randomFall = null;
		int fallnumber = 0;
		for(int i = 0; i<size; i++){
			
			
								
				randomSession = new Session.SessionBuilder(cal.getTime())
											.name("sessione " + i)
											.duration(MILLISINHOUR*randNumber.nextInt(7)+1)
											.build();
				//Generating falls
				fallnumber = randNumber.nextInt(2);
				for(int j=0; j<fallnumber;j++){
					cal.add(Calendar.HOUR_OF_DAY,1);
					randomFall = new Fall.FallBuilder(cal.getTime())
											.session(randomSession.getSessionBegin())
											.fallNumber(j+1)
											.notified(randNumber.nextBoolean())
											.latitude(randNumber.nextDouble())
											.longitude(randNumber.nextDouble())
											.build();
					//Generating accelerometer data
					for(int h=0; h<1000; h++){
						accData.add(new AccelerometerData( randNumber.nextInt(100), 
															randNumber.nextInt(100),
															randNumber.nextInt(100)));
					}
					randomFall.setFallData((ArrayList<AccelerometerData>)accData.clone());
					accData.clear();
					falls.add(randomFall);
				}
				randomSession.setFallList((ArrayList<Fall>)falls.clone());
				sessions.add(randomSession);
				//prepare for the next iteration
				accData.clear();
				falls.clear();
				cal.add(Calendar.DAY_OF_MONTH, 1);
		}


	
		return sessions;
	}
}
