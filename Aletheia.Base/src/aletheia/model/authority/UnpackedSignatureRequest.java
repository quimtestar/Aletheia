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
package aletheia.model.authority;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.UnpackedSignatureRequestEntity;
import aletheia.utilities.collections.AdaptedList;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.FilteredSet;
import aletheia.utilities.collections.NotNullFilter;

public class UnpackedSignatureRequest extends SignatureRequest
{

	public UnpackedSignatureRequest(PersistenceManager persistenceManager, UnpackedSignatureRequestEntity entity)
	{
		super(persistenceManager, entity);
	}

	protected UnpackedSignatureRequest(PersistenceManager persistenceManager, UUID uuid, Date creationDate, List<UUID> contextUuidPath)
	{
		super(persistenceManager, UnpackedSignatureRequestEntity.class, uuid, creationDate, contextUuidPath);
	}

	public static abstract class UnpackedSignatureRequestException extends SignatureRequestException
	{

		private static final long serialVersionUID = 884481473758270282L;

		public UnpackedSignatureRequestException()
		{
			super();
		}

		public UnpackedSignatureRequestException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UnpackedSignatureRequestException(String message)
		{
			super(message);
		}

		public UnpackedSignatureRequestException(Throwable cause)
		{
			super(cause);
		}

	}

	public static class BadContextUnpackedSignatureRequestException extends UnpackedSignatureRequestException
	{

		private static final long serialVersionUID = 3462753341366744565L;
	}

	private static void checkContextUuidPath(PersistenceManager persistenceManager, Transaction transaction, Collection<UUID> contextUuidPath)
			throws BadContextUnpackedSignatureRequestException
	{
		UUID prev = null;
		for (UUID uuid : contextUuidPath)
		{
			Context context = persistenceManager.getContext(transaction, uuid);
			if (context == null)
				throw new BadContextUnpackedSignatureRequestException();
			if (prev == null)
			{
				if (!(context instanceof RootContext))
					throw new BadContextUnpackedSignatureRequestException();
			}
			else
			{
				if (!prev.equals(context.getContextUuid()))
					throw new BadContextUnpackedSignatureRequestException();
			}
			prev = uuid;
		}
	}

	protected static UnpackedSignatureRequest create(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, Date creationDate,
			List<UUID> contextUuidPath) throws BadContextUnpackedSignatureRequestException
	{
		checkContextUuidPath(persistenceManager, transaction, contextUuidPath);
		UnpackedSignatureRequest unpackedSignatureRequest = new UnpackedSignatureRequest(persistenceManager, uuid, creationDate, contextUuidPath);
		unpackedSignatureRequest.persistenceUpdate(transaction);
		unpackedSignatureRequest.notifySignatureRequestAdded(transaction);
		return unpackedSignatureRequest;
	}

	protected static UnpackedSignatureRequest create(PersistenceManager persistenceManager, Transaction transaction, Date creationDate,
			List<UUID> contextUuidPath) throws BadContextUnpackedSignatureRequestException
	{
		return create(persistenceManager, transaction, UUID.randomUUID(), creationDate, contextUuidPath);
	}

	public static UnpackedSignatureRequest create(PersistenceManager persistenceManager, Transaction transaction, Date creationDate, Context context)
			throws BadContextUnpackedSignatureRequestException
	{
		return create(persistenceManager, transaction, creationDate, contextUuidPath(transaction, context));
	}

	@Override
	public UnpackedSignatureRequestEntity getEntity()
	{
		return (UnpackedSignatureRequestEntity) super.getEntity();
	}

	protected Set<UUID> getStatementUuids()
	{
		return getEntity().getStatementUuids();
	}

	public Set<UUID> statementUuids(Transaction transaction)
	{
		return Collections.unmodifiableSet(refresh(transaction).getStatementUuids());
	}

	public Set<StatementAuthority> statementAuthorities(final Transaction transaction)
	{
		return new FilteredSet<StatementAuthority>(new NotNullFilter<StatementAuthority>(), new BijectionSet<UUID, StatementAuthority>(
				new Bijection<UUID, StatementAuthority>()
				{

					@Override
					public StatementAuthority forward(UUID uuid)
					{
						return getPersistenceManager().getStatementAuthority(transaction, uuid);
					}

					@Override
					public UUID backward(StatementAuthority statementAuthority)
					{
						return statementAuthority.getStatementUuid();
					}
				}, statementUuids(transaction)));
	}

	public Set<Statement> statements(final Transaction transaction)
	{
		return new BijectionSet<UUID, Statement>(new Bijection<UUID, Statement>()
		{

			@Override
			public Statement forward(UUID uuid)
			{
				return getPersistenceManager().getStatement(transaction, uuid);
			}

			@Override
			public UUID backward(Statement statement)
			{
				return statement.getUuid();
			}
		}, statementUuids(transaction));
	}

	public class AuthorityMissingUnpackedSignatureRequestException extends UnpackedSignatureRequestException
	{

		private static final long serialVersionUID = 3602444260117117808L;

		public AuthorityMissingUnpackedSignatureRequestException()
		{
			super("Missing statement authority (may be on a dependency)");
		}

	}

	public class NotInContextUnpackedSignatureRequestException extends UnpackedSignatureRequestException
	{

		private static final long serialVersionUID = -1740900582767373200L;

		public NotInContextUnpackedSignatureRequestException()
		{
			super("Statement not in request's context");
		}

	}

	public void addStatementUuid(Transaction transaction, UUID statementUuid) throws UnpackedSignatureRequestException
	{
		if (getStatementUuids().contains(statementUuid))
			return;
		Statement statement_ = getPersistenceManager().getStatement(transaction, statementUuid);
		if (statement_ == null)
			throw new AuthorityMissingUnpackedSignatureRequestException();
		if (!getContext(transaction).statements(transaction).containsKey(statement_.getVariable()))
			throw new NotInContextUnpackedSignatureRequestException();
		Set<UUID> added = new HashSet<UUID>();
		added.add(statementUuid);
		Stack<UUID> stack = new Stack<UUID>();
		stack.push(statementUuid);
		Set<UUID> visited = new HashSet<UUID>();
		while (!stack.isEmpty())
		{
			UUID uuid = stack.pop();
			if (!visited.contains(uuid))
			{
				visited.add(uuid);
				StatementAuthority statementAuthority = getPersistenceManager().getStatementAuthority(transaction, uuid);
				if (statementAuthority == null)
					throw new AuthorityMissingUnpackedSignatureRequestException();
				if (!statementAuthority.isSignedDependencies())
				{
					if (!statementAuthority.isValidSignature())
						added.add(uuid);
					Statement statement = statementAuthority.getStatement(transaction);
					for (UUID depUuid : statement.getUuidDependencies())
						if (!getStatementUuids().contains(depUuid))
							stack.push(depUuid);
				}
			}
		}
		getStatementUuids().addAll(added);
		persistenceUpdate(transaction);
	}

	public void addStatement(Transaction transaction, Statement statement) throws UnpackedSignatureRequestException
	{
		addStatementUuid(transaction, statement.getUuid());
	}

	public void addStatementAuthority(Transaction transaction, StatementAuthority statementAuthority) throws UnpackedSignatureRequestException
	{
		addStatementUuid(transaction, statementAuthority.getStatementUuid());
	}

	public void removeStatementUuid(Transaction transaction, UUID statementUuid)
	{
		if (!getStatementUuids().contains(statementUuid))
			return;
		Stack<UUID> stack = new Stack<UUID>();
		stack.push(statementUuid);
		Set<UUID> removed = new HashSet<UUID>();
		while (!stack.isEmpty())
		{
			UUID uuid = stack.pop();
			if (removed.add(uuid))
			{
				Statement statement = getPersistenceManager().getStatement(transaction, uuid);
				if (statement != null)
				{
					for (Statement dep : statement.dependents(transaction))
					{
						if (getStatementUuids().contains(dep.getUuid()))
							stack.push(dep.getUuid());
					}
				}
			}
		}
		getStatementUuids().removeAll(removed);
		persistenceUpdate(transaction);
	}

	public void removeStatement(Transaction transaction, Statement statement)
	{
		removeStatementUuid(transaction, statement.getUuid());
	}

	public void removeStatementAuthority(Transaction transaction, StatementAuthority statementAuthority)
	{
		removeStatementUuid(transaction, statementAuthority.getStatementUuid());
	}

	public UUID rootContextSignatureUuid(Transaction transaction)
	{
		return getContext(transaction).rootContext(transaction).getSignatureUuid(transaction);
	}

	private static List<UUID> contextUuidPath(Transaction transaction, Context context)
	{
		return new BijectionList<Statement, UUID>(new Bijection<Statement, UUID>()
		{

			@Override
			public UUID forward(Statement input)
			{
				return input.getUuid();
			}

			@Override
			public Statement backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		}, new AdaptedList<Statement>(context.statementPath(transaction)));
	}

	public PackedBuilder packedBuilder(Transaction transaction, DataOutput dataOutput) throws IOException
	{
		return new DataOutputPackedBuilder(getPersistenceManager(), transaction, statements(transaction), dataOutput);
	}

	public Context commonContext(Transaction transaction)
	{
		Context ctx = null;
		for (Statement st : statements(transaction))
		{
			Context ctx_ = st.getContext(transaction);
			if (ctx == null || !ctx.isDescendent(transaction, ctx_))
				ctx = ctx_;
		}
		return ctx;
	}

	public Namespace commonPrefix(Transaction transaction)
	{
		Namespace prefix = null;
		for (Statement st : statements(transaction))
		{
			Namespace prefix_ = st.prefix();
			prefix = prefix == null ? prefix_ : prefix.commonPrefix(prefix_);
		}
		return prefix == null ? RootNamespace.instance : prefix;
	}

	public List<CloseableSet<Person>> delegatesList(Transaction transaction)
	{
		List<CloseableSet<Person>> delegatesList = new ArrayList<CloseableSet<Person>>();
		Context ctx = commonContext(transaction);
		Namespace prefix = commonPrefix(transaction);
		while (true)
		{
			StatementAuthority ctxAuth = ctx.getAuthority(transaction);
			if (ctxAuth != null)
			{
				CloseableSet<Person> delegates = ctxAuth.localDelegateAuthorizerMap(transaction, prefix).keySet();
				if (!delegates.isEmpty())
					delegatesList.add(delegates);
			}
			if (ctx instanceof RootContext)
				break;
			ctx = ctx.getContext(transaction);
		}
		return delegatesList;
	}

	public Iterable<Statement> missingForSignedProofRequest(final Transaction transaction)
	{
		return new Iterable<Statement>()
		{

			@Override
			public Iterator<Statement> iterator()
			{
				return new Iterator<Statement>()
				{
					final Collection<Statement> statements = new HashSet<Statement>(statements(transaction));
					final Stack<Statement> stack = new Stack<Statement>();
					{
						stack.addAll(statements);
					}
					final Set<Statement> visited = new HashSet<Statement>();
					Statement next = advance();

					private Statement advance()
					{
						while (!stack.isEmpty())
						{
							Statement st = stack.pop();
							if (!visited.contains(st))
							{
								visited.add(st);
								if (st.isProved())
								{
									StatementAuthority stAuth = st.getAuthority(transaction);
									if (!stAuth.isSignedProof())
									{
										stack.addAll(st.dependencies(transaction));
										if (st instanceof Context)
										{
											Context ctx = (Context) st;
											stack.addAll(ctx.solvers(transaction));
										}
										if (!stAuth.isValidSignature())
										{
											boolean descends = false;
											for (Statement st_ : statements)
											{
												if ((st_ instanceof Context) && ((Context) st_).isDescendent(transaction, st))
												{
													descends = true;
													break;
												}
											}
											if (!descends)
											{
												statements.add(st);
												return st;
											}
										}
									}
								}
							}
						}
						return null;
					}

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public Statement next()
					{
						Statement statement = next;
						next = advance();
						return statement;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public Collection<Statement> completeMissingForSignedProofRequest(Transaction transaction)
	{
		try
		{
			List<Statement> added = new ArrayList<Statement>();
			for (Statement statement : missingForSignedProofRequest(transaction))
			{
				added.add(statement);
				addStatement(transaction, statement);
			}
			return added;
		}
		catch (UnpackedSignatureRequestException e)
		{
			throw new Error(e);
		}

	}

	@Override
	public UnpackedSignatureRequest refresh(Transaction transaction)
	{
		SignatureRequest signatureRequest = super.refresh(transaction);
		return signatureRequest instanceof UnpackedSignatureRequest ? (UnpackedSignatureRequest) signatureRequest : null;
	}

}
