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

@TaggedCommand(tag = "dpp", groupPath = "/authority", factory = DeletePrivatePerson.Factory.class)
public class DeletePrivatePerson extends TransactionalCommand
{
	private final String nick;

	protected DeletePrivatePerson(CliJPanel from, Transaction transaction, String nick)
	{
		super(from, transaction);
		this.nick = nick;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		PrivatePerson person = getPersistenceManager().privatePersonsByNick(getTransaction()).get(nick);
		if (person == null)
			throw new Exception("Not found");
		if (!person.isOrphan())
			throw new Exception("Not orphan");
		person.delete(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeletePrivatePerson>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public DeletePrivatePerson parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			String nick = split.get(0);
			return new DeletePrivatePerson(cliJPanel, transaction, nick);
		}

		@Override
		protected String paramSpec()
		{
			return "<nick>";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes a person with private key by nick";
		}

	}

}
