/*******************************************************************************
 * Copyright (c) 2014, 2017 Quim Testar.
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
package aletheia.pdfexport.term;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.VariableTerm;

public abstract class VariableTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = -8504623430280520181L;

	private final Map<? extends VariableTerm, Identifier> variableToIdentifier;

	protected VariableTermPhrase(VariableTerm term, Map<? extends VariableTerm, Identifier> variableToIdentifier)
	{
		super(term);
		this.variableToIdentifier = variableToIdentifier;
	}

	protected Map<? extends VariableTerm, Identifier> getVariableToIdentifier()
	{
		return variableToIdentifier;
	}

	@Override
	public VariableTerm getTerm()
	{
		return (VariableTerm) super.getTerm();
	}

	protected abstract String getText();

}
