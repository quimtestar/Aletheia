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
package aletheia.gui.cli.command.statement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "export", groupPath = "/statement", factory = Export.Factory.class)
public class Export extends TransactionalCommand
{
	private final File file;
	private final List<Statement> statements;
	private final boolean signed;

	public Export(CommandSource from, Transaction transaction, File file, List<Statement> statements, boolean signed)
	{
		super(from, transaction);
		this.file = file;
		this.statements = statements;
		this.signed = signed;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		getPersistenceManager().export(file, getTransaction(), statements, signed);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Export>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public Export parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);

			File file = new File(split.get(0));

			List<Statement> statements = new ArrayList<Statement>();
			boolean signed = false;
			for (int i = 1; i < split.size(); i++)
			{
				if (split.get(i).equals("-signed"))
					signed = true;
				else
				{
					Statement st = findStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(i));
					if (st == null)
						throw new CommandParseException("Bad statement path: " + split.get(i));
					statements.add(st);
				}
			}
			return new Export(from, transaction, file, statements, signed);
		}

		@Override
		protected String paramSpec()
		{
			return "<file> <statement>* [-signed]";
		}

		@Override
		public String shortHelp()
		{
			return "Exports a set of statements to a file.";
		}

	}

}
