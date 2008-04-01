/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/NachrichtBox.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/04/01 09:50:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * BUGZILLA 331
 * Zeigt neue System-nachrichten der Bank an.
 */
public class NachrichtBox extends AbstractBox implements Box
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public NachrichtBox()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("System-Nachrichten der Bank");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    DBIterator iterator = Settings.getDBService().createList(Nachricht.class);
    iterator.setOrder("order by datum desc"); // Neueste zuerst
    iterator.addFilter("gelesen is null or gelesen = 0");
    
    StringBuffer sb = new StringBuffer();
    sb.append("<form>");
    
    FormTextPart text = new FormTextPart();
    while (iterator.hasNext())
    {
      Nachricht n = (Nachricht) iterator.next();
      String s = n.getNachricht();

      // Formatieren der Nachrichten - die haben Festbreite
      s = s.replaceAll("( {2,})","<br/>");
      s = s.replaceAll("\n","<br/>");
      s = s.replaceAll("&", "&amp;");
      s = s.replaceAll("\"","&quot;");

      sb.append("<p>");
      
      sb.append("<b>");
      if (n.getDatum() != null)
      {
        sb.append(HBCI.DATEFORMAT.format(n.getDatum()));
        sb.append(" ");
      }
      
      // Mal schauen, ob wir eine BLZ haben
      String blz = n.getBLZ();
      if (blz != null)
        sb.append(i18n.tr("{0} [BLZ: {1}]", new String[] {HBCIUtils.getNameForBLZ(blz),blz}));
      sb.append("</b><br/> ");

      sb.append(s);
      sb.append("</p>");
    }
    sb.append("</form>");
    text.setText(sb.toString());
    text.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && isEnabled(); // Nicht konfigurierbar
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    try
    {
      DBIterator iterator = Settings.getDBService().createList(Nachricht.class);
      iterator.addFilter("gelesen is null or gelesen = 0");
      return iterator.hasNext(); // Wenn Nachrichten vorliegen, wird die Box automatisch aktiviert
    }
    catch (Exception e)
    {
      Logger.error("unable to check for new messages",e);
    }
    return super.isEnabled();
  }
  
  
}


/*********************************************************************
 * $Log: NachrichtBox.java,v $
 * Revision 1.5  2008/04/01 09:50:17  willuhn
 * @B Fehlendes XML-Escaping
 *
 * Revision 1.4  2008/04/01 09:46:15  willuhn
 * @R removed debug output
 *
 * Revision 1.3  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.2  2007/03/02 14:49:14  willuhn
 * @R removed old firststart view
 * @C do not show boxes on first start
 *
 * Revision 1.1  2006/11/16 22:29:46  willuhn
 * @N Bug 331
 *
 **********************************************************************/