/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Combo-Box, die die verfuegbaren HBCI-Versionen enthaelt.
 */
public class HBCIVersionInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static List<HBCIVersion> VERSIONS = Collections.unmodifiableList(new ArrayList<HBCIVersion>()
  {{
    add(new HBCIVersion(org.kapott.hbci.manager.HBCIVersion.HBCI_201,true));
    add(new HBCIVersion(org.kapott.hbci.manager.HBCIVersion.HBCI_210,true));
    add(new HBCIVersion(org.kapott.hbci.manager.HBCIVersion.HBCI_220,false));
    add(new HBCIVersion(org.kapott.hbci.manager.HBCIVersion.HBCI_PLUS,true));
    add(new HBCIVersion(org.kapott.hbci.manager.HBCIVersion.HBCI_300,true));
    add(new HBCIVersion(org.kapott.hbci.manager.HBCIVersion.HBCI_400,false));
  }});

  /**
   * Liefert das HBCIVersion-Objekt fuer die angegebene ID oder NULL, wenn sie nicht existiert.
   * @param id die ID der HBCI-Version.
   * @return die HBCIVersion oder NULL.
   */
  private static HBCIVersion findById(String id)
  {
    if (id == null)
      return null;
    
    for (HBCIVersion version:VERSIONS)
    {
      if (id.equals(version.getId()))
        return version;
    }
    
    return null;
  }


  /**
   * ct.
   * @throws RemoteException
   */
  public HBCIVersionInput() throws RemoteException
  {
    this(null,null);
  }

  /**
   * ct.
   * @param passport Passport.
   * @param selected die vorausgewaehlte HBCI-Version.
   * @throws RemoteException
   */
  public HBCIVersionInput(HBCIPassport passport, String selected) throws RemoteException
  {
    super(createList(passport,selected),findById(selected));
    this.setName(i18n.tr("HBCI-Version"));
    
    // Default-Wert (ohne Versionsnummer) - nur, wenn Passport da
    if (passport != null)
      this.setPleaseChoose(i18n.tr("Version aus Sicherheitsmedium lesen"));
  }

  /**
   * Erzeugt eine Liste mit den unterstuetzten HBCI-Versionen des Passports.
   * @param passport Passport.
   * @param selected die aktuell ausgewaehlte Versionsnummer.
   * @return Liste der unterstuetzten HBCI-Versionen.
   */
  private static List createList(HBCIPassport passport, String selected)
  {
    List<String> list = null;
    
    if (passport != null)
    {
      String[] s = passport.getSuppVersions(); // Wir haben einen Passport, dann nur die unterstuetzten anzeigen
      list = new ArrayList<String>(Arrays.asList(s)); // Neue Array-List, damit wir Elemente drin aendern koennen

      // BUGZILLA 684
      if (list.contains("220") && !list.contains("plus")) // "220" enthalten aber nicht "plus", dann haengen wir das "plus" noch an
      {
        // Wir fuegen das direkt hinter "220" ein
        list.add(list.indexOf("220")+1,"plus");
      }
    }

    // BUGZILLA 37 http://www.willuhn.de/bugzilla/show_bug.cgi?id=37
    // Ansonsten alle, die wir kennen
    List<HBCIVersion> versions = new ArrayList<HBCIVersion>();
    if (list == null || list.size() == 0)
    {
      versions.addAll(VERSIONS);
    }
    else
    {
      for (String s:list)
      {
        HBCIVersion version = findById(s);
        if (version == null)
        {
          Logger.warn("unknown HBCI version: " + s + ", skipping");
          continue;
        }
        versions.add(version);
      }
    }
    
    // Rauswerfen der inaktiven HBCI-Versionen, wenn sie nicht gerade verwendet werden
    List<HBCIVersion> result = new ArrayList<HBCIVersion>();
    for (HBCIVersion v:versions)
    {
      // Version aktiv. Anzeigen
      if (v.active)
      {
        result.add(v);
      }
      else
      {
        // Nicht aktiv. Dann nur, wenn sie gerade verwendet wird.
        if (selected != null && selected.equals(v.getId()))
          result.add(v);
      }
    }
    
    return result;
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
    HBCIVersion o = (HBCIVersion) super.getValue();
    return o != null ? o.getId() : null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#setPreselected(java.lang.Object)
   */
  public void setPreselected(Object preselected)
  {
    super.setPreselected((preselected instanceof String) ? findById((String) preselected) : null);
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#setValue(java.lang.Object)
   */
  public void setValue(Object o)
  {
    super.setValue((o instanceof String) ? findById((String) o) : null);
  }
  
  /**
   * Bean, die die HBCI-Version kapselt.
   */
  private static class HBCIVersion
  {
    private org.kapott.hbci.manager.HBCIVersion version = null;
    private boolean active = true;

    /**
     * ct.
     * @param version die HBCI-Version.
     * @param active legt fest, ob die Version aktiv ist und angeboten werden soll.
     */
    private HBCIVersion(org.kapott.hbci.manager.HBCIVersion version, boolean active)
    {
      this.version = version;
      this.active = active;
    }
    
    /**
     * Liefert die ID der Version.
     * @return die ID der Version.
     */
    public String getId()
    {
      return this.version.getId();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return this.version.getName();
    }
  }
  
}
