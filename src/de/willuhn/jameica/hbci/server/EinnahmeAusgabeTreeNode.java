package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.util.DateUtil;

/**
 * Container für die EinnahmenAusgaben-Liste eines bestimmten Zeitraums.
 */
public class EinnahmeAusgabeTreeNode implements EinnahmeAusgabeZeitraum, GenericObjectNode
{
  private Date startdatum;
  private Date enddatum;
  private List<EinnahmeAusgabe> children;

  /**
   * @param from Startdatum des Zeitraums
   * @param to Enddatum des Zeitraums
   * @param children Liste der EinnameAusgabe-Daten für diesen Zeitraum
   */
  public EinnahmeAusgabeTreeNode(Date from, Date to, List<EinnahmeAusgabe> children)
  {
    this.startdatum = from;
    this.enddatum = to;
    this.children = children;
  }
  
  @Override
  public Date getStartdatum()
  {
    return this.startdatum;
  }
  
  @Override
  public Date getEnddatum()
  {
    return this.enddatum;
  }

  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    return this == arg0;
  }
  
  @Override
  public String getText()
  {
    return DateUtil.DEFAULT_FORMAT.format(this.startdatum) + " - " + DateUtil.DEFAULT_FORMAT.format(this.enddatum);
  }
  
  @Override
  public double getAusgaben()
  {
    if (this.children == null || this.children.size() == 0)
      return 0.0d;

    double sum = 0.0d;
    for (EinnahmeAusgabe e:this.children)
    {
      if (!e.isSumme()) // ueberspringen
        sum += e.getAusgaben();
    }
    return sum;
  }
  
  @Override
  public double getEinnahmen()
  {
    if (this.children == null || this.children.size() == 0)
      return 0.0d;

    double sum = 0.0d;
    for (EinnahmeAusgabe e:this.children)
    {
      if (!e.isSumme()) // ueberspringen
        sum += e.getEinnahmen();
    }
    return sum;
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if("text".equals(arg0))
      return this.getText();
    
    if ("startdatum".equals(arg0))
      return this.startdatum;
    
    if ("enddatum".equals(arg0))
      return this.enddatum;
    
    if ("children".equals(arg0))
      return this.children;
    
    return null;
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[]{"text","startdatum","enddatum","children"};
  }

  @Override
  public String getID() throws RemoteException
  {
    return null;
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "text";
  }

  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray((EinnahmeAusgabe[]) children.toArray(new EinnahmeAusgabe[children.size()]));
  }

  @Override
  public GenericObjectNode getParent() throws RemoteException
  {
    return null;
  }

  @Override
  public GenericIterator getPath() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public GenericIterator getPossibleParents() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasChild(GenericObjectNode arg0) throws RemoteException
  {
    if (!(arg0 instanceof EinnahmeAusgabe))
      return false;
    
    return this.children.contains((EinnahmeAusgabe)arg0);
  }
}