/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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

import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.FunctionTerm.DiffInfoFunction;
import aletheia.model.term.FunctionTerm.SearchInfoFunction;

/**
 * A projection of a function is a term which is roughly equivalent to the
 * function but:
 *
 * <ul>
 * <li>Its type is the type of the function's body (this is why it is called a
 * projection).</li>
 * <li>Its composition with another term is a composition term, not a
 * replacement of the body as in regular functions.</li>
 * </ul>
 *
 */
public class ProjectionTerm extends AtomicTerm
{
	private static final long serialVersionUID = -6771614369482434754L;
	private final static int hashPrime = 2961589;

	private final FunctionTerm function;

	public static class ProjectionTypeException extends TypeException
	{
		private static final long serialVersionUID = -1123291437628030140L;

		protected ProjectionTypeException()
		{
			super();
		}

		protected ProjectionTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected ProjectionTypeException(String message)
		{
			super(message);
		}

		protected ProjectionTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	private static Term computeType(FunctionTerm function) throws ProjectionTypeException
	{
		Term bodyType = function.getBody().getType();
		if (bodyType != null && bodyType.isFreeVariable(function.getParameter()))
			throw new ProjectionTypeException("Non projectable function: body's type depends on parameter");
		return bodyType;
	}

	/**
	 * Constructs the projection of a specific function.
	 *
	 * @param function
	 *            The function to project.
	 * @throws ProjectionTypeException
	 */
	public ProjectionTerm(FunctionTerm function) throws ProjectionTypeException
	{
		super(computeType(function));
		this.function = function;
	}

	/**
	 *
	 * @return The function projected.
	 */
	public FunctionTerm getFunction()
	{
		return function;
	}

	@Override
	public int size()
	{
		return function.size();
	}

	/**
	 * Performs a series of replacements on this projection. The replacements
	 * are first performed on the function and then the resulting function is
	 * projected. It is possible than the resulting term of the replacements is
	 * not a function in which case a {@link Term.ReplaceTypeException} will be
	 * thrown.
	 *
	 */
	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return new ProjectionTerm(function.replace(replaces, exclude));
		}
		catch (ProjectionTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		try
		{
			return function.replace(replaces).project();
		}
		catch (ProjectionTypeException e)
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

		try
		{
			Term function_ = function.replaceSubterm(subterm, replace);
			if (function_ instanceof FunctionTerm)
				return new ProjectionTerm((FunctionTerm) function_);
			else
				return this;
		}
		catch (ProjectionTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	/**
	 * The free variables of a projection are the free variables of the function
	 * projected.
	 */
	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		function.freeVariables(freeVars, localVars);
	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return function.isFreeVariable(variable);
	}

	@Override
	protected void stringAppend(StringAppender stringAppender, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		Term term = this;
		Stack<ParameterVariableTerm> stack = new Stack<>();
		while (term instanceof ProjectionTerm)
		{
			FunctionTerm function = ((ProjectionTerm) term).getFunction();
			stack.push(function.getParameter());
			term = function.getBody();
		}
		int nProjections = stack.size();
		while (!stack.isEmpty())
		{
			term = new FunctionTerm(stack.pop(), term);
		}
		term.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		if (nProjections <= 3)
		{
			for (int i = 0; i < nProjections; i++)
				stringAppender.append("*");
		}
		else
			stringAppender.append("*" + nProjections);
	}

	/**
	 * A projection unprojected is the unprojection of the function. The
	 * "projection element" recursively disappears.
	 * 
	 * @throws UnprojectTypeException
	 */
	@Override
	public Term unproject() throws UnprojectTypeException
	{
		return function.unproject();
	}

	@Override
	public ProjectionTerm project() throws ProjectionTypeException
	{
		return new ProjectionTerm(new FunctionTerm(function.getParameter(), function.getBody().project()));
	}

	@Override
	protected boolean equals(Term term, Map<ParameterVariableTerm, ParameterVariableTerm> parameterMap)
	{
		if (!(term instanceof ProjectionTerm))
			return false;
		ProjectionTerm projectionTerm = (ProjectionTerm) term;
		if (!function.equals(projectionTerm.function, parameterMap))
			return false;
		return true;
	}

	@Override
	protected int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + function.hashCode(hasher *= hashPrime);
		return ret;
	}

	public class DiffInfoProjection extends DiffInfoNotEqual
	{
		public final DiffInfo diffFunction;

		protected DiffInfoProjection(ProjectionTerm other, DiffInfo diffFunction)
		{
			super(other);
			this.diffFunction = diffFunction;
		}

		protected FunctionTerm getFunction()
		{
			return ProjectionTerm.this.getFunction();
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			DiffInfo di = this;
			class StackEntry
			{
				final FunctionTerm functionTerm;
				final FunctionTerm other;
				final DiffInfo diffParamType;

				StackEntry(FunctionTerm functionTerm, FunctionTerm other, DiffInfo diffParamType)
				{
					super();
					this.functionTerm = functionTerm;
					this.other = other;
					this.diffParamType = diffParamType;
				}

			}
			;
			Stack<StackEntry> stack = new Stack<>();
			while (di instanceof DiffInfoProjection)
			{
				DiffInfoFunction diffFunction = (DiffInfoFunction) (((DiffInfoProjection) di).diffFunction);
				stack.push(new StackEntry(diffFunction.getFunctionTerm(), (FunctionTerm) diffFunction.other, diffFunction.diffParamType));
				di = diffFunction.diffBody;
			}
			int nProjections = stack.size();
			while (!stack.isEmpty())
			{
				StackEntry se = stack.pop();
				di = se.functionTerm.new DiffInfoFunction(se.other, se.diffParamType, di);
			}
			StringBuilder stringBuilder = new StringBuilder(di.toStringLeft(variableToIdentifier, parameterNumerator));
			for (int i = 0; i < nProjections; i++)
				stringBuilder.append("*");
			return stringBuilder.toString();
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			DiffInfo di = this;
			class StackEntry
			{
				final FunctionTerm functionTerm;
				final FunctionTerm other;
				final DiffInfo diffParamType;

				StackEntry(FunctionTerm functionTerm, FunctionTerm other, DiffInfo diffParamType)
				{
					super();
					this.functionTerm = functionTerm;
					this.other = other;
					this.diffParamType = diffParamType;
				}

			}
			;
			Stack<StackEntry> stack = new Stack<>();
			while (di instanceof DiffInfoProjection)
			{
				DiffInfoFunction diffFunction = (DiffInfoFunction) (((DiffInfoProjection) di).diffFunction);
				stack.push(new StackEntry(diffFunction.getFunctionTerm(), (FunctionTerm) diffFunction.other, diffFunction.diffParamType));
				di = diffFunction.diffBody;
			}
			int nProjections = stack.size();
			while (!stack.isEmpty())
			{
				StackEntry se = stack.pop();
				di = se.functionTerm.new DiffInfoFunction(se.other, se.diffParamType, di);
			}
			StringBuilder stringBuilder = new StringBuilder(di.toStringRight(variableToIdentifier, parameterNumerator));
			for (int i = 0; i < nProjections; i++)
				stringBuilder.append("*");
			return stringBuilder.toString();
		}
	}

	@Override
	public DiffInfo diff(Term term)
	{
		DiffInfo di = super.diff(term);
		if (di != null)
			return di;
		if (!(term instanceof ProjectionTerm))
			return new DiffInfoNotEqual(term);
		ProjectionTerm proj = (ProjectionTerm) term;
		DiffInfo diffFunction = getFunction().diff(proj.getFunction());
		if (diffFunction instanceof DiffInfoEqual)
			return new DiffInfoEqual(proj);
		return new DiffInfoProjection(proj, diffFunction);
	}

	@Override
	public boolean castFree()
	{
		return getFunction().castFree();
	}

	@Override
	public FunctionParameterIdentification makeParameterIdentification(Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
	{
		return getFunction().makeParameterIdentification(parameterIdentifiers);
	}

	@Override
	protected void populateDomainParameterIdentificationMap(ParameterIdentification parameterIdentification,
			Map<ParameterVariableTerm, DomainParameterIdentification> domainParameterIdentificationMap)
	{
		getFunction().populateDomainParameterIdentificationMap(parameterIdentification, domainParameterIdentificationMap);
	}

	public class SearchInfoProjection extends SearchInfo
	{
		public final SearchInfo searchFunction;

		protected SearchInfoProjection(SearchInfo searchFunction)
		{
			this.searchFunction = searchFunction;
		}

		protected FunctionTerm getFunction()
		{
			return ProjectionTerm.this.getFunction();
		}

		@Override
		public String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			SearchInfo si = this;
			class StackEntry
			{
				final FunctionTerm functionTerm;
				final SearchInfo searchDomain;

				StackEntry(FunctionTerm functionTerm, SearchInfo searchDomain)
				{
					super();
					this.functionTerm = functionTerm;
					this.searchDomain = searchDomain;
				}

			}
			;
			Stack<StackEntry> stack = new Stack<>();
			while (si instanceof SearchInfoProjection)
			{
				SearchInfoFunction searchFunction = (SearchInfoFunction) (((SearchInfoProjection) si).searchFunction);
				stack.push(new StackEntry(searchFunction.getFunctionTerm(), searchFunction.searchDomain));
				si = searchFunction.searchBody;
			}
			int nProjections = stack.size();
			while (!stack.isEmpty())
			{
				StackEntry se = stack.pop();
				si = se.functionTerm.new SearchInfoFunction(se.searchDomain, si);
			}
			StringBuilder stringBuilder = new StringBuilder(si.toString(variableToIdentifier, parameterNumerator));
			for (int i = 0; i < nProjections; i++)
				stringBuilder.append("*");
			return stringBuilder.toString();
		}
	}

	@Override
	public SearchInfo search(Term sub)
	{
		SearchInfo si = super.search(sub);
		if (si instanceof SearchInfoFound)
			return si;

		SearchInfo siFunction = getFunction().search(sub);
		if (siFunction instanceof SearchInfoNotFound)
			return new SearchInfoNotFound();

		return new SearchInfoProjection(siFunction);
	}

}
