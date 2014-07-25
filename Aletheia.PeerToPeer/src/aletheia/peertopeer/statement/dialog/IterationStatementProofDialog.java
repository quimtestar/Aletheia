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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.DuplicateSuccessorException;
import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.dialog.StatementProofDialog.StatementStackEntry.SubscriptionDependencies;
import aletheia.peertopeer.statement.message.ContextProofRequestMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerRequestMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeInfoMessage;
import aletheia.peertopeer.statement.message.DelegateTreeInfoMessage.MissingDependencyException;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyResponseMessage;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonRequisiteMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementRequestMessage;
import aletheia.peertopeer.statement.message.StatementRequisiteMessage;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.ComposedBijection;
import aletheia.utilities.collections.DifferenceSet;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.FilteredKeyMap;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.ReverseList;
import aletheia.utilities.collections.UnionCollection;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class IterationStatementProofDialog extends StatementProofDialog
{
	private final static Logger logger = LoggerManager.logger();
	private final static int contextAmount = 100;
	private final static float statementExtendTime = 0.3f; // in seconds

	public IterationStatementProofDialog(Phase phase, Stack<StatementStackEntry> statementStack, boolean sending, boolean receiving)
	{
		super(phase, statementStack, sending, receiving);
	}

	private Map<Context, SubscriptionDependencies> pendingProofStatementsToContext() throws InterruptedException
	{
		Map<Context, SubscriptionDependencies> contexts = new HashMap<Context, SubscriptionDependencies>();
		Set<StatementStackEntry> visited = new HashSet<StatementStackEntry>();
		while (!getStatementStack().isEmpty() && contexts.size() < contextAmount)
		{
			StatementStackEntry sse = getStatementStack().pop();
			if (!visited.contains(sse))
			{
				visited.add(sse);
				if (sse.getSubscriptionDependencies().anyStillSubscribed(getPersistenceManager(), getTransaction()))
				{
					Statement st = getPersistenceManager().getStatement(getTransaction(), sse.getStatementUuid());
					if (st != null)
					{
						StatementAuthority stAuth = st.getAuthority(getTransaction());
						if (stAuth == null || !stAuth.isSignedProof())
						{
							for (Statement dep : st.dependencies(getTransaction()))
								getStatementStack().push(new StatementStackEntry(dep.getUuid(), sse.getSubscriptionDependencies()));
							if (st instanceof Context)
							{
								Context ctx = (Context) st;
								for (Statement sol : ctx.solvers(getTransaction()))
									getStatementStack().push(new StatementStackEntry(sol.getUuid(), sse.getSubscriptionDependencies()));
								contexts.put(ctx, sse.getSubscriptionDependencies().combine(contexts.get(ctx)));
							}
						}
					}
				}
			}
		}
		return contexts;
	}

	private void dialogateContextDescendentsDependenciesRequestSend(Collection<Context> contexts) throws IOException, InterruptedException
	{
		Collection<UUID> uuids = new BijectionCollection<>(new ComposedBijection<>(getStatementUuidBijection(), new CastBijection<Context, Statement>()),
				contexts);
		sendMessage(new ContextProofRequestMessage(uuids));
	}

	private ContextProofRequestMessage dialogateContextProofRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(ContextProofRequestMessage.class);
	}

	private void dialogatePersonStatementRequisiteSend(ContextProofRequestMessage contextDescendentsDependenciesRequestMessage) throws IOException,
			InterruptedException
	{
		Collection<PersonRequisiteMessage.Entry> personRequisiteMessageEntries = new ArrayList<PersonRequisiteMessage.Entry>();
		Collection<StatementRequisiteMessage.Entry> statementRequisiteMessageEntries = new ArrayList<StatementRequisiteMessage.Entry>();
		for (UUID uuid : contextDescendentsDependenciesRequestMessage.getUuids())
		{
			Context context = getPersistenceManager().getContext(getTransaction(), uuid);
			if (context != null)
			{
				Set<UUID> personUuidDependencies = new HashSet<UUID>();
				Set<UUID> statementUuidDependencies = new HashSet<UUID>();
				for (Statement st : context.localStatements(getTransaction()).values())
				{
					StatementAuthority stAuth = st.getAuthority(getTransaction());
					if (stAuth != null && stAuth.isSignedProof())
					{
						personUuidDependencies.add(stAuth.getAuthorUuid());
						statementUuidDependencies.add(st.getUuid());
						statementUuidDependencies.addAll(st.getUuidDependencies());
					}
				}
				personRequisiteMessageEntries.add(new PersonRequisiteMessage.Entry(context.getUuid(), personUuidDependencies));
				statementRequisiteMessageEntries.add(new StatementRequisiteMessage.Entry(context.getUuid(), statementUuidDependencies));
			}
		}
		sendMessage(new PersonRequisiteMessage(personRequisiteMessageEntries));
		sendMessage(new StatementRequisiteMessage(statementRequisiteMessageEntries));
	}

	private PersonRequisiteMessage dialogatePersonRequisiteRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonRequisiteMessage.class);
	}

	private StatementRequisiteMessage dialogateStatementRequisiteRecv() throws IOException, ProtocolException
	{
		return recvMessage(StatementRequisiteMessage.class);
	}

	private boolean filterDelegateTreeInfoMessageContextUuid(DelegateTreeInfoMessage delegateTreeInfoMessage, UUID contextUuid)
	{
		Set<UUID> filteredContextUuids = new DifferenceSet<>(delegateTreeInfoMessage.getMap().keySet(), delegateTreeInfoMessage.filterFullyUpdatedMap(
				getPersistenceManager(), getTransaction()).keySet());
		Context context = getPersistenceManager().getContext(getTransaction(), contextUuid);
		if (context == null)
			return false;
		for (Context ctx : new ReverseList<>(context.statementPath(getTransaction())))
			if (filteredContextUuids.contains(ctx.getUuid()))
				return false;
		return true;
	}

	private void dialogatePersonRequestSend(PersonRequisiteMessage personRequisiteMessage, final DelegateTreeInfoMessage delegateTreeInfoMessage)
			throws InterruptedException, IOException
	{
		Collection<UUID> personUuidsFiltered = new FilteredCollection<>(new Filter<UUID>()
		{
			@Override
			public boolean filter(UUID personUuid)
			{
				return getPersistenceManager().getPerson(getTransaction(), personUuid) == null;
			}
		}, new UnionCollection<UUID>(new FilteredKeyMap<UUID, Collection<UUID>>(new Filter<UUID>()
		{

			@Override
			public boolean filter(UUID contextUuid)
			{
				return filterDelegateTreeInfoMessageContextUuid(delegateTreeInfoMessage, contextUuid);
			}
		}, personRequisiteMessage.getMap()).values()));
		sendMessage(new PersonRequestMessage(personUuidsFiltered));
	}

	private void dialogateStatementRequestSend(StatementRequisiteMessage statementRequisiteMessage, final DelegateTreeInfoMessage delegateTreeInfoMessage)
			throws InterruptedException, IOException
	{
		Collection<UUID> statementUuidsFiltered = new FilteredCollection<>(new Filter<UUID>()
		{
			@Override
			public boolean filter(UUID uuid)
			{
				Statement st = getPersistenceManager().getStatement(getTransaction(), uuid);
				if (st == null)
					return true;
				StatementAuthority stAuth = st.getAuthority(getTransaction());
				if (stAuth == null)
					return true;
				if (!stAuth.isValidSignature())
					return true;
				return false;
			}
		}, new UnionCollection<UUID>(new FilteredKeyMap<UUID, Collection<UUID>>(new Filter<UUID>()
		{

			@Override
			public boolean filter(UUID contextUuid)
			{
				return filterDelegateTreeInfoMessageContextUuid(delegateTreeInfoMessage, contextUuid);
			}
		}, statementRequisiteMessage.getMap()).values()));

		StatementRequestMessage filtered = new StatementRequestMessage(statementUuidsFiltered);
		extendRemainingTime((long) (statementExtendTime * 1000 * filtered.getUuids().size()));
		sendMessage(filtered);
	}

	private PersonRequestMessage dialogatePersonRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonRequestMessage.class);
	}

	private StatementRequestMessage dialogateStatementRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(StatementRequestMessage.class);
	}

	private void dialogatePersonResponseSend(PersonRequestMessage personRequestMessage) throws IOException, InterruptedException
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

	private void dialogateStatementResponseSend(StatementRequestMessage statementRequestMessage) throws IOException, InterruptedException
	{
		Collection<Statement> statements = responseStatementListDependencySorted(getTransaction(), statementRequestMessage.getUuids());
		extendRemainingTime((long) (statementExtendTime * 1000 * statements.size()));
		sendMessage(StatementResponseMessage.create(statements));
	}

	private PersonResponseMessage dialogatePersonResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonResponseMessage.class);
	}

	private StatementResponseMessage dialogateStatementResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(StatementResponseMessage.class);
	}

	private void dialogateDelegateTreeInfoSend(ContextProofRequestMessage contextDescendentsDependenciesRequestMessage) throws IOException,
			InterruptedException
	{
		Collection<DelegateTreeInfoMessage.Entry> entries = new ArrayList<DelegateTreeInfoMessage.Entry>();
		for (UUID uuid : contextDescendentsDependenciesRequestMessage.getUuids())
		{
			Context ctx = getPersistenceManager().getContext(getTransaction(), uuid);
			if (ctx != null)
			{
				StatementAuthority ctxAuth = ctx.getAuthority(getTransaction());
				DelegateTreeRootNode delegateTreeRootNode = ctxAuth.getDelegateTreeRootNode(getTransaction());
				if (delegateTreeRootNode != null)
				{
					if (delegateTreeRootNode.isSigned())
					{

						DelegateTreeInfoMessage.DelegateTreeRootNodeInfo delegateTreeRootNodeInfo = new DelegateTreeInfoMessage.DelegateTreeRootNodeInfo(
								getTransaction(), delegateTreeRootNode);
						entries.add(new DelegateTreeInfoMessage.Entry(ctx.getUuid(), delegateTreeRootNodeInfo));
					}
				}
			}
		}
		sendMessage(new DelegateTreeInfoMessage(entries));
	}

	private DelegateTreeInfoMessage dialogateDelegateTreeInfoRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeInfoMessage.class);
	}

	private void dialogateDelegateTreeSuccessorDependencyRequestSend(DelegateTreeInfoMessage delegateTreeInfoMessage) throws InterruptedException, IOException
	{
		Collection<UUID> successorUuids = delegateTreeInfoMessage.successorUuidDependencies(getPersistenceManager(), getTransaction());
		sendMessage(new DelegateTreeSuccessorDependencyRequestMessage(successorUuids));
	}

	private void dialogateDelegateTreeDelegateDependencyRequestSend(DelegateTreeInfoMessage delegateTreeInfoMessage) throws InterruptedException, IOException
	{
		Collection<UUID> delegateUuids = delegateTreeInfoMessage.delegateUuidDependencies(getPersistenceManager(), getTransaction());
		sendMessage(new DelegateTreeDelegateDependencyRequestMessage(delegateUuids));
	}

	private void dialogateDelegateAuthorizerRequestSend(DelegateTreeInfoMessage delegateTreeInfoMessage) throws IOException, InterruptedException
	{
		sendMessage(new DelegateAuthorizerRequestMessage(getPersistenceManager(), getTransaction(), delegateTreeInfoMessage));
	}

	private DelegateTreeSuccessorDependencyRequestMessage dialogateDelegateTreeSuccessorDependencyRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeSuccessorDependencyRequestMessage.class);
	}

	private DelegateTreeDelegateDependencyRequestMessage dialogateDelegateTreeDelegateDependencyRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeDelegateDependencyRequestMessage.class);
	}

	private DelegateAuthorizerRequestMessage dialogateDelegateAuthorizerRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateAuthorizerRequestMessage.class);
	}

	private void dialogateDelegateTreeSuccessorDependencyResponseSend(
			DelegateTreeSuccessorDependencyRequestMessage delegateTreeSuccessorDependencyRequestMessage) throws InterruptedException, IOException
	{
		List<AbstractUUIDPersistentInfoMessage.Entry<Person>> successorEntryList = new ArrayList<AbstractUUIDPersistentInfoMessage.Entry<Person>>();
		for (UUID uuid : delegateTreeSuccessorDependencyRequestMessage.getUuids())
		{
			Person successor = getPersistenceManager().getPerson(getTransaction(), uuid);
			if (successor != null)
				successorEntryList.add(new AbstractUUIDPersistentInfoMessage.Entry<Person>(uuid, successor));
		}
		sendMessage(new DelegateTreeSuccessorDependencyResponseMessage(successorEntryList));
	}

	private void dialogateDelegateTreeDelegateDependencyResponseSend(DelegateTreeDelegateDependencyRequestMessage delegateTreeDelegateDependencyRequestMessage)
			throws InterruptedException, IOException
	{
		List<AbstractUUIDPersistentInfoMessage.Entry<Person>> delegateEntryList = new ArrayList<AbstractUUIDPersistentInfoMessage.Entry<Person>>();
		for (UUID uuid : delegateTreeDelegateDependencyRequestMessage.getUuids())
		{
			Person delegate = getPersistenceManager().getPerson(getTransaction(), uuid);
			if (delegate != null)
				delegateEntryList.add(new AbstractUUIDPersistentInfoMessage.Entry<Person>(uuid, delegate));
		}
		sendMessage(new DelegateTreeDelegateDependencyResponseMessage(delegateEntryList));
	}

	private void dialogateDelegateAuthorizerResponseSend(DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage) throws IOException,
			InterruptedException
	{
		sendMessage(new DelegateAuthorizerResponseMessage(getPersistenceManager(), getTransaction(), delegateAuthorizerRequestMessage));
	}

	private DelegateTreeSuccessorDependencyResponseMessage dialogateDelegateTreeSuccessorDependencyResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeSuccessorDependencyResponseMessage.class);
	}

	private DelegateTreeDelegateDependencyResponseMessage dialogateDelegateTreeDelegateDependencyResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeDelegateDependencyResponseMessage.class);
	}

	private DelegateAuthorizerResponseMessage dialogateDelegateAuthorizerResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateAuthorizerResponseMessage.class);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		Map<Context, SubscriptionDependencies> contexts = null;
		if (isReceiving())
		{
			contexts = pendingProofStatementsToContext();
			dialogateContextDescendentsDependenciesRequestSend(contexts.keySet());
			setReceiving(!contexts.isEmpty());
		}
		ContextProofRequestMessage contextProofRequestMessage = null;
		if (isSending())
		{
			contextProofRequestMessage = dialogateContextProofRequestRecv();
			logger.debug("Sending: " + contextProofRequestMessage.getUuids().size() + " contexts");
			setSending(!contextProofRequestMessage.getUuids().isEmpty());
			if (isSending())
				dialogateDelegateTreeInfoSend(contextProofRequestMessage);
			else if (!isReceiving())
				return;
		}
		else if (!isReceiving())
			return;
		DelegateTreeInfoMessage delegateTreeInfoMessage = null;
		if (isReceiving())
		{
			delegateTreeInfoMessage = dialogateDelegateTreeInfoRecv();
			dialogateDelegateTreeSuccessorDependencyRequestSend(delegateTreeInfoMessage);
			dialogateDelegateTreeDelegateDependencyRequestSend(delegateTreeInfoMessage);
		}
		if (isSending())
		{
			DelegateTreeSuccessorDependencyRequestMessage delegateTreeSuccessorDependencyRequestMessage = dialogateDelegateTreeSuccessorDependencyRequestRecv();
			dialogateDelegateTreeSuccessorDependencyResponseSend(delegateTreeSuccessorDependencyRequestMessage);
			DelegateTreeDelegateDependencyRequestMessage delegateTreeDelegateDependencyRequestMessage = dialogateDelegateTreeDelegateDependencyRequestRecv();
			dialogateDelegateTreeDelegateDependencyResponseSend(delegateTreeDelegateDependencyRequestMessage);
			DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage = dialogateDelegateAuthorizerRequestRecv();
			dialogateDelegateAuthorizerResponseSend(delegateAuthorizerRequestMessage);
			dialogatePersonStatementRequisiteSend(contextProofRequestMessage);
		}
		if (isReceiving())
		{
			dialogateDelegateTreeSuccessorDependencyResponseRecv();
			dialogateDelegateTreeDelegateDependencyResponseRecv();
			try
			{
				delegateTreeInfoMessage.update(getPersistenceManager(), getTransaction());
			}
			catch (SignatureVerifyException | MissingDependencyException | DateConsistenceException | DuplicateSuccessorException | SignatureVersionException e)
			{
				throw new ProtocolException(e);
			}
			dialogateDelegateAuthorizerRequestSend(delegateTreeInfoMessage);
			dialogateDelegateAuthorizerResponseRecv();
			PersonRequisiteMessage personRequisiteMessage = dialogatePersonRequisiteRecv();
			dialogatePersonRequestSend(personRequisiteMessage, delegateTreeInfoMessage);
			StatementRequisiteMessage statementRequisiteMessage = dialogateStatementRequisiteRecv();
			dialogateStatementRequestSend(statementRequisiteMessage, delegateTreeInfoMessage);
		}
		if (isSending())
		{
			PersonRequestMessage personRequestMessage = dialogatePersonRequestRecv();
			dialogatePersonResponseSend(personRequestMessage);
			StatementRequestMessage statementRequestMessage = dialogateStatementRequestRecv();
			dialogateStatementResponseSend(statementRequestMessage);
		}
		if (isReceiving())
		{
			dialogatePersonResponseRecv();
			StatementResponseMessage statementResponseMessage = dialogateStatementResponseRecv();
			for (Statement statement : statementResponseMessage.getMap().values())
			{
				if (!statement.isValidSignature(getTransaction()))
					throw new ProtocolException();
				SubscriptionDependencies subscriptionDependencies = contexts.get(statement.getContext(getTransaction()));
				if (subscriptionDependencies != null)
					getStatementStack().push(new StatementStackEntry(statement.getUuid(), subscriptionDependencies));
			}
		}
	}

}
