/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.gui.cli.command.prooffinder;

import java.util.List;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.log4j.LoggerManager;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "pf", groupPath = "/prooffinder", factory = ProofFind.Factory.class)
public class ProofFind extends TransactionalCommand
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.instance.logger();

	private final Context context;

	public ProofFind(CommandSource from, Transaction transaction, Context context)
	{
		super(from, transaction);
		this.context = context;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		getProofFinder().addToProvingPool(context);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ProofFind>
	{

		@Override
		public ProofFind parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				if (from.getActiveContext() == null)
					throw new NotActiveContextException();
				Context context;
				if (split.size() > 1)
				{
					try
					{
						context = (Context) from.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(1)));
						if (context == null)
							throw new CommandParseException("Invalid context");
					}
					catch (ClassCastException e)
					{
						throw new CommandParseException("Invalid context");
					}
				}
				else
					context = from.getActiveContext();
				return new ProofFind(from, transaction, context);
			}
			catch (InvalidNameException | NotActiveContextException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "Takes the active context to the automatic proof finder thread.";
		}

	}

}
