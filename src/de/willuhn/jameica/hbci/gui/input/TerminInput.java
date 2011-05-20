/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/TerminInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/20 16:22:31 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfiguriertes Eingabefeld fuer einen Termin.
 */
public class TerminInput extends DateInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Terminable auftrag = null;
  private Listener listener  = null;
  
  /**
   * ct.
   * @param auftrag der terminierbare Auftrag.
   * @throws RemoteException
   */
  public TerminInput(Terminable auftrag) throws RemoteException
  {
    super(getPreset(auftrag),HBCI.DATEFORMAT);
    this.auftrag = auftrag;
    
    // Deaktivieren, wenn ein ausgefuehrter Auftrag uebergeben wurde
    if (auftrag != null)
      this.setEnabled(!auftrag.ausgefuehrt());
    
    this.setName(i18n.tr("Termin"));
    this.setTitle(i18n.tr("Termin"));
    this.setText(i18n.tr("Bitte wählen Sie einen Termin"));
    this.setComment("");
    
    this.listener = new MyListener();
    this.listener.handleEvent(null); // einmal ausloesen
    this.addListener(this.listener);
  }
  
  /**
   * Liefert das Vorgabedatum fuer den Auftrag.
   * @param auftrag
   * @return das Vorgabedatum.
   * @throws RemoteException
   */
  private static Date getPreset(Terminable auftrag) throws RemoteException
  {
    if (auftrag == null)
      return new Date();
    Date date = auftrag.getTermin();
    return date != null ? date : new Date();
  }
  
  /**
   * Aktualisiert den Kommentar basierend auf den aktuellen Eigenschaften des Auftrages.
   */
  public void updateComment()
  {
    this.listener.handleEvent(null);
  }

  /**
   * Wird beim Aendern des Termins ausgeloest.
   */
  private class MyListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try
      {
        Date date = (Date) getValue();
        if (date == null)
        {
          setComment("");
          return;
        }

        if (auftrag == null)
          return;

        // Wir muessen den Termin im Objekt setzen, damit wir die
        // Faelligkeits-Entscheidung treffen koennen
        auftrag.setTermin(date);
        if (auftrag.ausgefuehrt())
        {
          // checken, ob wir auch noch das Ausfuehrungsdatum haben
          Date ausgefuehrt = auftrag.getAusfuehrungsdatum();
          if (ausgefuehrt != null)
            setComment(i18n.tr("Der Auftrag wurde am {0} ausgeführt",HBCI.DATEFORMAT.format(ausgefuehrt)));
          else
            setComment(i18n.tr("Der Auftrag wurde bereits ausgeführt"));
        }
        else if (auftrag.ueberfaellig())
          setComment(i18n.tr("Der Auftrag ist fällig"));
        else
          setComment("");
      }
      catch (Exception e)
      {
        Logger.error("unable to check overdue",e);
      }
    }
  }
}



/**********************************************************************
 * $Log: TerminInput.java,v $
 * Revision 1.1  2011/05/20 16:22:31  willuhn
 * @N Termin-Eingabefeld in eigene Klasse ausgelagert (verhindert duplizierten Code) - bessere Kommentare
 *
 **********************************************************************/