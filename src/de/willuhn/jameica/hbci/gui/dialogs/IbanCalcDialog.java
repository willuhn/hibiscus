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
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.structures.Konto;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Berechnen von IBAN-Nummern.
 */
public class IbanCalcDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 400;
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private TextInput konto      = null;
  private TextInput unterkonto = null;
  private TextInput blz        = null;
  private TextInput iban       = null;
  private TextInput bic        = null;
  
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
    Container container = new SimpleContainer(parent);
    container.addHeadline(i18n.tr("Nationale Bankverbindung"));
    container.addInput(this.getBlz());
    container.addInput(this.getKonto());
    container.addInput(this.getUnterkonto());
    container.addHeadline(i18n.tr("Zugehörige SEPA-Bankverbindung"));
    container.addInput(this.getBic());
    container.addInput(this.getIban());

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
    container.addButtonArea(buttons);
    
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
        
        calc();
      }
    });
    return this.konto;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer die Unterkontonummer.
   * @return das Eingabe-Feld fuer die Unterkontonummer.
   */
  private TextInput getUnterkonto()
  {
    if (this.unterkonto != null)
      return this.unterkonto;
    
    this.unterkonto = new TextInput("",5);
    this.unterkonto.setName(i18n.tr("Unterkonto"));
    this.unterkonto.setComment("falls vorhanden");
    this.unterkonto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    this.unterkonto.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        calc();
      }
    });
    return this.unterkonto;
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
    String kto = StringUtils.trimToNull((String) getKonto().getValue());
    String blz = StringUtils.trimToNull((String) getBlz().getValue());
    if (kto == null)
    {
      getKonto().setComment(i18n.tr("Bitte Kontonummer eingeben"));
    }
    if (blz == null)
    {
      getBlz().setComment(i18n.tr("Bitte BLZ eingeben"));
    }
    if (blz.length() != HBCIProperties.HBCI_BLZ_LENGTH)
    {
      getBlz().setComment(i18n.tr("BLZ ungültig"));
    }
    
    if (kto != null && blz != null && blz.length() == HBCIProperties.HBCI_BLZ_LENGTH)
    {
      boolean ok = HBCIProperties.checkAccountCRC(blz,kto);
      getKonto().setComment(i18n.tr(ok ? "Konto OK" : "BLZ/Kto ungültig, bitte prüfen"));
      
      if (ok)
      {
        Konto k = new Konto(blz,kto);
        k.subnumber = (String)getUnterkonto().getValue();
        iban = HBCIUtils.getIBANForKonto(k);
        bic = HBCIUtils.getBICForBLZ(blz);
      }
    }
    
    getIban().setValue(iban);
    getBic().setValue(bic);
  }
}
