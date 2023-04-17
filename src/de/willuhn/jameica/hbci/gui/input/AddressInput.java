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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Platform;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Autosuggest-Feld zur Eingabe/Auswahl einer Adresse.
 */
public class AddressInput implements Input
{
  private Map<String,Object> data = new HashMap<String,Object>();

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private AddressFilter filter   = null;
  private SuggestInput input     = null;
  private Button button          = null;
  private String validChars      = HBCIProperties.HBCI_DTAUS_VALIDCHARS;
  
  /**
   * ct.
   * @param name Anzuzeigender Name.
   */
  public AddressInput(String name)
  {
    this(name,null);
  }

  /**
   * ct.
   * @param name Anzuzeigender Name.
   * @param filter optionaler Adressfilter.
   */
  public AddressInput(String name, final AddressFilter filter)
  {
    this.filter = filter;
    this.input = new SuggestInput(name);

    String s = "...";
    final int os = Application.getPlatform().getOS();
    if (os == Platform.OS_WINDOWS || os == Platform.OS_WINDOWS_64)
      s = (" ... ");

    this.button = new Button(s,new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          AdresseAuswahlDialog d = new AdresseAuswahlDialog(AdresseAuswahlDialog.POSITION_MOUSE,filter);
          Address a = (Address) d.open();
          if (a != null)
          {
            input.setValue(a);
          }
        }
        catch (OperationCanceledException oce)
        {
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while applying address",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der Adresse: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#addListener(org.eclipse.swt.widgets.Listener)
   */
  public void addListener(Listener l)
  {
    this.input.addListener(l);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    this.input.disable();
    this.button.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    this.input.enable();
    this.button.setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.input.focus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    return this.input.getControl();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getName()
   */
  public String getName()
  {
    return this.input.getName();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return this.input.getValue();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#hasChanged()
   */
  public boolean hasChanged()
  {
    return this.input.hasChanged();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.input.isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isMandatory()
   */
  public boolean isMandatory()
  {
    return this.input.isMandatory();
  }
  
  /**
   * Liefert den angezeigten Text.
   * @return der angezeigte Text.
   */
  public String getText()
  {
    return this.input.getText();
  }
  
  /**
   * Speichert den uebergebenden Text.
   * @param text der anzuzeigende Text.
   */
  public void setText(String text)
  {
    this.input.setText(text);
  }

  /**
   * Definiert eine Liste von Zeichen, die eingegeben werden koennen.
   * Wird diese Funktion verwendet, dann duerfen nur noch die hier
   * angegebenen Zeichen eingegeben werden.
   * Wenn keine Zeichen angegeben sind, gelten die Zeichen aus HBCIProperties.HBCI_DTAUS_VALIDCHARS.
   * @param chars die erlaubten Zeichen.
   */
  public void setValidChars(String chars)
  {
    this.validChars = chars;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite, int)
   */
  public void paint(Composite parent, int width)
  {
    Composite comp = new Composite(parent,SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 1;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    comp.setLayout(layout);

    final GridData g = new GridData(GridData.FILL_HORIZONTAL);
    comp.setLayoutData(g);
    
    this.input.paint(comp);

    try
    {
      this.button.paint(comp);
    }
    catch (RemoteException re)
    {
      Logger.error("error while rendering button",re);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent)
  {
    this.paint(parent,0);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setComment(java.lang.String)
   */
  public void setComment(String comment)
  {
    this.input.setComment(comment);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.input.setEnabled(enabled);
    this.button.setEnabled(enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setMandatory(boolean)
   */
  public void setMandatory(boolean mandatory)
  {
    this.input.setMandatory(mandatory);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.input.setName(name);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    this.input.setValue(value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setData(java.lang.String, java.lang.Object)
   */
  public void setData(String key, Object data)
  {
    this.data.put(key,data);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getData(java.lang.String)
   */
  public Object getData(String key)
  {
    return this.data.get(key);
  }
  

  /**
   * Hilfsklasse fuer das Suggest.
   */
  private class SuggestInput extends SearchInput
  {
    /**
     * ct.
     * @param name Anzuzeigender Name.
     * @param filter optionaler Adressfilter.
     */
    private SuggestInput(String name)
    {
      super();
      this.setValue(name);
      this.setValidChars(validChars);
      this.setMaxLength(HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
      this.addListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          try
          {
            Address a = (Address) event.data;
            setText(a.getName());
          }
          catch (Exception e)
          {
            Logger.error("unable to apply name",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen des Namens"),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.gui.input.SearchInput#setText(java.lang.String)
     */
    public void setText(String s)
    {
      String before = s;
      String after = HBCIProperties.clean(s,validChars);
      // Alle Zeichen rauswerfen, die nicht zulaessig sind.
      super.setText(after);
      if (before != null && !before.equals(after))
        GUI.getView().setErrorText(i18n.tr("Im Namen wurden nicht zulässige Zeichen entfernt"));
    }

    /**
     * @see de.willuhn.jameica.gui.input.SearchInput#format(java.lang.Object)
     */
    protected String format(Object bean)
    {
      if (bean == null)
        return null;
      
      if (!(bean instanceof Address))
        return bean.toString();
      
      try
      {
        Address a = (Address) bean;
        StringBuffer sb = new StringBuffer(a.getName());
        
        String blz = a.getBlz();
        if (blz != null && blz.length() > 0)
        {
          sb.append(" - ");
          String bankName = HBCIProperties.getNameForBank(blz);
          if (bankName != null && bankName.length() > 0)
          {
            sb.append(bankName);
          }
          else
          {
            sb.append("BLZ ");
            sb.append(blz);
          }
        }
        String comment = a.getKommentar();
        if (comment != null && comment.length() > 0)
        {
          sb.append(" (");
          sb.append(comment);
          sb.append(")");
        }
        return sb.toString();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to format address",re);
        return null;
      }
    }


    /**
     * @see de.willuhn.jameica.gui.input.SearchInput#setValue(java.lang.Object)
     */
    public void setValue(Object o)
    {
      super.setValue(o);
    }

    /**
     * @see de.willuhn.jameica.gui.input.SearchInput#startSearch(java.lang.String)
     */
    public List startSearch(String text)
    {
      try
      {
        AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
        List l = service.findAddresses(text);
        if (l == null || l.size() == 0)
          return l;
        
        List result = new ArrayList();
        for (int i=0;i<l.size();++i)
        {
          Address a = (Address) l.get(i);
          if (filter == null || filter.accept(a))
            result.add(a);
        }
        return result;
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while searching in address book",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Suchen im Adressbuch: {0}",e.getMessage()), StatusBarMessage.TYPE_ERROR));
      }
      return new ArrayList();
    }
  }
}


/**********************************************************************
 * $Log: AddressInput.java,v $
 * Revision 1.11  2011/09/12 15:37:42  willuhn
 * @C GUI cleanup
 * @N Icons in Buttons anzeigen
 *
 * Revision 1.10  2011-05-11 10:20:29  willuhn
 * @N OCE fangen
 *
 * Revision 1.9  2011-05-11 08:42:32  willuhn
 * @N setData(String,Object) und getData(String) in Input. Damit koennen generische Nutzdaten im Eingabefeld gespeichert werden (siehe SWT-Widget)
 *
 * Revision 1.8  2011-05-03 10:13:15  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 **********************************************************************/