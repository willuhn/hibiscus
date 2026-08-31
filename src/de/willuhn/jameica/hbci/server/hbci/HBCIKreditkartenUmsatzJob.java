/**********************************************************************
 *
 * Copyright (c) 2026 Franz Bettag
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDate;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer den Abruf von Kreditkartenumsaetzen per DKKKU.
 */
public class HBCIKreditkartenUmsatzJob extends HBCIUmsatzJob
{
  /**
   * Standardwert fuer die maximale Anzahl Eintraege pro Antwort.
   */
  public final static int DEFAULT_MAX_ENTRIES = 999;

  /**
   * ct.
   * @param konto das Kreditkartenkonto.
   * @param timeRange maximaler Umsatzzeitraum laut BPD.
   * @param maxEntries maximale Anzahl Eintraege oder NULL, wenn der Parameter
   * von der Bank nicht unterstuetzt wird.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIKreditkartenUmsatzJob(Konto konto, int timeRange, Integer maxEntries)
      throws ApplicationException, RemoteException
  {
    super(konto);

    final LocalDate today = LocalDate.now();
    if (maxEntries != null && (maxEntries.intValue() < 1 || maxEntries.intValue() > 9999))
      throw new ApplicationException(i18n.tr("Ung\u00fcltige maximale Anzahl Eintr\u00e4ge: {0}",maxEntries.toString()));

    String cardNumber = konto.getKontonummer();
    if (cardNumber == null || cardNumber.isBlank())
      throw new ApplicationException(i18n.tr("Das Kreditkartenkonto besitzt keine Kartennummer"));

    setJobParam("cardnumber",cardNumber);
    String cardSubNumber = konto.getUnterkonto();
    if (cardSubNumber != null && !cardSubNumber.isBlank())
      setJobParam("cardsubnumber",cardSubNumber);

    LocalDate effectiveFrom = null;
    java.util.Date saldoDate = konto.getSaldoDatum();
    if (saldoDate != null)
      effectiveFrom = new Date(saldoDate.getTime()).toLocalDate();

    int days = timeRange > 0 ? timeRange : HBCIProperties.UMSATZ_DEFAULT_DAYS;
    LocalDate earliest = today.minusDays(days);
    if (effectiveFrom == null || effectiveFrom.isBefore(earliest))
      effectiveFrom = earliest;
    if (effectiveFrom.isAfter(today))
      effectiveFrom = today;

    // DKKKU uses these fields in the actual bank request. Filtering the
    // Hibiscus database after the request would not trigger a backfill.
    setJobParam("startdate",Date.valueOf(effectiveFrom));
    setJobParam("enddate",Date.valueOf(today));
    if (maxEntries != null)
      setJobParam("maxentries",maxEntries);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  @Override
  public String getIdentifier()
  {
    return "KreditkartenUmsatz";
  }
}
