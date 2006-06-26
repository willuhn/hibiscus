package de.willuhn.jameica.hbci.io;

public class EbayKontoData {

	protected String nummer;
	protected String inhaber;
	protected String blz;
		
	/**
	 * @param nummer
	 * @param inhaber
	 * @param blz
	 */
	public EbayKontoData(String nummer, String inhaber, String blz) {
		this.nummer = nummer;
		this.inhaber = inhaber;
		this.blz = blz;
	}

	public String getBlz() {
		return blz;
	}

	public String getInhaber() {
		return inhaber;
	}

	public String getNummer() {
		return nummer;
	}

	

}
