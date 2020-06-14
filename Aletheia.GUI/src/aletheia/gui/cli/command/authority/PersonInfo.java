/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.Person;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableSet;

@TaggedCommand(tag = "pi", groupPath = "/authority", factory = PersonInfo.Factory.class)
public class PersonInfo extends TransactionalCommand
{
	private final String nick;

	public PersonInfo(CommandSource from, Transaction transaction, String nick)
	{
		super(from, transaction);
		this.nick = nick;
	}

	@Override
	protected RunTransactionalReturnData runTransactional()
	{
		CloseableSet<Person> persons = getPersistenceManager().personsByNick(getTransaction()).get(nick);
		if (persons == null || persons.isEmpty())
		{
			getErr().println("No such nick.");
		}
		else
		{
			for (Person person : persons)
				getOut().println(person.getUuid() + "\t" + person.getName() + "\t" + person.getEmail());
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<PersonInfo>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public PersonInfo parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			String nick = split.get(0);
			return new PersonInfo(from, transaction, nick);
		}

		@Override
		protected String paramSpec()
		{
			return "<nick>";
		}

		@Override
		public String shortHelp()
		{
			return "Displays info about all the persons in the database with a given nick.";
		}

	}

}
