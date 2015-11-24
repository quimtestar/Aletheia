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
package aletheia.gui.cli.command.authority;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "authrec", groupPath = "/authority", factory = AuthRec.Factory.class)
public class AuthRec extends Auth
{

	public AuthRec(CommandSource from, Transaction transaction, PrivatePerson author, Context context)
	{
		super(from, transaction, author, context);
	}

	@Override
	protected Context getStatement()
	{
		return (Context) super.getStatement();
	}

	protected Context getContext()
	{
		return getStatement();
	}

	@Override
	protected void createAuthority() throws AuthorityWithNoParentException
	{
		getContext().createAuthorityRecursive(getTransaction(), getAuthor());
	}

	public static class Factory extends Auth.Factory
	{

		@Override
		protected AuthRec makeAuth(CliJPanel cliJPanel, Transaction transaction, PrivatePerson author, Statement statement) throws CommandParseException
		{
			if (!(statement instanceof Context))
				throw new CommandParseException("Not a context");
			return new AuthRec(cliJPanel, transaction, author, (Context) statement);
		}

		@Override
		public String shortHelp()
		{
			return "Creates the authority of a statement and its descendants.";
		}

	}

}
