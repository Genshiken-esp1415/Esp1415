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
 * Classe per l'invio di e-mail con i dettagli di una caduta agli indirizzi forniti dall'utente. Si usano socket SSL
 * @author Marco
 */
public class NotificationSender extends AsyncTask<String, Void, String> {
	private static BufferedReader br;

	private SSLSocket socket;
	private String server;
	private int port;

	private String username;
	private String password;
	private ArrayList<String> dest;

	private String date="";
	private String time="";
	private String latitude="";
	private String longitude="";

	public AsyncInterface delegate;

	public NotificationSender(String username, String password, ArrayList<String> dest, AsyncInterface delegate){
		this.server = "smtp.gmail.com";
		this.port = 465;
		this.username = username;
		this.password = Base64.encodeToString(password.getBytes(),Base64.CRLF);
		this.dest = dest;
		this.delegate = delegate;
	}
	
	//Inizializzazione parametri della caduta
	public void buildMessage(String date, String time, String latitude, String longitude){
		this.date = date;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	//Task eseguita in background per l'invio delle e-mail
	@Override
	protected String doInBackground(String... params) {
		publishProgress();
		String str="";
		try{
			socket = (SSLSocket) ((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(server,port);
			socket.startHandshake();

			DataOutputStream buffer = new DataOutputStream(socket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "\r\nCaduta avvenuta in data "+date+" alle "+time+". Latitudine: "+latitude+", longitudine: "+longitude+".\r\n";
			String recipients = "";

			buffer.writeBytes("EHLO smtp.gmail.com\r\n");
			Thread.sleep(500);
			buffer.writeBytes("AUTH LOGIN\r\n");
			Thread.sleep(500);
			buffer.writeBytes(Base64.encodeToString(username.getBytes(),Base64.CRLF));
			Thread.sleep(500);
			buffer.writeBytes(password);
			Thread.sleep(500);
			buffer.writeBytes("MAIL From:<"+username+">\r\n");
			Thread.sleep(500);
			for(int i=0;i<dest.size();i++){
				buffer.writeBytes("RCPT To:<"+dest.get(i)+">\r\n");
				Thread.sleep(500);
				if(i!=dest.size()-1)
					recipients = recipients+"\""+dest.get(i)+"\", ";
				else
					recipients = recipients+"\""+dest.get(i)+"\"";
			}
			buffer.writeBytes("DATA\r\n");
			Thread.sleep(500);
			buffer.writeBytes("DATE:"+Calendar.getInstance(TimeZone.getTimeZone("GMT+1")).getTime()+"\r\n");
			buffer.writeBytes("From:"+username+"\r\n");
			Thread.sleep(500);
			buffer.writeBytes("To:"+recipients+"\r\n");
			Thread.sleep(500);
			buffer.writeBytes("Subject:Caduta avvenuta\r\n");
			Thread.sleep(500);
			buffer.writeBytes(message);
			buffer.writeBytes("\r\n.\r\n");
			Thread.sleep(500);
			buffer.writeBytes("QUIT\r\n");
			
			String line;
			while((line = br.readLine()) != null)
				str=str+line+"\r\n";
			
			socket.close();
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
		if(findErrors(str))
			return "inviata";
		else
			return "non inviata";
	}
	
	//Ritorna il risultato dell'invio delle e-mail di notifica (inviato o non inviato)
	protected void onPostExecute(String result){
		delegate.notificationUpdate(result);
	}
	
	/**
	 * Parser della risposta del server SMTP per rilevare eventuali errori in trasmissione
	 * Codici di risposta 4xx o 5xx indicano solitamente errori di qualche tipo (cfr. RFC821)
	 * L'invio corretto di e-mail così come previsto in questa classe prevede solo codici 2xx o 3xx
	 */
	private boolean findErrors(String response){
		if(response=="")
			return false;
		for(int i=0;i<response.length();i++){
			if(response.charAt(i)=='5'||response.charAt(i)=='4')
				return false;
			for(;i<response.length()&&response.charAt(i)!='\n';i++);
		}
		return true;
	}
}
