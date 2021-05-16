/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.util.List;

import javax.annotation.Resource;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.bookmark.Context;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;

/**
 * Loescht das Lesezeichen eines Auftrages, wenn der Auftrag selbst geloescht wird.
 */
public class DeleteBookmarkMessageConsumer implements MessageConsumer
{
  @Resource
  private BookmarkService service;

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    Object data = msg.getData();
    if (!(data instanceof HibiscusDBObject))
      return;

    HibiscusDBObject o = (HibiscusDBObject) data;
    if (o.isNewObject())
      return;
    
    String id        = o.getID();
    String className = o.getClass().getName();
    
    List<Bookmark> bookmarks = service.getBookmarks();
    for (Bookmark b : bookmarks)
    {
      Context c = b.getContext();
      if (c == null)
        continue;

      String cClassName = c.getClassName();
      if (cClassName == null || !cClassName.equals(className))
        continue;

      String cId = c.getId();
      if (cId != null && cId.equals(id))
      {
        service.delete(b);
        // hier kein Break, weil eine Entity auf mehreren Seiten gebookmarkt
        // sein kann. Zum Beispiel ein Umsatz in UmsatzDetail und in UmsatzEditDetail
      }
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false; // registriert in plugin.xml
  }

}
