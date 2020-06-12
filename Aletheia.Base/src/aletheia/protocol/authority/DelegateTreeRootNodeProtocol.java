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
package aletheia.protocol.authority;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.DuplicateSuccessorException;
import aletheia.model.authority.DelegateTreeRootNode.SuccessorEntry;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.model.authority.StatementAuthority;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.model.SignatureData;
import aletheia.security.protocol.SignatureDataProtocol;
import aletheia.utilities.collections.BufferedList;

@ProtocolInfo(availableVersions = 0)
public class DelegateTreeRootNodeProtocol extends DelegateTreeNodeProtocol<DelegateTreeRootNode>
{
	private final IntegerProtocol integerProtocol;
	private final UUIDProtocol uuidProtocol;
	private final DateProtocol dateProtocol;
	private final SignatureDataProtocol signatureDataProtocol;

	private final StatementAuthority statementAuthority;

	private DelegateTreeRootNode delegateTreeRootNode;

	public DelegateTreeRootNodeProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction,
			StatementAuthority statementAuthority)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(DelegateTreeRootNodeProtocol.class, requiredVersion);
		this.integerProtocol = new IntegerProtocol(0);
		this.uuidProtocol = new UUIDProtocol(0);
		this.dateProtocol = new DateProtocol(0);
		this.signatureDataProtocol = new SignatureDataProtocol(0);
		this.statementAuthority = statementAuthority;
	}

	@Override
	protected DelegateTreeRootNode obtainDelegateTreeNode()
	{
		if ((delegateTreeRootNode == null) && (statementAuthority != null))
			delegateTreeRootNode = statementAuthority.getOrCreateDelegateTreeRootNodeNoSign(getTransaction());
		return delegateTreeRootNode;
	}

	@Override
	public void send(DataOutput out, DelegateTreeRootNode delegateTreeRootNode) throws IOException
	{
		super.send(out, delegateTreeRootNode);
		List<SuccessorEntry> successorEntries = new BufferedList<>(delegateTreeRootNode.successorEntries());
		integerProtocol.send(out, successorEntries.size());
		for (SuccessorEntry successorEntry : successorEntries)
		{
			uuidProtocol.send(out, successorEntry.getSuccessorUuid());
			dateProtocol.send(out, successorEntry.getSignatureDate());
			integerProtocol.send(out, successorEntry.getSignatureVersion());
			signatureDataProtocol.send(out, successorEntry.getSignatureData());
		}
		integerProtocol.send(out, delegateTreeRootNode.getSuccessorIndex());
		dateProtocol.send(out, delegateTreeRootNode.getSignatureDate());
		integerProtocol.send(out, delegateTreeRootNode.getSignatureVersion());
		signatureDataProtocol.send(out, delegateTreeRootNode.getSignatureData());
	}

	@Override
	public DelegateTreeRootNode recv(DataInput in) throws IOException, ProtocolException
	{
		DelegateTreeRootNode delegateTreeRootNode = super.recv(in);
		try
		{
			ListIterator<SuccessorEntry> listIterator = delegateTreeRootNode.successorEntries().listIterator();
			int successorEntrySize = integerProtocol.recv(in);
			for (int i = 0; i < successorEntrySize; i++)
			{
				UUID successorUuid = uuidProtocol.recv(in);
				Date signatureDate = dateProtocol.recv(in);
				int signatureVersion = integerProtocol.recv(in);
				SignatureData signatureData = signatureDataProtocol.recv(in);
				if (listIterator != null && listIterator.hasNext())
				{
					SuccessorEntry successorEntry = listIterator.next();
					if (successorEntry.updatable(successorUuid, signatureDate, signatureData))
					{
						int position = listIterator.previousIndex();
						delegateTreeRootNode.updateSuccessorEntriesSet(getTransaction(), position, successorUuid, signatureDate, signatureVersion,
								signatureData);
						listIterator = null;
					}
				}
				else
				{
					delegateTreeRootNode.updateSuccessorEntriesAdd(getTransaction(), successorUuid, signatureDate, signatureVersion, signatureData);
					listIterator = null;
				}
			}
			int successorIndex = integerProtocol.recv(in);
			Date signatureDate = dateProtocol.recv(in);
			int signatureVersion = integerProtocol.recv(in);
			SignatureData signatureData = signatureDataProtocol.recv(in);
			delegateTreeRootNode.update(getTransaction(), successorIndex, signatureDate, signatureVersion, signatureData);
			return delegateTreeRootNode;
		}
		catch (SignatureVerifyException | DateConsistenceException | DuplicateSuccessorException | SignatureVersionException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		super.skip(in);
		int successorEntrySize = integerProtocol.recv(in);
		for (int i = 0; i < successorEntrySize; i++)
		{
			uuidProtocol.skip(in);
			dateProtocol.skip(in);
			integerProtocol.skip(in);
			signatureDataProtocol.skip(in);
		}
		integerProtocol.skip(in);
		dateProtocol.skip(in);
		integerProtocol.skip(in);
		signatureDataProtocol.skip(in);
	}

}
