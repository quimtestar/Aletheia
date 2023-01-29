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
package aletheia.prooffinder;

import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;

public class ImpureCandidate extends Candidate
{
	private final Candidate parent;
	private final VariableTerm variable;
	private final Term assign;

	public ImpureCandidate(Candidate parent, VariableTerm variable, Term assign)
	{
		super(parent);
		this.parent = parent;
		this.variable = variable;
		this.assign = assign;
		assignVar(variable, assign);
	}

	public Candidate getParent()
	{
		return parent;
	}

	VariableTerm getVariable()
	{
		return variable;
	}

	Term getAssign()
	{
		return assign;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assign == null) ? 0 : assign.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		ImpureCandidate other = (ImpureCandidate) obj;
		if (assign == null)
		{
			if (other.assign != null)
				return false;
		}
		else if (!assign.equals(other.assign))
			return false;
		if (parent == null)
		{
			if (other.parent != null)
				return false;
		}
		else if (!parent.equals(other.parent))
			return false;
		if (variable == null)
		{
			if (other.variable != null)
				return false;
		}
		else if (!variable.equals(other.variable))
			return false;
		return true;
	}

}
