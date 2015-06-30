package it.unipd.dei.esp1415;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

import android.os.AsyncTask;
import android.util.Base64;

/**
 * Classe per l'invio di e-mail con i dettagli di una caduta agli indirizzi
 * forniti dall'utente. Si usano socket SSL.
 */
public class NotificationSender extends AsyncTask<String, Void, Boolean> {
	public final static boolean SENT = true;
	public final static boolean UNSENT = false;

	private SSLSocket mSocket;
	private String mServer;
	private int mPort;

	private String mUsername;
	private String mPassword;
	private ArrayList<String> mDest;

	private String mDate = "";
	private String mTime = "";
	private String mLatitude = "";
	private String mLongitude = "";

	public AsyncInterface delegate;

	public NotificationSender(String username, String password,
			ArrayList<String> dest, AsyncInterface delegate) {
		this.mServer = "smtp.gmail.com";
		this.mPort = 465;
		this.mUsername = username;
		this.mPassword = Base64
				.encodeToString(password.getBytes(), Base64.CRLF);
		this.mDest = dest;
		this.delegate = delegate;
	}

	/**
	 * Inizializzazione parametri della caduta
	 * 
	 * @param date
	 * @param time
	 * @param latitude
	 * @param longitude
	 */
	public void buildMessage(String date, String time, String latitude,
			String longitude) {
		this.mDate = date;
		this.mTime = time;
		this.mLatitude = latitude;
		this.mLongitude = longitude;
	}

	// Task eseguita in background per l'invio delle e-mail
	@Override
	protected Boolean doInBackground(String... params) {
		publishProgress();
		String str = "";
		try {
			// Inizializza il socket e stabilisce la connessione col server
			mSocket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory
					.getDefault()).createSocket(mServer, mPort);
			mSocket.startHandshake();

			// Prepara i buffer di input e di output per la comunicazione col
			// server
			DataOutputStream buffer = new DataOutputStream(
					mSocket.getOutputStream());
			BufferedReader sBr = new BufferedReader(new InputStreamReader(
					mSocket.getInputStream()));

			String message = "\r\nCaduta avvenuta in data " + mDate + " alle "
					+ mTime + ". Latitudine: " + mLatitude + ", longitudine: "
					+ mLongitude + ".\r\n";
			String recipients = "";

			// Invio e-mail (protocollo SMTP). Le attese dopo ogni invio sono
			// necessarie per assicurarsi che il server abbia il tempo
			// necessario per rispondere
			buffer.writeBytes("EHLO smtp.gmail.com\r\n");
			Thread.sleep(500);
			buffer.writeBytes("AUTH LOGIN\r\n");
			Thread.sleep(500);
			buffer.writeBytes(Base64.encodeToString(mUsername.getBytes(),
					Base64.CRLF));
			Thread.sleep(500);
			buffer.writeBytes(mPassword);
			Thread.sleep(500);
			buffer.writeBytes("MAIL From:<" + mUsername + ">\r\n");
			Thread.sleep(500);
			for (int i = 0; i < mDest.size(); i++) {
				buffer.writeBytes("RCPT To:<" + mDest.get(i) + ">\r\n");
				Thread.sleep(500);
				if (i != mDest.size() - 1) {
					recipients = recipients + "\"" + mDest.get(i) + "\", ";
				} else {
					recipients = recipients + "\"" + mDest.get(i) + "\"";
				}
			}
			buffer.writeBytes("DATA\r\n");
			Thread.sleep(500);
			buffer.writeBytes("DATE:"
					+ Calendar.getInstance(TimeZone.getTimeZone("GMT+1"))
							.getTime() + "\r\n");
			buffer.writeBytes("From:" + mUsername + "\r\n");
			Thread.sleep(500);
			buffer.writeBytes("To:" + recipients + "\r\n");
			Thread.sleep(500);
			buffer.writeBytes("Subject:Caduta avvenuta\r\n");
			Thread.sleep(500);
			buffer.writeBytes(message);
			buffer.writeBytes("\r\n.\r\n");
			Thread.sleep(500);
			buffer.writeBytes("QUIT\r\n");

			String line;
			while ((line = sBr.readLine()) != null) {
				str = str + line + "\r\n";
			}

			mSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Verifica se l'invio è avvenuto
		if (findErrors(str)) {
			return UNSENT;
		}
		return SENT;
	}

	/**
	 * Ritorna il risultato dell'invio delle e-mail di notifica (inviato o non
	 * inviato)
	 */
	protected void onPostExecute(Boolean result) {
		delegate.notificationUpdate(result);
	}

	/**
	 * Parser delle risposte del server SMTP per rilevare eventuali errori in
	 * trasmissione Codici di risposta 4xx o 5xx indicano solitamente errori di
	 * qualche tipo (cfr. RFC821) L'invio corretto di e-mail così come previsto
	 * in questa classe prevede solo codici 2xx o 3xx
	 * 
	 * @param response
	 * @return
	 */
	private boolean findErrors(String response) {
		if (response == "") {
			return false;
		}
		for (int i = 0; i < response.length(); i++) {
			if (response.charAt(i) == '5' || response.charAt(i) == '4') {
				return true;
			}
			for (; i < response.length() && response.charAt(i) != '\n'; i++);
		}
		return false;
	}
}
