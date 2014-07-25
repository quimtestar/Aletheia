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
package aletheia.gui.cli.command.peertopeer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.authority.Person;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

@TaggedCommand(tag = "spc", groupPath = "/p2p", factory = SendPersonCredentials.Factory.class)
public class SendPersonCredentials extends PeerToPeerCommand
{
	private final Person recipient;
	private final Collection<Person> persons;

	private class MyListenableAborter extends ListenableAborter
	{
		private boolean aborted = false;
		private String cause = null;

		public synchronized void abort(String cause)
		{
			this.aborted = true;
			this.cause = cause;
			super.abort();
		}

		@Override
		public synchronized void checkAbort() throws AbortException
		{
			if (aborted)
				throw new AbortException(cause);
		}

	}

	private final MyListenableAborter myListenableAborter;

	public SendPersonCredentials(CliJPanel from, Person recipient, Collection<Person> persons)
	{
		super(from);
		this.recipient = recipient;
		this.persons = persons;
		this.myListenableAborter = new MyListenableAborter();
	}

	@Override
	public void run() throws Exception
	{
		try
		{
			boolean received = getPeerToPeerNode().sendPersons(recipient, persons, myListenableAborter);
			if (!received)
				throw new Exception("Not received.");
		}
		catch (AbortException e)
		{
			throw makeCancelledCommandException(e);
		}

	}

	@Override
	public void cancel(String cause)
	{
		myListenableAborter.abort(cause);
	}

	public static class Factory extends AbstractVoidCommandFactory<SendPersonCredentials>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public SendPersonCredentials parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Person recipient = specToPerson(cliJPanel.getPersistenceManager(), transaction, split.get(0));
			if (recipient == null)
				throw new CommandParseException("Recipient not found.");
			Collection<Person> persons = new ArrayList<Person>();
			for (int i = 1; i < split.size(); i++)
			{
				Person person = specToPerson(cliJPanel.getPersistenceManager(), transaction, split.get(i));
				if (person == null)
					throw new CommandParseException("Person not found.");
				persons.add(person);
			}
			return new SendPersonCredentials(cliJPanel, recipient, persons);
		}

		@Override
		protected String paramSpec()
		{
			return "(<recipient UUID> | <nick>) (<person UUID> | <nick>)+";
		}

		@Override
		public String shortHelp()
		{
			return "Send person credentials to another person.";
		}
	}

}
