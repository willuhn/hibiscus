package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Objects;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

public class TransferQueryBuilderWithStatusbarMessage implements TransferQueryBuilder
{
  private TransferQueryBuilder inner;
  private final I18N i18n;
  private int filterCount;

  public TransferQueryBuilderWithStatusbarMessage(final TransferQueryBuilder inner, final I18N i18n)
  {
    filterCount = 0;
    this.inner = Objects.requireNonNull(inner);
    this.i18n = Objects.requireNonNull(i18n);
  }

  @Override
  public DBIterator build() throws RemoteException
  {
    DBIterator list = inner.build();
    if (filterCount > 0)
    {
      sendMessageToStatusbar();
    }
    return list;
  }

  private void sendMessageToStatusbar() throws RemoteException
  {
    final int all = inner.getElementCountWithoutFilter();
    final int filtered = inner.getElementCount();
    final String message;
    //@formatter:off
    if (all != filtered)
    {
      message = i18n.tr("Suchkriterien: {0} - Anzeige: {1} von {2} Aufträgen",
                        Integer.toString(filterCount),
                        Integer.toString(filtered),
                        Integer.toString(all)
                       );
    }
    else
    {
      message = i18n.tr("Suchkriterien: {0}",
                        Integer.toString(filterCount)
                       );
    }
    //@formatter:on

    Application.getMessagingFactory()
               .sendMessage(new StatusBarMessage(message, StatusBarMessage.TYPE_INFO));
  }

  //region Delegation with counter

  @Override
  public TransferQueryBuilderWithStatusbarMessage withStartDate(final Date from)
  {
    if (from != null)
    {
      filterCount++;
      inner = inner.withStartDate(from);
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithStatusbarMessage withEndDate(final Date to)
  {
    if (to != null)
    {
      filterCount++;
      inner = inner.withEndDate(to);
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithStatusbarMessage withTextLike(final String text)
  {
    if (text != null && text.length() > 0)
    {
      filterCount++;
      inner = inner.withTextLike(text);
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithStatusbarMessage withAccount(final Object konto)
  {
    if (konto != null)
    {
      filterCount++;
      inner = inner.withAccount(konto);
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithStatusbarMessage withOnlyPendingTransactions(final boolean yes)
  {
    if (yes)
    {
      filterCount++;
      inner = inner.withOnlyPendingTransactions(yes);
    }
    return this;
  }

  //endregion

  //region Delegation without counter

  @Override
  public TransferQueryBuilder orderedByDateDesc()
  {
    inner = inner.orderedByDateDesc();
    return this;
  }

  @Override
  public int getElementCount() throws RemoteException
  {
    return inner.getElementCount();
  }

  @Override
  public int getElementCountWithoutFilter() throws RemoteException
  {
    return inner.getElementCountWithoutFilter();
  }

  //endregion
}
