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
package aletheia.protocol.statement;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.Term;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.namespace.NamespaceProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.term.TermProtocol;
import aletheia.utilities.collections.BufferedList;

/**
 * {@link Protocol} for {@link Statement}s. The data is arranged as follows:
 * <ul>
 * <li>The code for the actual {@link Statement} class using the
 * {@link StatementCodeProtocol}.</li>
 * <li>If it's not a {@link RootContext}, the parent's {@link UUID} using the
 * {@link UUIDProtocol}.</li>
 * <li>The {@link UUID} of this {@link Statement} using the {@link UUIDProtocol}
 * .</li>
 * <li>If it's an {@link Assumption}:</li> <blockquote>
 * <li>The order using the {@link IntegerProtocol}.</li> </blockquote>
 * <li>If it's a {@link Context}:</li> <blockquote>
 * <li>The {@link Term} using the {@link TermProtocol}.</li>
 * <li>The number of {@link Assumption}s this context has using the
 * {@link IntegerProtocol}.</li>
 * <li>The {@link UUID}s of the {@link Assumption}s using the
 * {@link UUIDProtocol} (in the assumption order).</li>
 * <li>Moreover if it's an {@link UnfoldingContext}:</li> <blockquote>
 * <li>The unfolding declaration {@link UUID} with the {@link UUIDProtocol}.
 * </li> </blockquote> </blockquote>
 * <li>If it's a {@link Declaration}:</li> <blockquote>
 * <li>The value {@link Term} using the {@link TermProtocol}.</li> </blockquote>
 * <li>If it's a {@link Specialization}:</li> <blockquote>
 * <li>The general statement's {@link UUID} using the {@link UUIDProtocol}.</li>
 * <li>The instance {@link Term} using the {@link TermProtocol}.
 * </li> </blockquote>
 * </ul>
 */
@ProtocolInfo(availableVersions = 0)
public class StatementProtocol extends PersistentExportableProtocol<Statement>
{
	private final IntegerProtocol integerProtocol;
	private final StatementCodeProtocol statementCodeProtocol;
	private final UUIDProtocol uuidProtocol;
	private final TermProtocol termProtocol;
	private final NamespaceProtocol namespaceProtocol;
	private final NullableProtocol<Namespace> nullableNamespaceProtocol;

	public StatementProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(StatementProtocol.class, requiredVersion);
		integerProtocol = new IntegerProtocol(0);
		statementCodeProtocol = new StatementCodeProtocol(0);
		uuidProtocol = new UUIDProtocol(0);
		termProtocol = new TermProtocol(0, persistenceManager, transaction);
		namespaceProtocol = new NamespaceProtocol(0);
		nullableNamespaceProtocol = new NullableProtocol<>(0, namespaceProtocol);
	}

	@Override
	public void send(DataOutput out, Statement statement) throws IOException
	{
		Class<? extends Exportable> clazz = statement.getClass();
		StatementCode statementCode = StatementCode.classMap().get(clazz);
		statementCodeProtocol.send(out, statementCode);
		if (!(statement instanceof RootContext))
			uuidProtocol.send(out, statement.getContextUuid());
		uuidProtocol.send(out, statement.getUuid());
		switch (statementCode)
		{
		case _Assumption:
			sendAssumption(out, (Assumption) statement);
			break;
		case _Context:
			sendContext(out, (Context) statement);
			break;
		case _Declaration:
			sendDeclaration(out, (Declaration) statement);
			break;
		case _Specialization:
			sendSpecialization(out, (Specialization) statement);
			break;
		case _UnfoldingContext:
			sendUnfoldingContext(out, (UnfoldingContext) statement);
			break;
		case _RootContext:
			sendRootContext(out, (RootContext) statement);
			break;
		default:
			throw new Error();
		}
		nullableNamespaceProtocol.send(out, statement.identifier(getTransaction()));
	}

	@Override
	public Statement recv(DataInput in) throws IOException, ProtocolException
	{
		StatementCode statementCode = statementCodeProtocol.recv(in);
		Context context = null;
		try
		{
			if (statementCode != StatementCode._RootContext)
			{
				UUID uuidContext = uuidProtocol.recv(in);
				context = (Context) getPersistenceManager().getStatement(getTransaction(), uuidContext);
				if (context == null)
					throw new ProtocolException();
			}
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
		UUID uuid = uuidProtocol.recv(in);
		Statement old = getPersistenceManager().getStatement(getTransaction(), uuid);
		Statement statement;
		switch (statementCode)
		{
		case _Assumption:
			statement = recvAssumption(in, context, old, uuid);
			break;
		case _Context:
			statement = recvContext(in, context, old, uuid);
			break;
		case _Declaration:
			statement = recvDeclaration(in, context, old, uuid);
			break;
		case _Specialization:
			statement = recvSpecialization(in, context, old, uuid);
			break;
		case _UnfoldingContext:
			statement = recvUnfoldingContext(in, context, old, uuid);
			break;
		case _RootContext:
			statement = recvRootContext(in, old, uuid);
			break;
		default:
			throw new ProtocolException();
		}
		if (statement == null)
			throw new ProtocolException();
		try
		{
			Identifier identifier = (Identifier) nullableNamespaceProtocol.recv(in);
			Identifier oldId = statement.identifier(getTransaction());
			if (identifier == null)
			{
				if (oldId != null)
					statement.unidentify(getTransaction());
			}
			else if (!identifier.equals(oldId))
			{
				if (oldId != null)
					statement.unidentify(getTransaction());
				try
				{
					Nomenclator nomenclator = statement.getParentNomenclator(getTransaction());
					if (nomenclator.isLocalIdentifier(identifier))
						nomenclator.unidentifyStatement(identifier);
					statement.identify(getTransaction(), identifier);
				}
				catch (NomenclatorException e)
				{
					throw new Error(e);
				}
			}
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}

		return statement;
	}

	private void sendAssumption(DataOutput out, Assumption assumption) throws IOException
	{
		integerProtocol.send(out, assumption.getOrder());
	}

	private Assumption recvAssumption(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipAssumption(in);
			return null;
		}
		int order = integerProtocol.recv(in);
		if (old == null)
		{
			try
			{
				Assumption assumption = context.assumptions(getTransaction()).get(order);
				if (!assumption.getUuid().equals(uuid))
					throw new ProtocolException();
				return assumption;
			}
			catch (IndexOutOfBoundsException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			if (!(old instanceof Assumption))
				throw new ProtocolException();
			Assumption assumption = (Assumption) old;
			if (assumption.getOrder() != order)
				throw new ProtocolException();
			return assumption;
		}
	}

	private void sendContext(DataOutput out, Context context) throws IOException
	{
		termProtocol.send(out, context.getTerm());
		List<Assumption> list = new BufferedList<>(context.assumptions(getTransaction()));
		integerProtocol.send(out, list.size());
		for (Assumption assumption : list)
			uuidProtocol.send(out, assumption.getUuid());
	}

	private Context recvContext(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipContext(in);
			return null;
		}
		Term term = termProtocol.recv(in);
		int numAssumptions = integerProtocol.recv(in);
		List<UUID> uuidAssumptions = new ArrayList<UUID>();
		for (int i = 0; i < numAssumptions; i++)
			uuidAssumptions.add(uuidProtocol.recv(in));
		if (old == null)
		{
			Context ctx;
			try
			{
				ctx = context.openSubContext(getTransaction(), uuid, uuidAssumptions, term);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
			finally
			{

			}
			return ctx;
		}
		else
		{
			if (!(old instanceof Context))
				throw new ProtocolException();
			Context ctx = (Context) old;
			if (!ctx.getTerm().equals(term))
				throw new ProtocolException();
			return ctx;

		}
	}

	private void sendDeclaration(DataOutput out, Declaration statement) throws IOException
	{
		termProtocol.send(out, statement.getValue());
	}

	private Declaration recvDeclaration(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipDeclaration(in);
			return null;
		}
		Term value = termProtocol.recv(in);
		if (old == null)
		{
			Declaration declaration;
			try
			{
				declaration = context.declare(getTransaction(), uuid, value);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
			finally
			{

			}
			return declaration;
		}
		else
		{
			if (!(old instanceof Declaration))
				throw new ProtocolException();
			Declaration declaration = (Declaration) old;
			if (!declaration.getValue().equals(value))
				throw new ProtocolException();
			return declaration;
		}

	}

	private void sendSpecialization(DataOutput out, Specialization specialization) throws IOException
	{
		uuidProtocol.send(out, specialization.getGeneralUuid());
		termProtocol.send(out, specialization.getInstance());
	}

	private Specialization recvSpecialization(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipSpecialization(in);
			return null;
		}
		UUID uuidGeneral = uuidProtocol.recv(in);
		Statement general = getPersistenceManager().getStatement(getTransaction(), uuidGeneral);
		if (general == null)
			throw new ProtocolException();
		Term instance = termProtocol.recv(in);
		if (old == null)
		{
			Specialization specialization;
			try
			{
				specialization = context.specialize(getTransaction(), uuid, general, instance);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
			finally
			{

			}
			return specialization;
		}
		else
		{
			if (!(old instanceof Specialization))
				throw new ProtocolException();
			Specialization specialization = (Specialization) old;
			if (!specialization.getGeneral(getTransaction()).equals(general))
				throw new ProtocolException();
			if (!specialization.getInstance().equals(instance))
				throw new ProtocolException();
			return specialization;
		}
	}

	private void sendUnfoldingContext(DataOutput out, UnfoldingContext unfoldingContext) throws IOException
	{
		sendContext(out, unfoldingContext);
		uuidProtocol.send(out, unfoldingContext.getDeclarationUuid());
	}

	private Statement recvUnfoldingContext(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipUnfoldingContext(in);
			return null;
		}
		Term term = termProtocol.recv(in);
		int numAssumptions = in.readInt();
		List<UUID> uuidAssumptions = new ArrayList<UUID>();
		for (int i = 0; i < numAssumptions; i++)
			uuidAssumptions.add(uuidProtocol.recv(in));
		UUID uuidDeclaration = uuidProtocol.recv(in);
		Declaration declaration;
		try
		{
			declaration = (Declaration) getPersistenceManager().getStatement(getTransaction(), uuidDeclaration);
			if (declaration == null)
				throw new ProtocolException();
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
		if (old == null)
		{
			Context ctx;
			try
			{
				ctx = context.openUnfoldingSubContext(getTransaction(), uuid, uuidAssumptions, term, declaration);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
			finally
			{

			}
			return ctx;
		}
		else
		{
			if (!(old instanceof UnfoldingContext))
				throw new ProtocolException();
			UnfoldingContext ctx = (UnfoldingContext) old;
			if (!ctx.getTerm().equals(term))
				throw new ProtocolException();
			if (!ctx.getDeclaration(getTransaction()).equals(declaration))
				throw new ProtocolException();
			return ctx;

		}
	}

	private void sendRootContext(DataOutput out, RootContext rootContext) throws IOException
	{
		sendContext(out, rootContext);
	}

	private RootContext recvRootContext(DataInput in, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		Term term = termProtocol.recv(in);
		int numAssumptions = in.readInt();
		List<UUID> uuidAssumptions = new ArrayList<UUID>();
		for (int i = 0; i < numAssumptions; i++)
			uuidAssumptions.add(uuidProtocol.recv(in));
		if (old == null)
		{
			RootContext rootCtx;
			try
			{
				rootCtx = RootContext.create(getPersistenceManager(), getTransaction(), uuid, uuidAssumptions, term);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
			finally
			{

			}
			return rootCtx;
		}
		else
		{
			if (!(old instanceof RootContext))
				throw new ProtocolException();
			RootContext rootContext = (RootContext) old;
			if (!rootContext.getTerm().equals(term))
				throw new ProtocolException();
			return rootContext;

		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		StatementCode statementCode = statementCodeProtocol.recv(in);
		if (statementCode != StatementCode._RootContext)
			uuidProtocol.skip(in);
		uuidProtocol.skip(in);
		switch (statementCode)
		{
		case _Assumption:
			skipAssumption(in);
			break;
		case _Context:
			skipContext(in);
			break;
		case _Declaration:
			skipDeclaration(in);
			break;
		case _Specialization:
			skipSpecialization(in);
			break;
		case _UnfoldingContext:
			skipUnfoldingContext(in);
			break;
		case _RootContext:
			skipRootContext(in);
			break;
		default:
			throw new ProtocolException();
		}
		nullableNamespaceProtocol.skip(in);
	}

	private void skipRootContext(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		int numAssumptions = in.readInt();
		for (int i = 0; i < numAssumptions; i++)
			uuidProtocol.skip(in);
	}

	private void skipUnfoldingContext(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		int numAssumptions = in.readInt();
		for (int i = 0; i < numAssumptions; i++)
			uuidProtocol.skip(in);
		uuidProtocol.skip(in);
	}

	private void skipSpecialization(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		termProtocol.skip(in);
	}

	private void skipDeclaration(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
	}

	private void skipContext(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		int numAssumptions = integerProtocol.recv(in);
		for (int i = 0; i < numAssumptions; i++)
			uuidProtocol.skip(in);
	}

	private void skipAssumption(DataInput in) throws IOException
	{
		integerProtocol.skip(in);
	}

}
