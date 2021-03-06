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

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.ParameterIdentification;

/**
 * The sole primitive type of the term.
 *
 * <p>
 * There exists just one instance of this {@link Term} class, accessed through
 * the static member {@link TauTerm#instance}. The primitive type is represented
 * textually by the literal <b>"\u03a4"</b> (greek capital letter <b>tau</b>) or
 * the text "Tau".
 *
 *
 */
public class TauTerm extends AtomicTerm
{
	private static final long serialVersionUID = 2981030395329810419L;
	private final static int hashPrime = 2959937;

	private TauTerm()
	{
		super(null);
	}

	@Override
	public int size()
	{
		return 0;
	}

	/**
	 * The unique instance of this class.
	 */
	public static TauTerm instance = new TauTerm();

	/**
	 * The primitive type is represented with the String <b>"Tau"</b>.
	 */
	@Override
	protected void stringAppend(StringAppender stringAppender, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		stringAppender.append("Tau");
	}

	@Override
	protected TauTerm replace(Deque<Replace> replaces, Set<VariableTerm> exclude)
	{
		return this;
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces)
	{
		return this;
	}

	/**
	 * The primitive type is equals to the primitive type itself.
	 */
	@Override
	protected boolean equals(Term term, Map<ParameterVariableTerm, ParameterVariableTerm> parameterMap)
	{
		if (!(term instanceof TauTerm))
			return false;
		return true;
	}

	@Override
	protected int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	protected TauTerm clone()
	{
		return this;
	}

	/**
	 * The primitive type has no variables, free or not.
	 */
	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		return;
	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return false;
	}

	@Override
	public DiffInfo diff(Term term)
	{
		DiffInfo di = super.diff(term);
		if (di != null)
			return di;
		if (!(term instanceof TauTerm))
			return new DiffInfoNotEqual(term);
		return new DiffInfoEqual(term);
	}

	@Override
	public boolean castFree()
	{
		return true;
	}

}
