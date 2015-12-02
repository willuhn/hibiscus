/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getContext(java.lang.String)
   */
  public Object getContext(String key)
  {
    return this.ctx.get(key);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#setContext(java.lang.String, java.lang.Object)
   */
  public void setContext(String key, Object value)
  {
    this.ctx.put(key,value);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getKonto()
   */
  public Konto getKonto()
  {
    return this.konto;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto)
  {
    this.konto = konto;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#configure()
   */
  public void configure() throws ApplicationException
  {
    new Open().handleAction(this.getContext(SynchronizeJob.CTX_ENTITY));
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  @Override
  public String getName() throws ApplicationException
  {
    return HBCIContext.toString(this.getContext(SynchronizeJob.CTX_ENTITY));
  }

}
