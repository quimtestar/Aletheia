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
package aletheia.gui.common.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementTransferable extends AletheiaTransferable
{
	private final Statement statement;

	public StatementTransferable(Statement statement)
	{
		super(Arrays.<DataFlavor> asList(StatementDataFlavor.instance, DataFlavor.stringFlavor));
		this.statement = statement;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(StatementDataFlavor.instance))
			return statement;
		else if (flavor.equals(DataFlavor.stringFlavor))
		{
			Transaction transaction = statement.getPersistenceManager().beginDirtyTransaction();
			try
			{
				return statement.statementPathString(transaction);
			}
			finally
			{
				transaction.abort();
			}
		}
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
