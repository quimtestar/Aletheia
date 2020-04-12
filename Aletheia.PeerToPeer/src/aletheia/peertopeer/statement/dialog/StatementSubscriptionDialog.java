/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.DuplicateSuccessorException;
import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.LocalSubscription;
import aletheia.peertopeer.statement.LocalSubscription.LocalSubContextSubscription;
import aletheia.peertopeer.statement.StatementUuidBijection;
import aletheia.peertopeer.statement.message.ContextStatementSignaturesResponseMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerRequestMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeInfoMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyResponseMessage;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.peertopeer.statement.message.StatementAuthoritySubMessage.NoValidSignature;
import aletheia.peertopeer.statement.message.StatementRequestMessage;
import aletheia.peertopeer.statement.message.SubscriptionContextsMessage;
import aletheia.peertopeer.statement.message.SubscriptionSubContextsMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.ComposedBijection;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.InverseBijection;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.UnionCollection;

public abstract class StatementSubscriptionDialog extends StatementDialog
{
	private final static float statementExtendTime = 0.3f; //in seconds
	private final static float contextExtendTime = 0.1f; //in seconds

	private final StatementUuidBijection statementUuidBijection;
	private final LocalSubscription localSubscription;

	public StatementSubscriptionDialog(Phase phase)
	{
		super(phase);
		statementUuidBijection = new StatementUuidBijection(getPersistenceManager(), getTransaction());
		localSubscription = new LocalSubscription(getPersistenceManager(), getTransaction());
	}

	protected StatementUuidBijection getStatementUuidBijection()
	{
		return statementUuidBijection;
	}

	protected LocalSubscription getLocalSubscription()
	{
		return localSubscription;
	}

	protected SubscriptionContextsMessage dialogateSubscriptionContextsSend(Collection<Context> contexts) throws InterruptedException, IOException
	{
		List<SubscriptionContextsMessage.Entry> entries = new ArrayList<>();
		for (Context ctx : contexts)
		{
			StatementAuthority statementAuthority = ctx.getAuthority(getTransaction());
			if ((statementAuthority != null) && statementAuthority.isValidSignature())
				try
				{
					entries.add(new SubscriptionContextsMessage.Entry(new StatementAuthoritySubMessage(getTransaction(), statementAuthority)));
				}
				catch (NoValidSignature e)
				{
					entries.add(new SubscriptionContextsMessage.Entry(ctx.getUuid()));
				}
			else
				entries.add(new SubscriptionContextsMessage.Entry(ctx.getUuid()));
		}
		SubscriptionContextsMessage message = new SubscriptionContextsMessage(entries);
		sendMessage(message);
		return message;
	}

	protected SubscriptionContextsMessage dialogateSubscriptionContextsRecv() throws IOException, ProtocolException
	{
		return recvMessage(SubscriptionContextsMessage.class);
	}

	private Map<UUID, StatementAuthoritySubMessage> dialogateSubscriptionContexts(Collection<Context> contexts)
			throws IOException, ProtocolException, InterruptedException
	{
		SubscriptionContextsMessage sended = dialogateSubscriptionContextsSend(contexts);
		SubscriptionContextsMessage received = dialogateSubscriptionContextsRecv();
		Map<UUID, StatementAuthoritySubMessage> statementAuthoritySubMessages = new HashMap<>();
		for (Map.Entry<UUID, StatementAuthoritySubMessage> e : received.getMap().entrySet())
			if (sended.getMap().containsKey(e.getKey()))
				statementAuthoritySubMessages.put(e.getKey(), e.getValue());
		extendRemainingTime((long) (1000 * contextExtendTime * statementAuthoritySubMessages.size()));
		return Collections.unmodifiableMap(statementAuthoritySubMessages);
	}

	private ContextStatementSignaturesResponseMessage dialogateContextStatementSignaturesResponse(
			List<ContextStatementSignaturesResponseMessage.Entry> contextStatementSignaturesEntryList)
			throws IOException, ProtocolException, InterruptedException
	{
		ContextStatementSignaturesResponseMessage sended = new ContextStatementSignaturesResponseMessage(contextStatementSignaturesEntryList);
		sendMessage(sended);
		ContextStatementSignaturesResponseMessage received = recvMessage(ContextStatementSignaturesResponseMessage.class);
		extendRemainingTime((long) (1000 * statementExtendTime
				* Math.max(new UnionCollection<>(sended.getMap().values()).size(), new UnionCollection<>(received.getMap().values()).size())));
		return received;
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
					if (statementAuthoritySubMessage.getContextUuid() != null)
					{
						Context context = getPersistenceManager().getContext(getTransaction(), statementAuthoritySubMessage.getContextUuid());
						if (context != null && statementAuthoritySubMessage.getIdentifier() != null)
						{
							Statement st = context.localIdentifierToStatement(getTransaction()).get(statementAuthoritySubMessage.getIdentifier());
							if (st != null && !st.getUuid().equals(statementAuthoritySubMessage.getStatementUuid()))
							{
								StatementAuthority stAuth = st.getAuthority(getTransaction());
								if (stAuth != null)
								{
									StatementAuthoritySignature stAuthSig = stAuth.lastValidSignature(getTransaction());
									if (stAuthSig != null)
									{
										if (stAuthSig.getSignatureDate().compareTo(lastSignatureSubMessage.getSignatureDate()) > 0)
											return false;
									}
								}
							}
						}
					}
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

	private StatementRequestMessage dialogateStatementRequest(Collection<StatementAuthoritySubMessage> statementAuthoritySubMessages)
			throws IOException, ProtocolException, InterruptedException
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
		StatementRequestMessage sent = new StatementRequestMessage(statementRequestUuids);
		sendMessage(sent);
		StatementRequestMessage received = recvMessage(StatementRequestMessage.class);
		extendRemainingTime((long) (statementExtendTime * 1000 * Math.max(sent.getUuids().size(), received.getUuids().size())));
		return received;
	}

	private PersonRequestMessage dialogatePersonRequest(Collection<StatementAuthoritySubMessage> statementAuthoritySubMessages)
			throws IOException, ProtocolException, InterruptedException
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

		PersonRequestMessage sent = new PersonRequestMessage(personRequestUuids);
		sendMessage(sent);
		PersonRequestMessage received = recvMessage(PersonRequestMessage.class);
		return received;
	}

	private PersonResponseMessage dialogatePersonResponse(PersonRequestMessage personRequestMessage) throws IOException, ProtocolException, InterruptedException
	{
		Collection<Person> persons = new FilteredCollection<>(new NotNullFilter<Person>(), new BijectionCollection<>(new Bijection<UUID, Person>()
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
		return recvMessage(PersonResponseMessage.class);
	}

	private StatementResponseMessage dialogateStatementResponse(StatementRequestMessage statementRequestMessage)
			throws IOException, InterruptedException, ProtocolException
	{
		Collection<Statement> statements = responseStatementListDependencySorted(getTransaction(), statementRequestMessage.getUuids());
		extendRemainingTime((long) (statementExtendTime * 1000 * statements.size()));
		sendMessage(StatementResponseMessage.create(statements));
		return recvMessage(StatementResponseMessage.class);
	}

	private Collection<UUID> dialogateSubscriptionSubContextMessage(List<SubscriptionSubContextsMessage.Entry> subscriptionSubContextsEntryList)
			throws IOException, ProtocolException, InterruptedException
	{
		SubscriptionSubContextsMessage sended = new SubscriptionSubContextsMessage(subscriptionSubContextsEntryList);
		sendMessage(sended);
		SubscriptionSubContextsMessage received = recvMessage(SubscriptionSubContextsMessage.class);
		Set<UUID> uuids = new HashSet<>();
		for (Map.Entry<UUID, SubscriptionSubContextsMessage.SubContextSubscriptionUuids> e : received.getMap().entrySet())
		{
			SubscriptionSubContextsMessage.SubContextSubscriptionUuids subContextSubscriptionUuidsSended = sended.getMap().get(e.getKey());
			SubscriptionSubContextsMessage.SubContextSubscriptionUuids subContextSubscriptionUuidsReceived = e.getValue();
			for (UUID uuid : subContextSubscriptionUuidsReceived.getContextUuids())
				if (subContextSubscriptionUuidsSended.getContextUuids().contains(uuid))
					uuids.add(uuid);
			for (UUID uuid : e.getValue().getProofUuids())
			{
				Statement statement = getPersistenceManager().getStatement(getTransaction(), uuid);
				if (statement != null)
					getRemoteSubscription().addProof(getTransaction(), statement);
			}
		}
		extendRemainingTime((long) (1000 * contextExtendTime * uuids.size()));
		return Collections.unmodifiableCollection(uuids);
	}

	private boolean processContexts(Collection<Context> contexts, List<ContextStatementSignaturesResponseMessage.Entry> contextStatementSignaturesEntryList,
			List<SubscriptionSubContextsMessage.Entry> subscriptionSubContextsEntryList) throws InterruptedException
	{
		boolean processed = false;
		for (Context ctx : contexts)
		{
			processed = true;
			getStatementPhase().contextStateListenTo(getTransaction(), ctx);
			getRemoteSubscription().addContext(getTransaction(), ctx);
			Set<StatementAuthoritySubMessage> statementMessageDataSet = new HashSet<>();
			ContextAuthority ctxAuth = ctx.getAuthority(getTransaction());
			if (ctxAuth != null)
			{
				for (StatementAuthority statementAuthority : ctxAuth.signedDependenciesLocalAuthoritiesSet(getTransaction()))
					try
					{
						statementMessageDataSet.add(new StatementAuthoritySubMessage(getTransaction(), statementAuthority));
					}
					catch (NoValidSignature e)
					{
					}
			}
			contextStatementSignaturesEntryList.add(new ContextStatementSignaturesResponseMessage.Entry(ctx.getUuid(), statementMessageDataSet));
			Set<UUID> contextUuids = new HashSet<>();
			LocalSubContextSubscription localSubContextSubscription = localSubscription.subContextSubscriptions().get(ctx.getUuid());
			if (localSubContextSubscription != null)
			{
				for (UUID uuid : localSubscription.subContextSubscriptions().get(ctx.getUuid()).contextUuids())
					contextUuids.add(uuid);
			}
			SubscriptionSubContextsMessage.SubContextSubscriptionUuids subContextSubscriptionUuids = new SubscriptionSubContextsMessage.SubContextSubscriptionUuids(
					contextUuids, localSubscription.subContextSubscriptions().get(ctx.getUuid()).proofUuids());
			subscriptionSubContextsEntryList.add(new SubscriptionSubContextsMessage.Entry(ctx.getUuid(), subContextSubscriptionUuids));
		}
		return processed;
	}

	private DelegateTreeInfoMessage dialogateDelegateTreeInfo(Collection<Context> contexts) throws IOException, ProtocolException, InterruptedException
	{
		Collection<DelegateTreeInfoMessage.Entry> entries = new ArrayList<>();
		for (Context ctx : contexts)
		{
			StatementAuthority ctxAuth = ctx.getAuthority(getTransaction());
			if (ctxAuth != null)
			{
				DelegateTreeRootNode delegateTreeRootNode = ctxAuth.getDelegateTreeRootNode(getTransaction());
				if (delegateTreeRootNode != null)
				{
					if (delegateTreeRootNode.isSigned())
					{

						DelegateTreeInfoMessage.DelegateTreeRootNodeInfo delegateTreeRootNodeInfo = new DelegateTreeInfoMessage.DelegateTreeRootNodeInfo(
								getTransaction(), delegateTreeRootNode);
						entries.add(new DelegateTreeInfoMessage.Entry(ctx.getUuid(), delegateTreeRootNodeInfo));
					}
					else
						ctxAuth.deleteDelegateTree(getTransaction());
				}
			}
		}
		sendMessage(new DelegateTreeInfoMessage(entries));
		return recvMessage(DelegateTreeInfoMessage.class);
	}

	private DelegateTreeSuccessorDependencyRequestMessage dialogateDelegateTreeSuccessorDependencyRequest(DelegateTreeInfoMessage delegateTreeInfoMessage)
			throws InterruptedException, IOException, ProtocolException
	{
		Collection<UUID> successorUuids = delegateTreeInfoMessage.successorUuidDependencies(getPersistenceManager(), getTransaction());
		sendMessage(new DelegateTreeSuccessorDependencyRequestMessage(successorUuids));
		return recvMessage(DelegateTreeSuccessorDependencyRequestMessage.class);
	}

	private DelegateTreeDelegateDependencyRequestMessage dialogateDelegateTreeDelegateDependencyRequest(DelegateTreeInfoMessage delegateTreeInfoMessage)
			throws InterruptedException, IOException, ProtocolException
	{
		Collection<UUID> delegateUuids = delegateTreeInfoMessage.delegateUuidDependencies(getPersistenceManager(), getTransaction());
		sendMessage(new DelegateTreeDelegateDependencyRequestMessage(delegateUuids));
		return recvMessage(DelegateTreeDelegateDependencyRequestMessage.class);
	}

	private DelegateAuthorizerRequestMessage dialogateDelegateAuthorizerRequest(DelegateTreeInfoMessage delegateTreeInfoMessage)
			throws IOException, InterruptedException, ProtocolException
	{
		sendMessage(new DelegateAuthorizerRequestMessage(getPersistenceManager(), getTransaction(), delegateTreeInfoMessage));
		return recvMessage(DelegateAuthorizerRequestMessage.class);
	}

	private DelegateTreeSuccessorDependencyResponseMessage dialogateDelegateTreeSuccessorDependencyResponse(
			DelegateTreeSuccessorDependencyRequestMessage delegateTreeSuccessorDependencyRequestMessage)
			throws InterruptedException, IOException, ProtocolException
	{
		List<AbstractUUIDPersistentInfoMessage.Entry<Person>> successorEntryList = new ArrayList<>();
		for (UUID uuid : delegateTreeSuccessorDependencyRequestMessage.getUuids())
		{
			Person successor = getPersistenceManager().getPerson(getTransaction(), uuid);
			if (successor != null)
				successorEntryList.add(new AbstractUUIDPersistentInfoMessage.Entry<>(uuid, successor));
		}
		sendMessage(new DelegateTreeSuccessorDependencyResponseMessage(successorEntryList));
		return recvMessage(DelegateTreeSuccessorDependencyResponseMessage.class);
	}

	private DelegateTreeDelegateDependencyResponseMessage dialogateDelegateTreeDelegateDependencyResponse(
			DelegateTreeDelegateDependencyRequestMessage delegateTreeDelegateDependencyRequestMessage)
			throws InterruptedException, IOException, ProtocolException
	{
		List<AbstractUUIDPersistentInfoMessage.Entry<Person>> delegateEntryList = new ArrayList<>();
		for (UUID uuid : delegateTreeDelegateDependencyRequestMessage.getUuids())
		{
			Person delegate = getPersistenceManager().getPerson(getTransaction(), uuid);
			if (delegate != null)
				delegateEntryList.add(new AbstractUUIDPersistentInfoMessage.Entry<>(uuid, delegate));
		}
		sendMessage(new DelegateTreeDelegateDependencyResponseMessage(delegateEntryList));
		return recvMessage(DelegateTreeDelegateDependencyResponseMessage.class);
	}

	private DelegateAuthorizerResponseMessage dialogateDelegateAuthorizerResponse(DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage)
			throws IOException, InterruptedException, ProtocolException
	{
		sendMessage(new DelegateAuthorizerResponseMessage(getPersistenceManager(), getTransaction(), delegateAuthorizerRequestMessage));
		return recvMessage(DelegateAuthorizerResponseMessage.class);
	}

	protected void dialogateStatementSubscriptions(Collection<Context> contexts) throws IOException, ProtocolException, InterruptedException
	{
		Map<UUID, StatementAuthoritySubMessage> statementAuthoritySubMessages = dialogateSubscriptionContexts(contexts);
		Collection<UUID> uuids = statementAuthoritySubMessages.keySet();
		{
			Collection<StatementAuthoritySubMessage> filterRequestableStatementAuthoritySubMessages = filterRequestableStatementAuthoritySubMessages(
					statementAuthoritySubMessages.values());
			PersonRequestMessage personRequestMessage = dialogatePersonRequest(filterRequestableStatementAuthoritySubMessages);
			dialogatePersonResponse(personRequestMessage);
			StatementRequestMessage statementRequestMessage = dialogateStatementRequest(filterRequestableStatementAuthoritySubMessages);
			StatementResponseMessage statementResponseMessage = dialogateStatementResponse(statementRequestMessage);
			for (Statement st : statementResponseMessage.getMap().values())
				if (!st.isValidSignature(getTransaction()))
					throw new ProtocolException();
		}
		while (!uuids.isEmpty())
		{
			contexts = new FilteredCollection<>(new NotNullFilter<Context>(), new BijectionCollection<>(
					new ComposedBijection<>(new CastBijection<Statement, Context>(), new InverseBijection<>(statementUuidBijection)), uuids));

			DelegateTreeInfoMessage delegateTreeInfoMessage = dialogateDelegateTreeInfo(contexts);
			DelegateTreeSuccessorDependencyRequestMessage delegateTreeSuccessorDependencyRequestMessage = dialogateDelegateTreeSuccessorDependencyRequest(
					delegateTreeInfoMessage);
			DelegateTreeDelegateDependencyRequestMessage delegateTreeDelegateDependencyRequestMessage = dialogateDelegateTreeDelegateDependencyRequest(
					delegateTreeInfoMessage);
			dialogateDelegateTreeSuccessorDependencyResponse(delegateTreeSuccessorDependencyRequestMessage);
			dialogateDelegateTreeDelegateDependencyResponse(delegateTreeDelegateDependencyRequestMessage);
			try
			{
				delegateTreeInfoMessage.update(getPersistenceManager(), getTransaction());
			}
			catch (SignatureVerifyException | DelegateTreeInfoMessage.MissingDependencyException | DateConsistenceException | DuplicateSuccessorException
					| SignatureVersionException e)
			{
				throw new ProtocolException(e);
			}
			DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage = dialogateDelegateAuthorizerRequest(delegateTreeInfoMessage);
			dialogateDelegateAuthorizerResponse(delegateAuthorizerRequestMessage);

			List<ContextStatementSignaturesResponseMessage.Entry> contextStatementSignaturesEntryList = new ArrayList<>();
			List<SubscriptionSubContextsMessage.Entry> subscriptionSubContextsEntryList = new ArrayList<>();
			boolean processed = processContexts(contexts, contextStatementSignaturesEntryList, subscriptionSubContextsEntryList);
			if (!processed)
				break;

			ContextStatementSignaturesResponseMessage contextStatementSignaturesResponseMessage = dialogateContextStatementSignaturesResponse(
					contextStatementSignaturesEntryList);

			Collection<StatementAuthoritySubMessage> filterRequestableStatementAuthoritySubMessages = filterRequestableStatementAuthoritySubMessages(
					new UnionCollection<>(contextStatementSignaturesResponseMessage.getMap().values()));
			PersonRequestMessage personRequestMessage = dialogatePersonRequest(filterRequestableStatementAuthoritySubMessages);
			dialogatePersonResponse(personRequestMessage);
			StatementRequestMessage statementRequestMessage = dialogateStatementRequest(filterRequestableStatementAuthoritySubMessages);
			StatementResponseMessage statementResponseMessage = dialogateStatementResponse(statementRequestMessage);
			for (Statement st : statementResponseMessage.getMap().values())
				if (!st.isValidSignature(getTransaction()))
					throw new ProtocolException();

			uuids = dialogateSubscriptionSubContextMessage(subscriptionSubContextsEntryList);
		}

	}

}
