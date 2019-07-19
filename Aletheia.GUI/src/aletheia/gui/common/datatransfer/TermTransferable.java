/*******************************************************************************
 * Copyright (c) 2015, 2018 Quim Testar.
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

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceException;

public class TermTransferable extends AletheiaTransferable
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final Term term;
	private final Context context;
	private final ParameterIdentification parameterIdentification;

	public TermTransferable(Term term, Context context, ParameterIdentification parameterIdentification)
	{
		super(Arrays.<DataFlavor> asList(TermDataFlavor.instance, TermParameterIdentificationDataFlavor.instance, DataFlavor.stringFlavor));
		this.term = term;
		this.context = context;
		this.parameterIdentification = parameterIdentification;
	}

	public TermTransferable(Term term)
	{
		this(term, null, null);
	}

	public TermTransferable(Term term, Context context)
	{
		this(term, context, null);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(TermDataFlavor.instance))
			return term;
		else if (flavor.equals(TermParameterIdentificationDataFlavor.instance))
			return parameterIdentification;
		else if (flavor.equals(DataFlavor.stringFlavor))
		{
			String text;
			if (context != null)
			{
				Transaction transaction = context.getPersistenceManager().beginTransaction();
				try
				{
					text = term.toString(transaction, context, parameterIdentification);
				}
				catch (PersistenceException e)
				{
					logger.warn("Persistence error trying to convert term to string", e);
					text = term.toString(parameterIdentification);
				}
				finally
				{
					transaction.abort();
				}
			}
			else
				text = term.toString(parameterIdentification);
			if (text.contains(" ") || text.isEmpty())
				text = '"' + text + '"';
			return text;
		}
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
