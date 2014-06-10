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

import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceUniqueConstraintException;

@TaggedCommand(tag = "cpp", groupPath = "/authority", factory = CreatePrivatePerson.Factory.class)
public class CreatePrivatePerson extends TransactionalCommand
{
	private final String nick;
	private final String name;
	private final String email;

	protected CreatePrivatePerson(CliJPanel from, Transaction transaction, String nick, String name, String email)
	{
		super(from, transaction);
		this.nick = nick;
		this.name = name;
		this.email = email;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		PrivatePerson person = getPersistenceManager().privatePersonsByNick(getTransaction()).get(nick);
		if (person == null)
			try
			{
				person = PrivatePerson.create(getPersistenceManager(), getTransaction(), nick);
			}
			catch (PersistenceUniqueConstraintException e)
			{
				throw new Exception("Try again", e);
			}
		person.setName(name);
		person.setEmail(email);
		person.sign(getTransaction());
		person.persistenceUpdate(getTransaction());
		getOut().println(person.getUuid());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<CreatePrivatePerson>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public CreatePrivatePerson parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			String nick = split.get(0);
			String name = split.size() > 1 ? split.get(1) : null;
			String email = split.size() > 1 ? split.get(2) : null;
			return new CreatePrivatePerson(cliJPanel, transaction, nick, name, email);
		}

		@Override
		protected String paramSpec()
		{
			return "<nick> [<name> [<email>]]";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a new person with its private key into the system.";
		}

	}

}
