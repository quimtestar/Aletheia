/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementListTableModel implements TableModel
{
	private final String name;
	private final List<Statement> statementList;
	private final Set<TableModelListener> listeners;

	private class StateListener implements Statement.StateListener, StatementAuthority.StateListener
	{

		@Override
		public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
		{
			statementChanged(transaction, statementAuthority.getStatement(transaction));
		}

		@Override
		public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
			statementChanged(transaction, statementAuthority.getStatement(transaction));
		}

		@Override
		public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
		{
			statementChanged(transaction, statementAuthority.getStatement(transaction));
		}

		@Override
		public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
		{
			statementChanged(transaction, statement);
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
			statementChanged(transaction, statement);
		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
			statementChanged(transaction, statement);
		}

	}

	private final StateListener stateListener;

	public StatementListTableModel(String name, List<Statement> statementList)
	{
		this.name = name;
		this.statementList = statementList;
		this.listeners = Collections.synchronizedSet(new HashSet<>());
		this.stateListener = new StateListener();
		for (Statement st : statementList)
		{
			st.addStateListener(stateListener);
			st.addAuthorityStateListener(stateListener);
		}
	}

	public String getName()
	{
		return name;
	}

	public List<Statement> getStatementList()
	{
		return statementList;
	}

	@Override
	public int getRowCount()
	{
		return statementList.size();
	}

	@Override
	public int getColumnCount()
	{
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return name;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return Statement.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	@Override
	public Statement getValueAt(int rowIndex, int columnIndex)
	{
		return statementList.get(rowIndex);
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

	public void shutdown()
	{
		for (Statement st : statementList)
		{
			st.removeStateListener(stateListener);
			st.removeAuthorityStateListener(stateListener);
		}
	}

	private int statementRow(Statement statement)
	{
		int row = statementList.indexOf(statement);
		if (row < 0)
			return TableModelEvent.HEADER_ROW;
		return row;
	}

	protected class MyTableModelEvent extends TableModelEvent
	{

		private static final long serialVersionUID = -3280412990120035916L;
		private final Statement statement;

		private MyTableModelEvent(TableModel source, int row, Statement statement)
		{
			super(source, row, row, 0, TableModelEvent.UPDATE);
			this.statement = statement;
		}

		protected Statement getStatement()
		{
			return statement;
		}

	}

	protected void statementChanged(Transaction transaction, Statement statement)
	{
		final TableModelEvent event = new MyTableModelEvent(this, statementRow(statement), statement);
		transaction.runWhenCommit(new Transaction.Hook()
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				synchronized (listeners)
				{
					for (TableModelListener l : listeners)
					{
						l.tableChanged(event);
					}
				}
			}
		});
	}

}
