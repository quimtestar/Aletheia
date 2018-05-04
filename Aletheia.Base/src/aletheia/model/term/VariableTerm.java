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

import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;

/**
 * A term which is a single variable.
 *
 * <p>
 * By default, every variable is considered to be distinct (as far as the java
 * object representing them are distinct), and is represented textually with a
 * label of the format <b>"@<i>hash</i>"</b>, where <i>hash</i> is the
 * hexadecimal representation to eight digits of the hashcode of the java
 * object.
 * </p>
 *
 */
public abstract class VariableTerm extends AtomicTerm
{
	private static final long serialVersionUID = 6752417376520042927L;

	/**
	 * Create a new variable with the specified type.
	 *
	 * @param type
	 *            The type of the new variable.
	 * @throws NullVariableTypeException
	 */
	public VariableTerm(Term type)
	{
		super(type);
	}

	/**
	 * Replacing a variable term is quite trivial. If the variable to be
	 * replaced equals this, then the result is the term to replace; else the
	 * result is this variable term unaltered.
	 */
	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude)
	{
		Term term = this;
		for (Replace r : replaces)
		{
			if (!exclude.contains(r.variable) && !r.variable.equals(r.term))
			{
				if (term.equals(r.variable))
					term = r.term;
			}
		}
		return term;
	}

	/**
	 * The free variables of a variable term is the singleton set composed by
	 * itself.
	 */
	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		if (!localVars.contains(this))
			freeVars.add(this);
	}

	private void freeVariablesTypeRecursive(Set<VariableTerm> freeVars)
	{
		for (VariableTerm var : getType().freeVariables())
		{
			freeVars.add(var);
			var.freeVariablesTypeRecursive(freeVars);
		}
	}

	/**
	 * The type-recursive version of the free variables method. The result
	 * includes the free variables of its type term, and the type-recursive free
	 * variables of each of these variables, but does not include this variable
	 * object.
	 *
	 * @return The type-recursive free variables set of this variable (excluding
	 *         itself).
	 */
	public Set<VariableTerm> freeVariablesTypeRecursive()
	{
		Set<VariableTerm> freeVars = new HashSet<>();
		freeVariablesTypeRecursive(freeVars);
		return freeVars;
	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return equals(variable);
	}

	@Override
	public DiffInfo diff(Term term)
	{
		DiffInfo di = super.diff(term);
		if (di != null)
			return di;
		if (equals(term))
			return new DiffInfoEqual(term);
		return new DiffInfoNotEqual(term);
	}

	public abstract String hexRef();

	@Override
	public String toString(Map<? extends VariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification)
	{
		Identifier identifier = variableToIdentifier != null ? variableToIdentifier.get(this) : null;
		if (identifier != null)
			return identifier.toString();
		else
			return hexRef();
	}

}
