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
package aletheia.model.term.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.statement.Statement;
import aletheia.model.term.ProjectionCastTypeTerm;
import aletheia.model.term.CastTypeTerm;
import aletheia.model.term.CastTypeTerm.CastTypeException;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.CompositionTerm.CompositionTypeException;
import aletheia.model.term.FoldingCastTypeTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.TauTerm;
import aletheia.model.term.Term;
import aletheia.model.term.UnprojectedCastTypeTerm;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.protocol.PersistentExportableProtocol;
import aletheia.protocol.Exportable;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

/**
 * <p>
 * {@link Protocol} for {@link Term}s. Terms can only be exported when they have
 * not any free {@link VariableTerm} that is not a
 * {@link IdentifiableVariableTerm} (they must have that property too to be
 * associated to a {@link Statement}, by the way).
 * </p>
 * <p>
 * Data is arranged as follows:
 * <ul>
 * <li>The code for the actual {@link Term} class.</li>
 * <li>If it's a {@link CompositionTerm}:</li> <blockquote>
 * <li>The head {@link SimpleTerm}, following recursively this very arrangement
 * description.</li>
 * <li>The tail {@link Term}, following recursively this very arrangement
 * description.</li> </blockquote>
 * <li>If it's a {@link FunctionTerm}:</li> <blockquote>
 * <li>The parameter's type {@link Term} , following recursively this very
 * arrangement description.</li>
 * <li>An integer identifying the parameter {@link VariableTerm}, using the
 * {@link IntegerProtocol}.</li>
 * <li>The body {@link Term}, following recursively this very arrangement
 * description.</li> </blockquote>
 * <li>If it's a {@link VariableTerm} (not a {@link IdentifiableVariableTerm}):
 * </li> <blockquote>
 * <li>The integer that we associated to this {@link VariableTerm} when
 * processing the {@link FunctionTerm} that has this {@link VariableTerm} as a
 * parameter (every {@link VariableTerm} not a {@link IdentifiableVariableTerm}
 * must be a parameter of a {@link FunctionTerm}).</li> </blockquote>
 * <li>If it's a {@link IdentifiableVariableTerm}:</li> <blockquote>
 * <li>The {@link UUID} of the variable, using the {@link UUIDProtocol}.</li>
 * </blockquote>
 * <li>If it's a {@link ProjectionTerm}:</li> <blockquote>
 * <li>The projected {@link FunctionTerm}, following recursively this very
 * arrangement description.</li> </blockquote>
 * </ul>
 * </p>
 */
@ProtocolInfo(availableVersions = 0)
public class TermProtocol extends PersistentExportableProtocol<Term>
{
	private final IntegerProtocol integerProtocol;
	private final TermCodeProtocol termCodeProtocol;
	private final UUIDProtocol uuidProtocol;

	public TermProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(TermProtocol.class, requiredVersion);
		this.integerProtocol = new IntegerProtocol(0);
		this.termCodeProtocol = new TermCodeProtocol(0);
		this.uuidProtocol = new UUIDProtocol(0);
	}

	@Override
	public void send(DataOutput out, Term term) throws IOException
	{
		send(out, term.parameterNumerator(), term);
	}

	@Override
	public Term recv(DataInput in) throws IOException, ProtocolException
	{
		return recv(in, new Stack<VariableTerm>());
	}

	private void send(DataOutput out, Term.ParameterNumerator parameterNumerator, Term term) throws IOException
	{
		Class<? extends Exportable> clazz = term.getClass();
		TermCode termCode = TermCode.classMap().get(clazz);
		termCodeProtocol.send(out, termCode);
		switch (termCode)
		{
		case _CompositionTerm:
			sendCompositionTerm(out, parameterNumerator, (CompositionTerm) term);
			break;
		case _FunctionTerm:
			sendFunctionTerm(out, parameterNumerator, (FunctionTerm) term);
			break;
		case _TauTerm:
			sendTauTerm(out);
			break;
		case _ParameterVariableTerm:
			sendParameterVariableTerm(out, parameterNumerator, (ParameterVariableTerm) term);
			break;
		case _IdentifiableVariableTerm:
			sendIdentifiableVariableTerm(out, (IdentifiableVariableTerm) term);
			break;
		case _ProjectionTerm:
			sendProjectionTerm(out, parameterNumerator, (ProjectionTerm) term);
			break;
		case _ProjectedCastTypeTerm:
		case _UnprojectedCastTypeTerm:
			sendProjectionCastTypeTerm(out, parameterNumerator, (ProjectionCastTypeTerm) term);
			break;
		case _FoldingCastTypeTerm:
			sendFoldingCastTypeTerm(out, parameterNumerator, (FoldingCastTypeTerm) term);
			break;
		default:
			throw new Error();
		}
	}

	private Term recv(DataInput in, Stack<VariableTerm> varStack) throws IOException, ProtocolException
	{
		TermCode termCode = termCodeProtocol.recv(in);
		switch (termCode)
		{
		case _CompositionTerm:
			return recvCompositionTerm(in, varStack);
		case _FunctionTerm:
			return recvFunctionTerm(in, varStack);
		case _TauTerm:
			return recvTauTerm(in);
		case _ParameterVariableTerm:
			return recvParameterVariableTerm(in, varStack);
		case _IdentifiableVariableTerm:
			return recvIdentifiableVariableTerm(in);
		case _ProjectionTerm:
			return recvProjectionTerm(in, varStack);
		case _ProjectedCastTypeTerm:
		case _UnprojectedCastTypeTerm:
			return recvProjectionCastTypeTerm(in, varStack, termCode);
		case _FoldingCastTypeTerm:
			return recvFoldingCastTypeTerm(in, varStack);
		default:
			throw new ProtocolException();

		}
	}

	private void sendIdentifiableVariableTerm(DataOutput out, IdentifiableVariableTerm uuidVariableTerm) throws IOException
	{
		uuidProtocol.send(out, uuidVariableTerm.getUuid());
	}

	private IdentifiableVariableTerm recvIdentifiableVariableTerm(DataInput in) throws IOException, ProtocolException
	{
		UUID uuid = uuidProtocol.recv(in);
		Statement statement = getPersistenceManager().getStatement(getTransaction(), uuid);
		if (statement == null)
			throw new ProtocolException();
		return statement.getVariable();
	}

	private void sendParameterVariableTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, ParameterVariableTerm parameterVariableTerm)
			throws IOException
	{
		integerProtocol.send(out, parameterNumerator.parameterNumber(parameterVariableTerm));
	}

	private VariableTerm recvParameterVariableTerm(DataInput in, Stack<VariableTerm> varStack) throws IOException, ProtocolException
	{
		int i = integerProtocol.recv(in);
		try
		{
			return varStack.get(i);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ProtocolException(e);
		}
	}

	private void sendTauTerm(DataOutput out) throws IOException
	{
	}

	private TauTerm recvTauTerm(DataInput in)
	{
		return TauTerm.instance;
	}

	private void sendFunctionTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, FunctionTerm functionTerm) throws IOException
	{
		send(out, parameterNumerator, functionTerm.getParameter().getType());
		parameterNumerator.numberParameter(functionTerm.getParameter());
		send(out, parameterNumerator, functionTerm.getBody());
		parameterNumerator.unNumberParameter();
	}

	private FunctionTerm recvFunctionTerm(DataInput in, Stack<VariableTerm> varStack) throws IOException, ProtocolException
	{
		Term parameterType = recv(in, varStack);
		ParameterVariableTerm var = new ParameterVariableTerm(parameterType);
		varStack.push(var);
		Term body = recv(in, varStack);
		varStack.pop();
		return new FunctionTerm(var, body);
	}

	private void sendCompositionTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, CompositionTerm compositionTerm) throws IOException
	{
		send(out, parameterNumerator, compositionTerm.getHead());
		send(out, parameterNumerator, compositionTerm.getTail());
	}

	private CompositionTerm recvCompositionTerm(DataInput in, Stack<VariableTerm> varStack) throws IOException, ProtocolException
	{
		try
		{
			SimpleTerm head = (SimpleTerm) recv(in, varStack);
			Term tail = recv(in, varStack);
			return new CompositionTerm(head, tail);
		}
		catch (ClassCastException | CompositionTypeException e)
		{
			throw new ProtocolException(e);
		}
	}

	private void sendProjectionTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, ProjectionTerm projectionTerm) throws IOException
	{
		send(out, parameterNumerator, projectionTerm.getFunction());
	}

	private ProjectionTerm recvProjectionTerm(DataInput in, Stack<VariableTerm> varStack) throws IOException, ProtocolException
	{
		try
		{
			FunctionTerm function = (FunctionTerm) recv(in, varStack);
			return new ProjectionTerm(function);
		}
		catch (ClassCastException | ProjectionTypeException e)
		{
			throw new ProtocolException(e);
		}
	}

	private void sendCastTypeTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, CastTypeTerm castTypeTerm) throws IOException
	{
		send(out, parameterNumerator, castTypeTerm.getTerm());
	}

	private void sendProjectionCastTypeTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, ProjectionCastTypeTerm projectionCastTypeTerm)
			throws IOException
	{
		sendCastTypeTerm(out, parameterNumerator, projectionCastTypeTerm);
	}

	private ProjectionCastTypeTerm recvProjectionCastTypeTerm(DataInput in, Stack<VariableTerm> varStack, TermCode termCode)
			throws IOException, ProtocolException
	{
		try
		{
			Term term = recv(in, varStack);
			switch (termCode)
			{
			case _ProjectedCastTypeTerm:
				return new ProjectedCastTypeTerm(term);
			case _UnprojectedCastTypeTerm:
				return new UnprojectedCastTypeTerm(term);
			default:
				throw new ProtocolException();
			}
		}
		catch (CastTypeException e)
		{
			throw new ProtocolException(e);
		}
	}

	private void sendFoldingCastTypeTerm(DataOutput out, Term.ParameterNumerator parameterNumerator, FoldingCastTypeTerm foldingCastTypeTerm) throws IOException
	{
		sendCastTypeTerm(out, parameterNumerator, foldingCastTypeTerm);
		send(out, parameterNumerator, foldingCastTypeTerm.getType());
		sendIdentifiableVariableTerm(out, foldingCastTypeTerm.getVariable());
		send(out, parameterNumerator, foldingCastTypeTerm.getValue());
	}

	private FoldingCastTypeTerm recvFoldingCastTypeTerm(DataInput in, Stack<VariableTerm> varStack) throws IOException, ProtocolException
	{
		Term term = recv(in, varStack);
		Term type = recv(in, varStack);
		IdentifiableVariableTerm variable = recvIdentifiableVariableTerm(in);
		Term value = recv(in, varStack);
		try
		{
			return new FoldingCastTypeTerm(term, type, variable, value);
		}
		catch (CastTypeException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		TermCode termCode = termCodeProtocol.recv(in);
		switch (termCode)
		{
		case _CompositionTerm:
			skipCompositionTerm(in);
			break;
		case _FunctionTerm:
			skipFunctionTerm(in);
			break;
		case _TauTerm:
			skipTypeTerm(in);
			break;
		case _ParameterVariableTerm:
			skipParameterVariableTerm(in);
			break;
		case _IdentifiableVariableTerm:
			skipIdentifiableVariableTerm(in);
			break;
		case _ProjectionTerm:
			skipProjectionTerm(in);
			break;
		case _ProjectedCastTypeTerm:
		case _UnprojectedCastTypeTerm:
			skipProjectionCastTypeTerm(in);
		case _FoldingCastTypeTerm:
			skipFoldingCastTypeTerm(in);
		default:
			throw new ProtocolException();

		}

	}

	private void skipCompositionTerm(DataInput in) throws IOException, ProtocolException
	{
		skip(in);
		skip(in);
	}

	private void skipFunctionTerm(DataInput in) throws IOException, ProtocolException
	{
		skip(in);
		skip(in);
	}

	private void skipTypeTerm(DataInput in)
	{
	}

	private void skipParameterVariableTerm(DataInput in) throws IOException
	{
		integerProtocol.skip(in);
	}

	private void skipIdentifiableVariableTerm(DataInput in) throws IOException
	{
		uuidProtocol.skip(in);
	}

	private void skipProjectionTerm(DataInput in) throws IOException, ProtocolException
	{
		skip(in);
	}

	private void skipCastTypeTerm(DataInput in) throws IOException, ProtocolException
	{
		skip(in);
	}

	private void skipProjectionCastTypeTerm(DataInput in) throws IOException, ProtocolException
	{
		skipCastTypeTerm(in);
	}

	private void skipFoldingCastTypeTerm(DataInput in) throws IOException, ProtocolException
	{
		skipCastTypeTerm(in);
		skip(in);
		skipIdentifiableVariableTerm(in);
		skip(in);
	}

}
