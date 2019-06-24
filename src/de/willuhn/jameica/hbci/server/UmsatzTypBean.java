/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;

/**
 * Hilfsklasse zur formatierten Anzeige der Umsatz-Kategorien.
 */
public class UmsatzTypBean implements GenericObjectNode
{
  private UmsatzTypBean parent = null;
  private List<UmsatzTypBean> children = new LinkedList<UmsatzTypBean>();
  private UmsatzTyp typ = null;
  private Integer level = null;
  
  /**
   * ct.
   * @param typ
   */
  public UmsatzTypBean(UmsatzTyp typ)
  {
    this.typ = typ;
  }
  
  /**
   * Sammelt die Kinder aus der Liste aller Umsatz-Kategorien ein.
   * @param all Liste aller Umsatz-Kategorien.
   * @throws RemoteException
   */
  void collectChildren(List<UmsatzTypBean> all) throws RemoteException
  {
    for (UmsatzTypBean child:all)
    {
      Object parent = (Object) child.typ.getAttribute("parent_id");
      if (parent == null)
        continue; // Ist ein Root-Element
      String pid = (parent instanceof GenericObject) ? ((GenericObject)parent).getID() : parent.toString();
      if (pid != null && pid.equals(this.typ.getID()))
      {
        child.parent = this;
        this.children.add(child);
        
        // Rekursion nach unten
        child.collectChildren(all);
      }
    }
  }
  
  /**
   * Liefert das Eltern-Element oder NULL, wenn es ein Root-Element ist.
   * @return das Eltern-Element oder NULL, wenn es ein Root-Element ist.
   */
  public UmsatzTypBean getParent()
  {
    return parent;
  }
  
  /**
   * Liefert den Umsatz-Typ.
   * @return typ der Umsatz-Typ.
   */
  public UmsatzTyp getTyp()
  {
    return typ;
  }
  
  /**
   * Liefert das Level des Umsatzes in der Hierarchie.
   * Level 0 entspricht den Root-Elementen.
   * @return level das Level in der Hierarchie.
   */
  public int getLevel()
  {
    if (this.level != null)
      return this.level.intValue();

    int depth = 0;
    UmsatzTypBean parent = this.getParent();
    
    // Maximal 100 Level nach oben
    for (int i=0;i<100;++i)
    {
      if (parent == null)
        break; // Oben angekommen

      depth++;
      parent = parent.getParent();
    }
    
    this.level = Integer.valueOf(depth);
    return this.level.intValue();
  }
  
  /**
   * Liefert den formatierten Namen der Umsatzkategorie mit passender Einrueckung.
   * @return der formatierte Name der Umsatzkategorie mit passender Einrueckung.
   * @throws RemoteException
   */
  public String getIndented() throws RemoteException
  {
    return StringUtils.repeat("    ",this.getLevel()) + this.typ.getName();
  }
  
  /**
   * Liefert den Namen der Kategorie mit dem Namen der uebergeordneten Kategorie.
   * @return der Name der Kategorie mit dem Namen der uebergeordneten Kategorie.
   * @throws RemoteException
   */
  public String getPathName() throws RemoteException
  {
    if (this.parent == null)
      return this.typ.getName();

    return this.parent.getPathName() + "/" + this.typ.getName();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof UmsatzTypBean))
      return false;
    
    UmsatzTypBean other = (UmsatzTypBean) arg0;
    return this.typ.equals(other.typ);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if (arg0 != null && arg0.equals("indented"))
      return this.getIndented();
    
    return this.typ.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return this.typ.getAttributeNames();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  @Override
  public String getID() throws RemoteException
  {
    return this.typ.getID();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return this.typ.getPrimaryAttribute();
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray(this.children.toArray(new UmsatzTypBean[this.children.size()]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPath()
   */
  @Override
  public GenericIterator getPath() throws RemoteException
  {
    List<UmsatzTypBean> result = new LinkedList<UmsatzTypBean>();
    
    UmsatzTypBean parent = this.getParent();
    // Maximal 100 Level nach oben
    for (int i=0;i<100;++i)
    {
      if (parent == null)
        break; // Oben angekommen

      result.add(parent);
      parent = parent.getParent();
    }
    
    return PseudoIterator.fromArray(result.toArray(new UmsatzTypBean[result.size()]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
   */
  @Override
  public GenericIterator getPossibleParents() throws RemoteException
  {
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
   */
  @Override
  public boolean hasChild(GenericObjectNode node) throws RemoteException
  {
    if (node == null)
      return false;
    
    for (UmsatzTypBean child:this.children)
    {
      // Ist es das Kind selbst?
      if (BeanUtil.equals(child,node))
        return true;
      
      // Ist es in den Enkeln?
      if (child.hasChild(node))
        return true;
    }
    
    return false;
  }
}
