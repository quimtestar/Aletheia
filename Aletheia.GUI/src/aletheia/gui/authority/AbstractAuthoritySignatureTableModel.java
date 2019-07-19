/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.authority;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.BufferedList;

public abstract class AbstractAuthoritySignatureTableModel implements TableModel
{
	private static final int transactionTimeOut = 100;

	protected static class ColumnInfo
	{
		protected final String name;
		protected final Class<?> clazz;
		protected Method method;
		protected final int width;
		protected final boolean editable;

		protected ColumnInfo(String name, Class<?> clazz, Class<? extends RowData> rowDataClass, String methodName, int width, boolean editable)
		{
			super();
			this.name = name;
			this.clazz = clazz;
			try
			{
				this.method = rowDataClass.getDeclaredMethod(methodName);
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				throw new Error();
			}
			this.width = width;
			this.editable = editable;
		}
	}

	//@formatter:off
	protected final static List<ColumnInfo> columnInfoList = Arrays.asList(
			new ColumnInfo("Signed", Date.class, RowData.class, "getSigned", 15, false),
			new ColumnInfo("Valid", Boolean.class, RowData.class, "isValid", 5, false)
			);
	//@formatter:on

	private final PersistenceManager persistenceManager;
	private final Statement statement;
	private final StatementAuthority statementAuthority;
	private final Set<TableModelListener> listeners;

	protected abstract class RowData
	{
		private final StatementAuthoritySignature statementAuthoritySignature;
		private final Signatory authorizer;
		private final Date signed;
		private final boolean valid;

		protected RowData(StatementAuthoritySignature statementAuthoritySignature, Transaction transaction)
		{
			this.statementAuthoritySignature = statementAuthoritySignature;
			this.authorizer = statementAuthoritySignature.getAuthorizer(transaction);
			this.signed = statementAuthoritySignature.getSignatureDate();
			this.valid = statementAuthoritySignature.isValid();
		}

		protected StatementAuthoritySignature getStatementAuthoritySignature()
		{
			return statementAuthoritySignature;
		}

		protected Signatory getAuthorizer()
		{
			return authorizer;
		}

		protected Date getSigned()
		{
			return signed;
		}

		protected boolean isValid()
		{
			return valid;
		}

		protected Object getColumnValue(int columnIndex)
		{
			try
			{
				return getColumnInfoList().get(columnIndex).method.invoke(this);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	protected abstract RowData makeRowData(StatementAuthoritySignature statementAuthoritySignature, Transaction transaction);

	private SoftReference<List<RowData>> rowDataListRef;

	public AbstractAuthoritySignatureTableModel(PersistenceManager persistenceManager, Statement statement, StatementAuthority statementAuthority)
	{
		this.persistenceManager = persistenceManager;
		this.statement = statement;
		this.statementAuthority = statementAuthority;
		this.listeners = Collections.synchronizedSet(new HashSet<TableModelListener>());
	}

	protected PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	protected StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	protected Transaction beginTransaction()
	{
		return persistenceManager.beginTransaction(transactionTimeOut);
	}

	protected synchronized List<RowData> getRowDataList()
	{
		List<RowData> rowDataList = null;
		if (rowDataListRef != null)
			rowDataList = rowDataListRef.get();
		if (rowDataList == null)
		{
			final Transaction transaction = beginTransaction();
			try
			{
				rowDataList = new BufferedList<>(new BijectionCollection<>(new Bijection<StatementAuthoritySignature, RowData>()
				{

					@Override
					public RowData forward(StatementAuthoritySignature statementAuthoritySignature)
					{
						return makeRowData(statementAuthoritySignature, transaction);
					}

					@Override
					public StatementAuthoritySignature backward(RowData rowData)
					{
						return rowData.statementAuthoritySignature;
					}

				}, new BufferedList<>(statementAuthority.signatureDateSortedSet(transaction))));
				rowDataListRef = new SoftReference<>(rowDataList);
			}
			finally
			{
				transaction.abort();
			}
		}
		return rowDataList;
	}

	public List<StatementAuthoritySignature> getSignatureList()
	{
		return new BijectionList<>(new Bijection<RowData, StatementAuthoritySignature>()
		{

			@Override
			public StatementAuthoritySignature forward(RowData input)
			{
				return input.statementAuthoritySignature;
			}

			@Override
			public RowData backward(StatementAuthoritySignature output)
			{
				throw new UnsupportedOperationException();
			}
		}, getRowDataList());

	}

	@Override
	public int getRowCount()
	{
		return getRowDataList().size();
	}

	protected abstract List<ColumnInfo> getColumnInfoList();

	@Override
	public int getColumnCount()
	{
		return getColumnInfoList().size();
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return getColumnInfoList().get(columnIndex).name;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return getColumnInfoList().get(columnIndex).clazz;
	}

	public int getColumnWidth(int columnIndex)
	{
		return getColumnInfoList().get(columnIndex).width;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return getColumnInfoList().get(columnIndex).editable;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return getRowDataList().get(rowIndex).getColumnValue(columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listeners.remove(l);
	}

}
