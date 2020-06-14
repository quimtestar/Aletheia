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
package aletheia.pdfexport.statement;

import com.itextpdf.text.Document;

import aletheia.model.statement.Specialization;
import aletheia.pdfexport.BasePhrase;
import aletheia.pdfexport.SimpleChunk;
import aletheia.pdfexport.term.TermPhrase;
import aletheia.persistence.Transaction;

public class SpecializationTable extends StatementTable
{

	private class SpecializationPhrase extends BasePhrase
	{
		private static final long serialVersionUID = 1733632129361357209L;

		public SpecializationPhrase()
		{
			super();
			addSimpleChunk(new SimpleChunk("Specialization: "));
			addBasePhrase(TermPhrase.termPhrase(getPersistenceManager(), getTransaction(), variableToIdentifier(),
					getStatement().getGeneral(getTransaction()).getVariable()));
			addSimpleChunk(new SimpleChunk(" \u2190 "));
			addBasePhrase(TermPhrase.termPhrase(getPersistenceManager(), getTransaction(), variableToIdentifier(),
					getStatement().getInstanceParameterIdentification(), getStatement().getInstance()));
			if (!getStatement().getInstanceProof(getTransaction()).getVariable().equals(getStatement().getInstance()))
			{
				addSimpleChunk(new SimpleChunk(" \u2248 "));
				addBasePhrase(TermPhrase.termPhrase(getPersistenceManager(), getTransaction(), variableToIdentifier(),
						getStatement().getInstanceProof(getTransaction()).getVariable()));
			}
		}
	}

	public SpecializationTable(Document doc, int depth, Transaction transaction, Specialization statement)
	{
		super(doc, depth, transaction, statement);
		addCell(new MyCell(new SpecializationPhrase()));
	}

	@Override
	public Specialization getStatement()
	{
		return (Specialization) super.getStatement();
	}

}
