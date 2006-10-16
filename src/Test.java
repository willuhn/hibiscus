import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassportInternal;
import org.kapott.hbci.passport.HBCIPassportRDHNew;
import org.kapott.hbci.passport.HBCIPassportSIZRDHFile;

/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/Attic/Test.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/16 14:46:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

public class Test
{

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    HBCIUtils.init(null,null,new HBCICallbackConsole());
    HBCIUtils.setParam("client.passport.default","RDHNew");
    HBCIUtils.setParam("client.passport.RDHNew.filename","rdhnew.rdh");
    HBCIUtils.setParam("client.passport.RDHNew.init","1");
    HBCIPassportInternal from = (HBCIPassportInternal) AbstractHBCIPassport.getInstance("RDHNew");

    HBCIUtils.setParam("client.passport.SIZRDHFile.filename","sizrdh.key");
    HBCIUtils.setParam("client.passport.SIZRDHFile.libname","/work/willuhn/eclipse3/hibiscus/lib/libhbci4java-sizrdh-linux-gcc3.so");
    HBCIUtils.setParam("client.passport.SIZRDHFile.init","0");
    HBCIPassportInternal to = (HBCIPassportInternal) AbstractHBCIPassport.getInstance("SIZRDHFile");

    to.setCountry(from.getCountry());
    to.setBLZ(from.getBLZ());
    to.setHost(from.getHost());
    to.setPort(from.getPort());
    to.setUserId(from.getUserId());
    to.setCustomerId(from.getCustomerId());
    to.setSysId(from.getSysId());
    to.setSigId(from.getSigId());
    to.setHBCIVersion(from.getHBCIVersion());
    to.setBPD(from.getBPD());
    to.setUPD(from.getUPD());
        
    ((HBCIPassportSIZRDHFile)to).setInstSigKey(from.getInstSigKey());
    ((HBCIPassportSIZRDHFile)to).setInstEncKey(from.getInstEncKey());
    ((HBCIPassportSIZRDHFile)to).setMyPublicSigKey(from.getMyPublicSigKey());
    ((HBCIPassportSIZRDHFile)to).setMyPrivateSigKey(from.getMyPrivateSigKey());
    ((HBCIPassportSIZRDHFile)to).setMyPublicEncKey(from.getMyPublicEncKey());
    ((HBCIPassportSIZRDHFile)to).setMyPrivateEncKey(from.getMyPrivateEncKey());
        
    to.saveChanges();
    to.close();
    from.close();
    
    
    HBCIUtils.done();
  }

}


/*********************************************************************
 * $Log: Test.java,v $
 * Revision 1.1  2006/10/16 14:46:30  willuhn
 * @N CSV-Export von Ueberweisungen und Lastschriften
 *
 **********************************************************************/