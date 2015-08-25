/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Eine Auswahl-Box fuer das Bank-Backend.
 */
public class BackendInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @throws RemoteException
   */
  public BackendInput() throws RemoteException
  {
    this(null);
  }

  /**
   * ct.
   * @param konto das Konto.
   * @throws RemoteException
   */
  public BackendInput(Konto konto) throws RemoteException
  {
    super((List)null,null);
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine = service.get(SynchronizeEngine.class);
    List<SynchronizeBackend> list = engine.getBackends();
    this.setList(list);

    SynchronizeBackend current = engine.getBackend(konto);
    this.setPreselected(current != null ? current : engine.getPrimary());
    
    this.setAttribute("name");
    this.setName(i18n.tr("Zugangsweg"));
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  @Override
  protected String format(Object bean)
  {
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeBackend primary = service.get(SynchronizeEngine.class).getPrimary();
    
    String name = super.format(bean);
    if (bean != null && bean.equals(primary))
      name += " (" + i18n.tr("Standard") + ")";
    
    return name;
    
  }

}

