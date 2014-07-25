/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.statement.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage;
import aletheia.peertopeer.statement.message.StatementRequestMessage;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage.NoValidSignature;
import aletheia.peertopeer.statement.message.SubscriptionContextsMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;

public class NewStatementsLoopDialogClient extends NewStatementsLoopDialog
{

	public NewStatementsLoopDialogClient(Phase phase)
	{
		super(phase);
	}

	private void dialogateSubscriptionContextsMessageSend(Set<StatementAuthority> statementAuthorities) throws IOException, InterruptedException
	{
		List<SubscriptionContextsMessage.Entry> entries = new ArrayList<SubscriptionContextsMessage.Entry>();
		for (StatementAuthority statementAuthority : statementAuthorities)
			try
			{
				entries.add(new SubscriptionContextsMessage.Entry(new StatementAuthoritySubMessage(getTransaction(), statementAuthority)));
			}
			catch (NoValidSignature e)
			{
			}
		sendMessage(new SubscriptionContextsMessage(entries));
	}

	private StatementRequestMessage dialogateStatementRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(StatementRequestMessage.class);
	}

	private PersonRequestMessage dialogatePersonRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonRequestMessage.class);
	}

	private void dialogatePersonResponseSend(PersonRequestMessage personRequestMessage) throws IOException, ProtocolException, InterruptedException
	{
		Collection<Person> persons = new FilteredCollection<Person>(new NotNullFilter<Person>(), new BijectionCollection<UUID, Person>(
				new Bijection<UUID, Person>()
				{

					@Override
					public Person forward(UUID uuid)
					{
						return getPersistenceManager().getPerson(getTransaction(), uuid);
					}

					@Override
					public UUID backward(Person output)
					{
						throw new UnsupportedOperationException();
					}
				}, personRequestMessage.getUuids()));

		sendMessage(PersonResponseMessage.create(persons));
	}

	private void dialogateStatementResponseSend(StatementRequestMessage statementRequestMessage) throws IOException, InterruptedException, ProtocolException
	{
		Collection<Statement> statements = responseStatementListDependencySorted(getTransaction(), statementRequestMessage.getUuids());
		sendMessage(StatementResponseMessage.create(statements));
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		Set<StatementAuthority> pendingStatementAuthorities = getPendingPersistentDataChanges().dumpPendingStatementAuthoritySignedDependencies();
		if (pendingStatementAuthorities == null)
			pendingStatementAuthorities = Collections.emptySet();
		try
		{
			dialogateSubscriptionContextsMessageSend(pendingStatementAuthorities);
			PersonRequestMessage personRequestMessage = dialogatePersonRequestRecv();
			dialogatePersonResponseSend(personRequestMessage);
			StatementRequestMessage statementRequestMessage = dialogateStatementRequestRecv();
			dialogateStatementResponseSend(statementRequestMessage);
		}
		catch (Exception e)
		{
			getPendingPersistentDataChanges().statementAuthoritySignedDependenciesChanged(pendingStatementAuthorities);
			throw e;
		}

	}

}
