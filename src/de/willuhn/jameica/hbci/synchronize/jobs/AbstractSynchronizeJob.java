/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIContext;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung aller SynchronizeJobs,
 */
public abstract class AbstractSynchronizeJob implements SynchronizeJob
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Konto konto = null;
  private Map<String,Object> ctx = new HashMap<String,Object>();

  @Override
  public Object getContext(String key)
  {
    return this.ctx.get(key);
  }
  
  @Override
  public void setContext(String key, Object value)
  {
    this.ctx.put(key,value);
  }
  
  @Override
  public Konto getKonto()
  {
    return this.konto;
  }
  
  @Override
  public void setKonto(Konto konto)
  {
    this.konto = konto;
  }
  
  @Override
  public void configure() throws ApplicationException
  {
    new Open().handleAction(this.getContext(SynchronizeJob.CTX_ENTITY));
  }
  
  @Override
  public String getName() throws ApplicationException
  {
    return HBCIContext.toString(this.getContext(SynchronizeJob.CTX_ENTITY));
  }

}
