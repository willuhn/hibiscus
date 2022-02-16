/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.GV.parsers.SEPAParserFactory;
import org.kapott.hbci.sepa.SepaVersion;
import org.kapott.hbci.sepa.SepaVersion.Type;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;


/**
 * Abstrakte Basis-Klasse fuer SEPA-Import.
 */
public abstract class AbstractSepaImporter extends AbstractImporter
{
  private Map<String,Konto> kontenCache = new HashMap<String,Konto>();

  @Override
  public String getName()
  {
    return i18n.tr("SEPA-XML");
  }
  
  @Override
  String[] getFileExtensions()
  {
    return new String[]{"*.xml"};
  }
  
  @Override
  Object[] setup(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws Exception
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtil.copy(is,bos);
    
    SepaVersion version = SepaVersion.autodetect(new ByteArrayInputStream(bos.toByteArray()));
    if (version == null)
      throw new ApplicationException(i18n.tr("SEPA-Version der XML-Datei nicht ermittelbar"));
    
    monitor.log(i18n.tr("SEPA-Version: {0}",version.getURN()));
    
    // PAIN-Typ nicht kompatibel. User fragen, ob er trotzdem importieren  moechte
    SepaVersion.Type type = this.getSupportedPainType();
    if (type != null && type != version.getType())
    {
      boolean b = type == SepaVersion.Type.PAIN_001;
      String l = i18n.tr("Lastschrift");
      String u = i18n.tr("�berweisung");
      String q = i18n.tr("Sie versuchen, eine {0} als {1} zu importieren.\nVorgang wirklich fortsetzen?",b ? l : u, b ? u : l);
      
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle(i18n.tr("Warnung"));
      d.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
      d.setText("\n" + q + "\n");
      b = ((Boolean) d.open()).booleanValue();
      if (!b)
        throw new OperationCanceledException();
    }
    
    List<Properties> props = new ArrayList<Properties>();
    ISEPAParser parser = SEPAParserFactory.get(version);
    parser.parse(new ByteArrayInputStream(bos.toByteArray()),props);

    return props.toArray(new Properties[0]);
  }
  
  //final, damit Subklassen zuk�nftig nichts komisches machen k�nnen (siehe Exception-Handling in AbstractImporter#doImport)
  @Override
  final void commit(Object[] objects, IOFormat format, InputStream is, ProgressMonitor monitor) throws Exception
  {
    kontenCache.clear();
    super.commit(objects,format,is,monitor);
  }
  
  /**
   * Sucht nach dem Konto mit der angegebenen IBAN.
   * @param iban
   * @return das gefundene Konto oder wenn es nicht gefunden wurde, dann das vom Benutzer ausgewaehlte.
   * Die Funktion liefert nie <code>null</code> sondern wirft eine ApplicationException, wenn kein Konto ausgewaehlt wurde.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  protected Konto findKonto(String iban) throws RemoteException, ApplicationException
  {
    // Erstmal schauen, ob der User das Konto schonmal ausgewaehlt hat:
    Konto k = (Konto) kontenCache.get(iban);

    // Haben wir im Cache
    if (k != null)
      return k;

    // In der Datenbank suchen
    k = KontoUtil.findByIBAN(iban);

    // Nichts gefunden. Dann fragen wir den User
    if (k == null)
    {
      // Das Konto existiert nicht im Hibiscus-Datenbestand. Also soll der
      // User eines auswaehlen
      KontoAuswahlDialog d = new KontoAuswahlDialog(null,KontoFilter.FOREIGN,KontoAuswahlDialog.POSITION_CENTER);
      d.setText(i18n.tr("Konto {0} nicht gefunden\n" +
                        "Bitte w�hlen Sie das zu verwendende Konto aus.",iban == null || iban.length() == 0 ? i18n.tr("<unbekannt>") : iban));

      try
      {
        k = (Konto) d.open();
      }
      catch (OperationCanceledException oce)
      {
        throw new ApplicationException(i18n.tr("Auftrag wird �bersprungen"));
      }
      catch (Exception e)
      {
        throw new ApplicationException(i18n.tr("Fehler beim Ausw�hlen des Kontos"),e);
      }
    }

    if (k != null)
    {
      kontenCache.put(iban,k);
      return k;
    }
    throw new ApplicationException(i18n.tr("Kein Konto ausgew�hlt"));
  }
  
  /**
   * Versucht den Text als Double zu parsen.
   * @param s der Text.
   * @return das geparste Double oder NaN, wenn es nicht zu parsen ist.
   */
  protected double parseValue(String s)
  {
    if (s == null || s.trim().length() == 0)
      return Double.NaN;
    
    try
    {
      return Double.parseDouble(s);
    }
    catch (Exception e)
    {
      Logger.error("unable to parse as double: " + s,e);
    }
    return Double.NaN;
  }
  
  /**
   * Der zul�ssige SEPA PAIN-Typ.
   * Wird ben�tigt, damit eine Lastschrift nicht versehentlich als �berweisung importiert wird.
   * @return erlaubter SEPA PAIN-Typ.
   */
  abstract Type getSupportedPainType(); 

}
