package de.willuhn.jameica.hbci.server.parser;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.logging.Logger;

/**
 * Dieser Parser versucht einen Workaround bei den Banken zu bieten, die unvollständige HBCI-Gegenkontendaten liefern und
 * beispielspweise den Inhaber des Gegenkontos mit in den Verwendungszweck packen.
 * 
 * Diese Klasse bietet daher die Möglichkeit, den Verwendungszweck zu parsen und daraus die Infos zum Gegenkonto zu gewinnen.
 *  
 * @author Michael Lambers
 * @date Aug 12, 2006
 * @version $Id: PsdBankParser.java,v 1.2 2007/03/12 14:18:19 willuhn Exp $
 *
 */
public class PsdBankParser implements UmsatzParser
{

	private static final int INHABER_INT           = 1;
	private static final int KONTONUMMER_INT       = 2;
	private static final int BLZ_INT               = 3;
	private static final int VERWENDUNGSZWECK_INT  = 4;
	private static final int FAELLT_WEG_INT		   = 5;
  
  //Das scheint bei der PSD Bank Westfalen Lippe (40090900) zu passen, wobei KTO/BLZ nur bei selbst initiierten Aktionen gefüllt ist
  private final static String expression = "#i\n#v\nKTO/BLZ #n/#b";
	
	/**
	 * Gibt den Teil von <code>daten</code> zurück, der vor <code>token</code> liegt. 
	 * @param daten
	 * @param token
	 * @return String-Array; 0. Element Reststring von <code>daten</code> nach <code>token</code>, 1. Element: der Teil vor <code>token</code>: 
	 */
	private static String[] leseBis(String daten, String token) {
		int pos = daten.indexOf(token);
		String ret[] = new String[2];
		
		if (pos == -1) {
			/* Wenn token nicht auffindbar, dann den ganzen String nehmen */
			ret[1] = daten;
			ret[0] = "";
		}
		else {
			/* Token gefunden: Teil bis token und Teil nach token zurückgeben */
			ret[1] = daten.substring(0, pos);
			ret[0] = daten.substring(pos+token.length());
		}
		return ret;
	}
	
	/**
	 * Parst einen Expressionstring, und gibt das Ergebnis in einer ArrayList zurück.
	 * An Stelle von Variablen (#irgendwas) werden die entsprechenden Integer-Codes zurückgegeben,
	 * statische Texte werden als Strings in die Liste gepackt.
	 * Dabei ist sichergestellt, dass immer Integer auf String folgt, nicht festgelegt ist allerdings der Anfang:
	 * Je nach Situation kann entweder ein String oder ein Integer beginnen.
	 * @param expression
	 * @return ArrayList
	 * @throws ParseException
	 */
	private static List exprParser() throws ParseException
  {
		List liste = new ArrayList();

		/* Aktuelle Position für die ParseException */
		int aktuellePosition = 0;
    String s = expression;
		
    boolean letztesZeichenWarVariable = false;
    while (s.length() != 0) {
				
			if (s.startsWith("#")) {
				if (letztesZeichenWarVariable) {
					throw new ParseException("Es können nicht zwei Variablen am Stück verwendet werden.", aktuellePosition);
				}
				letztesZeichenWarVariable = true;
				
				String variable = s.substring(0, 2);
				int code;
				if      (variable.equals("#i")) code = INHABER_INT;
				else if (variable.equals("#n")) code = KONTONUMMER_INT;
				else if (variable.equals("#b")) code = BLZ_INT;
				else if (variable.equals("#v")) code = VERWENDUNGSZWECK_INT; 
				else if (variable.equals("#w")) code = FAELLT_WEG_INT;
				else throw new ParseException("Fehler beim Parsen der Expression: " + variable + " ist unbekannt.", aktuellePosition);
				liste.add(new Integer(code));
				
				aktuellePosition += 2;
				s = s.substring(2);
			}
			else {
				letztesZeichenWarVariable = false;
				
				int pos = s.indexOf('#');
				if (pos == -1) {
					/* # kommt nicht vor, also ist der Rest ein String */
					liste.add(s);
					aktuellePosition += s.length();
					s = new String("");
				}
				else {
					/* Text bis zur nächsten # in die Liste packen und expression um den Teil vor # kürzen. */
					String text = s.substring(0, pos);
					liste.add(text);

					s = s.substring(pos);
					aktuellePosition += pos;
				}
			}
    }
			
		/* Als letztes Zeichen brauchen wir zwingend einen String, sonst wird der Wert für die letzte Variable später
		 * nicht korrekt verarbeitet. Damit es keine versehentlichen Kollisionen gibt, nehmen wir einen String,
		 * der so garantiert nicht in einem Umsatz vorkommt. */
		if (letztesZeichenWarVariable) {
			liste.add("%\"%?*&%/{!-");
		}
		
		/* Und noch die Liste zurückgeben*/
		return liste;
	}
	
	/**
	 * Parst die Daten mit der übergebenen Expression. Zurück kommt eine Map mit einem Umsatz,
	 * die zu folgenden Schlüsseln die entsprechenden Werte enthält.
	 * Gab es beim Parsen ein Problem, wird null zurückgegeben.
	 * Gibt es für einen Schlüssel keinen Wert, enthält die Map für diesen Schlüssel null.
	 * <li> Inhaber
	 * <li> Kontonummer
	 * <li> Bankleitzahl
	 * <li> Verwendungszweck
	 * <br>
	 * Eine gültige Expression besteht aus einem String mit Variablen.
	 * Feste Texte der Expression müssen auch in den Daten entsprechend vorkommen,
	 * an Stelle der Variablen wird der Text ermittelt, der in den Daten an der jeweiligen Stelle steht.
	 * Die anstelle der Variablen ermittelten Daten werden in der o. a. HashMap zurückgegeben.
	 * Variablen beginnen mit #, folgende Variablennamen sind definiert:
	 * <li> Inhaber: #i
	 * <li> Kontonummer: #n
	 * <li> Bankleitzahl: #b
	 * <li> Verwendungszweck: #v
	 * <li> Text wird ignoriert: #w
	 * <br>
	 * Wird eine Variable mehrfach angegeben, wird nur der Text zurückgegeben, der beim letztmaligen Vorkommen steht.
	 * Ausnahme ist der Verwendungszweck (#v): Tritt er mehrfach auf, werden die einzelnen Teile durch
	 * Zeilenumbrüche aneinandergehängt.
     * <br>
	 * Einige sonstige Tipps:
	 * <li> Zeilenumbruch: \n
	 * <br><br>
	 * Beispiel 1: <br>
	 * Als Daten werden geliefert: Max Mustermann\n123456789-40040000\nGeld fuers nixtun <br>
	 * Als Expression wird angegeben: #i\n#n-#b\n#v <br>
	 * Dann ergibt dies:
	 * <li> Inhaber: Max Mustermann
	 * <li> Kontonummer: 123456789
	 * <li> Bankleitzahl: 40040000
	 * <li> Verwendungszweck: Geld fuers nixtun
	 * <br> <br>
	 * Beispiel 2: <br>
	 * Als Daten werden geliefert: Max Mustermann\nGeld fuers nixtun\nKTO/BLZ 123456789/40040000 <br>
	 * Als Expression wird angegeben: #i\n#v\nKTO/BLZ #n/#b <br>
	 * Dann ergibt dies:
	 * <li> Inhaber: Max Mustermann
	 * <li> Kontonummer: 123456789
	 * <li> Bankleitzahl: 40040000
	 * <li> Verwendungszweck: Geld fuers nixtun
	 * <br>
   * @see de.willuhn.jameica.hbci.server.parser.UmsatzParser#parse(java.lang.String[], de.willuhn.jameica.hbci.rmi.Umsatz)
	 */
	/**
	 */
	public void parse(String[] lines, Umsatz umsatz) throws RemoteException
	{
		try {
      
			StringBuffer sb_gesamt = new StringBuffer();
			for (int i=0;i<lines.length;i++) {
				sb_gesamt.append(lines[i]);
				sb_gesamt.append('\n');
			}
      
			String daten = sb_gesamt.toString();
			ArrayList zweck = new ArrayList();
      
			/* Expression vorbereiten */
			List expr = exprParser();

      
			/* Check: Passt der Anfang der Daten (nur relevant, wenn keine Variable am Anfang der Expression)? */
			Object obj = expr.get(0);
			if (obj instanceof String) {
				/* Die Daten sollen mit einem bestimmten Text beginnen, dieser muss also passen, sonst ist es ein Fehler */
				String start = (String) obj;
				if (daten.startsWith(start) == false)
					throw new ParseException("Daten beginnen nicht mit dem vorgebenen Text " + start, 0);

				/* Daten um diesen statischen Teil kürzen */
				daten = daten.substring(start.length());
				
				/* Erstes Element rausnehmen, damit es unten mit einer Variablen losgeht */
				expr.remove(0);
			}
			
			/* Hier wird gespeichert, welcher Variablentyp zuletzt gefunden wurde. Daran wird beim darauffolgenden String
			 * festgemacht, was der dann soeben gelesene String darstellt */
			int letzterVariablentyp=0;
			
			for (Iterator it = expr.iterator(); it.hasNext(); ) {
				obj = it.next();
				
				/* Ab hier beginnt es definitiv mit einer Variablen,
				 * das erste if ist also beim ersten Durchlauf nicht true! */
				if (obj instanceof String) {
					
					/* Bis zum entsprechenden Token lesen, das Rückgabe-Array enthält dann:
					 * 0. Stelle: Den Reststring, der bei nächsten Durchlauf noch verarbeitet werden muss
					 * 1. Stelle: Den gefundenen String */
					String r[] = leseBis(daten, (String) obj);
					
					/* daten auf Reststring setzen */
					daten = r[0];
					
					/* Da die for-Schleife definitiv mit einer Variablen beginnt, ist letzterVariablentyp immer vorbelegt! */
					switch (letzterVariablentyp) {
						case INHABER_INT:
							umsatz.setEmpfaengerName(r[1]);
							break;
						case KONTONUMMER_INT:
							umsatz.setEmpfaengerKonto(r[1]);
							break;
						case BLZ_INT:
							umsatz.setEmpfaengerBLZ(r[1]);
							break;
						case VERWENDUNGSZWECK_INT:
							/* Sind in r[i] Zeilenumbrüche enthalten, führt das später
							 * bei der Anzeige des Verwendungszwecks zu komischen Darstellungen.
							 * Wir müssen also die Eingangsdaten splitten und pro Zeile
							 * einen Eintrag in der ArrayList vornehmen, dann klappts auch in der GUI */
							String[] splitted = r[1].split("\n");
							for (int i = 0; i < splitted.length; i++) {
								/* Leerzeilen im Verwendungszweck brauchen wir nicht */
								if (splitted[i].trim().length() != 0) {
									zweck.add(splitted[i]);
								}
							}
							break;
						case FAELLT_WEG_INT:
							/* Diese Daten wunschgemäß ignorieren */
							break;
						case 0:
							/* Darf eigentlich nie auftreten, wenn doch, dann haben wir einen Bug im exprParser */
							throw new ParseException("Zwei Texte am Stück", 0);
					}
					
					/* So, wir haben soeben einen Text für eine Variable verarbeitet, also letzterVariablentyp zurücksetzen */
					letzterVariablentyp = 0;
				}
				else {
					if (letzterVariablentyp != 0) {
						/* Darf eigentlich nicht auftreten, wenn doch, dann haben wir einen Bug im exprParser */
						throw new ParseException("Zwei Variablen am Stück", 0);
					}
					
					/* Wir haben eine Variable, also merken, welche das war */
					letzterVariablentyp = ((Integer) obj).intValue();
				}
			}
      
			// Jetzt noch die Verwendungszwecke anhaengen
			if (zweck.size() > 0) {
	    	  
//				/* DEBUG */
//				for (int x = 0; x < zweck.size(); x++) {
//					System.out.println("Zeile " + x + ": " + (String)zweck.get(x));
//				}
				  
				/* if (zweck.size() >= 1) */ // Können wir uns sparen. wenn > 0, dann gilt automatisch >= 1
				umsatz.setZweck((String)zweck.get(0)); // Bugfix: get(1) -> get(0)
				if (zweck.size() >= 2) umsatz.setZweck2((String)zweck.get(1)); // Bugfix: get(2) -> get(1)
				if (zweck.size() >= 3) {
					// Wenn noch mehr da ist, pappen wir den Rest zusammen in
					// den Kommentar
					StringBuffer sb = new StringBuffer();
					for (int i=2;i<zweck.size();++i) {
						sb.append(zweck.get(i).toString());
					}
					umsatz.setKommentar(sb.toString());
				}
			}
     
		} catch (ParseException e) {
			Logger.error("unable to parse data",e);
		}
	}
}

/*********************************************************************
 * $Log: PsdBankParser.java,v $
 * Revision 1.2  2007/03/12 14:18:19  willuhn
 * @C Michael's Aenderungen
 *
 * Revision 1.1  2007/02/26 12:48:23  willuhn
 * @N Spezial-PSD-Parser von Michael Lambers
 *
 *********************************************************************/
