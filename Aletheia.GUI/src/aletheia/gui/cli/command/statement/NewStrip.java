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

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "strip", factory = NewStrip.Factory.class)
public class NewStrip extends NewAuto
{

	public NewStrip(CommandSource from, Transaction transaction, Identifier identifier, Context context, Statement statement, List<Term> hints)
	{
		super(from, transaction, identifier, context, statement, null, hints);
	}

	public static class Factory extends AbstractNewStatementFactory<NewStrip>
	{

		@Override
		public NewStrip parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Context ctx = from.getActiveContext();
				if (ctx == null)
					throw new NotActiveContextException();
				Statement statement = ctx.identifierToStatement(transaction).get(Identifier.parse(split.get(0)));
				if (statement == null)
					throw new CommandParseException("Statement not found: " + split.get(0));
				List<Term> hints = new LinkedList<>();
				for (String s : split.subList(1, split.size()))
					hints.add(ctx.parseTerm(transaction, s));
				return new NewStrip(from, transaction, identifier, ctx, statement, hints);
			}
			catch (NotActiveContextException | InvalidNameException | TermParserException e)
			{
				throw new CommandParseException(e);
			}
			finally
			{

			}
		}

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> <hint>*";
		}

		@Override
		public String shortHelp()
		{
			return "Strips the given statement.";
		}

		@Override
		public void longHelp(PrintStream out)
		{
			super.longHelp(out);
			out.println("A list of hint terms might be provided to be assigned to the parameters left undetermined.");
		}

	}

}
