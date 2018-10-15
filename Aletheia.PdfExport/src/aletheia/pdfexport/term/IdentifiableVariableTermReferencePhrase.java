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
package aletheia.pdfexport.term;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.pdfexport.SimpleChunk;
import aletheia.pdfexport.font.FontManager;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

import com.itextpdf.text.BaseColor;

public class IdentifiableVariableTermReferencePhrase extends IdentifiableVariableTermPhrase
{
	private static final long serialVersionUID = 3336805645960371025L;

	private static final BaseColor provenColor = new BaseColor(0, 124, 0);
	private static final BaseColor unprovenColor = new BaseColor(124, 98, 0);

	private static BaseColor color(PersistenceManager persistenceManager, Transaction transaction, IdentifiableVariableTerm var)
	{
		return persistenceManager.statements(transaction).get(var).isProved() ? provenColor : unprovenColor;
	}

	protected IdentifiableVariableTermReferencePhrase(PersistenceManager persistenceManager, Transaction transaction,
			Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, IdentifiableVariableTerm term)
	{
		super(variableToIdentifier, term);
		SimpleChunk c = new SimpleChunk(getText(), FontManager.instance.getFont(fontSize, color(persistenceManager, transaction, term)));
		//c.setLocalGoto(term.getUuid().toString());
		addSimpleChunk(c);

	}

}
