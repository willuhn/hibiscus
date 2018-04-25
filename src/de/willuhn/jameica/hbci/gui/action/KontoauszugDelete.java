/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Kontoauszuges.
 */
public class KontoauszugDelete implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Kontoauszug</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Kontoauszug[] list = null;
    if (context instanceof Kontoauszug)
      list = new Kontoauszug[]{(Kontoauszug)context};
    else if (context instanceof Kontoauszug[])
      list = (Kontoauszug[]) context;
    
    if (list == null || list.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Kontoauszüge aus"));

		try
		{
	    final int size = list.length;
	    final String file = size == 1 ? list[0].getDateiname() : null;
	    
	    final CheckboxInput check = new CheckboxInput(false);
	    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER)
	    {
	      protected void extend(Container container) throws Exception
	      {
	        String text = null;
	        
	        if (file != null)
	        {
            text = i18n.tr("Datei \"{0}\" ebenfalls löschen",file);
	        }
	        else
	        {
	          // Es kann sein, dass wir nur eine Datei habe und trotzdem keinen Dateinamen
	          // ... naemlich wenn die per Messaging gespeichert wurde
	          if (size > 1)
	            text = i18n.tr("{0} zugehörige Dateien ebenfalls löschen",Integer.toString(size));
	          else
              text = i18n.tr("Zugehörige Datei ebenfalls löschen");
	        }
	        
          final LabelInput warn = new LabelInput("");
          warn.setColor(Color.ERROR);
          check.addListener(new Listener() {
            public void handleEvent(Event event)
            {
              // Warnhinweis anzeigen, dass der Auftrag nur lokal geloescht wird
              Boolean b = (Boolean) check.getValue();
              if (b.booleanValue())
                warn.setValue(size > 1 ? i18n.tr("{0} Dateien werden gelöscht",Integer.toString(size)) : i18n.tr("Die Datei wird ebenfalls gelöscht"));
              else
                warn.setValue("");
            }
          });
          container.addCheckbox(check,text);
          container.addLabelPair("",warn);
	        super.extend(container);
	      }
	    };
	    d.setTitle(i18n.tr("Kontoauszug löschen"));
	    d.setText(i18n.tr(size == 1 ? "Wollen Sie diesen Kontoauszug wirklich löschen?" : "Wollen Sie diese Kontoauszüge wirklich löschen?"));
	    d.setSize(350,SWT.DEFAULT);

	    Boolean choice = (Boolean) d.open();
	    if (!choice.booleanValue())
	      return;

      Boolean b = (Boolean) check.getValue();

	    KontoauszugPdfUtil.delete(b,list);
		}
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting",e);
      throw new ApplicationException(i18n.tr("Fehler beim Löschen der Kontoauszüge: {0}",e.getMessage()));
    }
  }
}
