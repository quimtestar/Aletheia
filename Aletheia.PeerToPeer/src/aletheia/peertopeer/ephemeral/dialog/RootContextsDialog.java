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
package aletheia.peertopeer.ephemeral.dialog;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.RootContextStatementSignaturesResponseMessage;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage;
import aletheia.peertopeer.statement.message.StatementRequestMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage.NoValidSignature;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.persistence.collections.authority.RootContextAuthorityBySignatureUuid;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;

public abstract class RootContextsDialog extends EphemeralDialog
{

	public RootContextsDialog(Phase phase)
	{
		super(phase);
	}

	protected void dialogateRootContextStatementSignaturesResponse(Collection<UUID> signatureUuids) throws IOException, InterruptedException
	{
		Collection<RootContextStatementSignaturesResponseMessage.Entry> rootContextAuthorities = new FilteredCollection<RootContextStatementSignaturesResponseMessage.Entry>(
				new NotNullFilter<RootContextStatementSignaturesResponseMessage.Entry>(),
				new BijectionCollection<UUID, RootContextStatementSignaturesResponseMessage.Entry>(
						new Bijection<UUID, RootContextStatementSignaturesResponseMessage.Entry>()
						{
							final RootContextAuthorityBySignatureUuid rootContextAuthorityBySignatureUuid = getPersistenceManager()
									.rootContextAuthorityBySignatureUuid(getTransaction());

							@Override
							public RootContextStatementSignaturesResponseMessage.Entry forward(UUID uuid)
							{
								RootContextAuthority rootContextAuthority = rootContextAuthorityBySignatureUuid.get(uuid);
								if (rootContextAuthority != null)
									try
									{
										return new RootContextStatementSignaturesResponseMessage.Entry(uuid, new StatementAuthoritySubMessage(getTransaction(),
												rootContextAuthority));
									}
									catch (NoValidSignature e)
									{
										return new RootContextStatementSignaturesResponseMessage.Entry(uuid);
									}
								else
									return new RootContextStatementSignaturesResponseMessage.Entry(uuid);
							}

							@Override
							public UUID backward(RootContextStatementSignaturesResponseMessage.Entry output)
							{
								throw new UnsupportedOperationException();
							}
						}, signatureUuids));
		sendMessage(new RootContextStatementSignaturesResponseMessage(rootContextAuthorities));
	}

	protected void dialogatePersonStatementRequestMessage(RootContextStatementSignaturesResponseMessage rootContextStatementSignaturesResponseMessage,
			Map<UUID, RootContext> rootContexts) throws IOException, InterruptedException
	{
		Set<UUID> personUuids = new HashSet<UUID>();
		Set<UUID> statementUuids = new HashSet<UUID>();
		for (AbstractUUIDInfoMessage.Entry<StatementAuthoritySubMessage> e : rootContextStatementSignaturesResponseMessage.getEntries())
		{
			UUID signatureUuid = e.getKey();
			StatementAuthoritySubMessage statementAuthoritySubMessage = e.getValue();
			{
				boolean stReq = false;
				Statement st = getPersistenceManager().getStatement(getTransaction(), statementAuthoritySubMessage.getStatementUuid());
				if (st == null)
					stReq = true;
				else
				{
					StatementAuthority stAuth = st.getAuthority(getTransaction());
					if (stAuth == null)
						stReq = true;
					else if (!stAuth.isValidSignature())
						stReq = true;
					else if (!(stAuth instanceof RootContextAuthority))
						stReq = true;
					else
					{
						RootContextAuthority rootCtxAuth = (RootContextAuthority) stAuth;
						if (!signatureUuid.equals(rootCtxAuth.getSignatureUuid()))
							stReq = true;
						else
							rootContexts.put(signatureUuid, (RootContext) st);
					}
				}
				if (stReq)
				{
					statementUuids.add(statementAuthoritySubMessage.getStatementUuid());
					for (UUID uuid : statementAuthoritySubMessage.getPersonDependencies())
						if (getPersistenceManager().getPerson(getTransaction(), uuid) == null)
							personUuids.add(uuid);
				}
			}
		}
		sendMessage(new PersonRequestMessage(personUuids));
		sendMessage(new StatementRequestMessage(statementUuids));
	}

	protected void dialogatePersonResponse(PersonRequestMessage personRequestMessage) throws IOException, ProtocolException, InterruptedException
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

	protected void dialogateStatementResponse(StatementRequestMessage statementRequestMessage) throws IOException, InterruptedException
	{
		sendMessage(StatementResponseMessage.create(responseStatementListDependencySorted(getTransaction(), statementRequestMessage.getUuids())));
	}

}
