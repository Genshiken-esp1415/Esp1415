package it.unipd.dei.esp1415;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Questo metodo incapsula l'interazione col database in modo da non dover
 * sporcare di opendb, rawquery, execsql, eccetera le activity. Conterrà metodi
 * per l'inserimento di dati e il reperimento di dati in forma di array,
 * tabelle, eccetera.
 * 
 * @author Andrea, Laura
 *
 */
public class DBManager {
	  // Database fields
	  private SQLiteDatabase database;
	  private DBOpenHelper dbHelper;
	  private String[] SessionColumns = {	DBOpenHelper.COLUMN_TIMESTAMP_S,
			  								DBOpenHelper.COLUMN_NAME,
			  								DBOpenHelper.COLUMN_DURATA,
			  								DBOpenHelper.COLUMN_ATTIVA};
	  private String[] FallColumns = {	DBOpenHelper.COLUMN_TIMESTAMP_F,
										DBOpenHelper.COLUMN_NUMBER,
										DBOpenHelper.COLUMN_NOTIFIED,
										DBOpenHelper.COLUMN_LATITUDE,
										DBOpenHelper.COLUMN_LONGITUDE,
										DBOpenHelper.COLUMN_SESSION};	 
	  private String[] AccelerometerColumns = {	DBOpenHelper.COLUMN_TIMESTAMP_A,
												DBOpenHelper.COLUMN_X,
												DBOpenHelper.COLUMN_Y,
												DBOpenHelper.COLUMN_Z,
												DBOpenHelper.COLUMN_FALL};  
	  

	  public DBManager(Context context) {
	    dbHelper = new DBOpenHelper(context);
	    //this.context = context;
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  /**
	   * Utilizzato quando si crea una nuova sessione. La sessione viene creata, inserita nel db e restituita al chiamante.
	   * @param sessionName Il nome della sessione che si vuole creare.
	   * @return
	   */
	  public Session createSession(String sessionName) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_NAME, sessionName);
	    Long insertId = database.insert(DBOpenHelper.TABLE_SESSION, null, values);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
	        SessionColumns, null, null,
	        null, null, DBOpenHelper.COLUMN_TIMESTAMP_S + " DESC");
	    cursor.moveToFirst();
	    Session newSession = cursorToSession(cursor);
	    cursor.close();
	    
	    /*Date newSessionBegin = newSession.getSessionBegin();
		Bitmap thumb = ThumbnailGenerator.createThumbnail(newSessionBegin); // genero la thumbnail
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //salvo la data come nome dell'immagine
		String name = formatter.format(newSessionBegin);
		String thumbnail;
		if(saveToInternalStorage(thumb, name)) //la salvo in memoria
			 {thumbnail = name;
			 newSession.setThumbnail(thumbnail);}*/
	    return newSession;
	  }
	 
	/*  public static boolean saveToInternalStorage(Bitmap image, String name) {

		  
		try {
			// Creo la directory nell'archivio interno
			File mydir = context.getDir("Thumbnails", Context.MODE_PRIVATE); 
			// Metto il file nella directory
			File fileWithinMyDir = new File(mydir, name); 
			// Stream per scrivere nel file
			FileOutputStream out = new FileOutputStream(fileWithinMyDir); 
			// Scrivo la bitmap nello stream
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}*/
	    
	  /**
	   * Utilizzato quando si crea una nuova sessione. La sessione viene creata, inserita nel db e restituita al chiamante.
	   * @param sessionName Il nome della sessione che si vuole creare.
	   * @return
	   */
	  public Fall createFall(int fallNumber, double latitude, double longitude, ArrayList<AccelerometerData> accData, Date session) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_NUMBER, fallNumber);
	    values.put(DBOpenHelper.COLUMN_LATITUDE, latitude);
	    values.put(DBOpenHelper.COLUMN_LONGITUDE, longitude);
	    values.put(DBOpenHelper.COLUMN_SESSION, dateToSqlDate(session));
	    Long insertIdF = database.insert(DBOpenHelper.TABLE_FALL, null, values);  
	    Cursor cursor = database.query(DBOpenHelper.TABLE_FALL,
	        FallColumns, null, null,
	        null, null, DBOpenHelper.COLUMN_TIMESTAMP_F + " DESC");
	    cursor.moveToFirst();
	    Fall newFall = cursorToFall(cursor);
	    //Inserisco i dati dell'accelerometro relativi alla caduta
	    values.clear(); 
	    values.put(DBOpenHelper.COLUMN_FALL, dateToSqlDate(newFall.getFallTimestamp()));
	    for(int accIndex = 0; accIndex < accData.size(); accIndex++){
	    	 values.put(DBOpenHelper.COLUMN_TIMESTAMP_A, accData.get(accIndex).getTimestamp());
	    	 values.put(DBOpenHelper.COLUMN_X, accData.get(accIndex).getX());
	    	 values.put(DBOpenHelper.COLUMN_Y, accData.get(accIndex).getY());
	    	 values.put(DBOpenHelper.COLUMN_Z, accData.get(accIndex).getZ());
	    	 Long insertIdA = database.insert(DBOpenHelper.TABLE_ACCELEROMETER, null, values);  	    	 
	    }
	    newFall.setFallData(accData);
		
	    cursor.close();
	    return newFall;
	  }
	  
	  /**
	   * Utilizzato quando si crea una nuova sessione. La sessione viene creata, inserita nel db e restituita al chiamante.
	   * @param sessionName Il nome della sessione che si vuole creare.
	   * @return
	   */
	  public Fall createFall(Date timestampFall, int fallNumber, Double latitude, Double longitude, LinkedList<AccelerometerData> accData, Date session) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_TIMESTAMP_F, dateToSqlDate(timestampFall));
	    values.put(DBOpenHelper.COLUMN_NUMBER, fallNumber);
	    values.put(DBOpenHelper.COLUMN_LATITUDE, latitude);
	    values.put(DBOpenHelper.COLUMN_LONGITUDE, longitude);
	    values.put(DBOpenHelper.COLUMN_SESSION, dateToSqlDate(session));
	    Long insertIdF = database.insert(DBOpenHelper.TABLE_FALL, null, values);  
	    if(insertIdF==-1){
	    	return null;
	    }
	    Cursor cursor = database.query(DBOpenHelper.TABLE_FALL,
	        FallColumns, null, null,
	        null, null, DBOpenHelper.COLUMN_TIMESTAMP_F + " DESC");
	    cursor.moveToFirst();
	    Fall newFall = cursorToFall(cursor);
	    //Inserisco i dati dell'accelerometro relativi alla caduta
	    values.clear(); 
	    values.put(DBOpenHelper.COLUMN_FALL, dateToSqlDate(newFall.getFallTimestamp()));
	    for(int accIndex = 0; accIndex < accData.size(); accIndex++){
	    	 values.put(DBOpenHelper.COLUMN_TIMESTAMP_A, accData.get(accIndex).getTimestamp());
	    	 values.put(DBOpenHelper.COLUMN_X, accData.get(accIndex).getX());
	    	 values.put(DBOpenHelper.COLUMN_Y, accData.get(accIndex).getY());
	    	 values.put(DBOpenHelper.COLUMN_Z, accData.get(accIndex).getZ());
	    	 Long insertIdA = database.insert(DBOpenHelper.TABLE_ACCELEROMETER, null, values);  	    	 
	    }
	    //newFall.setFallData(accData);
		
	    cursor.close();
	    return newFall;
	  }
	  
	  
	  /**
	   * chiamare questo metodo per rinominare la sessione.
	   * Passare come parametro la sessione con già il campo name aggiornato!
	   * @param session la sessione che si vuole rinominare
	   * @return 
	   */
	  public Session renameSession(Session session) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_NAME, session.getName());
	    String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S +" = ?";
	    String[] whereArgs = new String[1];
	    whereArgs[0] =  dateToSqlDate(session.getSessionBegin());
	    int updateId = database.update(DBOpenHelper.TABLE_SESSION, values, whereClause, whereArgs);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
		        SessionColumns, whereClause, whereArgs,
		        null, null, null);
	    cursor.moveToFirst();
	    session = cursorToSession(cursor);
	    cursor.close();
	    return session;
	  }
	  /**
	   * chiamare questo metodo per settare la sessione come attiva o non attiva.
	   * Passare come parametro la sessione con già il campo active aggiornato!
	   * @param session la sessione che si vuole aggiornare
	   * @return 
	   */
	  public Session updateDuration(Session session) {
	    ContentValues values = new ContentValues();
	    	values.put(DBOpenHelper.COLUMN_DURATA, session.getDuration());
	    String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S +" = ?";
	    String[] whereArgs = new String[1];
	    whereArgs[0] =  dateToSqlDate(session.getSessionBegin());
	    int updateId = database.update(DBOpenHelper.TABLE_SESSION, values, whereClause, whereArgs);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
		        SessionColumns, whereClause, whereArgs,
		        null, null, null);
	    cursor.moveToFirst();
	    session = cursorToSession(cursor);
	    cursor.close();
	    return session;
	  }
	  
	  /**
	   * chiamare questo metodo per aggiornare la durata della sessione.
	   * Passare come parametro la sessione con già il campo durata aggiornato!
	   * @param session la sessione che si vuole aggiornare
	   * @return 
	   */
	  public Session setActiveSession(Session session) {
	    ContentValues values = new ContentValues();
	    if(session.isActive()){
	    	values.put(DBOpenHelper.COLUMN_ATTIVA, 1);
	    }
	    else {
	    	values.put(DBOpenHelper.COLUMN_ATTIVA, 0);
	    }
	    String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S +" = ?";
	    String[] whereArgs = new String[1];
	    whereArgs[0] =  dateToSqlDate(session.getSessionBegin());
	    int updateId = database.update(DBOpenHelper.TABLE_SESSION, values, whereClause, whereArgs);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
		        SessionColumns, whereClause, whereArgs,
		        null, null, null);
	    cursor.moveToFirst();
	    session = cursorToSession(cursor);
	    cursor.close();
	    return session;
	  }

	  /** 
	   * Cancella una sessione presente nel db, data la sessione da cancellare
	   * @param session
	   */
	  public void deleteSession(Session session) {
	    Date timestamp = session.getSessionBegin();
	    System.out.println("Comment deleted with timestamp: " + timestamp);
	    database.delete(DBOpenHelper.TABLE_SESSION, DBOpenHelper.COLUMN_TIMESTAMP_S
	        + " = " + dateToSqlDate(timestamp), null);
	  }

	  /**
	   * restituisce una lista contenente tutte le sessioni presenti nella tabella SESSIONE
	   * @return
	   */
	  public List<Session> getAllSessions() {
	    List<Session> sessions = new ArrayList<Session>();

	    Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
	        SessionColumns, null, null, null, null, DBOpenHelper.COLUMN_TIMESTAMP_S+" DESC");
	    if(cursor.getCount()==0){
			  cursor.close();
			  return sessions;
		  }
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Session session = cursorToSession(cursor);
	      session.setFallList((ArrayList<Fall>)getAllFalls(session.getSessionBegin()));
	      sessions.add(session);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return sessions;
	  }
	  
	  /**
	   * restituisce una lista contenente tutte le cadute relative ad una certa sessione
	   * @return
	   */
	  public List<Fall> getAllFalls(Date sessionBegin) {
		  List<Fall> falls = new ArrayList<Fall>();

		  String[] whereArgs = new String[1];
		  whereArgs[0] =  dateToSqlDate(sessionBegin);
		  Cursor cursor = database.query(DBOpenHelper.TABLE_FALL,
				  FallColumns, DBOpenHelper.COLUMN_SESSION + " = ?", whereArgs, null, null, DBOpenHelper.COLUMN_TIMESTAMP_F+" DESC");
		  cursor.moveToFirst();
//		  if(cursor.getCount()<1){
//			  cursor.close();
//			  return falls;
//		  }
		  while (!cursor.isAfterLast()) {
			  Fall fall = cursorToFall(cursor);
			  falls.add(fall);
			  cursor.moveToNext();
		  }
		  // make sure to close the cursor
		  cursor.close();
		  return falls;
	  }

	  public List<AccelerometerData> getAccData(Date fallTimestamp){
		  	List<AccelerometerData> accDataList = new ArrayList<AccelerometerData>();

		    String[] whereArgs = new String[1];
		    whereArgs[0] =  dateToSqlDate(fallTimestamp);
		    Cursor cursor = database.query(DBOpenHelper.TABLE_ACCELEROMETER,
		    		AccelerometerColumns, DBOpenHelper.COLUMN_TIMESTAMP_F + " = ?" , whereArgs,
		    		null, null, DBOpenHelper.COLUMN_TIMESTAMP_A);
		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		    	AccelerometerData accData = cursorToAccData(cursor);
		    	accDataList.add(accData);
		    	cursor.moveToNext();
		    }
		    cursor.close();
		    return accDataList;
	  }
	  
	  /**
	   * restituisce una sessione dato il timestamp
	   * @return
	   */
	  public Session getSession(Date sessionBegin) {

		  String[] whereArgs = new String[1];
		  whereArgs[0] =  dateToSqlDate(sessionBegin);
		  Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
				  SessionColumns, DBOpenHelper.COLUMN_TIMESTAMP_S + " = ?", whereArgs, null, null, null);

		  cursor.moveToFirst();
		  Session session = cursorToSession(cursor);


		  // make sure to close the cursor
		  cursor.close();
		  return session;
	  }
	  
	  /**
	   * restituisce la sessione attiva
	   * @return
	   */
	  public Session getActiveSession() {

		  Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
				  SessionColumns, DBOpenHelper.COLUMN_ATTIVA + " = 1", null, null, null, null);
		  cursor.moveToFirst();
		  Session session = cursorToSession(cursor);
		  // make sure to close the cursor
		  cursor.close();
		  return session;
	  }
	  
	  /**
	   * restituisce una caduta dato il timestamp
	   * @return
	   */
	  public Fall getFall(Date fallTimestamp) {

		  String[] whereArgs = new String[1];
		  whereArgs[0] =  dateToSqlDate(fallTimestamp);
		  Cursor cursor = database.query(DBOpenHelper.TABLE_FALL,
				  FallColumns, DBOpenHelper.COLUMN_TIMESTAMP_F + " = ?", whereArgs, null, null, null);

		  cursor.moveToFirst();
		  Fall fall = cursorToFall(cursor);


		  // make sure to close the cursor
		  cursor.close();
		  return fall;
	  }
	  

	  /**
	   * chiamare questo metodo per settare che una caduta è stata notificata con successo
	   * Da chiamare dopo aver settato notified a true nella caduta!
	   * @param fall
	   * @return
	   */
	  public Fall setNotified(Fall fall) {
	    ContentValues values = new ContentValues();
	    values.put(DBOpenHelper.COLUMN_NOTIFIED, 1);
	    String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_F +" = ?";
	    String[] whereArgs = new String[1];
	    whereArgs[0] =  dateToSqlDate(fall.getFallTimestamp());
	    int updateId = database.update(DBOpenHelper.TABLE_FALL, values, whereClause, whereArgs);
	    Cursor cursor = database.query(DBOpenHelper.TABLE_FALL,
		        SessionColumns, whereClause, whereArgs,
		        null, null, null);
	    cursor.moveToFirst();
	    fall = cursorToFall(cursor);
	    cursor.close();
	    return fall;
	  }
	  /**Usato per sapere se c'è una sessione attiva nel database
	   * @return
	   */
	  public boolean hasActiveSession(){
		  Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
				  SessionColumns, DBOpenHelper.COLUMN_ATTIVA + " = " + 1, null, null, null, null);
		  if (cursor.getCount () == 0) {cursor.close(); return false;}
		  cursor.close();
		  return true; 
		 
		  
	  }
	  
//	  /**Usato per sapere se c'è una sessione attiva nel database
//	   * @return
//	   */
//	  public boolean hasActiveSession(){
//		  Cursor cursor = database.query(DBOpenHelper.TABLE_SESSION,
//				  SessionColumns, DBOpenHelper.COLUMN_ATTIVA + " = " + 1, null, null, null, null);
//		  if (cursor.getCount () == 0) return false;
//		  return true;
//		  
//	  }
//	  
	  /**
	   * Restituisce una sessione Session a partire da un cursor contenente dati di una riga di una query sql
	   * @param cursor
	   * @return
	   */
	  private Session cursorToSession(Cursor cursor) {
	    Session session = new Session.SessionBuilder(sqlDateToDate(cursor.getString(0)))
	    								.name(cursor.getString(1))
	    								.duration(cursor.getInt(2))
	    								.active(cursor.getInt(3)!=0)
	    								.build();	    							
	    return session;
	  }
	  
	  /**
	   * Restituisce una sessione Session a partire da un cursor contenente dati di una riga di una query sql
	   * @param cursor
	   * @return
	   */
	  private Fall cursorToFall(Cursor cursor) {
	    Fall fall = new Fall.FallBuilder(sqlDateToDate(cursor.getString(0)))
	    								.fallNumber(cursor.getInt(1))
	    								.notified(cursor.getInt(2)!=0)
	    								.latitude(cursor.getDouble(3))
	    								.longitude(cursor.getDouble(4))
	    								.session(sqlDateToDate(cursor.getString(5)))
	    								.build();	    							
	    return fall;
	  }
	  
	  private AccelerometerData cursorToAccData(Cursor cursor){
		  AccelerometerData accData = new AccelerometerData(cursor.getLong(0),
				  											  cursor.getFloat(1),
															  cursor.getFloat(2),
															  cursor.getFloat(3));
		  return accData;
	  }
	  
	  /**
	   * Usato per convertire una stringa sql contenente una data in una data Date
	   * @param sqlDate
	   * @return
	   */
	  private Date sqlDateToDate(String sqlDate){
		  Date date = null;
		  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		  try {
			  date = dateFormat.parse(sqlDate);
		  }
		  catch (ParseException e) {		  
		  }
		  return date;
	  }
	  
	  
	  /**
	   * Usato per convertire una data Date in una stringa contenente una data riconoscibile da sql
	   * @param date
	   * @return
	   */
	  public static String dateToSqlDate(Date date){
		  SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		  String sqlDate = sqlDateFormat.format(date);
		  return sqlDate;
	  }
	  
	  /**
	   * metodo usato per popolare il db con dati casuali, only for testing
	   */
	  public void dummyInsert(){
		  ArrayList<Session> sessions = new ArrayList<Session>();
		  sessions = Randomizer.randomSession(10);
		  //metto una sessione attiva per test
		  sessions.get(0).setActive(true);
		  ContentValues values = new ContentValues();
		  for(int i = 0; i<sessions.size();i++){
			  values.clear();
			  values.put(DBOpenHelper.COLUMN_TIMESTAMP_S, dateToSqlDate(sessions.get(i).getSessionBegin()));
			  values.put(DBOpenHelper.COLUMN_NAME, sessions.get(i).getName());
			  values.put(DBOpenHelper.COLUMN_DURATA, sessions.get(i).getDuration());
			  values.put(DBOpenHelper.COLUMN_ATTIVA, sessions.get(i).isActive());
			  Long insertId = database.insert(DBOpenHelper.TABLE_SESSION, null, values);
			  for(int j = 0; j<sessions.get(i).getFallList().size();j++){
				  values.clear();
				  values.put(DBOpenHelper.COLUMN_TIMESTAMP_F, dateToSqlDate(sessions.get(i).getFallList().get(j).getFallTimestamp()));
				  values.put(DBOpenHelper.COLUMN_NUMBER, sessions.get(i).getFallList().get(j).getFallNumber());
				  values.put(DBOpenHelper.COLUMN_LATITUDE, sessions.get(i).getFallList().get(j).getLatitude());
				  values.put(DBOpenHelper.COLUMN_LONGITUDE, sessions.get(i).getFallList().get(j).getLongitude());
				  values.put(DBOpenHelper.COLUMN_SESSION, dateToSqlDate(sessions.get(i).getFallList().get(j).getSession()));
				  values.put(DBOpenHelper.COLUMN_NOTIFIED, sessions.get(i).getFallList().get(j).isNotified());
				  Long insertIdF = database.insert(DBOpenHelper.TABLE_FALL, null, values);  
				  for(int z = 0; z<sessions.get(i).getFallList().get(j).getFallData().size();z++){
					  values.clear();
					  values.put(DBOpenHelper.COLUMN_FALL, dateToSqlDate(sessions.get(i).getFallList().get(j).getFallTimestamp()));
					  values.put(DBOpenHelper.COLUMN_TIMESTAMP_A, z);
					  values.put(DBOpenHelper.COLUMN_X, sessions.get(i).getFallList().get(j).getFallData().get(z).getX());
					  values.put(DBOpenHelper.COLUMN_Y, sessions.get(i).getFallList().get(j).getFallData().get(z).getY());
					  values.put(DBOpenHelper.COLUMN_Z, sessions.get(i).getFallList().get(j).getFallData().get(z).getZ());
					  Long insertIdA = database.insert(DBOpenHelper.TABLE_ACCELEROMETER, null, values); 
				  }
			  }
		  }
	  }
	  

}
