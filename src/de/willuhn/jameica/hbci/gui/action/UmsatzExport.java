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

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Umsaetze exportieren werden koennen.
 * Als Parameter kann eine einzelnes Umsatz-Objekt oder ein Array uebergeben werden.
 */
public class UmsatzExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Umsatz[]</code>.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens einen Umsatz aus"));

    Umsatz[] u = null;
		try {
			if (context instanceof Umsatz)
			{
				u = new Umsatz[1];
        u[0] = (Umsatz) context;
			}
      else if (context instanceof Umsatz[])
      {
        u = (Umsatz[]) context;
      }
      else if (context instanceof UmsatzTreeNode)
      {
        UmsatzTreeNode node = (UmsatzTreeNode) context;
        List<Umsatz> result = new ArrayList<Umsatz>();
        collect(node,result);
        u = result.toArray(new Umsatz[0]);
      }
      else if (context instanceof UmsatzTreeNode[])
      {
        List<Umsatz> result = new ArrayList<Umsatz>();
        for (UmsatzTreeNode node:(UmsatzTreeNode[])context)
        {
          collect(node,result);
        }
        u = result.toArray(new Umsatz[0]);
      }

		   if (u == null || u.length == 0)
		      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Umsätze aus"));

      ExportDialog d = new ExportDialog(u, Umsatz.class);
      d.open();
		}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Umsätze"));
		}
  }
  
  /**
   * Sammelt rekursiv alle Umsaetze aus der Kategorie ein.
   * Unterkategorien werden mit beruecksichtigt.
   * BUGZILLA 1750.
   * @param node die Kategorie.
   * @param target die Liste, in der die Umsaetze gesammelt werden sollen.
   */
  private void collect(UmsatzTreeNode node, List<Umsatz> target)
  {
    target.addAll(node.getUmsaetze());
    List<UmsatzTreeNode> children = node.getSubGroups();
    if (children == null || children.size() == 0)
      return;
    
    for (UmsatzTreeNode c:children)
    {
      collect(c,target);
    }
  }
}
