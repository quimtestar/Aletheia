/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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

import java.util.Arrays;
import java.util.List;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CombinedList;

public class AuthoritySignatureTableModel extends AbstractAuthoritySignatureTableModel
{
	//@formatter:off
	protected final static List<ColumnInfo> columnInfoList = new CombinedList<>(AbstractAuthoritySignatureTableModel.columnInfoList, Arrays.asList(
			new ColumnInfo("Context", Context.class, RowData.class, "getContext", 20, true),
			new ColumnInfo("Prefix", Namespace.class, RowData.class, "getPrefix", 20, false),
			new ColumnInfo("Delegate", Person.class, RowData.class, "getDelegate", 15, true)
			));
	//@formatter:on

	protected class RowData extends AbstractAuthoritySignatureTableModel.RowData
	{
		private final Context context;
		private final Namespace prefix;
		private final Person delegate;

		private RowData(StatementAuthoritySignature statementAuthoritySignature, Transaction transaction)
		{
			super(statementAuthoritySignature, transaction);
			Context context_ = null;
			Namespace prefix_ = null;
			Person delegate_ = null;
			DelegateAuthorizer da = getStatementAuthority().getContextAuthority(transaction)
					.delegateAuthorizerByAuthorizerMap(transaction, getStatement().prefix()).get(getAuthorizer());
			if (da != null)
			{
				Statement st = da.getStatement(transaction);
				if (st instanceof Context)
				{
					context_ = (Context) st;
					prefix_ = da.getPrefix();
					delegate_ = da.getDelegate(transaction);
				}
			}
			this.context = context_;
			this.prefix = prefix_;
			this.delegate = delegate_;
		}

		protected Context getContext()
		{
			return context;
		}

		protected Namespace getPrefix()
		{
			return prefix;
		}

		protected Person getDelegate()
		{
			return delegate;
		}
	}

	public AuthoritySignatureTableModel(PersistenceManager persistenceManager, Statement statement, StatementAuthority statementAuthority)
	{
		super(persistenceManager, statement, statementAuthority);
		if (statement instanceof RootContext)
			throw new IllegalArgumentException(
					"Statement of AuthoritySignatureTableModel cannot be a RootContext. Use the RootContextAuthoritySignatureTableModel instead");
	}

	@Override
	protected RowData makeRowData(StatementAuthoritySignature statementAuthoritySignature, Transaction transaction)
	{
		return new RowData(statementAuthoritySignature, transaction);
	}

	@Override
	protected List<ColumnInfo> getColumnInfoList()
	{
		return columnInfoList;
	}

}
