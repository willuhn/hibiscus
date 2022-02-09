/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.PasswordInput;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Komponente, welche ein PIN-Pad anzeigt.
 * Damit koennen PIN- oder TAN-Eingaben mit der Maus durchgefuehrt
 * werden. Ein Keyboard-Sniffer kann somit die Eingabe nicht ausspaehen.
 */
public class PinPad implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Composite comp      = null;
  private PasswordInput input = null;
  
  /**
   * ct.
   * @param input das Eingabe-Feld, auf welches die Klicks uebertragen werden sollen.
   */
  public PinPad(PasswordInput input)
  {
    this.input = input;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    if (this.comp != null)
      return;
    
    this.comp = new Composite(parent,SWT.NONE);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    this.comp.setLayoutData(gridData);

    GridLayout layout = new GridLayout(3,true);
    layout.horizontalSpacing = 4;
    layout.verticalSpacing = 4;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    this.comp.setLayout(layout);

    // Buttons 1-9
    for (int i=1;i<10;++i)
    {
      addButton(i);
    }
    
    // Button 0
    addButton(0);
    
    // Button Backspace
    final Button b = GUI.getStyleFactory().createButton(this.comp);
    b.setText(i18n.tr("Löschen"));
    b.setAlignment(SWT.CENTER);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    b.setLayoutData(gd);
    b.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e) {
        input.setValue("");
      }
    });
  }
  
  /**
   * Fuegt den Button mit der genannten Nummer hinzu.
   * @param i Nummer.
   */
  private void addButton(int i)
  {
    final String s = Integer.toString(i);
    final Button b = GUI.getStyleFactory().createButton(this.comp);
    b.setText(s);
    b.setFont(Font.BOLD.getSWTFont());
    b.setAlignment(SWT.CENTER);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = 50;
    b.setLayoutData(gd);
    b.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e) {
        String curr = (String) input.getValue();
        if (curr == null)
          curr = "";
        curr += s;
        input.setValue(curr);
      }
    });
  }

}


/**********************************************************************
 * $Log: PinPad.java,v $
 * Revision 1.3  2011/05/03 10:13:15  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.2  2009/11/10 12:00:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/07/12 23:19:29  willuhn
 * @N Code fuer ein GUI-Pin-Pad. Mal sehen, ob ich das irgendwann in Hibiscus uebernehme
 *
 **********************************************************************/
