/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.IBANCode;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Berechnen von IBAN-Nummern.
 */
public class IbanCalcDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 500;
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private TextInput konto      = null;
  private TextInput blz        = null;
  private TextInput iban       = null;
  private TextInput bic        = null;
  private LabelInput msg       = null;
  
  /**
   * ct
   * @param position
   */
  public IbanCalcDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("IBAN-Rechner"));
    
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#isModeless()
   */
  protected boolean isModeless()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container1 = new SimpleContainer(parent);
    container1.addHeadline(i18n.tr("Wichtiger Hinweis"));
    container1.addText(i18n.tr("Für einige Banken gelten Sonderregeln für die IBAN-Berechnung, die von " +
                               "Hibiscus u.U. nicht unterstützt werden." +
                               "\n\nDaher gilt: " +
                               "Bitte verifizieren Sie daher die errechnete IBAN."),true);

    Container container2 = new SimpleContainer(parent);
    container2.addHeadline(i18n.tr("Nationale Bankverbindung"));
    container2.addInput(this.getBlz());
    container2.addInput(this.getKonto());
    container2.addHeadline(i18n.tr("Zugehörige SEPA-Bankverbindung"));
    container2.addInput(this.getBic());
    container2.addInput(this.getIban());
    container2.addInput(this.getMessage());

    ButtonArea buttons = new ButtonArea();
    
    buttons.addButton(i18n.tr("Berechnen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        calc();
      }
    },null,true,"accessories-calculator.png");
    buttons.addButton(i18n.tr("Schließen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"window-close.png");
    container2.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return das Eingabe-Feld fuer die Kontonummer.
   */
  private TextInput getKonto()
  {
    if (this.konto != null)
      return this.konto;
    
    this.konto = new TextInput("",HBCIProperties.HBCI_KTO_MAXLENGTH_SOFT);
    this.konto.setName(i18n.tr("Kontonummer"));
    this.konto.setComment("");
    this.konto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS + " ");
    this.konto.setMandatory(true);
    this.konto.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String) konto.getValue();
        if (s == null || s.length() == 0)
          return;
        
        if (s.indexOf(" ") != -1)
          konto.setValue(s.replaceAll(" ",""));
      }
    });
    return this.konto;
  }
  
  /**
   * Liefert ein Label fuer die Fehlermeldungen.
   * @return ein Label fuer die Fehlermeldungen.
   */
  private LabelInput getMessage()
  {
    if (this.msg != null)
      return this.msg;
    
    this.msg = new LabelInput("");
    this.msg.setColor(Color.ERROR);
    return this.msg;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   */
  private TextInput getBlz()
  {
    if (this.blz != null)
      return this.blz;
    this.blz = new BLZInput("");
    this.blz.setMandatory(true);
    return this.blz;
  }
  
  /**
   * Liefert das Input fuer die IBAN.
   * @return das Input fuer die IBAN.
   */
  private TextInput getIban()
  {
    if (this.iban != null)
      return this.iban;
    
    this.iban = new TextInput("");
    this.iban.setName(i18n.tr("IBAN"));
    return this.iban;
  }
  
  /**
   * Liefert das Input fuer die BIC.
   * @return das Input fuer die BIC.
   */
  private TextInput getBic()
  {
    if (this.bic != null)
      return this.bic;
    
    this.bic = new TextInput("");
    this.bic.setName(i18n.tr("BIC"));
    return this.bic;
  }
  
  /**
   * Berechnet die Daten.
   */
  private void calc()
  {
    String iban = "";
    String bic  = "";

    // Bankverbindung checken
    String kto = StringUtils.trimToEmpty((String) getKonto().getValue());
    String blz = StringUtils.trimToEmpty((String) getBlz().getValue());
    if (kto.length() == 0)
    {
      getKonto().setComment(i18n.tr("Bitte Kontonummer eingeben"));
    }
    if (blz.length() == 0)
    {
      getBlz().setComment(i18n.tr("Bitte BLZ eingeben"));
    }
    if (blz.length() != HBCIProperties.HBCI_BLZ_LENGTH)
    {
      getBlz().setComment(i18n.tr("BLZ ungültig"));
    }
    
    if (kto.length() > 0 && blz.length() == HBCIProperties.HBCI_BLZ_LENGTH)
    {
      boolean ok = HBCIProperties.checkAccountCRC(blz,kto);
      getKonto().setComment(i18n.tr(ok ? "Konto OK" : "BLZ/Kto ungültig, bitte prüfen"));
      
      if (ok)
      {
        LabelInput msg = this.getMessage();
        
        try
        {
          IBAN newIban = HBCIProperties.getIBAN(blz,kto);
          iban = newIban.getIBAN();
          bic = newIban.getBIC();
          
          IBANCode code = newIban.getCode();
          if (code != null && code == IBANCode.PRUEFZIFFERNMETHODEFEHLT)
          {
            msg.setColor(Color.COMMENT);
            msg.setValue(i18n.tr("IBAN ermittelt, Prüfziffer jedoch nich kontrolliert"));
          }
          else
          {
            msg.setColor(Color.SUCCESS);
            msg.setValue(i18n.tr("Prüfziffer korrekt"));
          }
        }
        catch (ApplicationException ae)
        {
          msg.setColor(Color.ERROR);
          msg.setValue(ae.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("error while calculating IBAN/BIC",e);
          msg.setColor(Color.ERROR);
          msg.setValue(i18n.tr("IBAN-Berechnung fehlgeschlagen"));
        }
      }
    }
    
    getIban().setValue(iban);
    getBic().setValue(bic);
  }
}
