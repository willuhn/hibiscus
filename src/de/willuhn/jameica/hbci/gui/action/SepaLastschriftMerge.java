/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.SepaLastschriftMerger;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Zusammenfassen von SEPA-Lastschriften zu ein oder mehreren SEPA-Sammellastschriften.
 */
public class SepaLastschriftMerge implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof SepaLastschrift) && !(context instanceof SepaLastschrift[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));

    SepaLastschrift[] source = null;
    
    if (context instanceof SepaLastschrift)
      source = new SepaLastschrift[]{(SepaLastschrift) context};
    else
      source = (SepaLastschrift[]) context;
    
    if (source.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));
    
    List<SepaLastschrift> lastschriften = Arrays.asList(source);
    SepaLastschriftMerger merger = new SepaLastschriftMerger();
    List<SepaSammelLastschrift> result = merger.merge(lastschriften);

    int count = result.size();
    if (count > 1)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("{0} Sammelaufträge erzeugt",String.valueOf(count)), StatusBarMessage.TYPE_SUCCESS));
    else
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sammelauftrag erzeugt"), StatusBarMessage.TYPE_SUCCESS));
  }
}
