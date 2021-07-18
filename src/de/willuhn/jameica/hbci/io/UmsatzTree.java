/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Container fuer einen Umsatztree samt Meta-Daten.
 */
public class UmsatzTree implements Serializable
{
  private List tree     = null;
  private String title  = null;
  private Date start    = null;
  private Date end      = null;

  /**
   * Liefert das End-Datum.
   * @return das End-Datum.
   */
  public Date getEnd()
  {
    return this.end;
  }

  /**
   * Speichert das End-Datum.
   * @param end das End-Datum.
   */
  public void setEnd(Date end)
  {
    this.end = end;
  }

  /**
   * Liefert den Titel.
   * @return den Titel.
   */
  public String getTitle()
  {
    return this.title;
  }

  /**
   * Speichert den Titel.
   * @param title den Titel.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }

  /**
   * Liefert den Tree der Umsaetze.
   * @return tree der Umsatz-Tree.
   */
  public List getUmsatzTree()
  {
    return this.tree;
  }

  /**
   * Speichert den Tree der Umsaetze.
   * @param tree der Umsatz-Tree.
   */
  public void setUmsatzTree(List tree)
  {
    this.tree = tree;
  }

  /**
   * Liefert das Start-Datum.
   * @return das Start-Datum.
   */
  public Date getStart()
  {
    return this.start;
  }

  /**
   * Speichert das Start-Datum.
   * @param start das Start-Datum.
   */
  public void setStart(Date start)
  {
    this.start = start;
  }
}

/*********************************************************************
 * $Log: UmsatzTree.java,v $
 * Revision 1.1  2007/05/02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 **********************************************************************/