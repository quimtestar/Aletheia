/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.Term;
import aletheia.pdfexport.SimpleChunk;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class ProjectedCastTypeTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = 4487221289844310777L;

	protected ProjectedCastTypeTermPhrase(PersistenceManager persistenceManager, Transaction transaction,
			Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, ProjectedCastTypeTerm term)
	{
		super(term);
		addSimpleChunk(new SimpleChunk("["));
		addBasePhrase(TermPhrase.termPhrase(persistenceManager, transaction, variableToIdentifier, parameterNumerator, parameterIdentification,
				parameterToIdentifier, term.getTerm()));
		addSimpleChunk(new SimpleChunk("]"));
	}

	@Override
	public ProjectedCastTypeTerm getTerm()
	{
		return (ProjectedCastTypeTerm) super.getTerm();
	}

}
