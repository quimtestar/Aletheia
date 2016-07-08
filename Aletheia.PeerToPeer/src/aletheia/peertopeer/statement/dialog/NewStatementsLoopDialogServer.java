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
import java.util.Collection;
import java.util.UUID;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage;
import aletheia.peertopeer.statement.message.StatementRequestMessage;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.peertopeer.statement.message.SubscriptionContextsMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.UnionCollection;

public class NewStatementsLoopDialogServer extends NewStatementsLoopDialog
{

	public NewStatementsLoopDialogServer(Phase phase)
	{
		super(phase);
	}

	private SubscriptionContextsMessage dialogateSubscriptionContextsMessageRecv() throws IOException, ProtocolException
	{
		return recvMessage(SubscriptionContextsMessage.class);
	}

	private Collection<StatementAuthoritySubMessage> filterRequestableStatementAuthoritySubMessages(
			Collection<StatementAuthoritySubMessage> statementAuthoritySubMessages)
	{
		return new BufferedList<>(new FilteredCollection<>(new Filter<StatementAuthoritySubMessage>()
		{

			@Override
			public boolean filter(StatementAuthoritySubMessage statementAuthoritySubMessage)
			{
				if (statementAuthoritySubMessage != null)
				{
					StatementAuthoritySubMessage.LastSignatureSubMessage lastSignatureSubMessage = statementAuthoritySubMessage.getLastSignatureSubMessage();
					Statement statement = getPersistenceManager().getStatement(getTransaction(), statementAuthoritySubMessage.getStatementUuid());
					if (statement == null)
						return true;
					else
					{
						StatementAuthority statementAuthority = statement.getAuthority(getTransaction());
						if (statementAuthority == null)
							return true;
						else
						{
							StatementAuthoritySignature statementAuthoritySignature = statementAuthority.lastValidSignature(getTransaction());
							if (statementAuthoritySignature == null)
								return true;
							else
							{
								if (statementAuthoritySignature.getSignatureDate().compareTo(lastSignatureSubMessage.getSignatureDate()) < 0)
									return true;
							}
						}
					}
				}
				return false;
			}
		}, statementAuthoritySubMessages));
	}

	private void dialogateStatementRequestSend(Collection<StatementAuthoritySubMessage> statementAuthoritySubMessages) throws IOException, InterruptedException
	{
		Collection<UUID> statementRequestUuids = new BijectionCollection<>(new Bijection<StatementAuthoritySubMessage, UUID>()
		{

			@Override
			public UUID forward(StatementAuthoritySubMessage statementAuthoritySubMessage)
			{
				return statementAuthoritySubMessage.getStatementUuid();
			}

			@Override
			public StatementAuthoritySubMessage backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		}, statementAuthoritySubMessages);
		sendMessage(new StatementRequestMessage(statementRequestUuids));
	}

	private void dialogatePersonRequestSend(Collection<StatementAuthoritySubMessage> statementAuthoritySubMessages) throws IOException, InterruptedException
	{
		Collection<UUID> personRequestUuids = new UnionCollection<>(new BijectionCollection<>(new Bijection<StatementAuthoritySubMessage, Collection<UUID>>()
		{

			@Override
			public Collection<UUID> forward(StatementAuthoritySubMessage statementAuthoritySubMessage)
			{
				return new FilteredCollection<>(new Filter<UUID>()
				{

					@Override
					public boolean filter(UUID personUuid)
					{
						return getPersistenceManager().getPerson(getTransaction(), personUuid) == null;
					}
				}, statementAuthoritySubMessage.getPersonDependencies());
			}

			@Override
			public StatementAuthoritySubMessage backward(Collection<UUID> output)
			{
				throw new UnsupportedOperationException();
			}
		}, statementAuthoritySubMessages));

		sendMessage(new PersonRequestMessage(personRequestUuids));
	}

	private void dialogatePersonStatementRequestSend(SubscriptionContextsMessage subscriptionContextsMessage) throws IOException, InterruptedException
	{

		Bijection<AbstractUUIDInfoMessage.Entry<StatementAuthoritySubMessage>, StatementAuthoritySubMessage> bijection = new Bijection<AbstractUUIDInfoMessage.Entry<StatementAuthoritySubMessage>, StatementAuthoritySubMessage>()
		{
			@Override
			public StatementAuthoritySubMessage forward(AbstractUUIDInfoMessage.Entry<StatementAuthoritySubMessage> input)
			{
				return input.getValue();
			}

			@Override
			public AbstractUUIDInfoMessage.Entry<StatementAuthoritySubMessage> backward(StatementAuthoritySubMessage output)
			{
				throw new UnsupportedOperationException();
			}
		};

		Collection<StatementAuthoritySubMessage> filterRequestableStatementAuthoritySubMessages = filterRequestableStatementAuthoritySubMessages(
				new BijectionCollection<>(bijection, subscriptionContextsMessage.getEntries()));

		dialogatePersonRequestSend(filterRequestableStatementAuthoritySubMessages);
		dialogateStatementRequestSend(filterRequestableStatementAuthoritySubMessages);
	}

	private PersonResponseMessage dialogatePersonResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonResponseMessage.class);
	}

	private StatementResponseMessage dialogateStatementResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(StatementResponseMessage.class);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		SubscriptionContextsMessage subscriptionContextsMessage = dialogateSubscriptionContextsMessageRecv();
		dialogatePersonStatementRequestSend(subscriptionContextsMessage);
		dialogatePersonResponseRecv();
		StatementResponseMessage statementResponseMessage = dialogateStatementResponseRecv();
		for (Statement st : statementResponseMessage.getMap().values())
			if (!st.isValidSignature(getTransaction()))
				throw new ProtocolException();
	}

}
