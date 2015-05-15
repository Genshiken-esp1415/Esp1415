package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

/**
 * Classe contenitore per i dati relativi ad una sessione.
 * Presente un builder per sostituire il costruttore default.
 * Articoletto sui builder da leggere:
 * http://jlordiales.me/2012/12/13/the-builder-pattern-in-practice/
 */
public class Session {
	
	private Date SessionBegin;
	private String name;
	//int basta, i millisecondi di 8 ore sono di gran lunga inferiori a Integer.MAX_VALUE
	private int duration; 
	//questo campo serve per distingure quale sessione è attiva
	private boolean active;
	//arrayList con riferimenti alle cadute, tenuto ordinato per dataora di caduta
	private ArrayList<Fall> fallList;
	
	private Session(SessionBuilder builder) {
		this.SessionBegin = builder.sessionBegin;
		this.name = builder.name;
		this.duration = builder.duration;
		this.active = builder.active;
		this.fallList = builder.fallList;
	}
	
	public Date getSessionBegin() {
		return SessionBegin;
	}
	
	public void setSessionBegin(Date sessionBegin) {
		SessionBegin = sessionBegin;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public ArrayList<Fall> getFallList() {
		return fallList;
	}

	public void setFallList(ArrayList<Fall> fallList) {
		this.fallList = fallList;
	}
	
	public int getNumberOfFalls() {
		return fallList.size();
	}
	
	public static class SessionBuilder {
		private Date sessionBegin;
		private String name;
		private int duration; 
		private boolean active;
		private ArrayList<Fall> fallList;
		
		public SessionBuilder(Date sessionBegin) {
			this.sessionBegin = sessionBegin;
			// Inizializzo campi facoltativi, nel senso che posso non avere da
			// subito la lista di cadute o avere una sessione già attiva.
			fallList = new ArrayList<Fall>();
			active = false;
		}
		
		public SessionBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public SessionBuilder duration(int duration) {
			this.duration = duration;
			return this;
		}
		
		public SessionBuilder active(boolean active) {
			this.active = active;
			return this;
		}
		
		public SessionBuilder fallList(ArrayList<Fall> fallList) {
			this.fallList = fallList;
			return this;
		}
		
		public Session build() {
			return new Session(this);
		}
		
	}
	
}
