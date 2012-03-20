package org.anuta.xmltv.grabber.tvgidsnl.json;

public class JsonProgram {

	
	private String db_id;
	private String is_highlight;
	private String titel;
	private String genre;
	private String soort;
	private String kijkwijzer;
	private String artikel_id;
	private String datum_start; // 2012-03-12 23:55:00
	private String datum_end; // 2012-03-13 00:15:00

	public String getDb_id() {
		return db_id;
	}
	public void setDb_id(String db_id) {
		this.db_id = db_id;
	}
	public String getTitel() {
		return titel;
	}
	public void setTitel(String titel) {
		this.titel = titel;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getSoort() {
		return soort;
	}
	public void setSoort(String soort) {
		this.soort = soort;
	}
	public String getKijkwijzer() {
		return kijkwijzer;
	}
	public void setKijkwijzer(String kijkwijzer) {
		this.kijkwijzer = kijkwijzer;
	}
	public String getArtikel_id() {
		return artikel_id;
	}
	public void setArtikel_id(String artikel_id) {
		this.artikel_id = artikel_id;
	}
	public String getDatum_start() {
		return datum_start;
	}
	public void setDatum_start(String datum_start) {
		this.datum_start = datum_start;
	}
	public String getDatum_end() {
		return datum_end;
	}
	public void setDatum_end(String datum_end) {
		this.datum_end = datum_end;
	}
	public void setIs_highlight(String is_highlight) {
		this.is_highlight = is_highlight;
	}
	public String getIs_highlight() {
		return is_highlight;
	}
}
