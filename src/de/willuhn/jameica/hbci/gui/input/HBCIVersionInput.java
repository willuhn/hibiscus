/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/HBCIVersionInput.java,v $
 * $Revision: 1.10 $
 * $Date: 2005/11/01 22:53:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Combo-Box, die die verfuegbaren HBCI-Versionen enthaelt.
 * @author willuhn
 */
public class HBCIVersionInput extends SelectInput implements Input
{

  private static I18N i18n;

  private static Hashtable nameLookup = new Hashtable();
  
  static
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    nameLookup.put("201","HBCI 2.01");
    nameLookup.put("210","HBCI 2.1");
    nameLookup.put("220","HBCI 2.2");
    nameLookup.put("plus","HBCI+");
    nameLookup.put("300","FinTS 3.0");
  }

  /**
   * @param passport Passport.
   * @param selectedVersion die vorausgewaehlte HBCI-Version.
   * @param showAll legt fest, ob alle HBCI-Versionen angezeigt werden sollen oder
   * nur jene, welche laut Passport unterstuetzt werden.
   * @throws RemoteException
   */
  public HBCIVersionInput(HBCIPassport passport, String selectedVersion, boolean showAll) throws RemoteException
  {
    super(createList(passport,showAll),selectedVersion == null ? null : new HBCIVersionObject(selectedVersion));
  }

  /**
   * ct.
   * @param passport Passport.
   * @param selectedVersion die vorausgewaehlte HBCI-Version.
   * @throws RemoteException
   */
  public HBCIVersionInput(HBCIPassport passport, String selectedVersion) throws RemoteException
  {
    this(passport,selectedVersion,true);
  }

  /**
   * ct.
   * @param selectedVersion die vorausgewaehlte HBCI-Version.
   * @throws RemoteException
   */
  public HBCIVersionInput(String selectedVersion) throws RemoteException
  {
    this(null,selectedVersion,true);
  }

  /**
   * Erzeugt einen GenericIterator fuer die Auswahl der HBCI-Versionen.
   * @param passport Passport.
   * @param showAll legt fest, ob alle HBCI-Versionen angezeigt werden sollen oder
   * nur jene, welche laut Passport unterstuetzt werden.
   * @return Liste der unterstuetzten HBCI-Versionen.
   * @throws RemoteException
   */
  private static GenericIterator createList(HBCIPassport passport, boolean showAll) throws RemoteException
  {
    // Wir erzeugen eine Liste von HBCI-Versionen, die nur
    // genau die enthaelt, die vom Schluessel unterstuetzt werden

    ArrayList l = new ArrayList();
    l.add(new HBCIVersionObject(null)); // Default-Wert

    try
    {
      String[] s = null;
      if (passport != null)
        s = passport.getSuppVersions();
      // BUGZILLA 37 http://www.willuhn.de/bugzilla/show_bug.cgi?id=37
      if (showAll || s == null || s.length == 0)
      {
        // Wir liefern alle HBCI-Versionen
        s = new String[nameLookup.size()];
        Enumeration e = nameLookup.keys();
        int i = 0;
        while (e.hasMoreElements())
        {
          String sk = (String) e.nextElement();
          if (sk == null || sk.length() == 0)
            continue;
          s[i++] = sk;
        }
      }

      for (int i=0;i<s.length;++i)
      {
        l.add(new HBCIVersionObject(s[i]));
      }
    }
    catch (Exception e)
    {
      Logger.error("error while loading hbci versions from key",e);
      throw new RemoteException(i18n.tr("Fehler beim Lesen der unterstützten HBCI-Versionen"),e);
    }

    HBCIVersionObject[] array = (HBCIVersionObject[]) l.toArray(new HBCIVersionObject[l.size()]);
    return PseudoIterator.fromArray(array);
  }

  /**
   * Hilfs-Objekt.
   * @author willuhn
   */
  private static class HBCIVersionObject implements GenericObject
  {

    private String id   = null;

    /**
     * ct.
     * @param id ID der HBCI-Version.
     */
    private HBCIVersionObject(String id)
    {
      this.id   = id;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if (this.id == null)
        return i18n.tr("Version aus Sicherheitsmedium lesen");

      String name = (String) nameLookup.get(this.id);
      if (name == null)
      {
        Logger.warn("unknown hbci version: " + name);
        return id;
      }
      return name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"foo"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return id;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "foo";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;

      boolean nullOwn   = getID() == null;
      boolean nullOther = arg0.getID() == null;

      // Die ID kann <null> sein.
      if (nullOwn && nullOther)  return true;
      if (nullOwn && !nullOther) return false;

      return this.getID().equals(arg0.getID());
    }
  }

  /**
   * Liefert die ID der ausgwaehlten HBCI-Version als <code>java.lang.String</code>.
   * Moegliche Rueckgabe-Werte:<br>
   * <ul>
   *   <li><code>null</code> (Version wird aus Sicherheitsmedium gelesen)</li>
   *   <li>201</li>
   *   <li>210</li>
   *   <li>220</li>
   *   <li>plus</li>
   *   <li>300</li>
   * </ul>
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    try
    {
      HBCIVersionObject o = (HBCIVersionObject) super.getValue();
      return o.getID();
    }
    catch (Exception e)
    {
      return null;
    }
  }
}

/*****************************************************************************
 * $Log: HBCIVersionInput.java,v $
 * Revision 1.10  2005/11/01 22:53:44  willuhn
 * @N hbci4java updated to 2.5.0 rc1
 * @N added "FinTS 3.0" to HBCIVersionInput
 *
 * Revision 1.9  2005/10/17 11:36:38  willuhn
 * @B bug 141 FinTS 3 entfernt
 *
 * Revision 1.8  2005/07/12 23:29:01  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/06/27 11:26:30  web0
 * @N neuer Test bei Dauerauftraegen (zum Monatsletzten)
 * @N neue DDV-Lib
 *
 * Revision 1.6  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.5  2005/04/18 09:28:45  web0
 * @B zu wenig HBCI-Versionen in Auswahl
 * @B Refresh der Liste nach RDH-Schluesselimport
 *
 * Revision 1.4  2005/04/05 23:43:53  web0
 * @C moved HBCIVersionInput into Hibiscus source tree
 *
*****************************************************************************/