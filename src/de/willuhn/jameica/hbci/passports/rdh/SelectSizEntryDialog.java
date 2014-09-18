/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/SelectSizEntryDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/24 09:06:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Ein Dialog zur Auswahl des zu importierenden SizRDH-Schluessels.
 */
public class SelectSizEntryDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private String data     = null;
  private Entry selected  = null;

  /**
   * ct.
   * @param position
   * @param data die vom Callback uebermittelten Schluesseldaten.
   */
  public SelectSizEntryDialog(int position, String data)
  {
    super(position);
    this.data = data;
    setTitle(i18n.tr("Schlüsselauswahl"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addText(i18n.tr("Bitte wählen Sie den zu verwendenden Schlüssel aus"),true);
    
    List<Entry> list = new ArrayList<Entry>();

    StringTokenizer tok = new StringTokenizer(data,"|");
    while (tok.hasMoreTokens())
    {
      Entry e = new Entry(tok.nextToken());
      list.add(e);
    }
    
    final TablePart table = new TablePart(list, new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Entry))
          return;
        Entry e = (Entry) context;
        selected = e;
        close();
      }
    });
    table.addColumn(i18n.tr("Bank"),"bank");
    table.addColumn(i18n.tr("Benutzerkennung"),"user");
    table.setMulti(false);
    table.setSummary(false);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = table.getSelection();
        if (o == null || !(o instanceof Entry))
          return;

        Entry e = (Entry) context;
        selected = e;
        close();
      }
    },null,false,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    buttons.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return selected.getId();
  }

  /**
   * Bean fuer den ausgewaehlten Schluessel.
   */
  public class Entry
  {

    private String id     = null;
    private String userid = null;
    private String bank   = null;
    
    private Entry(String data)
    {
      StringTokenizer tok = new StringTokenizer(data,";");

      this.id     = tok.nextToken();
      this.bank   = tok.nextToken();
      this.userid = tok.nextToken();
      
      try
      {
        String bankName = HBCIProperties.getNameForBank(this.bank);
        if (bankName != null && bankName.length() > 0)
          this.bank += " [" + bankName + "]";
      }
      catch (Exception e)
      {
        // ignore
      }
    }

    /**
     * Liefert die User-ID.
     * @return die User-ID.
     */
    public String getUser()
    {
      return this.userid;
    }
    
    /**
     * Liefert die Bank.
     * @return die Bank.
     */
    public String getBank()
    {
      return this.bank;
    }

    /**
     * Liefert die ID des Schluessels.
     * @return die ID des Schluessels.
     */
    public String getId()
    {
      return this.id;
    }
  }
}


/*********************************************************************
 * $Log: SelectSizEntryDialog.java,v $
 * Revision 1.2  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.1  2006/10/12 12:53:01  willuhn
 * @B bug 289 + Callback NEED_SIZENTRY_SELECT
 *
 **********************************************************************/