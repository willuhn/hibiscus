package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;

public interface TransferQueryBuilder
{
  DBIterator build() throws RemoteException;

  int getElementCount() throws RemoteException;
  int getElementCountWithoutFilter() throws RemoteException;

  TransferQueryBuilder orderedByDateDesc();
  TransferQueryBuilder withAccount(Object konto);
  TransferQueryBuilder withEndDate(Date to);
  TransferQueryBuilder withOnlyPendingTransactions(boolean yes);
  TransferQueryBuilder withStartDate(Date from);
  TransferQueryBuilder withTextLike(String text);
}
