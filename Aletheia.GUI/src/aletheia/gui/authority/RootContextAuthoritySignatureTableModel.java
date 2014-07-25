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

import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CombinedList;

public class RootContextAuthoritySignatureTableModel extends AbstractAuthoritySignatureTableModel
{
	//@formatter:off
	protected final static List<ColumnInfo> columnInfoList = new CombinedList<>(AbstractAuthoritySignatureTableModel.columnInfoList,Arrays.asList(
			new ColumnInfo("Author", Person.class, RowData.class, "getAuthor", 15, true)
			));
	//@formatter:on

	public RootContextAuthoritySignatureTableModel(PersistenceManager persistenceManager, Statement statement, StatementAuthority statementAuthority)
	{
		super(persistenceManager, statement, statementAuthority);
	}

	protected class RowData extends AbstractAuthoritySignatureTableModel.RowData
	{
		private final Person author;

		protected RowData(StatementAuthoritySignature statementAuthoritySignature, Transaction transaction)
		{
			super(statementAuthoritySignature, transaction);
			author = getPersistenceManager().persons(transaction).get(getAuthorizer());
		}

		protected Person getAuthor()
		{
			return author;
		}

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
