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
package aletheia.model.term;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;

/**
 * A term representing a generic function.
 *
 * <p>
 * A function is composed by two elements: a <i>parameter</i> which is a
 * {@link VariableTerm}, and a <i>body</i> which is a {@link Term}, where the
 * <i>parameter</i> variable may or may not occur.
 * </p>
 * <p>
 * A function is textually represented by the schema <b>
 * "<<i>parameter</i>:<i>type</i> -> <i>body</i>>"</b> where <i>type</i> is the
 * type term of the parameter variable.
 * </p>
 *
 */
public class FunctionTerm extends Term
{
	private static final long serialVersionUID = 1599150832249973L;
	private final static int hashPrime = 2959783;

	/**
	 * The parameter.
	 */
	private final ParameterVariableTerm parameter;

	/**
	 * The body.
	 */
	private final Term body;

	public static class NullParameterTypeException extends TypeException
	{
		private static final long serialVersionUID = -3907724715830285395L;

		public NullParameterTypeException()
		{
			super("Null parameter type function");
		}

		public NullParameterTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public NullParameterTypeException(String message)
		{
			super(message);
		}

		public NullParameterTypeException(Throwable cause)
		{
			super(cause);
		}

	}

	private static FunctionTerm computeType(ParameterVariableTerm parameter, Term body) throws NullParameterTypeException
	{
		if (parameter.getType() == null)
			throw new NullParameterTypeException();
		if (body.getType() == null)
			return null;
		return new FunctionTerm(parameter, body.getType());
	}

	/**
	 * Create a new function term with the specified parameter and body.
	 *
	 * @param parameter
	 *            The parameter of the function.
	 * @param body
	 *            The body of the function.
	 * @throws NullParameterTypeException
	 * @throws IllegalArgumentException
	 *             Function parameter cannot be a UUIDVariableTerm.
	 */
	public FunctionTerm(ParameterVariableTerm parameter, Term body) throws NullParameterTypeException
	{
		super(computeType(parameter, body));
		this.parameter = parameter;
		this.body = body;
	}

	/**
	 *
	 * @return The parameter.
	 */
	public ParameterVariableTerm getParameter()
	{
		return parameter;
	}

	/**
	 *
	 * @return The body.
	 */
	public Term getBody()
	{
		return body;
	}

	/**
	 * In the case of a function, the composition is performed replacing the
	 * parameter with the term in the body. A function can only be composed if the
	 * type of the term which it is being composed to matches the function
	 * parameter's type. If it's not the case, a {@link Term.ComposeTypeException}
	 * is thrown.
	 */
	@Override
	public Term compose(Term term) throws ComposeTypeException
	{
		if (parameter.getType().equals(term.getType()))
		{
			if (term.freeVariables().contains(parameter))
				throw new ComposeTypeException("Invalid composition: function parameter is free in instance", this, term);
			try
			{
				return body.replace(parameter, term);
			}
			catch (ReplaceTypeException e)
			{
				throw new ComposeTypeException(e, this, term);
			}
		}
		else
			throw new ComposeTypeException("Can't compose term in function", this, term);
	}

	/**
	 * Computes a series of replacements on this function.
	 * 
	 * @throws NullParameterTypeException
	 */
	@Override
	protected FunctionTerm replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		Term partype = parameter.getType();
		Term rpartype = partype.replace(replaces, exclude);
		Term rparam;
		if (!partype.equals(rpartype))
			rparam = new ParameterVariableTerm(rpartype);
		else
			rparam = parameter;
		boolean added = false;
		boolean exadd = false;
		if (!rparam.equals(parameter))
			added = replaces.add(new Replace(parameter, rparam));
		else
			exadd = exclude.add(parameter);
		Term body_ = body.replace(replaces, exclude);
		if (exadd)
			exclude.remove(parameter);
		if (added)
			replaces.removeLast();
		if (rparam.equals(parameter) && body_.equals(body))
			return this;
		else
			try
			{
				return new FunctionTerm((ParameterVariableTerm) rparam, body_);
			}
			catch (NullParameterTypeException e)
			{
				throw new ReplaceTypeException(e);
			}
	}

	@Override
	public Term replaceSubterm(Term subterm, Term replace) throws ReplaceTypeException
	{
		Term replaced = super.replaceSubterm(subterm, replace);
		if (replaced != this)
			return replaced;

		Term partype = parameter.getType();
		Term rpartype = partype.replaceSubterm(subterm, replace);
		Term rparam;
		if (!partype.equals(rpartype))
			rparam = new ParameterVariableTerm(rpartype);
		else
			rparam = parameter;
		Term body_ = body.replaceSubterm(subterm, replace);

		if (!rparam.equals(parameter))
			body_ = body_.replace(parameter, rparam);
		if (rparam.equals(parameter) && body_.equals(body))
			return this;
		else
			try
			{
				return new FunctionTerm((ParameterVariableTerm) rparam, body_);
			}
			catch (NullParameterTypeException e)
			{
				throw new ReplaceTypeException(e);
			}

	}

	/**
	 * A function is textually represented by the schema <b>
	 * "<<i>parameter</i>:<i>type</i> -> <i>body</i>>"</b>.
	 */
	@Override
	public String toString(Map<? extends VariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
	{
		boolean mappedParameter = variableToIdentifier != null && variableToIdentifier.containsKey(parameter);
		String sType = parameter.getType().toString(variableToIdentifier, parameterNumerator);
		if (!mappedParameter)
			parameterNumerator.numberParameter(parameter);
		String sParameter = parameter.toString(variableToIdentifier, parameterNumerator);
		String sBody = body.toString(variableToIdentifier, parameterNumerator);
		if (!mappedParameter)
			parameterNumerator.unNumberParameter();
		return "<" + sParameter + ":" + sType + " -> " + sBody + ">";
	}

	/**
	 * Two functions are equal when the parameters are of the same type and when
	 * replacing one parameter with another in one body results to the other body.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof FunctionTerm))
			return false;
		FunctionTerm functionTerm = (FunctionTerm) obj;
		if (!parameter.getType().equals(functionTerm.parameter.getType()))
			return false;
		try
		{
			if (!body.replace(parameter, functionTerm.parameter).equals(functionTerm.body))
				return false;
		}
		catch (ReplaceTypeException e)
		{
			throw new Error(e);
		}
		return true;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		final int fhasher = hasher *= hashPrime;
		VariableTerm vthasher = new ParameterVariableTerm(parameter.getType())
		{
			private static final long serialVersionUID = -8390284352572376476L;

			@Override
			public int hashCode(int hasher_)
			{
				return fhasher;
			}

		};
		try
		{
			ret = ret * hashPrime + body.replace(parameter, vthasher).hashCode(hasher *= hashPrime);
		}
		catch (ReplaceTypeException e)
		{
			throw new Error(e);
		}

		return ret;
	}

	/**
	 * The free variables of the parameter's type plus the free variables of the
	 * body minus the parameter variable.
	 */
	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		parameter.getType().freeVariables(freeVars, localVars);
		boolean added = localVars.add(parameter);
		body.freeVariables(freeVars, localVars);
		if (added)
			localVars.remove(parameter);
	}

	public abstract class DiffInfoFunction extends DiffInfoNotEqual
	{
		public DiffInfo diffParamType;
		public DiffInfo diffBody;

		public DiffInfoFunction(FunctionTerm other, DiffInfo diffParamType, DiffInfo diffBody)
		{
			super(other);
			this.diffParamType = diffParamType;
			this.diffBody = diffBody;
		}

	}

	public class DiffInfoFunctionParam extends DiffInfoFunction
	{
		public DiffInfoFunctionParam(FunctionTerm other, DiffInfo diffParamType)
		{
			super(other, diffParamType, null);
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			String sParamType = diffParamType.toStringLeft(variableToIdentifier, parameterNumerator);
			parameterNumerator.numberParameter(FunctionTerm.this.getParameter());
			String sParameter = FunctionTerm.this.getParameter().toString(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameter();
			return "<" + sParameter + ":" + sParamType + " -> " + "..." + ">";
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			String sParamType = diffParamType.toStringRight(variableToIdentifier, parameterNumerator);
			parameterNumerator.numberParameter(((FunctionTerm) other).getParameter());
			String sParameter = ((FunctionTerm) other).getParameter().toString(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameter();
			return "<" + sParameter + ":" + sParamType + " -> " + "..." + ">";
		}

	}

	public class DiffInfoFunctionBody extends DiffInfoFunction
	{
		public DiffInfoFunctionBody(FunctionTerm other, DiffInfo diffBody)
		{
			super(other, getParameter().getType().new DiffInfoEqual(other.getParameter().getType()), diffBody);
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			String sParamType = diffParamType.toStringLeft(variableToIdentifier, parameterNumerator);
			parameterNumerator.numberParameter(getParameter());
			String sParameter = getParameter().toString(variableToIdentifier, parameterNumerator);
			String sBody = diffBody.toStringLeft(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameter();
			return "<" + sParameter + ":" + sParamType + " -> " + sBody + ">";
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			String sParamType = diffParamType.toStringRight(variableToIdentifier, parameterNumerator);
			parameterNumerator.numberParameter(getParameter());
			String sParameter = getParameter().toString(variableToIdentifier, parameterNumerator);
			String sBody = diffBody.toStringRight(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameter();
			return "<" + sParameter + ":" + sParamType + " -> " + sBody + ">";
		}

	}

	@Override
	public DiffInfo diff(Term term)
	{
		DiffInfo di = super.diff(term);
		if (di != null)
			return di;
		if (!(term instanceof FunctionTerm))
			return new DiffInfoNotEqual(term);
		FunctionTerm func = (FunctionTerm) term;
		DiffInfo diffParamType = getParameter().getType().diff(func.getParameter().getType());
		if (!(diffParamType instanceof DiffInfoEqual))
			return new DiffInfoFunctionParam(func, diffParamType);
		try
		{
			DiffInfo diffBody = getBody().diff(func.getBody().replace(func.getParameter(), getParameter()));
			if (!(diffBody instanceof DiffInfoEqual))
				return new DiffInfoFunctionBody(func, diffBody);
			return new DiffInfoEqual(func);
		}
		catch (ReplaceTypeException e)
		{
			throw new Error(e);
		}
	}

	/**
	 * The consequent of this function. That is, the consequent of its body.
	 */
	@Override
	public SimpleTerm consequent(Collection<ParameterVariableTerm> parameters)
	{
		if (parameters != null)
			parameters.add(getParameter());
		return getBody().consequent(parameters);
	}

	@Override
	protected void parameters(Collection<ParameterVariableTerm> parameters)
	{
		parameters.add(getParameter());
		getBody().parameters(parameters);
	}

	/**
	 * Unprojects this function combining the unprojection of the parameter type
	 * with the unprojection of the body. A new and different parameter variable
	 * might be generated if the unprojection of the parameter type results in a
	 * different term than the original parameter type itself.
	 */
	@Override
	public Term unproject() throws UnprojectTypeException
	{
		Term partype = parameter.getType();
		Term ppartype = partype.unproject();
		Term pbody = body.unproject();
		if (!partype.equals(ppartype))
		{
			ParameterVariableTerm pparam = new ParameterVariableTerm(ppartype);
			try
			{
				return new FunctionTerm(pparam, pbody.replace(parameter, pparam));
			}
			catch (ReplaceTypeException | NullParameterTypeException e)
			{
				throw new UnprojectTypeException(e);
			}
		}
		else
		{
			try
			{
				return new FunctionTerm(parameter, pbody);
			}
			catch (NullParameterTypeException e)
			{
				throw new UnprojectTypeException(e);
			}
		}

	}

	@Override
	public ProjectionTerm project() throws ProjectionTypeException
	{
		return new ProjectionTerm(this);
	}

}
