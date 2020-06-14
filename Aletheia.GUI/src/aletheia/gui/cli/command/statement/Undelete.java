/*******************************************************************************
 * Copyright (c) 2016, 2020 Quim Testar.
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

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "undelete", groupPath = "/statement", factory = Undelete.Factory.class)
public class Undelete extends TransactionalCommand
{

	public Undelete(CommandSource from, Transaction transaction)
	{
		super(from, transaction);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		List<Statement> undeleted = getPersistenceManager().getUndeleteManager().undelete(getTransaction());
		if (!undeleted.isEmpty())
			putSelectStatement(getTransaction(), undeleted.get(undeleted.size() - 1));
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Undelete>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Undelete parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			return new Undelete(from, transaction);
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "Undeletes a batch of deleted statements.";
		}

	}

}
