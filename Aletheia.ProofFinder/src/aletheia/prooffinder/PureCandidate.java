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

import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;

public abstract class PureCandidate extends Candidate
{

	public PureCandidate(Term term, SimpleTerm target)
	{
		super(term, target);
	}

	@Override
	protected void assignVar(VariableTerm var, Term assign)
	{
		throw new UnsupportedOperationException();
	}

}
