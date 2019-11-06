package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.VergleichPlusMinusBarChart;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt die Differenz von Ein- und Ausnahmen im zeitlichen Verlauf.
 */
public class PlusMinusVerlauf implements Part
{
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private VergleichPlusMinusBarChart chart   = null;
  private List<EinnahmeAusgabeZeitraum> data = null;

  /**
   * Konstruktor mit anzuzeigenden Werten
   * @param werte
   */
  public PlusMinusVerlauf(List<EinnahmeAusgabeZeitraum> werte)
  {
    this.data = werte;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      this.chart = new VergleichPlusMinusBarChart();
      this.chart.setTitle(i18n.tr("Plus/Minus"));
      this.chart.setData(this.data);
      this.chart.paint(parent);
    }
    catch (Exception e)
    {
      Logger.error("unable to create chart",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen des Diagramms"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Setzt die anzuzeigenden Werte
   * @param werte die Werte.
   * @throws RemoteException 
   */
  public void setList(List<EinnahmeAusgabeZeitraum> werte) throws RemoteException
  {
    this.data = werte;
    this.chart.setData(werte);
    this.chart.redraw();
  }
}
