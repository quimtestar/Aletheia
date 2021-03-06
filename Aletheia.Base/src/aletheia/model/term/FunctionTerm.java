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

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.CombinedMap;

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

	private static FunctionTerm computeType(ParameterVariableTerm parameter, Term body)
	{
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
	 */
	public FunctionTerm(ParameterVariableTerm parameter, Term body)
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

	@Override
	public int size()
	{
		return parameter.getType().size() + body.size();
	}

	/**
	 * In the case of a function, the composition is performed replacing the
	 * parameter with the term in the body. A function can only be composed if
	 * the type of the term which it is being composed to matches the function
	 * parameter's type. If it's not the case, a
	 * {@link Term.ComposeTypeException} is thrown.
	 */
	@Override
	public Term compose(Term term) throws ComposeTypeException
	{
		if (parameter.equals(term))
			return body;
		if (parameter.getType().equals(term.getType()))
		{
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
	 */
	@Override
	protected FunctionTerm replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		Term partype = parameter.getType();
		Term rpartype = partype.replace(replaces, exclude);
		Term rparam = new ParameterVariableTerm(rpartype);
		replaces.addFirst(new Replace(parameter, rparam));
		Term body_ = body.replace(replaces, exclude);
		replaces.removeFirst();
		return new FunctionTerm((ParameterVariableTerm) rparam, body_);
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		Term parType = getParameter().getType().replace(replaces);
		ParameterVariableTerm parameter;
		Map<VariableTerm, Term> bodyReplaces;
		parameter = new ParameterVariableTerm(parType);
		bodyReplaces = new CombinedMap<>(Collections.singletonMap(getParameter(), parameter), replaces);
		Term body = getBody().replace(bodyReplaces);
		return new FunctionTerm(parameter, body);
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
			return new FunctionTerm((ParameterVariableTerm) rparam, body_);
	}

	/**
	 * A function is textually represented by the schema <b>
	 * "<<i>parameter</i>:<i>type</i> -> <i>body</i>>"</b>.
	 */
	@Override
	protected void stringAppend(StringAppender stringAppender, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		Term term = this;
		Map<ParameterVariableTerm, Identifier> localVariableToIdentifier = new HashMap<>();
		Map<VariableTerm, Identifier> totalVariableToIdentifier = variableToIdentifier == null ? new AdaptedMap<>(localVariableToIdentifier)
				: new CombinedMap<>(new AdaptedMap<>(localVariableToIdentifier), new AdaptedMap<>(variableToIdentifier));
		stringAppender.append("<");
		stringAppender.openSub();
		boolean first = true;
		int numberedParameters = 0;
		while (term instanceof FunctionTerm)
		{
			if (!first)
			{
				stringAppender.append(", ");
				stringAppender.closeSub();
				stringAppender.openSub();
			}
			first = false;

			ParameterVariableTerm parameter = ((FunctionTerm) term).getParameter();
			Term body = ((FunctionTerm) term).getBody();

			Identifier parameterIdentifier = null;
			ParameterIdentification domainParameterIdentification = null;
			ParameterIdentification bodyParameterIdentification = null;
			if (parameterIdentification instanceof FunctionParameterIdentification)
			{
				parameterIdentifier = ((FunctionParameterIdentification) parameterIdentification).getParameter();
				domainParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getDomain();
				bodyParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getBody();
			}
			boolean numberedParameter = false;
			if (body.isFreeVariable(parameter))
			{
				if (parameterIdentifier != null)
				{
					stringAppender.append(parameterIdentifier);
					localVariableToIdentifier.put(parameter, parameterIdentifier);
				}
				else
				{
					Identifier id = totalVariableToIdentifier.get(parameter);
					if (id != null)
						stringAppender.append(id);
					else
					{
						stringAppender.append(parameter.numRef(parameterNumerator.nextNumber()));
						numberedParameter = true;
					}
				}
				stringAppender.append(":");
			}
			parameter.getType().stringAppend(stringAppender, totalVariableToIdentifier, parameterNumerator, domainParameterIdentification);

			if (numberedParameter)
			{
				parameterNumerator.numberParameter(parameter);
				numberedParameters++;
			}

			parameterIdentification = bodyParameterIdentification;
			term = body;
		}
		stringAppender.closeSub();
		stringAppender.openSub();
		stringAppender.append(" -> ");
		term.stringAppend(stringAppender, totalVariableToIdentifier, parameterNumerator, parameterIdentification);
		parameterNumerator.unNumberParameters(numberedParameters);
		stringAppender.append(">");
		stringAppender.closeSub();
	}

	@Override
	public Map<ParameterVariableTerm, Identifier> parameterVariableToIdentifier(ParameterIdentification parameterIdentification)
	{
		Map<ParameterVariableTerm, Identifier> localMap = null;
		if (parameterIdentification instanceof FunctionParameterIdentification)
		{
			Identifier identifier = ((FunctionParameterIdentification) parameterIdentification).getParameter();
			if (identifier != null)
				localMap = Collections.singletonMap(parameter, identifier);
		}
		Map<ParameterVariableTerm, Identifier> bodyMap = null;
		if (body instanceof FunctionTerm)
		{
			ParameterIdentification bodyParameterIdentification = null;
			if (parameterIdentification instanceof FunctionParameterIdentification)
				bodyParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getBody();
			bodyMap = ((FunctionTerm) body).parameterVariableToIdentifier(bodyParameterIdentification);
		}
		if (localMap == null)
			return bodyMap;
		else if (bodyMap == null)
			return localMap;
		else
			return new CombinedMap<>(localMap, bodyMap);
	}

	/**
	 * Two functions are equal when the parameters are of the same type and when
	 * replacing one parameter with another in one's body results to the other's
	 * body.
	 */
	@Override
	protected boolean equals(Term term, Map<ParameterVariableTerm, ParameterVariableTerm> parameterMap)
	{
		if (!(term instanceof FunctionTerm))
			return false;
		FunctionTerm functionTerm = (FunctionTerm) term;
		if (!parameter.getType().equals(functionTerm.parameter.getType(), parameterMap))
			return false;
		ParameterVariableTerm oldParameter = parameterMap.put(parameter, functionTerm.parameter);
		try
		{
			if (!body.equals(functionTerm.body, parameterMap))
				return false;
			return true;
		}
		finally
		{
			if (oldParameter == null)
				parameterMap.remove(parameter);
			else
				parameterMap.put(parameter, oldParameter);
		}
	}

	@Override
	protected int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + parameter.getType().hashCode(hasher *= hashPrime);
		final int fhasher = hasher *= hashPrime;
		VariableTerm vthasher = new ParameterVariableTerm(parameter.getType())
		{
			private static final long serialVersionUID = -8390284352572376476L;

			@Deprecated
			@Override
			protected int hashCode(int hasher_)
			{
				return fhasher;
			}

		};
		try
		{
			Term rbody = body.replace(parameter, vthasher);
			ret = ret * hashPrime + rbody.hashCode(hasher *= hashPrime);
		}
		catch (ReplaceTypeException e)
		{
			throw new RuntimeException(e);
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

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return !parameter.equals(variable) && (parameter.getType().isFreeVariable(variable) || body.isFreeVariable(variable));
	}

	public class DiffInfoFunction extends DiffInfoNotEqual
	{
		public final DiffInfo diffParamType;
		public final DiffInfo diffBody;

		protected DiffInfoFunction(FunctionTerm other, DiffInfo diffParamType, DiffInfo diffBody)
		{
			super(other);
			this.diffParamType = diffParamType;
			this.diffBody = diffBody;
		}

		protected FunctionTerm getFunctionTerm()
		{
			return FunctionTerm.this;
		}

		protected ParameterVariableTerm getParameter()
		{
			return getFunctionTerm().getParameter();
		}

		protected Term getBody()
		{
			return getFunctionTerm().getBody();
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			DiffInfo di = this;
			StringBuilder parameterListStringBuilder = new StringBuilder();
			boolean first = true;
			int numberedParameters = 0;
			while (di instanceof DiffInfoFunction)
			{
				DiffInfo diffParamType = ((DiffInfoFunction) di).diffParamType;
				DiffInfo diffBody = ((DiffInfoFunction) di).diffBody;
				ParameterVariableTerm parameter = ((DiffInfoFunction) di).getParameter();
				Term body = ((DiffInfoFunction) di).getBody();
				String sType = diffParamType.toStringLeft(variableToIdentifier, parameterNumerator);
				String sParameter = null;
				if (body.isFreeVariable(parameter))
				{
					parameterNumerator.numberParameter(parameter);
					numberedParameters++;
					sParameter = parameter.toString(variableToIdentifier, parameterNumerator);
				}
				if (!first)
					parameterListStringBuilder.append(", ");
				parameterListStringBuilder.append((sParameter != null ? (sParameter + ":") : "") + sType);
				first = false;
				di = diffBody;
			}
			String sBody = null;
			if (di != null)
				sBody = di.toStringLeft(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameters(numberedParameters);
			return "<" + parameterListStringBuilder + " " + (sBody != null ? ("-> " + sBody) : "...") + ">";
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			DiffInfo di = this;
			StringBuilder parameterListStringBuilder = new StringBuilder();
			boolean first = true;
			int numberedParameters = 0;
			while (di instanceof DiffInfoFunction)
			{
				DiffInfo diffParamType = ((DiffInfoFunction) di).diffParamType;
				DiffInfo diffBody = ((DiffInfoFunction) di).diffBody;
				ParameterVariableTerm parameter = ((DiffInfoFunction) di).getParameter();
				Term body = ((DiffInfoFunction) di).getBody();
				String sType = diffParamType.toStringRight(variableToIdentifier, parameterNumerator);
				String sParameter = null;
				if (body.isFreeVariable(parameter))
				{
					parameterNumerator.numberParameter(parameter);
					numberedParameters++;
					sParameter = parameter.toString(variableToIdentifier, parameterNumerator);
				}
				if (!first)
					parameterListStringBuilder.append(", ");
				parameterListStringBuilder.append((sParameter != null ? (sParameter + ":") : "") + sType);
				first = false;
				di = diffBody;
			}
			String sBody = null;
			if (di != null)
				sBody = di.toStringRight(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameters(numberedParameters);
			return "<" + parameterListStringBuilder + " " + (sBody != null ? ("-> " + sBody) : "...") + ">";
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
		DiffInfo diffBody;
		try
		{
			diffBody = getBody().diff(func.getBody().replace(func.getParameter(), getParameter()));
		}
		catch (ReplaceTypeException e)
		{
			diffBody = null;
		}
		if ((diffParamType instanceof DiffInfoEqual) && (diffBody instanceof DiffInfoEqual))
			return new DiffInfoEqual(func);
		return new DiffInfoFunction(func, diffParamType, diffBody);
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
	 * 
	 * @throws UnprojectTypeException
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
			catch (ReplaceTypeException e)
			{
				throw new UnprojectTypeException(e);
			}
		}
		else
		{
			return new FunctionTerm(parameter, pbody);
		}
	}

	@Override
	public ProjectionTerm project() throws ProjectionTypeException
	{
		return new ProjectionTerm(this);
	}

	@Override
	public Term domain()
	{
		return getParameter().getType();
	}

	@Override
	public boolean castFree()
	{
		return domain().castFree() && getBody().castFree();
	}

	@Override
	public FunctionParameterIdentification makeParameterIdentification(Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
	{
		Identifier parameterIdentifier = parameterIdentifiers.get(getParameter());
		ParameterIdentification domainParameterIdentification = domain().makeParameterIdentification(parameterIdentifiers);
		ParameterIdentification bodyParameterIdentification = getBody().makeParameterIdentification(parameterIdentifiers);
		return FunctionParameterIdentification.make(parameterIdentifier, domainParameterIdentification, bodyParameterIdentification);
	}

	@Override
	protected void populateDomainParameterIdentificationMap(ParameterIdentification parameterIdentification,
			Map<ParameterVariableTerm, DomainParameterIdentification> domainParameterIdentificationMap)
	{
		if (parameterIdentification instanceof FunctionParameterIdentification)
		{
			FunctionParameterIdentification functionParameterIdentification = (FunctionParameterIdentification) parameterIdentification;
			getBody().populateDomainParameterIdentificationMap(functionParameterIdentification.getBody(), domainParameterIdentificationMap);
			domain().populateDomainParameterIdentificationMap(functionParameterIdentification.getDomain(), domainParameterIdentificationMap);
			domainParameterIdentificationMap.put(getParameter(),
					new DomainParameterIdentification(functionParameterIdentification.getParameter(), functionParameterIdentification.getDomain()));
		}
	}

	@Override
	protected void findSimpleTermByAtom(AtomicTerm atom, Collection<SimpleTerm> results)
	{
		domain().findSimpleTermByAtom(atom, results);
		body.findSimpleTermByAtom(atom, results);
	}

	@Override
	public Term unfunctionalize()
	{
		Term body_ = body.unfunctionalize();
		if (body_ instanceof CompositionTerm)
		{
			SimpleTerm head = ((CompositionTerm) body_).getHead();
			Term tail = ((CompositionTerm) body_).getTail();
			if (tail.equals(parameter) && !head.isFreeVariable(parameter))
				return head;
		}
		return new FunctionTerm(parameter, body_);
	}

	public class SearchInfoFunction extends SearchInfo
	{
		public final SearchInfo searchDomain;
		public final SearchInfo searchBody;

		protected SearchInfoFunction(SearchInfo searchDomain, SearchInfo searchBody)
		{
			this.searchDomain = searchDomain;
			this.searchBody = searchBody;
		}

		protected FunctionTerm getFunctionTerm()
		{
			return FunctionTerm.this;
		}

		protected ParameterVariableTerm getParameter()
		{
			return getFunctionTerm().getParameter();
		}

		protected Term getBody()
		{
			return getFunctionTerm().getBody();
		}

		@Override
		public String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			SearchInfo si = this;
			StringBuilder parameterListStringBuilder = new StringBuilder();
			boolean first = true;
			int numberedParameters = 0;
			while (si instanceof SearchInfoFunction)
			{
				SearchInfo siDomain = ((SearchInfoFunction) si).searchDomain;
				SearchInfo siBody = ((SearchInfoFunction) si).searchBody;
				ParameterVariableTerm parameter = ((SearchInfoFunction) si).getParameter();
				Term body = ((SearchInfoFunction) si).getBody();
				String sDomain = siDomain.toString(variableToIdentifier, parameterNumerator);
				String sParameter = null;
				if (body.isFreeVariable(parameter))
				{
					parameterNumerator.numberParameter(parameter);
					numberedParameters++;
					sParameter = parameter.toString(variableToIdentifier, parameterNumerator);
				}
				if (!first)
					parameterListStringBuilder.append(", ");
				parameterListStringBuilder.append((sParameter != null ? (sParameter + ":") : "") + sDomain);
				first = false;
				si = siBody;
			}
			String sBody = null;
			if (si != null)
				sBody = si.toString(variableToIdentifier, parameterNumerator);
			parameterNumerator.unNumberParameters(numberedParameters);
			return "<" + parameterListStringBuilder + " " + (sBody != null ? ("-> " + sBody) : "...") + ">";
		}
	}

	@Override
	public SearchInfo search(Term sub)
	{
		SearchInfo si = super.search(sub);
		if (si instanceof SearchInfoFound)
			return si;

		SearchInfo siDomain = domain().search(sub);
		SearchInfo siBody = getBody().search(sub);
		if ((siDomain instanceof SearchInfoNotFound) && (siBody instanceof SearchInfoNotFound))
			return new SearchInfoNotFound();

		return new SearchInfoFunction(siDomain, siBody);
	}

}
