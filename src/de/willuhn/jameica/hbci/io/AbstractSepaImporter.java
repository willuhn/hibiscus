/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
import org.kapott.hbci.sepa.PainVersion;

import de.willuhn.io.IOUtil;
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

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("SEPA-XML");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractExporter#getFileExtensions()
   */
  @Override
  String[] getFileExtensions()
  {
    return new String[]{"*.xml"};
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#setup(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  @Override
  Object[] setup(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws Exception
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtil.copy(is,bos);
    
    PainVersion version = PainVersion.autodetect(new ByteArrayInputStream(bos.toByteArray()));
    if (version == null)
      throw new ApplicationException(i18n.tr("SEPA-Version der XML-Datei nicht ermittelbar"));
    
    monitor.log(i18n.tr("SEPA-Version: {0}",version.getURN()));
    
    // Überprüfe PAIN Typ
    PainVersion.Type[] types = this.getSupportedPainTypes();
    PainVersion.Type type = version.getType();
    boolean found = false;
    for (int i = 0; types != null && i < types.length; i++) {
      if (types[i] == type) {
        found = true;
      }
    }
    if (!found) {
      Logger.error("invalid file PAIN type");
      monitor.log(i18n.tr("Ungültiger PAIN Typ: {0}",type.getName()));
      if (!this.useForce) {
        throw new ApplicationException(i18n.tr("Unzulässige SEPA-Version in der XML-Datei - Überweisung und Lastschrift verwechselt?"));
      }
    }

    List<Properties> props = new ArrayList<Properties>();
    ISEPAParser parser = SEPAParserFactory.get(version);
    parser.parse(new ByteArrayInputStream(bos.toByteArray()),props);
    
    return props.toArray(new Properties[props.size()]);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#commit(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  @Override
  void commit(Object[] objects, IOFormat format, InputStream is, ProgressMonitor monitor) throws Exception
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
                        "Bitte wählen Sie das zu verwendende Konto aus.",iban == null || iban.length() == 0 ? i18n.tr("<unbekannt>") : iban));

      try
      {
        k = (Konto) d.open();
      }
      catch (OperationCanceledException oce)
      {
        throw new ApplicationException(i18n.tr("Auftrag wird übersprungen"));
      }
      catch (Exception e)
      {
        throw new ApplicationException(i18n.tr("Fehler beim Auswählen des Kontos"),e);
      }
    }

    if (k != null)
    {
      kontenCache.put(iban,k);
      return k;
    }
    throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));
  }

  /**
   * Liste der zulässigen XML Daten
   * Wird benötigt, damit eine Lastschrift nicht als Überweisung importiert werden kann.
   * @return erlaubte PAIN Typen
   */
  abstract PainVersion.Type[] getSupportedPainTypes();

}
