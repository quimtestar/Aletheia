/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.model.statement.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.protocol.NamespaceProtocol;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.nomenclator.Nomenclator.SignatureIsValidNomenclatorException;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.SignatureIsValidException;
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
import aletheia.protocol.parameteridentification.ParameterIdentificationProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.term.TermProtocol;

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
 * <li>The instance {@link Term} using the {@link TermProtocol}.</li>
 * </blockquote>
 * </ul>
 */
@ProtocolInfo(availableVersions =
{ 4 })
public class StatementProtocol extends PersistentExportableProtocol<Statement>
{
	private final IntegerProtocol integerProtocol;
	private final StatementCodeProtocol statementCodeProtocol;
	private final UUIDProtocol uuidProtocol;
	private final TermProtocol termProtocol;
	private final NamespaceProtocol namespaceProtocol;
	private final NullableProtocol<Namespace> nullableNamespaceProtocol;
	private final ParameterIdentificationProtocol parameterIdentificationProtocol;

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
		parameterIdentificationProtocol = new ParameterIdentificationProtocol(0);
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
				context = getPersistenceManager().getContext(getTransaction(), uuidContext);
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
			if (identifier == null || !identifier.equals(oldId))
			{
				if (oldId != null)
					try
					{
						statement.unidentify(getTransaction(), true);
					}
					catch (SignatureIsValidNomenclatorException e)
					{
						throw new ProtocolException(e);
					}
			}
			if (identifier != null && !identifier.equals(oldId))
			{
				try
				{
					Nomenclator nomenclator = statement.getParentNomenclator(getTransaction());
					if (nomenclator.isLocalIdentifier(identifier))
						nomenclator.unidentifyStatement(identifier, true);
					statement.identify(getTransaction(), identifier, true);
				}
				catch (NomenclatorException e)
				{
					throw new ProtocolException(e);
				}
			}
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}

		return statement;
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

	private void sendAssumption(DataOutput out, Assumption assumption) throws IOException
	{
		parameterIdentificationProtocol.send(out, assumption.getTermParameterIdentification());
		integerProtocol.send(out, assumption.getOrder());
	}

	private Assumption recvAssumption(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipAssumption(in);
			return null;
		}
		ParameterIdentification termParameterIdentification = parameterIdentificationProtocol.recv(in);
		int order = integerProtocol.recv(in);
		Assumption assumption;
		if (old == null)
		{
			try
			{
				assumption = context.assumptions(getTransaction()).get(order);
				if (!assumption.getUuid().equals(uuid))
					throw new ProtocolException();
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
			assumption = (Assumption) old;
			if (assumption.getOrder() != order)
				throw new ProtocolException();
		}
		try
		{
			assumption.updateTermParameterIdentification(getTransaction(), termParameterIdentification, true);
		}
		catch (SignatureIsValidException e)
		{
			throw new ProtocolException(e);
		}
		return assumption;
	}

	private void skipAssumption(DataInput in) throws IOException, ProtocolException
	{
		parameterIdentificationProtocol.skip(in);
		integerProtocol.skip(in);
	}

	private void sendDeclaration(DataOutput out, Declaration declaration) throws IOException
	{
		termProtocol.send(out, declaration.getValue());
		parameterIdentificationProtocol.send(out, declaration.getValueParameterIdentification());
		uuidProtocol.send(out, declaration.getValueProofUuid());
	}

	private Declaration recvDeclaration(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipDeclaration(in);
			return null;
		}
		Term value = termProtocol.recv(in);
		ParameterIdentification valueParameterIdentification = parameterIdentificationProtocol.recv(in);
		UUID uuidValueProof = uuidProtocol.recv(in);
		Statement valueProof = getPersistenceManager().getStatement(getTransaction(), uuidValueProof);
		if (valueProof == null)
			throw new ProtocolException();
		Declaration declaration;
		if (old == null)
		{

			try
			{
				declaration = context.declare(getTransaction(), uuid, value, valueProof);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			if (!(old instanceof Declaration))
				throw new ProtocolException();
			declaration = (Declaration) old;
			if (!declaration.getValue().equals(value))
				throw new ProtocolException();
		}
		try
		{
			declaration.updateValueParameterIdentification(getTransaction(), valueParameterIdentification, true);
		}
		catch (SignatureIsValidException e)
		{
			throw new ProtocolException(e);
		}
		return declaration;
	}

	private void skipDeclaration(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		parameterIdentificationProtocol.skip(in);
		uuidProtocol.skip(in);
	}

	private void sendSpecialization(DataOutput out, Specialization specialization) throws IOException
	{
		uuidProtocol.send(out, specialization.getGeneralUuid());
		termProtocol.send(out, specialization.getInstance());
		parameterIdentificationProtocol.send(out, specialization.getInstanceParameterIdentification());
		uuidProtocol.send(out, specialization.getInstanceProofUuid());
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
		ParameterIdentification instanceParameterIdentification = parameterIdentificationProtocol.recv(in);
		UUID uuidInstanceProof = uuidProtocol.recv(in);
		Statement instanceProof = getPersistenceManager().getStatement(getTransaction(), uuidInstanceProof);
		if (instanceProof == null)
			throw new ProtocolException();

		Specialization specialization;
		if (old == null)
		{
			try
			{
				specialization = context.specialize(getTransaction(), uuid, general, instance, instanceProof);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			if (!(old instanceof Specialization))
				throw new ProtocolException();
			specialization = (Specialization) old;
			if (!specialization.getGeneral(getTransaction()).equals(general))
				throw new ProtocolException();
			if (!specialization.getInstance().equals(instance))
				throw new ProtocolException();
			if (!specialization.getInstanceProof(getTransaction()).equals(instanceProof))
				throw new ProtocolException();
		}
		try
		{
			specialization.updateInstanceParameterIdentification(getTransaction(), instanceParameterIdentification, true);
		}
		catch (SignatureIsValidException e)
		{
			throw new ProtocolException(e);
		}
		return specialization;
	}

	private void skipSpecialization(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		termProtocol.skip(in);
		parameterIdentificationProtocol.skip(in);
		uuidProtocol.skip(in);
	}

	private void sendContext(DataOutput out, Context context) throws IOException
	{
		termProtocol.send(out, context.getTerm());
		parameterIdentificationProtocol.send(out, context.getConsequentParameterIdentification());
	}

	private Context recvContext(DataInput in, Context context, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		if (context == null)
		{
			skipContext(in);
			return null;
		}
		Term term = termProtocol.recv(in);
		ParameterIdentification consequentParameterIdentification = parameterIdentificationProtocol.recv(in);
		Context ctx;
		if (old == null)
		{
			try
			{
				ctx = context.openSubContext(getTransaction(), uuid, term);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			if (!(old instanceof Context))
				throw new ProtocolException();
			ctx = (Context) old;
			if (!ctx.getTerm().equals(term))
				throw new ProtocolException();
		}
		try
		{
			ctx.updateConsequentParameterIdentification(getTransaction(), consequentParameterIdentification, true);
		}
		catch (SignatureIsValidException e)
		{
			throw new ProtocolException(e);
		}
		return ctx;
	}

	private void skipContext(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		parameterIdentificationProtocol.skip(in);
	}

	private void sendUnfoldingContext(DataOutput out, UnfoldingContext unfoldingContext) throws IOException
	{
		termProtocol.send(out, unfoldingContext.getTerm());
		parameterIdentificationProtocol.send(out, unfoldingContext.getConsequentParameterIdentification());
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
		ParameterIdentification consequentParameterIdentification = parameterIdentificationProtocol.recv(in);
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
		UnfoldingContext unf;
		if (old == null)
		{
			try
			{
				unf = context.openUnfoldingSubContext(getTransaction(), uuid, term, declaration);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			if (!(old instanceof UnfoldingContext))
				throw new ProtocolException();
			unf = (UnfoldingContext) old;
			if (!unf.getTerm().equals(term))
				throw new ProtocolException();
			if (!unf.getDeclaration(getTransaction()).equals(declaration))
				throw new ProtocolException();
		}
		try
		{
			unf.updateConsequentParameterIdentification(getTransaction(), consequentParameterIdentification);
		}
		catch (SignatureIsValidException e)
		{
			throw new ProtocolException(e);
		}
		return unf;
	}

	private void skipUnfoldingContext(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		parameterIdentificationProtocol.skip(in);
		uuidProtocol.skip(in);
	}

	private void sendRootContext(DataOutput out, RootContext rootContext) throws IOException
	{
		termProtocol.send(out, rootContext.getTerm());
		parameterIdentificationProtocol.send(out, rootContext.getConsequentParameterIdentification());
	}

	private RootContext recvRootContext(DataInput in, Statement old, UUID uuid) throws IOException, ProtocolException
	{
		Term term = termProtocol.recv(in);
		ParameterIdentification consequentParameterIdentification = parameterIdentificationProtocol.recv(in);
		RootContext rootCtx;
		if (old == null)
		{
			try
			{
				rootCtx = RootContext.create(getPersistenceManager(), getTransaction(), uuid, term);
			}
			catch (StatementException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			if (!(old instanceof RootContext))
				throw new ProtocolException();
			rootCtx = (RootContext) old;
			if (!rootCtx.getTerm().equals(term))
				throw new ProtocolException();
		}
		try
		{
			rootCtx.updateConsequentParameterIdentification(getTransaction(), consequentParameterIdentification);
		}
		catch (SignatureIsValidException e)
		{
			throw new ProtocolException(e);
		}
		return rootCtx;
	}

	private void skipRootContext(DataInput in) throws IOException, ProtocolException
	{
		termProtocol.skip(in);
		parameterIdentificationProtocol.skip(in);
	}

}
