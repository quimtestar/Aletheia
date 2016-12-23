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
package aletheia.gui.common.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.StatementGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class SorterTransferable extends AletheiaTransferable
{
	private final PersistenceManager persistenceManager;
	private final Sorter sorter;
	private final Statement statement;

	public SorterTransferable(PersistenceManager persistenceManager, Sorter sorter)
	{
		super(Arrays.<DataFlavor> asList(StatementDataFlavor.instance, DataFlavor.stringFlavor));
		this.persistenceManager = persistenceManager;
		this.sorter = sorter;
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			this.statement = sorter.getStatement(transaction);
		}
		finally
		{
			transaction.abort();
		}

	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if (flavor.equals(StatementDataFlavor.instance))
			return statement != null;
		else
			return super.isDataFlavorSupported(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(StatementDataFlavor.instance))
			return statement;
		else if (flavor.equals(DataFlavor.stringFlavor))
		{
			if (sorter instanceof StatementSorter)
			{
				Transaction transaction = persistenceManager.beginDirtyTransaction();
				try
				{
					return ((StatementSorter) sorter).getStatement().statementPathString(transaction);
				}
				finally
				{
					transaction.abort();
				}
			}
			else if (sorter instanceof GroupSorter)
			{
				String path;
				if (sorter instanceof StatementGroupSorter)
				{
					Transaction transaction = persistenceManager.beginDirtyTransaction();
					try
					{
						path = ((StatementGroupSorter) sorter).getContext().statementPathString(transaction);
					}
					finally
					{
						transaction.abort();
					}
				}
				else
					path = "";
				return path + "/" + sorter.getPrefix().qualifiedName();
			}
			else
				throw new Error();
		}
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
