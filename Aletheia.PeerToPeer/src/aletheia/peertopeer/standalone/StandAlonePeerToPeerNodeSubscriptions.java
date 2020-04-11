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
package aletheia.peertopeer.standalone;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNodeProperties;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.MiscUtilities.ParseHexStringException;
import aletheia.utilities.StreamAsStringIterable;
import aletheia.utilities.collections.ArrayAsList;
import aletheia.utilities.collections.CloseableIterator;

public class StandAlonePeerToPeerNodeSubscriptions
{
	private final static String configurationFileName = PeerToPeerNodeProperties.instance.getStandaloneSubscriptions();

	private static abstract class ConfigurationTreeNode
	{
		protected final int depth;
		protected final Map<Identifier, SubConfigurationTreeNode> children;

		public ConfigurationTreeNode(int depth)
		{
			super();
			this.depth = depth;
			this.children = new HashMap<>();
		}
	}

	private static abstract class ValidConfigurationTreeNode extends ConfigurationTreeNode
	{
		@SuppressWarnings("unused")
		protected final Identifier identifier;

		public ValidConfigurationTreeNode(int depth, Identifier identifier)
		{
			super(depth);
			this.identifier = identifier;
		}
	}

	private static class RootConfigurationTreeNode extends ValidConfigurationTreeNode
	{
		protected final UUID signatureUuid;

		public RootConfigurationTreeNode(UUID signatureUuid, Identifier identifier)
		{
			super(0, identifier);
			this.signatureUuid = signatureUuid;
		}
	}

	private static class SubConfigurationTreeNode extends ValidConfigurationTreeNode
	{
		@SuppressWarnings("unused")
		protected final ValidConfigurationTreeNode parent;

		public SubConfigurationTreeNode(ValidConfigurationTreeNode parent, Identifier identifier)
		{
			super(parent.depth + 1, identifier);
			this.parent = parent;
			parent.children.put(identifier, this);
		}
	}

	private static class CommentedOutConfigurationTreeNode extends ConfigurationTreeNode
	{

		public CommentedOutConfigurationTreeNode(int depth)
		{
			super(depth);
		}

	}

	private static class ConfigurationTree
	{
		Map<UUID, RootConfigurationTreeNode> rootConfigurationTreeNodes;

		public ConfigurationTree()
		{
			super();
			this.rootConfigurationTreeNodes = new HashMap<>();
		}

		public ValidConfigurationTreeNode contextToNode(Transaction transaction, Context context)
		{
			ValidConfigurationTreeNode node = null;
			for (Context ctx : context.statementPath(transaction))
			{
				if (ctx instanceof RootContext)
					node = rootConfigurationTreeNodes.get(((RootContext) ctx).getSignatureUuid(transaction));
				else
					node = node.children.get(ctx.getIdentifier());
				if (node == null)
					return null;
			}
			return node;
		}

	}

	private static List<String> tuples(String s)
	{
		int i = s.indexOf('(');
		if (i < 0)
			return Collections.emptyList();
		int j = s.indexOf(')', i);
		return new ArrayAsList<>(s.substring(i + 1, j).split(","));
	}

	public static class ConfigurationException extends Exception
	{
		private static final long serialVersionUID = -8476852542961325812L;

		public ConfigurationException()
		{
			super();
		}

		public ConfigurationException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ConfigurationException(String message)
		{
			super(message);
		}

		public ConfigurationException(Throwable cause)
		{
			super(cause);
		}
	}

	private static ConfigurationTree loadConfiguration() throws IOException, ConfigurationException
	{
		ConfigurationTree configurationTree = new ConfigurationTree();
		if (configurationFileName != null)
		{
			CloseableIterator<String> iterator = new StreamAsStringIterable(new FileInputStream(configurationFileName)).iterator();
			try
			{
				Stack<ConfigurationTreeNode> stack = new Stack<>();
				while (iterator.hasNext())
				{
					String s = iterator.next();
					int sp = 0;
					while (sp < s.length() && Character.isWhitespace(s.charAt(sp)))
						sp++;
					while (!stack.isEmpty() && stack.peek().depth >= sp)
						stack.pop();
					String s_ = s.substring(sp);
					if (!s_.isEmpty())
					{
						String s_trim = s_.trim();
						if (!s_trim.startsWith("#"))
						{
							if (sp <= 0)
							{
								try
								{
									List<String> tuples = tuples(s_trim);
									if (tuples.size() != 2)
										throw new ConfigurationException();
									UUID signatureUuid = UUID.fromString(tuples.get(0));
									Identifier identifier = Identifier.parse(tuples.get(1));
									RootConfigurationTreeNode rootNode = new RootConfigurationTreeNode(signatureUuid, identifier);
									configurationTree.rootConfigurationTreeNodes.put(signatureUuid, rootNode);
									stack.push(rootNode);
								}
								catch (IllegalArgumentException | ParseHexStringException | InvalidNameException e)
								{
									throw new ConfigurationException(e);
								}
							}
							else
							{
								ConfigurationTreeNode parent = stack.peek();
								if (parent instanceof ValidConfigurationTreeNode)
								{
									try
									{
										Identifier identifier = Identifier.parse(s_trim);
										SubConfigurationTreeNode node = new SubConfigurationTreeNode((ValidConfigurationTreeNode) parent, identifier);
										stack.push(node);
									}
									catch (InvalidNameException e)
									{
										throw new ConfigurationException(e);
									}
								}
								else
									stack.push(new CommentedOutConfigurationTreeNode(sp));
							}
						}
						else
							stack.push(new CommentedOutConfigurationTreeNode(sp));
					}
				}
			}
			finally
			{
				iterator.close();
			}
		}
		return configurationTree;
	}

	private final PersistenceManager persistenceManager;
	private final PeerToPeerNode peerToPeerNode;

	private class Listener implements RootContext.TopStateListener, Statement.StateListener, Nomenclator.Listener, StatementAuthority.StateListener
	{
		@Override
		public void rootContextAdded(Transaction transaction, final RootContext rootContext)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
					{

						@Override
						public void invoke()
						{
							rootContext.addStateListener(Listener.this);
							rootContext.addNomenclatorListener(Listener.this);
							Transaction transaction = persistenceManager.beginTransaction();
							try
							{
								RootContextAuthority rootContextAuthority = rootContext.getAuthority(transaction);
								if (rootContextAuthority != null)
								{
									signedDependenciesStateChanged(transaction, rootContextAuthority, rootContextAuthority.isSignedDependencies());
									rootContextAuthority.addStateListener(Listener.this);
								}
								transaction.commit();
							}
							finally
							{
								transaction.abort();
							}
						}
					});
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
					rootContext.removeStateListener(Listener.this);
					rootContext.removeNomenclatorListener(Listener.this);
				}
			});

		}

		private void listenToContext(final ValidConfigurationTreeNode validConfigurationTreeNode, final Context context)
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{

					class StackEntry
					{
						final ValidConfigurationTreeNode validConfigurationTreeNode;
						final Context context;

						public StackEntry(ValidConfigurationTreeNode validConfigurationTreeNode, Context context)
						{
							super();
							this.validConfigurationTreeNode = validConfigurationTreeNode;
							this.context = context;
						}
					}
					Stack<StackEntry> stack = new Stack<>();
					stack.push(new StackEntry(validConfigurationTreeNode, context));
					while (!stack.isEmpty())
					{
						StackEntry se = stack.pop();
						Transaction transaction = persistenceManager.beginTransaction();
						try
						{
							for (Statement statement : se.context.localStatements(transaction).values())
							{
								statement.addStateListener(Listener.this);
								boolean subscribeProof = true;
								if (statement instanceof Context)
								{
									SubConfigurationTreeNode subConfigurationTreeNode = se.validConfigurationTreeNode.children.get(statement.getIdentifier());
									if (subConfigurationTreeNode != null)
									{
										Context ctx = (Context) statement;
										ctx.addNomenclatorListener(Listener.this);
										ctx.getOrCreateLocal(transaction).setSubscribeProof(transaction, false);
										ctx.getOrCreateLocal(transaction).setSubscribeStatements(transaction, true);
										stack.push(new StackEntry(subConfigurationTreeNode, ctx));
										subscribeProof = false;
									}
								}
								if (subscribeProof)
									statement.getOrCreateLocal(transaction).setSubscribeProof(transaction, true);
							}
							transaction.commit();
						}
						finally
						{
							transaction.abort();
						}
					}
				}
			});

		}

		@Override
		public void statementAddedToContext(Transaction transaction, Context context, final Statement statement)
		{
			final ValidConfigurationTreeNode validConfigurationTreeNode = configurationTree.contextToNode(transaction, context);
			if (validConfigurationTreeNode != null)
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{
					@Override
					public void run(Transaction closedTransaction)
					{
						statement.addStateListener(Listener.this);
						Transaction transaction = persistenceManager.beginTransaction();
						try
						{
							SubConfigurationTreeNode subConfigurationTreeNode = validConfigurationTreeNode.children.get(statement.getIdentifier());
							if ((statement instanceof Context) && subConfigurationTreeNode != null)
							{
								Context ctx = (Context) statement;
								ctx.getOrCreateLocal(transaction).setSubscribeProof(transaction, false);
								ctx.getOrCreateLocal(transaction).setSubscribeStatements(transaction, true);
								ctx.addNomenclatorListener(Listener.this);
								listenToContext(subConfigurationTreeNode, ctx);
							}
							else
							{
								statement.getOrCreateLocal(transaction).setSubscribeProof(transaction, true);
							}
							transaction.commit();
						}
						finally
						{
							transaction.abort();
						}
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
					statement.removeStateListener(Listener.this);
					if (statement instanceof Context)
						((Context) statement).removeNomenclatorListener(Listener.this);
				}
			});
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, final StatementAuthority statementAuthority)
		{
			if (statementAuthority instanceof RootContextAuthority)
			{
				signedDependenciesStateChanged(transaction, statementAuthority, statementAuthority.isSignedDependencies());
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						statementAuthority.addStateListener(Listener.this);
					}
				});
			}

		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, final StatementAuthority statementAuthority)
		{
			if (statementAuthority instanceof RootContextAuthority)
			{
				signedDependenciesStateChanged(transaction, statementAuthority, false);
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						statementAuthority.removeStateListener(Listener.this);
					}
				});
			}
		}

		@Override
		public void statementIdentified(Transaction transaction, Statement statement, Identifier identifier)
		{
			statementAddedToContext(transaction, statement.getContext(transaction), statement);
		}

		private void rootContextAuthoritySignatureUuidChanged(Transaction transaction, RootContextAuthority rootContextAuthority)
		{
			final RootContext rootContext = rootContextAuthority.getRootContext(transaction);
			final RootConfigurationTreeNode rootConfigurationTreeNode = configurationTree.rootConfigurationTreeNodes
					.get(rootContextAuthority.getSignatureUuid());
			rootContext.getOrCreateLocal(transaction).setSubscribeProof(transaction, false);
			rootContext.getOrCreateLocal(transaction).setSubscribeStatements(transaction, rootConfigurationTreeNode != null);

			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					if (rootConfigurationTreeNode != null)
						listenToContext(rootConfigurationTreeNode, rootContext);
				}
			});
		}

		@Override
		public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
			if (signedDependencies && (statementAuthority instanceof RootContextAuthority))
				rootContextAuthoritySignatureUuidChanged(transaction, (RootContextAuthority) statementAuthority);
		}

		@Override
		public void signatureAdded(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
			if (statementAuthority instanceof RootContextAuthority)
				rootContextAuthoritySignatureUuidChanged(transaction, (RootContextAuthority) statementAuthority);
		}

		@Override
		public void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
			if (statementAuthority instanceof RootContextAuthority)
				rootContextAuthoritySignatureUuidChanged(transaction, (RootContextAuthority) statementAuthority);
		}

	}

	private final Listener listener;

	private ConfigurationTree configurationTree;

	public StandAlonePeerToPeerNodeSubscriptions(PersistenceManager persistenceManager, PeerToPeerNode peerToPeerNode)
			throws IOException, ConfigurationException, InterruptedException
	{
		this.persistenceManager = persistenceManager;
		this.peerToPeerNode = peerToPeerNode;
		this.peerToPeerNode.waitForInitializedResources();
		this.listener = new Listener();
		persistenceManager.getListenerManager().getRootContextTopStateListeners().add(listener);
		this.configurationTree = loadConfiguration();
		for (RootConfigurationTreeNode rootConfigurationTreeNode : configurationTree.rootConfigurationTreeNodes.values())
			peerToPeerNode.rootContextSignatureSubscribe(rootConfigurationTreeNode.signatureUuid, true);
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			for (RootContext rootCtx : persistenceManager.rootContexts(transaction).values())
				listener.rootContextAdded(transaction, rootCtx);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

}
