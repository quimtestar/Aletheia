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
package aletheia.peertopeer.statement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.persistence.Transaction;

public class PendingPersistentDataChanges
{
	private Set<RootContextLocal> pendingSubscribedRootContextLocals;
	private Set<RootContextLocal> pendingUnsubscribedRootContextLocals;
	private final Map<ContextLocal, Set<ContextLocal>> pendingSubscribedContextLocals;
	private final Map<ContextLocal, Set<ContextLocal>> pendingUnsubscribedContextLocals;
	private Set<StatementAuthority> pendingStatementAuthoritySignedDependencies;
	private Set<RootContextLocal> pendingSubscribedProofRootContextLocals;
	private Set<RootContextLocal> pendingUnsubscribedProofRootContextLocals;
	private final Map<ContextLocal, Set<StatementLocal>> pendingSubscribedProofStatementLocals;
	private final Map<ContextLocal, Set<StatementLocal>> pendingUnsubscribedProofStatementLocals;
	private Set<StatementAuthority> pendingStatementAuthoritySignedProofs;
	private Set<StatementAuthority> pendingDelegateTree;

	public PendingPersistentDataChanges()
	{
		super();
		this.pendingSubscribedRootContextLocals = null;
		this.pendingUnsubscribedRootContextLocals = null;
		this.pendingSubscribedContextLocals = new HashMap<ContextLocal, Set<ContextLocal>>();
		this.pendingUnsubscribedContextLocals = new HashMap<ContextLocal, Set<ContextLocal>>();
		this.pendingStatementAuthoritySignedDependencies = null;
		this.pendingSubscribedProofRootContextLocals = null;
		this.pendingUnsubscribedProofRootContextLocals = null;
		this.pendingSubscribedProofStatementLocals = new HashMap<ContextLocal, Set<StatementLocal>>();
		this.pendingUnsubscribedProofStatementLocals = new HashMap<ContextLocal, Set<StatementLocal>>();
		this.pendingStatementAuthoritySignedProofs = null;
		this.pendingDelegateTree = null;
	}

	public synchronized void subscribeStatementsChanged(ContextLocal contextLocal, ContextLocal contextLocal_, boolean subscribed)
	{
		Map<ContextLocal, Set<ContextLocal>> map = subscribed ? pendingSubscribedContextLocals : pendingUnsubscribedContextLocals;
		Set<ContextLocal> set = map.get(contextLocal);
		if (set == null)
		{
			set = new HashSet<ContextLocal>();
			map.put(contextLocal, set);
		}
		set.add(contextLocal_);
		Map<ContextLocal, Set<ContextLocal>> map_ = subscribed ? pendingUnsubscribedContextLocals : pendingSubscribedContextLocals;
		Set<ContextLocal> set_ = map_.get(contextLocal);
		if (set_ != null)
			set_.remove(contextLocal_);
	}

	public synchronized void subscribeStatementsChanged(RootContextLocal rootContextLocal, boolean subscribed)
	{
		Set<RootContextLocal> set = subscribed ? pendingSubscribedRootContextLocals : pendingUnsubscribedRootContextLocals;
		if (set == null)
			set = new HashSet<RootContextLocal>();
		set.add(rootContextLocal);
		if (subscribed)
		{
			pendingSubscribedRootContextLocals = set;
			if (pendingUnsubscribedRootContextLocals != null)
				pendingUnsubscribedRootContextLocals.remove(rootContextLocal);
		}
		else
		{
			pendingUnsubscribedRootContextLocals = set;
			if (pendingSubscribedRootContextLocals != null)
				pendingSubscribedRootContextLocals.remove(rootContextLocal);
		}
	}

	public synchronized void subscribeStatementsChanged(Transaction transaction, ContextLocal contextLocal, boolean subscribed)
	{
		if (contextLocal instanceof RootContextLocal)
			subscribeStatementsChanged((RootContextLocal) contextLocal, subscribed);
		else
			subscribeStatementsChanged(contextLocal.getContextLocal(transaction), contextLocal, subscribed);
	}

	public synchronized void subscribeStatementsChanged(Transaction transaction, Set<ContextLocal> contextLocals, boolean subscribed)
	{
		for (ContextLocal contextLocal : contextLocals)
			subscribeStatementsChanged(transaction, contextLocal, subscribed);
	}

	public synchronized Set<RootContextLocal> dumpPendingSubscribedRootContextLocals()
	{
		Set<RootContextLocal> set = pendingSubscribedRootContextLocals;
		pendingSubscribedRootContextLocals = null;
		return set;
	}

	public synchronized Set<RootContextLocal> dumpPendingUnsubscribedRootContextLocals()
	{
		Set<RootContextLocal> set = pendingUnsubscribedRootContextLocals;
		pendingUnsubscribedRootContextLocals = null;
		return set;
	}

	public synchronized Set<ContextLocal> dumpPendingSubscribedContextLocals(ContextLocal contextLocal)
	{
		return pendingSubscribedContextLocals.remove(contextLocal);
	}

	public synchronized Set<ContextLocal> dumpPendingUnsubscribedContextLocals(ContextLocal contextLocal)
	{
		return pendingUnsubscribedContextLocals.remove(contextLocal);
	}

	public synchronized void statementAuthoritySignedDependenciesChanged(StatementAuthority statementAuthority)
	{
		if (pendingStatementAuthoritySignedDependencies == null)
			pendingStatementAuthoritySignedDependencies = new HashSet<StatementAuthority>();
		if (statementAuthority.isSignedDependencies())
			pendingStatementAuthoritySignedDependencies.add(statementAuthority);
		else
			pendingStatementAuthoritySignedDependencies.remove(statementAuthority);
	}

	public synchronized void statementAuthoritySignedDependenciesChanged(Collection<StatementAuthority> statementAuthorities)
	{
		for (StatementAuthority statementAuthority : statementAuthorities)
			statementAuthoritySignedDependenciesChanged(statementAuthority);
	}

	public synchronized Set<StatementAuthority> dumpPendingStatementAuthoritySignedDependencies()
	{
		Set<StatementAuthority> set = pendingStatementAuthoritySignedDependencies;
		pendingStatementAuthoritySignedDependencies = null;
		return set;
	}

	public synchronized void subscribeProofStatementsChanged(ContextLocal contextLocal, StatementLocal statementLocal, boolean subscribed)
	{
		Map<ContextLocal, Set<StatementLocal>> map = subscribed ? pendingSubscribedProofStatementLocals : pendingUnsubscribedProofStatementLocals;
		Set<StatementLocal> set = map.get(contextLocal);
		if (set == null)
		{
			set = new HashSet<StatementLocal>();
			map.put(contextLocal, set);
		}
		set.add(statementLocal);
		Map<ContextLocal, Set<StatementLocal>> map_ = subscribed ? pendingUnsubscribedProofStatementLocals : pendingSubscribedProofStatementLocals;
		Set<StatementLocal> set_ = map_.get(contextLocal);
		if (set_ != null)
			set_.remove(statementLocal);
	}

	public synchronized void subscribeProofStatementsChanged(RootContextLocal rootContextLocal, boolean subscribed)
	{
		Set<RootContextLocal> set = subscribed ? pendingSubscribedProofRootContextLocals : pendingUnsubscribedProofRootContextLocals;
		if (set == null)
			set = new HashSet<RootContextLocal>();
		set.add(rootContextLocal);
		if (subscribed)
		{
			pendingSubscribedProofRootContextLocals = set;
			if (pendingUnsubscribedProofRootContextLocals != null)
				pendingUnsubscribedProofRootContextLocals.remove(rootContextLocal);
		}
		else
		{
			pendingUnsubscribedProofRootContextLocals = set;
			if (pendingSubscribedProofRootContextLocals != null)
				pendingSubscribedProofRootContextLocals.remove(rootContextLocal);
		}
	}

	public synchronized void subscribeProofStatementsChanged(Transaction transaction, Set<StatementLocal> statementLocals, boolean subscribed)
	{
		for (StatementLocal statementLocal : statementLocals)
		{
			if (statementLocal instanceof RootContextLocal)
				subscribeProofStatementsChanged((RootContextLocal) statementLocal, subscribed);
			else
				subscribeProofStatementsChanged(statementLocal.getContextLocal(transaction), statementLocal, subscribed);
		}
	}

	public synchronized Set<RootContextLocal> dumpPendingSubscribedProofRootContextLocals()
	{
		Set<RootContextLocal> set = pendingSubscribedProofRootContextLocals;
		pendingSubscribedProofRootContextLocals = null;
		return set;
	}

	public synchronized Set<RootContextLocal> dumpPendingUnsubscribedProofRootContextLocals()
	{
		Set<RootContextLocal> set = pendingUnsubscribedProofRootContextLocals;
		pendingUnsubscribedProofRootContextLocals = null;
		return set;
	}

	public synchronized Set<StatementLocal> dumpPendingSubscribedProofStatementLocals(ContextLocal contextLocal)
	{
		return pendingSubscribedProofStatementLocals.remove(contextLocal);
	}

	public synchronized Set<StatementLocal> dumpPendingUnsubscribedProofStatementLocals(ContextLocal contextLocal)
	{
		return pendingUnsubscribedProofStatementLocals.remove(contextLocal);
	}

	public synchronized void statementAuthoritySignedProofsChanged(StatementAuthority statementAuthority)
	{
		if (pendingStatementAuthoritySignedProofs == null)
			pendingStatementAuthoritySignedProofs = new HashSet<StatementAuthority>();
		if (statementAuthority.isSignedProof())
			pendingStatementAuthoritySignedProofs.add(statementAuthority);
		else
			pendingStatementAuthoritySignedDependencies.remove(statementAuthority);
	}

	public synchronized void statementAuthoritySignedProofsChanged(Collection<StatementAuthority> statementAuthorities)
	{
		for (StatementAuthority statementAuthority : statementAuthorities)
			statementAuthoritySignedProofsChanged(statementAuthority);
	}

	public synchronized Set<StatementAuthority> dumpPendingStatementAuthoritySignedProofs()
	{
		Set<StatementAuthority> set = pendingStatementAuthoritySignedProofs;
		pendingStatementAuthoritySignedProofs = null;
		return set;
	}

	public synchronized void delegateTreeModified(StatementAuthority statementAuthority)
	{
		if (pendingDelegateTree == null)
			pendingDelegateTree = new HashSet<StatementAuthority>();
		pendingDelegateTree.add(statementAuthority);
	}

	public synchronized void delegateTreeModified(Collection<StatementAuthority> statementAuthorities)
	{
		for (StatementAuthority statementAuthority : statementAuthorities)
			delegateTreeModified(statementAuthority);
	}

	public synchronized Set<StatementAuthority> dumpPendingDelegateTree()
	{
		Set<StatementAuthority> set = pendingDelegateTree;
		pendingDelegateTree = null;
		return set;
	}

}
