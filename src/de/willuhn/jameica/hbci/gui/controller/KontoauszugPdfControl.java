/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.hbci.gui.parts.KontoauszugPdfList;

/**
 * Controller fuer die Anzeige der Kontoauszuege im PDF-Format.
 */
public class KontoauszugPdfControl extends AbstractControl
{
  private KontoauszugPdfList list = null;
  
  /**
   * ct.
   * @param view
   */
  public KontoauszugPdfControl(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert die Tabelle mit den Kontoauszuegen.
   * @return die Tabelle mit den Kontoauszuegen.
   */
  public KontoauszugPdfList getList()
  {
    if (this.list != null)
      return this.list;
    
    this.list = new KontoauszugPdfList();
    return this.list;
  }

}


