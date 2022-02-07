package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.util.DateUtil;

public class TransferQueryBuilderWithDBService implements TransferQueryBuilder
{
  private final HBCIDBService service;
  private final Class<? extends DBObject> objectType;
  private final List<FilterArgument> filters = new LinkedList<>();

  private boolean orderByDateDesc = false;

  public TransferQueryBuilderWithDBService(final HBCIDBService service, final Class<? extends DBObject> objectType) throws RemoteException
  {
    this.service = service;
    this.objectType = objectType;
  }

  @Override
  public TransferQueryBuilderWithDBService withStartDate(final Date from)
  {
    if (from != null)
    {
      addFilter("termin >= ?", new java.sql.Date(DateUtil.startOfDay(from).getTime()));
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithDBService withEndDate(final Date to)
  {
    if (to != null)
    {
      addFilter("termin <= ?", new java.sql.Date(DateUtil.endOfDay(to).getTime()));
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithDBService withTextLike(final String text)
  {
    if (text != null && text.length() > 0)
    {
      addFilter("LOWER(bezeichnung) like ?", "%" + text.toLowerCase() + "%");
    }
    return this;
  }

  private TransferQueryBuilderWithDBService withAccount(final Konto konto)
  {
    if (konto != null)
    {
      try
      {
        final String id = konto.getID();
        addFilter("konto_id = " + id);
      }
      catch (RemoteException e)
      {
        // Should never happen since KontoImpl#getID() today only returns
        // an internally stored string. But if this behaviour changes,
        // this exception will indicate it.
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  private TransferQueryBuilderWithDBService withAccount(final String konto)
  {
    if (konto != null)
    {
      addFilter("konto_id in (select id from konto where kategorie = ?)", konto);
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithDBService withAccount(final Object konto)
  {
    if (konto != null)
    {
      if (konto instanceof Konto)
        return withAccount((Konto) konto);
      else if (konto instanceof String)
        return withAccount((String) konto);
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithDBService withOnlyPendingTransactions(final boolean yes)
  {
    if (yes)
    {
      addFilter("ausgefuehrt = 0");
    }
    return this;
  }

  @Override
  public TransferQueryBuilderWithDBService orderedByDateDesc()
  {
    orderByDateDesc = true;
    return this;
  }

  @Override
  public DBIterator build() throws RemoteException
  {
    final DBIterator list = service.createList(objectType);
    for (FilterArgument f : filters)
    {
      if (f.isPreparedStatement())
        list.addFilter(f.sql, f.values);
      else
        list.addFilter(f.sql);
    }
    if (orderByDateDesc)
    {
      list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC, id DESC");
    }

    return list;
  }

  @Override
  public int getElementCount() throws RemoteException
  {
    return this.build().size();
  }

  @Override
  public int getElementCountWithoutFilter() throws RemoteException
  {
    return service.createList(objectType).size();
  }

  private void addFilter(final String sql, final Date date)
  {
    filters.add(new FilterArgument(sql, date));
  }

  private void addFilter(final String sql, final String value)
  {
    filters.add(new FilterArgument(sql, value));
  }

  private void addFilter(final String sql)
  {
    filters.add(new FilterArgument(sql));
  }

  private static class FilterArgument
  {
    private final String sql;
    private final Object[] values;

    FilterArgument(String sql)
    {
      this.sql = Objects.requireNonNull(sql);
      this.values = null;
    }

    FilterArgument(String preparedStatement, String... values)
    {
      this.sql = Objects.requireNonNull(preparedStatement);
      this.values = Objects.requireNonNull(values);
    }

    FilterArgument(final String preparedStatement, final Date... values)
    {
      this.sql = Objects.requireNonNull(preparedStatement);
      this.values = Objects.requireNonNull(values);
    }

    public boolean isPreparedStatement()
    {
      return values == null;
    }
  }
}
