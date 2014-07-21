package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Eine Auswahl-Box fuer das Sicherheitsmedium.
 */
public class BackendInput extends SelectInput
{
  private final static I18N i18n          = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static List<SynchronizeBackend> backendList = null;

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
    super(init(),null);
    setPleaseChoose("Automatisch");

    List<SynchronizeBackend> list = init();
    String currentClass = null;
    if (konto.getBackendClass() != null && !konto.getBackendClass().isEmpty()) {
      currentClass = konto.getBackendClass();
    }
    for (SynchronizeBackend p:list)
    {
      if (currentClass != null && p.getClass().getName().equals(currentClass))
      {
        setPreselected(p);
        break;
      }
    }
    this.setAttribute("name");
    this.setName(i18n.tr("Zugangs-Verfahren"));
  }

  /**
   * Initialisiert die Passport-Liste.
   * @return Liste der Passports.
   */
  private static List<SynchronizeBackend> init()
  {
    if (backendList == null)
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine = service.get(SynchronizeEngine.class);
      backendList = new ArrayList<SynchronizeBackend>();
      try
      {
        for (SynchronizeBackend x : engine.getBackends()) {
//          if (x instanceof de.willuhn.jameica.hbci.synchronize.scripting.ScriptingSynchronizeBackend) {
//            ScriptingSynchronizeBackend ssb = (ScriptingSynchronizeBackend) x;
//          }
          backendList.add(x);
        }
      }
      catch (Exception e)
      {
        Logger.error("error while loading backend list",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der Zugangs-Verfahren"),StatusBarMessage.TYPE_ERROR));
      }
    }

    if (backendList == null)
      backendList = new ArrayList<SynchronizeBackend>();

    return backendList;
  }
}

