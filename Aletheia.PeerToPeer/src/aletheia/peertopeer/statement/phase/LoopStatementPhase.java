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
package aletheia.peertopeer.statement.phase;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.LoopSubPhase;
import aletheia.peertopeer.statement.PendingPersistentDataChanges;
import aletheia.peertopeer.statement.RemoteSubscription;
import aletheia.peertopeer.statement.dialog.DelegateTreeDialogClient;
import aletheia.peertopeer.statement.dialog.DelegateTreeDialogServer;
import aletheia.peertopeer.statement.dialog.LoopStatementDialogType;
import aletheia.peertopeer.statement.dialog.LoopStatementDialogTypeDialogActive;
import aletheia.peertopeer.statement.dialog.LoopStatementDialogTypeDialogPassive;
import aletheia.peertopeer.statement.dialog.NewSignedProofsLoopDialogClient;
import aletheia.peertopeer.statement.dialog.NewSignedProofsLoopDialogServer;
import aletheia.peertopeer.statement.dialog.NewStatementsLoopDialogClient;
import aletheia.peertopeer.statement.dialog.NewStatementsLoopDialogServer;
import aletheia.peertopeer.statement.dialog.StatementProofSubscriptionLoopDialogClient;
import aletheia.peertopeer.statement.dialog.StatementProofSubscriptionLoopDialogServer;
import aletheia.peertopeer.statement.dialog.StatementSubscriptionLoopDialogClient;
import aletheia.peertopeer.statement.dialog.StatementSubscriptionLoopDialogServer;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public class LoopStatementPhase extends StatementSubPhase
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.instance.logger();

	private class LoopStatementSubPhase extends LoopSubPhase<LoopStatementDialogType>
	{

		protected abstract class Command<C extends Command<C>> extends LoopSubPhase<LoopStatementDialogType>.Command<C>
		{

			public Command(LoopStatementDialogType loopDialogType)
			{
				super(loopDialogType);
			}
		}

		protected class DelegateTreeCommand extends Command<DelegateTreeCommand>
		{
			public DelegateTreeCommand()
			{
				super(LoopStatementDialogType.DelegateTree);
			}
		}

		protected class NewSignedProofsCommand extends Command<NewSignedProofsCommand>
		{
			public NewSignedProofsCommand()
			{
				super(LoopStatementDialogType.NewSignedProofs);
			}
		}

		protected class NewStatementsCommand extends Command<NewStatementsCommand>
		{
			public NewStatementsCommand()
			{
				super(LoopStatementDialogType.NewStatements);
			}
		}

		protected class StatementProofSubscriptionCommand extends Command<StatementProofSubscriptionCommand>
		{
			public StatementProofSubscriptionCommand()
			{
				super(LoopStatementDialogType.StatementProofSubscription);
			}
		}

		protected class StatementSubscriptionCommand extends Command<StatementSubscriptionCommand>
		{
			public StatementSubscriptionCommand()
			{
				super(LoopStatementDialogType.StatementSubscription);
			}
		}

		protected class ValedictionCommand extends Command<ValedictionCommand>
		{
			public ValedictionCommand()
			{
				super(LoopStatementDialogType.Valediction);
			}

		}

		private class StatementStateListener implements StatementAuthority.StateListener, Statement.StateListener
		{

			@Override
			public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
			{
			}

			@Override
			public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
			{
				if (signedDependencies)
				{
					getPendingPersistentDataChanges().statementAuthoritySignedDependenciesChanged(statementAuthority);
					command(new NewStatementsCommand(), transaction);
				}
				else
				{
					Statement statement = statementAuthority.getStatement(transaction);
					if (statement != null)
					{
						if (statement instanceof RootContext)
						{
							RootContext rootContext = (RootContext) statement;
							if (getRemoteSubscription().isContextSubscribed(transaction, rootContext))
							{
								RootContextLocal rootContextLocal = rootContext.getLocal(transaction);
								getPendingPersistentDataChanges().subscribeStatementsChanged(rootContextLocal, rootContextLocal.isSubscribeStatements());
								command(new StatementSubscriptionCommand(), transaction);
							}
						}
						else
						{
							Context context = statement.getContext(transaction);
							if (getRemoteSubscription().isContextSubscribed(transaction, context))
							{
								ContextLocal contextLocal = context.getLocal(transaction);
								getPendingPersistentDataChanges().subscribeStatementsChanged(transaction, contextLocal, contextLocal.isSubscribeStatements());
								command(new StatementSubscriptionCommand(), transaction);
							}
						}
					}
				}
			}

			@Override
			public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
			{
				if (statementAuthority.isSignedProof())
				{
					Statement statement = statementAuthority.getStatement(transaction);
					if (getRemoteSubscription().isProofSubscribed(transaction, statement))
					{
						getPendingPersistentDataChanges().statementAuthoritySignedProofsChanged(statementAuthority);
						command(new NewSignedProofsCommand(), transaction);
					}
				}
				else
				{
					StatementLocal statementLocal = getPersistenceManager().getStatementLocal(transaction, statementAuthority.getStatementUuid());
					if ((statementLocal != null) && statementLocal.isSubscribeProof())
					{
						if (statementLocal instanceof RootContextLocal)
							getPendingPersistentDataChanges().subscribeProofStatementsChanged((RootContextLocal) statementLocal, true);
						else
						{
							ContextLocal contextLocal = statementLocal.getContextLocal(transaction);
							getPendingPersistentDataChanges().subscribeProofStatementsChanged(contextLocal, statementLocal, true);
						}
						command(new StatementProofSubscriptionCommand(), transaction);
					}
				}
			}

			@Override
			public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
			{
				statementAuthority.addStateListener(this);
			}

			@Override
			public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
			{
				statementAuthority.removeStateListener(this);
				if (!(statement instanceof RootContext))
				{
					Context context = statement.getContext(transaction);
					if (getRemoteSubscription().isContextSubscribed(transaction, context))
					{
						ContextLocal contextLocal = context.getLocal(transaction);
						getPendingPersistentDataChanges().subscribeStatementsChanged(transaction, contextLocal, contextLocal.isSubscribeStatements());
						command(new StatementSubscriptionCommand(), transaction);
					}
				}
			}

			@Override
			public void signatureAdded(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
			{
			}

			@Override
			public void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
			{
			}

			@Override
			public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
			{
			}

			@Override
			public void statementAddedToContext(Transaction transaction, Context context, Statement statement)
			{
			}

			@Override
			public void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier)
			{
			}

			@Override
			public void delegateTreeChanged(Transaction transaction, StatementAuthority statementAuthority)
			{
			}

			@Override
			public void successorEntriesChanged(Transaction transaction, StatementAuthority statementAuthority)
			{
			}

			@Override
			public void delegateAuthorizerChanged(Transaction transaction, StatementAuthority statementAuthority, Namespace prefix, Person delegate,
					Signatory authorizer)
			{
			}

		}

		private final StatementStateListener statementStateListener;

		private class ContextStateListener implements RootContextLocal.StateListener, ContextLocal.StateListener, Statement.StateListener,
				StatementAuthority.StateListener
		{
			@Override
			public void subscribeStatementsChanged(Transaction transaction, ContextLocal contextLocal, ContextLocal contextLocal_, boolean subscribed)
			{
				if (getRemoteSubscription().isContextSubscribed(transaction, contextLocal.getStatement(transaction)))
				{
					getPendingPersistentDataChanges().subscribeStatementsChanged(contextLocal, contextLocal_, subscribed);
					if (subscribed)
						contextStateListenTo(transaction, contextLocal_.getStatement(transaction));
					else
						contextStateUnlisten(transaction, contextLocal_.getStatement(transaction));
					command(new StatementSubscriptionCommand(), transaction);
				}
			}

			@Override
			public void subscribeStatementsChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
			{
				getPendingPersistentDataChanges().subscribeStatementsChanged(rootContextLocal, subscribed);
				if (subscribed)
					contextStateListenTo(transaction, rootContextLocal.getStatement(transaction));
				else
					contextStateUnlisten(transaction, rootContextLocal.getStatement(transaction));
				command(new StatementSubscriptionCommand(), transaction);
			}

			@Override
			public void subscribeProofChanged(Transaction transaction, ContextLocal contextLocal, StatementLocal statementLocal, boolean subscribed)
			{
				getPendingPersistentDataChanges().subscribeProofStatementsChanged(contextLocal, statementLocal, subscribed);
				command(new StatementProofSubscriptionCommand(), transaction);
			}

			@Override
			public void subscribeProofChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
			{
				getPendingPersistentDataChanges().subscribeProofStatementsChanged(rootContextLocal, subscribed);
				command(new StatementProofSubscriptionCommand(), transaction);
			}

			@Override
			public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
			{
			}

			@Override
			public void statementAddedToContext(Transaction transaction, Context context, Statement statement)
			{
				statementStateListenTo(transaction, statement);
			}

			@Override
			public void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier)
			{
				statementStateUnlisten(transaction, statement);
			}

			@Override
			public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
			{
			}

			@Override
			public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
			{
			}

			@Override
			public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
			{
			}

			@Override
			public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
			{
			}

			@Override
			public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
			{
			}

			@Override
			public void signatureAdded(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
			{
			}

			@Override
			public void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
			{
			}

			@Override
			public void delegateTreeChanged(Transaction transaction, StatementAuthority statementAuthority)
			{
				getPendingPersistentDataChanges().delegateTreeModified(statementAuthority);
				command(new DelegateTreeCommand(), transaction);
			}

			@Override
			public void successorEntriesChanged(Transaction transaction, StatementAuthority statementAuthority)
			{
				delegateTreeChanged(transaction, statementAuthority);
			}

			@Override
			public void delegateAuthorizerChanged(Transaction transaction, StatementAuthority statementAuthority, Namespace prefix, Person delegate,
					Signatory authorizer)
			{
				delegateTreeChanged(transaction, statementAuthority);
			}

		}

		private final ContextStateListener contextStateListener;

		private final Set<Context> listeningToContexts;

		public LoopStatementSubPhase() throws IOException
		{
			super(LoopStatementPhase.this, LoopStatementDialogTypeDialogActive.class, LoopStatementDialogTypeDialogPassive.class);
			this.statementStateListener = new StatementStateListener();
			this.contextStateListener = new ContextStateListener();
			getPersistenceManager().getListenerManager().getRootContextLocalStateListeners().add(contextStateListener);
			this.listeningToContexts = new HashSet<Context>();
		}

		@Override
		protected boolean serverPhase(LoopStatementDialogType loopDialogType) throws IOException, ProtocolException, InterruptedException,
				DialogStreamException
		{
			switch (loopDialogType)
			{
			case Valediction:
				valedictionDialog();
				return false;
			case StatementSubscription:
				statementSubscriptionLoopDialogServer();
				return true;
			case NewStatements:
				newStatementsLoopDialogServer();
				return true;
			case StatementProofSubscription:
				statementProofSubscriptionLoopDialogServer();
				return true;
			case NewSignedProofs:
				newSignedProofsLoopDialogServer();
				return true;
			case DelegateTree:
				delegateTreeDialogServer();
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected boolean clientPhase(LoopSubPhase<LoopStatementDialogType>.Command<?> command) throws IOException, ProtocolException, InterruptedException,
				DialogStreamException
		{
			switch (command.getLoopDialogType())
			{
			case Valediction:
				valedictionDialog();
				return false;
			case StatementSubscription:
				statementSubscriptionLoopDialogClient();
				return true;
			case NewStatements:
				newStatementsLoopDialogClient();
				return true;
			case StatementProofSubscription:
				statementProofSubscriptionLoopDialogClient();
				return true;
			case NewSignedProofs:
				newSignedProofsLoopDialogClient();
				return true;
			case DelegateTree:
				delegateTreeDialogClient();
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected void loopPhaseTerminate() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			super.loopPhaseTerminate();
			contextStateUnlistenAll();
			rootContextsStateUnlisten();
		}

		@Override
		protected ValedictionCommand makeValedictionCommand()
		{
			return new ValedictionCommand();
		}

		protected <C extends Command<C>> void command(final Command<C> command, Transaction transaction)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					command(command);
				}
			});
		}

		protected <C extends Command<C>> Command<C>.Result commandResult(Command<C> command, ListenableAborter aborter) throws InterruptedException,
				CancelledCommandException, AbortException
		{
			return super.commandResult(command, aborter);
		}

		@SuppressWarnings("unused")
		protected <C extends Command<C>> Command<C>.Result commandResult(final Command<C> command, final ListenableAborter aborter, Transaction transaction)
				throws InterruptedException
		{
			class Exec
			{
				private Command<C>.Result result;
				private Exception exception;
			}

			final Exec exec = new Exec();

			synchronized (exec)
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						synchronized (exec)
						{
							try
							{
								exec.result = commandResult(command, aborter);
							}
							catch (Exception e)
							{
								exec.exception = e;
							}
							finally
							{
								exec.notifyAll();
							}
						}
					}
				});
				while (exec.result == null && exec.exception == null)
					exec.wait();
				if (exec.exception != null)
				{
					if (exec.exception instanceof InterruptedException)
						throw (InterruptedException) exec.exception;
					else if (exec.exception instanceof RuntimeException)
						throw (RuntimeException) exec.exception;
					else
						throw new RuntimeException(exec.exception);
				}
				return exec.result;
			}

		}

		private synchronized void contextStateListenTo(Transaction transaction, final Context context)
		{
			if (context instanceof RootContext)
				statementStateListenTo(transaction, context);
			for (Statement statement : context.localStatements(transaction).values())
				statementStateListenTo(transaction, statement);
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					context.addStateListener(contextStateListener);
					context.addLocalStateListener(contextStateListener);
					context.addAuthorityStateListener(contextStateListener);
					listeningToContexts.add(context);
				}
			});
		}

		private synchronized void statementStateListenTo(Transaction transaction, final Statement statement)
		{
			statement.addStateListener(statementStateListener);
			statement.addAuthorityStateListener(statementStateListener);

			transaction.runWhenClose(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					if (!closedTransaction.isCommited())
					{
						statement.addStateListener(statementStateListener);
						statement.addAuthorityStateListener(statementStateListener);
					}
				}
			});
		}

		private synchronized void contextStateUnlisten(Transaction transaction, final Context context)
		{
			if (context instanceof RootContext)
				statementStateUnlisten(transaction, context);
			for (Statement statement : context.localStatements(transaction).values())
				statementStateUnlisten(transaction, statement);
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					context.removeStateListener(contextStateListener);
					context.removeLocalStateListener(contextStateListener);
					context.removeAuthorityStateListener(contextStateListener);
					listeningToContexts.remove(context);
				}
			});

		}

		private synchronized void statementStateUnlisten(Transaction transaction, final Statement statement)
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

		private synchronized void contextStateUnlistenAll(Transaction transaction)
		{
			getPersistenceManager().getListenerManager().getRootContextLocalStateListeners().remove(contextStateListener);
			for (Context context : listeningToContexts)
				contextStateUnlisten(transaction, context);
		}

		private void contextStateUnlistenAll()
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				contextStateUnlistenAll(transaction);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}

		private synchronized void rootContextsStateUnlisten()
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				for (RootContext rootCtx : getPersistenceManager().rootContexts(transaction).values())
					statementStateUnlisten(transaction, rootCtx);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}

		private void statementSubscriptionLoopDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(StatementSubscriptionLoopDialogClient.class, this);
		}

		private void statementSubscriptionLoopDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(StatementSubscriptionLoopDialogServer.class, this);
		}

		private void newStatementsLoopDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(NewStatementsLoopDialogClient.class, this);
		}

		private void newStatementsLoopDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(NewStatementsLoopDialogServer.class, this);
		}

		private void statementProofSubscriptionLoopDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			statementProofSubPhase(StatementProofSubscriptionLoopDialogClient.class, this);
		}

		private void statementProofSubscriptionLoopDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			statementProofSubPhase(StatementProofSubscriptionLoopDialogServer.class, this);
		}

		private void newSignedProofsLoopDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			statementProofSubPhase(NewSignedProofsLoopDialogClient.class, this);
		}

		private void newSignedProofsLoopDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			statementProofSubPhase(NewSignedProofsLoopDialogServer.class, this);
		}

		private void delegateTreeDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(DelegateTreeDialogClient.class, this);
		}

		private void delegateTreeDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(DelegateTreeDialogServer.class, this);
		}

	}

	private LoopStatementSubPhase loopStatementSubPhase;

	public LoopStatementPhase(StatementPhase statementPhase) throws IOException
	{
		super(statementPhase);
		this.loopStatementSubPhase = new LoopStatementSubPhase();

	}

	@Override
	protected StatementPhase getParentPhase()
	{
		return super.getParentPhase();
	}

	protected RemoteSubscription getRemoteSubscription()
	{
		return getParentPhase().getRemoteSubscription();
	}

	protected PendingPersistentDataChanges getPendingPersistentDataChanges()
	{
		return getParentPhase().getPendingPersistentDataChanges();
	}

	public synchronized void contextStateListenTo(Transaction transaction, Context context)
	{
		loopStatementSubPhase.contextStateListenTo(transaction, context);
	}

	public synchronized void statementStateListenTo(Transaction transaction, Statement statement)
	{
		loopStatementSubPhase.statementStateListenTo(transaction, statement);
	}

	public synchronized void contextStateUnlisten(Transaction transaction, Context context)
	{
		loopStatementSubPhase.contextStateUnlisten(transaction, context);
	}

	public synchronized void contextStateUnlistenAll()
	{
		loopStatementSubPhase.contextStateUnlistenAll();
	}

	public synchronized void statementStateUnlisten(Transaction transaction, Statement statement)
	{
		loopStatementSubPhase.statementStateUnlisten(transaction, statement);
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		loopStatementSubPhase.run();
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		loopStatementSubPhase.shutdown(fast);
	}

	public boolean isOpen()
	{
		return loopStatementSubPhase.isOpen();
	}

}
