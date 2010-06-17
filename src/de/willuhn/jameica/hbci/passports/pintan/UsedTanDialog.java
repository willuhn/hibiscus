/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/Attic/UsedTanDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:38:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Anzeige der verbrauchten TANs.
 * @author willuhn
 */
public class UsedTanDialog extends AbstractDialog
{
  private I18N i18n           = null;
  private PinTanConfig config = null;
  private TablePart table     = null;

  /**
   * ct.
   * @param config die PinTan-Config.
   */
  public UsedTanDialog(PinTanConfig config)
  {
    super(UsedTanDialog.POSITION_CENTER);
    this.config = config;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setSize(300,300);
    setTitle(i18n.tr("Verbrauchte TANs"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent,true);
    
    String[] tans = config.getUsedTans();
    
    ArrayList usedTans = new ArrayList();
    for (int i=0;i<tans.length;++i)
    {
      usedTans.add(new UsedTan(tans[i]));
    }
    
    GenericIterator list = PseudoIterator.fromArray((UsedTan[])usedTans.toArray(new UsedTan[usedTans.size()]));
    this.table = new TablePart(list,null);
    this.table.addColumn(i18n.tr("TAN"),"tan");
    this.table.addColumn(i18n.tr("Benutzt am"),"date",new DateFormatter(HBCI.LONGDATEFORMAT));
    this.table.setMulti(false);
    this.table.setRememberOrder(true);
    this.table.setSummary(true);
    this.table.setRememberColWidths(true);
    
    group.addPart(table);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Alle löschen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          boolean b = Application.getCallback().askUser(i18n.tr("Sind Sie sicher, dass Sie die Liste " +
          "der verbrauchten TANs löschen möchten?"));
          if (!b)
            return;

          config.clearUsedTans();
          table.removeAll();
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Liste der verbrauchten TANs gelöscht"),StatusBarMessage.TYPE_SUCCESS));
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to delete used tans",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der TANs"),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    buttons.addButton(i18n.tr("Schliessen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    
    },null,true);
  }

  /**
   * Hilfsklasse zum Anzeigen der TANs samt Verbrauchsdatum.
   * @author willuhn
   */
  private class UsedTan implements GenericObject
  {
    private String tan = null;
    private Date date  = null;
    
    /**
     * ct.
     * @param tan
     * @throws Exception
     */
    private UsedTan(String tan) throws Exception
    {
      this.tan  = tan;
      this.date = config.getTanUsed(this.tan);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if ("tan".equals(arg0))
        return this.tan;
      if ("date".equals(arg0))
        return this.date;
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"tan","date"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.tan;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "tan";
    }
  }
}


/*********************************************************************
 * $Log: UsedTanDialog.java,v $
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.1  2006/08/03 15:31:35  willuhn
 * @N Bug 62 completed
 *
 *********************************************************************/