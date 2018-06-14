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
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;

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
		if (!(obj instanceof SimpleTerm))
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
		Term type = getType();
		if (type == null)
			return this;
		List<ParameterVariableTerm> typeParameters = type.parameters();
		SimpleTerm consequent = this;
		for (ParameterVariableTerm v : typeParameters)
		{
			ParameterVariableTerm v_ = new ParameterVariableTerm(v.getType());
			try
			{
				consequent = new CompositionTerm(consequent, v_);
			}
			catch (CompositionTypeException e)
			{
				throw new RuntimeException(e);
			}
			parameters.add(v_);
		}
		return consequent;
	}

	@Override
	protected void parameters(Collection<ParameterVariableTerm> parameters)
	{
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

	public class FunctionalizeTypeException extends TypeException
	{
		private static final long serialVersionUID = -3917425606014092703L;

		private FunctionalizeTypeException()
		{
			super();
		}

		private FunctionalizeTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private FunctionalizeTypeException(String message)
		{
			super(message);
		}

		private FunctionalizeTypeException(Throwable cause)
		{
			super(cause);
		}

	}

	public FunctionTerm functionalize() throws FunctionalizeTypeException
	{
		try
		{
			ParameterVariableTerm v = new ParameterVariableTerm(domain());
			return new FunctionTerm(v, new CompositionTerm(this, v));
		}
		catch (CompositionTypeException | DomainTypeException e)
		{
			throw new FunctionalizeTypeException(e);
		}
	}

	@Override
	public ProjectionTerm project() throws ProjectionTypeException
	{
		try
		{
			return functionalize().project();
		}
		catch (FunctionalizeTypeException e)
		{
			throw new ProjectionTypeException(e.getMessage(), e);
		}
	}

	protected abstract AtomicTerm atom();

}
