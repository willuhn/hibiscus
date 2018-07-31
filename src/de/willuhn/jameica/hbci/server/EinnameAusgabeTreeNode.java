package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.util.DateUtil;

/**
 * Container für die EinnahmenAusgaben-Liste eines bestimmten Zeitraums
 */
//GenericObjectNode wird für die Anzeige im Baum minimal implementiert
public class EinnameAusgabeTreeNode implements GenericObjectNode
{

  private Date from;
  private Date to;
  private List<EinnahmeAusgabe> children;

  /**
   * @param from Startdatum des Zeitraums
   * @param to Enddatum des Zeitraums
   * @param children Liste der EinnameAusgabe-Daten für diesen Zeitraum
   */
  public EinnameAusgabeTreeNode(Date from, Date to, List<EinnahmeAusgabe> children)
  {
    this.from=from;
    this.to=to;
    this.children=children;
  }

  /**
   * @return Startdatum des Zeitraums
   */
  public Date getFrom()
  {
    return from;
  }

  /**
   * @return Enddatum des Zeitraums
   */
  public Date getTo()
  {
    return to;
  }

  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    return arg0==this;
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    //wir liefern ausschließlich für die erste Spalte Daten
    if("text".equals(arg0))
    {
      return DateUtil.DEFAULT_FORMAT.format(from)+"-"+DateUtil.DEFAULT_FORMAT.format(to);
    }
    return null;
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getID() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray((EinnahmeAusgabe[])children.toArray(new EinnahmeAusgabe[children.size()]));
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
    return true;
  }
}