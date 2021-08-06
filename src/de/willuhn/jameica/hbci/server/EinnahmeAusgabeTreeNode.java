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
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getStartdatum()
   */
  @Override
  public Date getStartdatum()
  {
    return this.startdatum;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getEnddatum()
   */
  @Override
  public Date getEnddatum()
  {
    return this.enddatum;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    return this == arg0;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getText()
   */
  @Override
  public String getText()
  {
    return DateUtil.DEFAULT_FORMAT.format(this.startdatum) + " - " + DateUtil.DEFAULT_FORMAT.format(this.enddatum);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getAusgaben()
   */
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
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getEinnahmen()
   */
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

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
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

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[]{"text","startdatum","enddatum","children"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  @Override
  public String getID() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "text";
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray((EinnahmeAusgabe[]) children.toArray(new EinnahmeAusgabe[0]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getParent()
   */
  @Override
  public GenericObjectNode getParent() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPath()
   */
  @Override
  public GenericIterator getPath() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
   */
  @Override
  public GenericIterator getPossibleParents() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
   */
  @Override
  public boolean hasChild(GenericObjectNode arg0) throws RemoteException
  {
    if (!(arg0 instanceof EinnahmeAusgabe))
      return false;
    
    return this.children.contains((EinnahmeAusgabe)arg0);
  }
}