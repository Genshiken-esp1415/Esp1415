package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

/**
 * Classe contenitore per i dati relativi ad una sessione. Presente un builder
 * per sostituire il costruttore default. Articoletto sui builder da leggere:
 * http://jlordiales.me/2012/12/13/the-builder-pattern-in-practice/
 */
public class Session {

	private Date mSessionBegin;
	private String mName;
	// int basta, i millisecondi di 8 ore sono di gran lunga inferiori a
	// Integer.MAX_VALUE
	private int mDuration;
	// questo campo serve per distingure quale sessione è attiva
	private boolean mActive;
	// arrayList con riferimenti alle cadute, tenuto ordinato per dataora di
	// caduta
	private ArrayList<Fall> mFallList;

	private Session(SessionBuilder builder) {
		this.mSessionBegin = builder.mSessionBegin;
		this.mName = builder.mName;
		this.mDuration = builder.mDuration;
		this.mActive = builder.mActive;
		this.mFallList = builder.mFallList;
	}

	public Date getSessionBegin() {
		return mSessionBegin;
	}

	public void setSessionBegin(Date sessionBegin) {
		mSessionBegin = sessionBegin;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getDuration() {
		return mDuration;
	}

	// Restituisce il nome del dile contenente la thumbnail
	public String getThumbnail() {
		String thumbnailName = DBManager.dateToSqlDate(this.mSessionBegin);
		return thumbnailName;
	}

	public void setDuration(int duration) {
		this.mDuration = duration;
	}

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		this.mActive = active;
	}

	public ArrayList<Fall> getFallList() {
		return mFallList;
	}

	public void setFallList(ArrayList<Fall> fallList) {
		this.mFallList = fallList;
	}

	public int getNumberOfFalls() {
		return mFallList.size();
	}

	public static class SessionBuilder {
		private Date mSessionBegin;
		private String mName;
		private int mDuration;
		private boolean mActive;
		private ArrayList<Fall> mFallList;

		public SessionBuilder(Date sessionBegin) {
			this.mSessionBegin = sessionBegin;
			// Inizializzo campi facoltativi, nel senso che posso non avere da
			// subito la lista di cadute o avere una sessione già attiva.
			mFallList = new ArrayList<Fall>();
			mActive = false;
		}

		public SessionBuilder name(String name) {
			this.mName = name;
			return this;
		}

		public SessionBuilder duration(int duration) {
			this.mDuration = duration;
			return this;
		}

		public SessionBuilder active(boolean active) {
			this.mActive = active;
			return this;
		}

		public SessionBuilder fallList(ArrayList<Fall> fallList) {
			this.mFallList = fallList;
			return this;
		}

		public Session build() {
			return new Session(this);
		}

	}

}
