/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines fix und fertig vorkonfigurierten Trees mit den existiernden Umsatz-Typen.
 */
public class UmsatzTypTree extends TreePart
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Initialisiert die Liste der Root-Elemente.
   * @return Liste der Root-Elemente.
   * @throws RemoteException
   */
  private final static GenericIterator init() throws RemoteException
  {
    return UmsatzTypUtil.getRootElements();
  }
  
  /**
   * ct.
   * @param action
   * @throws RemoteException
   */
  public UmsatzTypTree(Action action) throws RemoteException
  {
    super(init(), action);
    addColumn(i18n.tr("Bezeichnung"),"name");
    addColumn(i18n.tr("Reihenfolge"),"nummer"); // BUGZILLA 554/988
    addColumn(i18n.tr("Suchbegriff"),"pattern"); // BUGZILLA 756
    addColumn(i18n.tr("Umsatzart"),"umsatztyp",new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return i18n.tr("egal");
        return UmsatzTypUtil.getNameForType(((Integer) o).intValue());
      }
    });
    addColumn(i18n.tr("Konto"),"dummy");
    addColumn(i18n.tr("Kommentar"),"kommentar");
    
    this.setFormatter(new TreeFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TreeFormatter#format(org.eclipse.swt.widgets.TreeItem)
       */
      public void format(TreeItem item)
      {
        if (item == null)
          return;

        try
        {
          UmsatzTyp ut = (UmsatzTyp) item.getData();
          if (ut == null)
            return;
          
          final String kat = ut.getKontoKategorie();
          final Konto k = ut.getKonto();
          if (k != null)
            item.setText(4,KontoUtil.toString(k));
          else if (kat != null)
            item.setText(4,kat);

          ColorUtil.setForeground(item,-1,ut);
        }
        catch (Exception e)
        {
          Logger.error("unable to apply custom color",e);
        }
      }
    });

    this.setMulti(true);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(true);
    this.setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzTypList());
  }
  
  
  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    final MessageConsumer mc = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(mc);
    parent.addDisposeListener(new DisposeListener()
    {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
  }

  /**
   * Hilfsklasse, um ueber das Loeschen von Kategorien benachrichtigt zu werden.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ObjectDeletedMessage.class,ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      ObjectMessage msg = (ObjectMessage) message;
      GenericObject o = msg.getObject();
      if (!(o instanceof UmsatzTyp))
        return;
      
      GUI.getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          try
          {
            setList(init());
          }
          catch (RemoteException re)
          {
            Logger.error("unable to reload list",re);
          }
        }
      });
    }
    
  }
}


/**********************************************************************
 * $Log: UmsatzTypTree.java,v $
 * Revision 1.18  2011/06/08 08:12:48  willuhn
 * @C BUGZILLA 988 "Nummer" in "Reihenfolge" geaendert
 *
 * Revision 1.17  2011-02-09 08:32:16  willuhn
 * @B BUGZILLA 988
 *
 * Revision 1.16  2010/04/16 12:46:03  willuhn
 * @B Parent-ID beim Import von Kategorien beruecksichtigen und neu mappen - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=66546#66546
 *
 * Revision 1.15  2010/04/16 12:20:52  willuhn
 * @B Parent-ID beim Import von Kategorien beruecksichtigen und neu mappen
 *
 * Revision 1.14  2010/03/05 23:59:31  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.13  2010/03/05 23:29:18  willuhn
 * @N Statische Basis-Funktion zum Laden der Kategorien in der richtigen Reihenfolge
 *
 * Revision 1.12  2010/03/05 17:54:13  willuhn
 * @C Umsatz-Kategorien nach Nummer und anschliessend nach Name sortieren
 *
 * Revision 1.11  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 **********************************************************************/