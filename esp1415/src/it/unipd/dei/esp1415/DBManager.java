package it.unipd.dei.esp1415;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Questa classe incapsula l'interazione col database in modo da non dover
 * sporcare di opendb, rawquery, execsql, eccetera le activity. Contiene metodi
 * per l'inserimento di dati e il reperimento di dati in forma di array principalmente.
 * 
 *
 */
public class DBManager {
	// Campi del database
	private SQLiteDatabase mDatabase;
	private DBOpenHelper mDbHelper;
	private String[] mSessionColumns = { DBOpenHelper.COLUMN_TIMESTAMP_S,
			DBOpenHelper.COLUMN_NAME, DBOpenHelper.COLUMN_DURATION,
			DBOpenHelper.COLUMN_ACTIVE };
	private String[] mFallColumns = { DBOpenHelper.COLUMN_TIMESTAMP_F,
			DBOpenHelper.COLUMN_NUMBER, DBOpenHelper.COLUMN_NOTIFIED,
			DBOpenHelper.COLUMN_LATITUDE, DBOpenHelper.COLUMN_LONGITUDE,
			DBOpenHelper.COLUMN_SESSION };
	private String[] mAccelerometerColumns = { DBOpenHelper.COLUMN_TIMESTAMP_A,
			DBOpenHelper.COLUMN_X, DBOpenHelper.COLUMN_Y,
			DBOpenHelper.COLUMN_Z, DBOpenHelper.COLUMN_FALL };

	public DBManager(Context context) {
		mDbHelper = new DBOpenHelper(context);
		// this.context = context;
	}

	public void open() throws SQLException {
		mDatabase = mDbHelper.getWritableDatabase();
	}

	public void close() {
		mDbHelper.close();
	}

	// METODI CREATE
	/**
	 * Utilizzato per creare una nuova sessione. La sessione viene
	 * creata, impostata come attiva, inserita nel db e restituita al chiamante.
	 * 
	 * @param sessionName
	 *            Il nome della sessione che si vuole creare.
	 * 
	 */
	public void createSession(String sessionName) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.COLUMN_NAME, sessionName);
		values.put(DBOpenHelper.COLUMN_ACTIVE, 1);
		mDatabase.insert(DBOpenHelper.TABLE_SESSION, null, values);
		return ;
	}

	/**
	 * Crea una nuova caduta, relativa ad una certa sessione, nel db e la restituisce al chiamante.
	 * 
	 * @param fallTimestamp data e ora della caduta
	 * @param fallNumber numero progressivo della caduta
	 * @param latitude latitudine della posizione nella quale si stima sia avvenuta la caduta
	 * @param longitude  longitudine della posizione nella quale si stima sia avvenuta la caduta
	 * @param accData una lista di dati dell'accelerometro, relativi a 500ms prima e dopo la caduta.
	 * @param session il timestamp della sessione a cui fa riferimento la caduta
	 * @return	la caduta creata
	 */
	public Fall createFall(Date fallTimestamp, int fallNumber, Double latitude,
			Double longitude, LinkedList<AccelerometerData> accData,
			Date session) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.COLUMN_TIMESTAMP_F,
				dateToSqlDate(fallTimestamp));
		values.put(DBOpenHelper.COLUMN_NUMBER, fallNumber);
		values.put(DBOpenHelper.COLUMN_LATITUDE, latitude);
		values.put(DBOpenHelper.COLUMN_LONGITUDE, longitude);
		values.put(DBOpenHelper.COLUMN_SESSION, dateToSqlDate(session));
		Long insertIdF = mDatabase
				.insert(DBOpenHelper.TABLE_FALL, null, values);
		if (insertIdF == -1) {
			return null;
		}
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_FALL, mFallColumns,
				null, null, null, null, DBOpenHelper.COLUMN_TIMESTAMP_F
						+ " DESC");
		cursor.moveToFirst();
		Fall newFall = cursorToFall(cursor);
		// Inserisco i dati dell'accelerometro relativi alla caduta
		values.clear();
		values.put(DBOpenHelper.COLUMN_FALL,
				dateToSqlDate(newFall.getFallTimestamp()));
		for (int accIndex = 0; accIndex < accData.size(); accIndex++) {
			values.put(DBOpenHelper.COLUMN_TIMESTAMP_A, accData.get(accIndex)
					.getTimestamp());
			values.put(DBOpenHelper.COLUMN_X, accData.get(accIndex).getX());
			values.put(DBOpenHelper.COLUMN_Y, accData.get(accIndex).getY());
			values.put(DBOpenHelper.COLUMN_Z, accData.get(accIndex).getZ());
			mDatabase.insert(DBOpenHelper.TABLE_ACCELEROMETER, null, values);
		}
		cursor.close();
		return newFall;
	}
	
	//METODI DI UPDATE
	
	/**
	 * Rinomina la sessione. Passare come parametro
	 * la sessione con il campo name già aggiornato.
	 * 
	 * @param session
	 *            la sessione che si vuole rinominare nel db
	 * @return la sessione appena rinominata
	 */
	public Session renameSession(Session session) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.COLUMN_NAME, session.getName());
		String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S + " = ?";
		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(session.getSessionBegin());
		mDatabase.update(DBOpenHelper.TABLE_SESSION, values, whereClause,
				whereArgs);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, whereClause, whereArgs, null, null, null);
		cursor.moveToFirst();
		session = cursorToSession(cursor);
		cursor.close();
		return session;
	}

	/**
	 * Chiamare questo metodo per aggiornare la durata della sessione. 
	 * Passare come parametro la sessione con il campo durata già aggiornato.
	 * 
	 * @param session
	 *            la sessione di cui si vuole aggiornare la durata
	 * @return la sessione modificata
	 */
	public Session updateDuration(Session session) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.COLUMN_DURATION, session.getDuration());
		String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S + " = ?";
		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(session.getSessionBegin());
		mDatabase.update(DBOpenHelper.TABLE_SESSION, values, whereClause,
				whereArgs);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, whereClause, whereArgs, null, null, null);
		cursor.moveToFirst();
		session = cursorToSession(cursor);
		cursor.close();
		return session;
	}

	
	/**
	 * Setta la sessione come attiva o non attiva.
	 * Passare come parametro la sessione con il campo active già aggiornato.
	 * 
	 * @param session
	 *            la sessione di cui si vuole modificare il flag active.
	 * @return la sessione modificata
	 */
	public Session setActiveSession(Session session) {
		ContentValues values = new ContentValues();
		if (session.isActive()) {
			values.put(DBOpenHelper.COLUMN_ACTIVE, 1);
		} else {
			values.put(DBOpenHelper.COLUMN_ACTIVE, 0);
		}
		String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_S + " = ?";
		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(session.getSessionBegin());
		mDatabase.update(DBOpenHelper.TABLE_SESSION, values, whereClause,
				whereArgs);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, whereClause, whereArgs, null, null, null);
		cursor.moveToFirst();
		session = cursorToSession(cursor);
		cursor.close();
		return session;
	}

	/**
	 * Cancella una sessione presente nel db, data la sessione da cancellare.
	 * 
	 * @param session la sessione che si vuole cancellare.
	 */
	public void deleteSession(Session session) {
		Date timestamp = session.getSessionBegin();
		System.out.println("Comment deleted with timestamp: " + timestamp);
		mDatabase.delete(DBOpenHelper.TABLE_SESSION,
				DBOpenHelper.COLUMN_TIMESTAMP_S + " = '"
						+ dateToSqlDate(timestamp) + "'", null);
	}

	//METODI GETTER
	
	/**
	 * Restituisce una lista contenente tutte le sessioni presenti nella tabella
	 * SESSIONE.
	 * 
	 * @return una lista con tutte le sessioni nel db.
	 */
	public List<Session> getAllSessions() {
		List<Session> sessions = new ArrayList<Session>();

		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, null, null, null, null,
				DBOpenHelper.COLUMN_TIMESTAMP_S + " DESC");
		if (cursor.getCount() == 0) {
			cursor.close();
			return sessions;
		}
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Session session = cursorToSession(cursor);
			session.setFallList((ArrayList<Fall>) getAllFalls(session
					.getSessionBegin()));
			sessions.add(session);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return sessions;
	}
	
	/**
	 * Restituisce una lista contenente tutte le cadute relative ad una certa
	 * sessione. I dati dell'accelerometro non sono compresi, vanno recuperati
	 * con il metodo apposito getAccData.
	 * 
	 * @param sessionBegin
	 *            il timestamp della sessione di cui si vogliono recuperare le
	 *            cadute
	 * @return una lista con tutte le cadute relative alla sessione di interesse.
	 */
	public List<Fall> getAllFalls(Date sessionBegin) {
		List<Fall> falls = new ArrayList<Fall>();

		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(sessionBegin);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_FALL, mFallColumns,
				DBOpenHelper.COLUMN_SESSION + " = ?", whereArgs, null, null,
				DBOpenHelper.COLUMN_TIMESTAMP_F + " DESC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Fall fall = cursorToFall(cursor);
			falls.add(fall);
			cursor.moveToNext();
		}
		cursor.close();
		return falls;
	}

	/**
	 * Restituisce una lista contenente tutti i dati dell'accelerometro relativi ad una certa
	 * caduta.
	 * 
	 * @param fallTimestamp il timestamp della caduta di cui si vogliono recuperare i
	 *            dati dell'accelerometro
	 * @return una lista di tutti i dati dell'accelerometro relativa alla caduta di interesse
	 */
	public List<AccelerometerData> getAccData(Date fallTimestamp) {
		List<AccelerometerData> accDataList = new ArrayList<AccelerometerData>();

		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(fallTimestamp);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_ACCELEROMETER,
				mAccelerometerColumns,
				DBOpenHelper.COLUMN_TIMESTAMP_F + " = ?", whereArgs, null,
				null, DBOpenHelper.COLUMN_TIMESTAMP_A);
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
	 * Restituisce i dati di una sessione dato il timestamp.
	 * 
	 * @param sessionStart il timestamp della sessione di interesse.
	 * @return i dati della sessione.
	 */
	public Session getSession(Date sessionStart) {

		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(sessionStart);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, DBOpenHelper.COLUMN_TIMESTAMP_S + " = ?",
				whereArgs, null, null, null);

		cursor.moveToFirst();
		Session session = cursorToSession(cursor);
		cursor.close();
		return session;
	}

	/**
	 * Restituisce i dati della sessione attiva.
	 * 
	 * @return la sessione attiva
	 */
	public Session getActiveSession() {
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, DBOpenHelper.COLUMN_ACTIVE + " = 1", null,
				null, null, null);
		cursor.moveToFirst();
		Session session = cursorToSession(cursor);
		cursor.close();
		return session;
	}

	/**
	 *  Restituisce una caduta dato il timestamp.
	 * @param fallTimestamp timestamp della caduta di interesse
	 * @return i dati della caduta
	 */
	public Fall getFall(Date fallTimestamp) {

		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(fallTimestamp);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_FALL, mFallColumns,
				DBOpenHelper.COLUMN_TIMESTAMP_F + " = ?", whereArgs, null,
				null, null);

		cursor.moveToFirst();
		Fall fall = cursorToFall(cursor);
		cursor.close();
		return fall;
	}

	/**
	 * Chiamare questo metodo per settare che una caduta è stata notificata con
	 * successo. Da chiamare dopo aver settato notified a true nella caduta.
	 * 
	 * @param fall la caduta di cui si vuole cambiare lo stato della notifica.
	 * @return la caduta modificata.
	 */
	public Fall setNotified(Fall fall) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.COLUMN_NOTIFIED, 1);
		String whereClause = DBOpenHelper.COLUMN_TIMESTAMP_F + " = ?";
		String[] whereArgs = new String[1];
		whereArgs[0] = dateToSqlDate(fall.getFallTimestamp());
		mDatabase.update(DBOpenHelper.TABLE_FALL, values, whereClause,
				whereArgs);
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_FALL, mFallColumns,
				whereClause, whereArgs, null, null, null);
		cursor.moveToFirst();
		fall = cursorToFall(cursor);
		cursor.close();
		return fall;
	}

	/**
	 * Dice se c'è una sessione attiva in db.
	 * 
	 * @return vero se c'è una sessione attiva, falso altrimenti.
	 */
	public boolean hasActiveSession() {
		Cursor cursor = mDatabase.query(DBOpenHelper.TABLE_SESSION,
				mSessionColumns, DBOpenHelper.COLUMN_ACTIVE + " = " + 1, null,
				null, null, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		}
		cursor.close();
		return true;

	}

	/**
	 * Restituisce una sessione Session a partire da un cursor contenente dati
	 * di una riga di risposta ad una query sql.
	 * 
	 * @param cursor i dati di una riga di una risposta ad una query
	 * @return i dati della sessione
	 */
	private Session cursorToSession(Cursor cursor) {
		Session session = new Session.SessionBuilder(
				sqlDateToDate(cursor.getString(0))).name(cursor.getString(1))
				.duration(cursor.getInt(2)).active(cursor.getInt(3) != 0)
				.build();
		return session;
	}

	/**
	 * Restituisce una caduta Fall a partire da un cursor contenente dati
	 * di una riga di risposta ad una query sql.
	 * 
	 * @param cursor i dati di una riga di una risposta ad una query
	 * @return i dati della caduta
	 */
	private Fall cursorToFall(Cursor cursor) {
		Fall fall = new Fall.FallBuilder(sqlDateToDate(cursor.getString(0)))
				.fallNumber(cursor.getInt(1)).notified(cursor.getInt(2) != 0)
				.latitude(cursor.getDouble(3)).longitude(cursor.getDouble(4))
				.session(sqlDateToDate(cursor.getString(5))).build();
		return fall;
	}

	/**
	 * Restituisce dati dell'accelerometro AccelerometerData a partire da un cursor contenente dati
	 * di una riga di risposta ad una query sql.
	 * 
	 * @param cursor i dati di una riga di una risposta ad una query
	 * @return i dati dell'accelerometro
	 */
	private AccelerometerData cursorToAccData(Cursor cursor) {
		AccelerometerData accData = new AccelerometerData(cursor.getLong(0),
				cursor.getFloat(1), cursor.getFloat(2), cursor.getFloat(3));
		return accData;
	}

	/**
	 * Usato per convertire una stringa sql contenente una data in una data Date.
	 * 
	 * @param sqlDate stringa in formato data di sql
	 * @return una data
	 */
	private Date sqlDateToDate(String sqlDate) {
		Date date = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.ITALY);
		try {
			date = dateFormat.parse(sqlDate);
		} catch (ParseException e) {
		}
		return date;
	}

	/**
	 * Usato per convertire una data Date in una stringa contenente una data
	 * riconoscibile da sql.
	 * 
	 * @param date una data java
	 * @return una stringa formattata come da data di sql
	 */
	public static String dateToSqlDate(Date date) {
		SimpleDateFormat sqlDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.ITALY);
		String sqlDate = sqlDateFormat.format(date);
		return sqlDate;
	}

}
