/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog mit dem Sicherheitshinweis beim Loeschen eines Kontos.
 */
public class KontoDeleteDialog extends AbstractDialog<Boolean>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 800;
  private final static int WINDOW_HEIGHT= 400;
  
  private Konto konto         = null;
  private TablePart deps      = null;
  private CheckboxInput check = null;
  private Button apply        = null;
  private Boolean choice      = null;

  /**
   * ct.
   * @param k das zu loeschende Konto.
   */
  public KontoDeleteDialog(Konto k)
  {
    super(KontoDeleteDialog.POSITION_CENTER);
    this.konto = k;
    this.setTitle(i18n.tr("Konto löschen"));
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
    this.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent,true);
    c.addHeadline(i18n.tr("Warnung"));
    c.addText(i18n.tr("Wollen Sie das Konto wirklich löschen?\nHierbei werden auch alle Daten gelöscht, die diesem Konto zugeordnet sind.") + "\n",true);
    c.addPart(this.getDependencies());
    c.addInput(this.getCheck());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getApply());
    buttons.addButton(new Cancel());
    c.addButtonArea(buttons);
    
    this.getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }
  
  /**
   * Liefert eine Checkbox, die der User ankreuzen muss, um den Loesch-Button freizuschalten.
   * @return Checkbox.
   */
  private CheckboxInput getCheck()
  {
    if (this.check != null)
      return this.check;
    
    this.check = new CheckboxInput(false);
    this.check.setName(i18n.tr("Konto und alle zugeordneten Daten löschen"));
    this.check.addListener(new Listener()
    {
      
      @Override
      public void handleEvent(Event event)
      {
        getApply().setEnabled(((Boolean)getCheck().getValue()).booleanValue());
      }
    });
    return this.check;
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApply()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Jetzt löschen"), new Action()
    {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        choice = Boolean.TRUE;
        close();
      }
    },null,false,"user-trash-full.png");
    this.apply.setEnabled(false);
    
    return this.apply;
  }
  
  /**
   * Liefert die Liste der Daten, die beim Loeschen des Kontos ebenfalls geloescht werden.
   * @return Liste der abhaengigen Daten.
   * @throws RemoteException
   */
  private TablePart getDependencies() throws RemoteException
  {
    if (this.deps != null)
      return this.deps;
    
    this.deps = new TablePart(null);
    this.deps.removeFeature(FeatureSummary.class);
    this.deps.addColumn(i18n.tr("Art der zugeordneten Daten"),"name");
    this.deps.addColumn(i18n.tr("Anzahl"),"size",null,false,Column.ALIGN_RIGHT);
    this.deps.addColumn(i18n.tr("Bemerkung"),"comment");
    
    // Wir laden die Daten im Hintergrund. Das kann sonst bei vielen Daten laenger dauern
    new Thread()
    {
      public void run()
      {
        GUI.getDisplay().asyncExec(new Runnable()
        {
          
          @Override
          public void run()
          {
            BusyIndicator.showWhile(GUI.getDisplay(), new Runnable()
            {
              
              @Override
              public void run()
              {
                try
                {
                  boolean added = false;
                  added |= add(i18n.tr("Umsätze"),konto.getNumUmsaetze(),null);
                  added |= add(i18n.tr("Umsatzkategorien"),konto.getUmsatzTypen().size(),i18n.tr("Kategorien werden nicht gelöscht sondern nur die Verbindung zum Konto aufgehoben"));
                  added |= add(i18n.tr("Elektr. Kontoauszüge"),konto.getKontoauszuege().size(),i18n.tr("Die PDF-Dateien werden nicht gelöscht"));
                  added |= add(i18n.tr("Überweisungen"),konto.getAuslandsUeberweisungen().size(),i18n.tr("Gesendete Terminüberweisungen werden nicht bei der Bank gelöscht"));
                  added |= add(i18n.tr("Daueraufträge"),konto.getDauerauftraege().size(),i18n.tr("Aufträge werden nicht bei der Bank gelöscht"));
                  added |= add(i18n.tr("Lastschriften"),konto.getSepaLastschriften().size(),null);
                  added |= add(i18n.tr("Sammelüberweisungen"),konto.getSepaSammelUeberweisungen().size(),null);
                  added |= add(i18n.tr("Sammellastschriften"),konto.getSepaSammelLastschriften().size(),null);
                  
                  if (!added)
                  {
                    getDependencies().addItem(new Dep("-",0,i18n.tr("Keine weiteren zugeordneten Daten")));
                    getCheck().setName(i18n.tr("Konto löschen"));
                  }
                }
                catch (RemoteException re)
                {
                  Logger.error("unable to add data",re);
                }
              }
            });
          }
        });
      };
    }.start();
    
    return this.deps;
  }
  
  /**
   * Fuegt den Eintrag zur Liste hinzu, wenn data Daten enthaelt.
   * @param name Name des Eintrages.
   * @param size die Anzahl der Daten.
   * @param comment optionaler Kommentar.
   * @return true, wenn die Zeile hinzugefuegt wurde.
   * @throws RemoteException
   */
  private boolean add(String name, int size, String comment) throws RemoteException
  {
    if (size == 0)
      return false;
    
    this.getDependencies().addItem(new Dep(name,size,comment));
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Boolean getData() throws Exception
  {
    return choice;
  }
  
  /**
   * Kapselt eine Zeile in den Abhaengigkeiten
   */
  public class Dep
  {
    private String name = null;
    private int size = 0;
    private String comment = null;
    
    /**
     * ct.
     * @param name
     * @param size
     * @param comment
     */
    private Dep(String name, int size, String comment)
    {
      this.name = name;
      this.size = size;
      this.comment = comment;
    }
    
    /**
     * Liefert einen sprechenden Namen.
     * @return sprechender Name.
     */
    public String getName()
    {
      return this.name;
    }
    
    /**
     * Speichert den Namen.
     * Methode noetig wegen Bean-Spezifikation.
     * @param name der Name.
     */
    public void setName(String name)
    {
      this.name = name;
    }
    
    /**
     * Liefert die Anzahl der Datensaetze.
     * @return Anzahl der Datensaetze.
     */
    public int getSize()
    {
      return this.size;
    }
    
    /**
     * Speichert die Anzahl-
     * Methode noetig wegen Bean-Spezifikation.
     * @param size die Groesse.
     */
    public void setSize(int size)
    {
      this.size = size;
    }
    
    /**
     * Liefert einen Kommentar.
     * @return Kommentar.
     */
    public String getComment()
    {
      return this.comment;
    }
    
    /**
     * Speichert den Kommentar.
     * Methode noetig wegen Bean-Spezifikation.
     * @param comment der Kommentar.
     */
    public void setComment(String comment)
    {
      this.comment = comment;
    }
  }

}
