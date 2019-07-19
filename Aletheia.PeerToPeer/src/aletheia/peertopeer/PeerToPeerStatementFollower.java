/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.peertopeer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.Aborter;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.collections.CloseableIterator;

public class PeerToPeerStatementFollower extends PeerToPeerFollower
{

	public interface Listener extends PeerToPeerFollower.Listener
	{
		void contextLocalSubscribed(ContextLocal contextLocal);

		void contextLocalUnsubscribed(ContextLocal contextLocal);

		void statementAuthoritySignedProved(StatementAuthority statementAuthority);

		void statementAuthoritySignedUnproved(StatementAuthority statementAuthority);

		void statementLocalSubscribedProof(StatementLocal statementLocal);

		void statementLocalUnsubscribedProof(StatementLocal statementLocal);

		void rootContextSignatureUpdated(UUID rootContextUuid);

	}

	private class StatementStateListener implements RootContextLocal.StateListener, ContextLocal.StateListener, Context.StateListener,
			StatementAuthority.StateListener, RootContext.TopStateListener
	{
		@Override
		public void subscribeStatementsChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
		{
			if (subscribed)
				listenToContextLocal(transaction, rootContextLocal);
			else
				unlistenToContextLocal(transaction, rootContextLocal);
		}

		@Override
		public void subscribeStatementsChanged(Transaction transaction, ContextLocal contextLocal, ContextLocal contextLocal_, boolean subscribed)
		{
			if (subscribed)
				listenToContextLocal(transaction, contextLocal_);
			else
				unlistenToContextLocal(transaction, contextLocal_);
		}

		@Override
		public void subscribeProofChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
		{
		}

		@Override
		public void subscribeProofChanged(Transaction transaction, ContextLocal contextLocal, final StatementLocal statementLocal, final boolean subscribed)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					if (subscribed)
						getListener().statementLocalSubscribedProof(statementLocal);
					else
						getListener().statementLocalUnsubscribedProof(statementLocal);
				}
			});

		}

		@Override
		public void statementAddedToContext(Transaction transaction, Context context, final Statement statement)
		{
			ContextLocal contextLocal = context.getLocal(transaction);
			if (contextLocal != null && contextLocal.isSubscribeStatements())
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						statement.addStateListener(statementStateListener);
						statement.addAuthorityStateListener(statementStateListener);
					}
				});
			}
		}

		@Override
		public void statementDeletedFromContext(Transaction transaction, Context context, final Statement statement, Identifier identifier)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					statement.removeStateListener(statementStateListener);
					statement.removeAuthorityStateListener(statementStateListener);
				}
			});
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, final StatementAuthority statementAuthority)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					statementAuthority.addStateListener(statementStateListener);
				}
			});
		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, final StatementAuthority statementAuthority)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					statementAuthority.removeStateListener(statementStateListener);
				}
			});
		}

		@Override
		public void signedProofStateChanged(Transaction transaction, final StatementAuthority statementAuthority, final boolean signedProof)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					if (signedProof)
						getListener().statementAuthoritySignedProved(statementAuthority);
					else
						getListener().statementAuthoritySignedUnproved(statementAuthority);
				}
			});
		}

		private void rootContextSignatureUpdated(Transaction transaction, final RootContextAuthority rootContextAuthority)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					getListener().rootContextSignatureUpdated(rootContextAuthority.getStatementUuid());
				}
			});
		}

		@Override
		public void signatureAdded(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
			if (statementAuthority instanceof RootContextAuthority)
				rootContextSignatureUpdated(transaction, (RootContextAuthority) statementAuthority);
		}

		@Override
		public void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
			if (statementAuthority instanceof RootContextAuthority)
				rootContextSignatureUpdated(transaction, (RootContextAuthority) statementAuthority);
		}

		@Override
		public void rootContextAdded(Transaction transaction, final RootContext rootContext)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					rootContext.addStateListener(statementStateListener);
					rootContext.addAuthorityStateListener(statementStateListener);
					getListener().rootContextSignatureUpdated(rootContext.getUuid());
				}
			});
		}

		@Override
		public void rootContextDeleted(Transaction transaction, final RootContext rootContext, Identifier identifier)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					rootContext.removeStateListener(statementStateListener);
					rootContext.removeAuthorityStateListener(statementStateListener);
					getListener().rootContextSignatureUpdated(rootContext.getUuid());
				}
			});
		}

	}

	private final StatementStateListener statementStateListener;

	public PeerToPeerStatementFollower(PersistenceManager persistenceManager, Listener listener)
	{
		super(persistenceManager, listener);
		this.statementStateListener = new StatementStateListener();
	}

	@Override
	public Listener getListener()
	{
		return (Listener) super.getListener();
	}

	@Override
	public void follow(Aborter aborter) throws AbortException
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			getPersistenceManager().getListenerManager().getRootContextTopStateListeners().add(statementStateListener);
			for (RootContext rootContext : getPersistenceManager().rootContexts(transaction).values())
			{
				rootContext.addStateListener(statementStateListener);
				RootContextAuthority rootContextAuthority = rootContext.getAuthority(transaction);
				if (rootContextAuthority != null)
				{
					rootContextAuthority.addStateListener(statementStateListener);
					getListener().rootContextSignatureUpdated(rootContextAuthority.getStatementUuid());
				}
			}
			getPersistenceManager().getListenerManager().getRootContextLocalStateListeners().add(statementStateListener);
			Stack<ContextLocal> stack = new Stack<>();
			stack.addAll(getPersistenceManager().subscribeStatementsRootContextLocalSet(transaction));
			while (!stack.isEmpty())
			{
				aborter.checkAbort();
				ContextLocal contextLocal = stack.pop();
				stack.addAll(contextLocal.subscribeStatementsContextLocalSet(transaction));
				listenToContextLocal(transaction, contextLocal, aborter);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	private void fillListenableStatementCollections(Transaction transaction, ContextLocal contextLocal, Collection<ContextLocal> contextLocals,
			Collection<Statement> statements, Collection<StatementAuthority> statementAuthorities, Aborter aborter) throws AbortException
	{
		contextLocals.add(contextLocal);
		Context context = contextLocal.getStatement(transaction);
		if (context != null)
		{
			statements.add(context);
			CloseableIterator<Statement> iterator = context.localStatements(transaction).values().iterator();
			try
			{
				while (iterator.hasNext())
				{
					aborter.checkAbort();
					Statement statement = iterator.next();
					statements.add(statement);
					StatementAuthority statementAuthority = statement.getAuthority(transaction);
					if (statementAuthority != null)
						statementAuthorities.add(statementAuthority);
				}
			}
			finally
			{
				iterator.close();
			}
		}
	}

	private void fillListenableStatementCollections(Transaction transaction, ContextLocal contextLocal, Collection<ContextLocal> contextLocals,
			Collection<Statement> statements, Collection<StatementAuthority> statementAuthorities)
	{
		try
		{
			fillListenableStatementCollections(transaction, contextLocal, contextLocals, statements, statementAuthorities, Aborter.nullAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void listenToContextLocal(Transaction transaction, ContextLocal contextLocal)
	{
		try
		{
			listenToContextLocal(transaction, contextLocal, Aborter.nullAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void listenToContextLocal(Transaction transaction, ContextLocal contextLocal, Aborter aborter) throws AbortException
	{
		final Collection<ContextLocal> contextLocals = new ArrayList<>();
		final Collection<Statement> statements = new ArrayList<>();
		final Collection<StatementAuthority> statementAuthorities = new ArrayList<>();
		fillListenableStatementCollections(transaction, contextLocal, contextLocals, statements, statementAuthorities, aborter);
		transaction.runWhenCommit(new Transaction.Hook()
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				for (ContextLocal contextLocal : contextLocals)
				{
					contextLocal.addStateListener(statementStateListener);
					getListener().contextLocalSubscribed(contextLocal);
				}
				for (Statement statement : statements)
					statement.addStateListener(statementStateListener);
				for (StatementAuthority statementAuthority : statementAuthorities)
				{
					statementAuthority.addStateListener(statementStateListener);
					if (statementAuthority.isSignedProof())
						getListener().statementAuthoritySignedProved(statementAuthority);
				}
			}
		});
	}

	private void unlistenToContextLocal(Transaction transaction, ContextLocal contextLocal)
	{
		final Collection<ContextLocal> contextLocals = new ArrayList<>();
		final Collection<Statement> statements = new ArrayList<>();
		final Collection<StatementAuthority> statementAuthorities = new ArrayList<>();
		fillListenableStatementCollections(transaction, contextLocal, contextLocals, statements, statementAuthorities);
		transaction.runWhenCommit(new Transaction.Hook()
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				for (ContextLocal contextLocal : contextLocals)
				{
					contextLocal.removeStateListener(statementStateListener);
					getListener().contextLocalUnsubscribed(contextLocal);
				}
				for (Statement statement : statements)
					statement.removeStateListener(statementStateListener);
				for (StatementAuthority statementAuthority : statementAuthorities)
				{
					statementAuthority.removeStateListener(statementStateListener);
					if (statementAuthority.isSignedProof())
						getListener().statementAuthoritySignedUnproved(statementAuthority);
				}
			}
		});
	}

	@Override
	public void unfollow()
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			getPersistenceManager().getListenerManager().getRootContextTopStateListeners().remove(statementStateListener);
			for (RootContext rootContext : getPersistenceManager().rootContexts(transaction).values())
			{
				rootContext.removeStateListener(statementStateListener);
				rootContext.removeAuthorityStateListener(statementStateListener);
			}
			getPersistenceManager().getListenerManager().getRootContextLocalStateListeners().remove(statementStateListener);
			Stack<ContextLocal> stack = new Stack<>();
			stack.addAll(getPersistenceManager().subscribeStatementsRootContextLocalSet(transaction));
			while (!stack.isEmpty())
			{
				ContextLocal contextLocal = stack.pop();
				stack.addAll(contextLocal.subscribeStatementsContextLocalSet(transaction));
				unlistenToContextLocal(transaction, contextLocal);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

}
