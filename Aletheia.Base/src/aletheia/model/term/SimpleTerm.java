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
import java.util.List;

import aletheia.model.term.CompositionTerm.CompositionTypeException;

/**
 * A simple term is a term which is not a function; an {@link AtomicTerm}
 * possibly composed with a succession of terms.
 *
 */
public abstract class SimpleTerm extends Term
{
	private static final long serialVersionUID = 390523679292889973L;
	private final static int hashPrime = 2962273;

	public SimpleTerm(Term type)
	{
		super(type);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof CompositionTerm))
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		return ret;
	}

	/**
	 * Simple term is composed by simply creating a composition term with itself
	 * as the head and the term to compose with as the tail.
	 *
	 * @throws ComposeTypeException
	 */
	@Override
	public CompositionTerm compose(Term term) throws ComposeTypeException
	{
		try
		{
			return new CompositionTerm(this, term);
		}
		catch (CompositionTypeException e)
		{
			throw new ComposeTypeException(e.getMessage(), e, this, term);
		}
	}

	/**
	 * The consequent of a simple term is itself.
	 */
	@Override
	public SimpleTerm consequent(Collection<ParameterVariableTerm> parameters)
	{
		return this;
	}

	/**
	 * The length of a simple term is defined to be as the total number of atoms
	 * composed (recursively).
	 *
	 * @return The length.
	 */
	public abstract int length();

	public abstract SimpleTerm head();

	public abstract List<Term> components();

	public abstract List<Term> aggregateComponents();

}
