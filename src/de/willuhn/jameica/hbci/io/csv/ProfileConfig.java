/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Container-Klasse zum Speichern der Profile.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileConfig implements Serializable
{
  private static final long serialVersionUID = 1L;

  @XmlElementWrapper
  @XmlElement(name="profile")
  private List<Profile> profiles = new ArrayList<>();
  
  /**
   * Liefert die Liste der Profile. 
   * @return die Liste der Profile.
   */
  public List<Profile> getProfiles()
  {
    return profiles;
  }
  
  /**
   * Speichert die Liste der Profile. 
   * @param profiles die Liste der Profile.
   */
  public void setProfiles(List<Profile> profiles)
  {
    this.profiles = profiles;
  }
}
