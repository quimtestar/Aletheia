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
import java.util.List;
import java.util.UUID;

import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.DuplicateSuccessorException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.message.DelegateAuthorizerRequestMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeInfoMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeInfoMessage.MissingDependencyException;
import aletheia.protocol.ProtocolException;

public abstract class DelegateTreeDialog extends StatementDialog
{

	public DelegateTreeDialog(Phase phase)
	{
		super(phase);
	}

	protected DelegateTreeInfoMessage dialogateDelegateTreeInfoSend(Collection<DelegateTreeRootNode> delegateTreeRootNodes) throws IOException,
			InterruptedException
	{
		Collection<DelegateTreeInfoMessage.Entry> entries = new ArrayList<DelegateTreeInfoMessage.Entry>();
		for (DelegateTreeRootNode delegateTreeRootNode : delegateTreeRootNodes)
		{
			DelegateTreeInfoMessage.DelegateTreeRootNodeInfo delegateTreeRootNodeInfo = new DelegateTreeInfoMessage.DelegateTreeRootNodeInfo(getTransaction(),
					delegateTreeRootNode);
			entries.add(new DelegateTreeInfoMessage.Entry(delegateTreeRootNode.getStatementUuid(), delegateTreeRootNodeInfo));
		}
		DelegateTreeInfoMessage delegateTreeInfoMessage = new DelegateTreeInfoMessage(entries);
		sendMessage(delegateTreeInfoMessage);
		return delegateTreeInfoMessage;
	}

	protected DelegateTreeInfoMessage dialogateDelegateTreeInfoRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeInfoMessage.class);
	}

	protected void dialogateDelegateTreeSuccessorDependencyRequestSend(DelegateTreeInfoMessage delegateTreeInfoMessage) throws InterruptedException,
			IOException
	{
		Collection<UUID> successorUuids = delegateTreeInfoMessage.successorUuidDependencies(getPersistenceManager(), getTransaction());
		sendMessage(new DelegateTreeSuccessorDependencyRequestMessage(successorUuids));
	}

	protected void dialogateDelegateTreeDelegateDependencyRequestSend(DelegateTreeInfoMessage delegateTreeInfoMessage) throws InterruptedException, IOException
	{
		Collection<UUID> delegateUuids = delegateTreeInfoMessage.delegateUuidDependencies(getPersistenceManager(), getTransaction());
		sendMessage(new DelegateTreeDelegateDependencyRequestMessage(delegateUuids));
	}

	protected void dialogateDelegateAuthorizerRequestSend(DelegateTreeInfoMessage delegateTreeInfoMessage) throws IOException, InterruptedException
	{
		sendMessage(new DelegateAuthorizerRequestMessage(getPersistenceManager(), getTransaction(), delegateTreeInfoMessage));
	}

	protected DelegateTreeSuccessorDependencyRequestMessage dialogateDelegateTreeSuccessorDependencyRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeSuccessorDependencyRequestMessage.class);
	}

	protected DelegateTreeDelegateDependencyRequestMessage dialogateDelegateTreeDelegateDependencyRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeDelegateDependencyRequestMessage.class);
	}

	protected DelegateAuthorizerRequestMessage dialogateDelegateAuthorizerRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateAuthorizerRequestMessage.class);
	}

	protected void dialogateDelegateTreeSuccessorDependencyResponseSend(
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

	protected void dialogateDelegateTreeDelegateDependencyResponseSend(DelegateTreeDelegateDependencyRequestMessage delegateTreeDelegateDependencyRequestMessage)
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

	protected void dialogateDelegateAuthorizerResponseSend(DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage) throws IOException,
			InterruptedException
	{
		sendMessage(new DelegateAuthorizerResponseMessage(getPersistenceManager(), getTransaction(), delegateAuthorizerRequestMessage));
	}

	protected DelegateTreeSuccessorDependencyResponseMessage dialogateDelegateTreeSuccessorDependencyResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeSuccessorDependencyResponseMessage.class);
	}

	protected DelegateTreeDelegateDependencyResponseMessage dialogateDelegateTreeDelegateDependencyResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateTreeDelegateDependencyResponseMessage.class);
	}

	protected DelegateAuthorizerResponseMessage dialogateDelegateAuthorizerResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(DelegateAuthorizerResponseMessage.class);
	}

	protected void delegateTreeSend(Collection<DelegateTreeRootNode> delegateTreeRootNodes) throws IOException, InterruptedException, ProtocolException
	{
		DelegateTreeInfoMessage delegateTreeInfoMessage = dialogateDelegateTreeInfoSend(delegateTreeRootNodes);
		if (!delegateTreeInfoMessage.isEmpty())
		{
			DelegateTreeSuccessorDependencyRequestMessage delegateTreeSuccessorDependencyRequestMessage = dialogateDelegateTreeSuccessorDependencyRequestRecv();
			dialogateDelegateTreeSuccessorDependencyResponseSend(delegateTreeSuccessorDependencyRequestMessage);
			DelegateTreeDelegateDependencyRequestMessage delegateTreeDelegateDependencyRequestMessage = dialogateDelegateTreeDelegateDependencyRequestRecv();
			dialogateDelegateTreeDelegateDependencyResponseSend(delegateTreeDelegateDependencyRequestMessage);
			DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage = dialogateDelegateAuthorizerRequestRecv();
			dialogateDelegateAuthorizerResponseSend(delegateAuthorizerRequestMessage);
		}
	}

	protected void delegateTreeRecv() throws IOException, ProtocolException, InterruptedException
	{
		DelegateTreeInfoMessage delegateTreeInfoMessage = dialogateDelegateTreeInfoRecv();
		if (!delegateTreeInfoMessage.isEmpty())
		{
			dialogateDelegateTreeSuccessorDependencyRequestSend(delegateTreeInfoMessage);
			dialogateDelegateTreeDelegateDependencyRequestSend(delegateTreeInfoMessage);
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
		}
	}

}
