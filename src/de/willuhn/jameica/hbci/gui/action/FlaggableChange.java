/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Flaggable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Setz oder entfermnt die genannten Flags in ein oder mehreren Objekten.
 */
public class FlaggableChange implements Action
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private int flags   = 0;
  private boolean add = true;
  
  /**
   * ct.
   * @param flags die zu setzenden Flags.
   * @param add true, wenn Flags hinzugefuegt werden sollen. Andernfalls werden sie entfernt.
   */
  public FlaggableChange(int flags, boolean add)
  {
    this.flags = flags;
    this.add   = add;
  }

  /**
   * Erwartet ein Objekt vom Typ <code>Flaggable</code> oder <code>Flaggable[]</code>.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Datensätze aus"));

    if (!(context instanceof Flaggable) && !(context instanceof Flaggable[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Datensätze aus"));

    Flaggable[] objects = null;
    
    if (context instanceof Flaggable)
      objects = new Flaggable[]{(Flaggable) context};
    else
      objects = (Flaggable[]) context;

    if (objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Datensätze aus"));

    try
    {
      objects[0].transactionBegin();
      for (int i=0;i<objects.length;++i)
      {
        final int current = objects[i].getFlags();
        final boolean have = objects[i].hasFlag(this.flags);
        if (this.add && !have)
          objects[i].setFlags(current | this.flags);
        else if (!this.add && have)
          objects[i].setFlags(current ^ this.flags);
        
        this.postProcess(objects[i]);
        objects[i].store();
        
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(objects[i]));
      }
      objects[0].transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Änderungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
    }
		catch (Exception e)
		{
	    try {
	      objects[0].transactionRollback();
	    }
	    catch (Exception e1) {
	      Logger.error("unable to rollback transaction",e1);
	    }
	    
	    if (e instanceof ApplicationException)
	      throw (ApplicationException) e;

	    Logger.error("error while setting flags",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern der Änderungen"));
		}
  }
  
  /**
   * Optionales Postprocessing.
   * Kann von abgeleiteten Klassen ueberschrieben werden.
   * @param o das Objekt.
   * @throws Exception
   */
  protected void postProcess(Flaggable o) throws Exception
  {
  }
  
  /**
   * Liefert die zu setzenden Flags.
   * @return flags
   */
  protected int getFlags()
  {
    return flags;
  }
  
  /**
   * Liefert true, wenn die Flags gesetzt werden sollen.
   * @return true, wenn die Flags gesetzt werden sollen.
   */
  protected boolean getAdd()
  {
    return this.add;
  }
}
