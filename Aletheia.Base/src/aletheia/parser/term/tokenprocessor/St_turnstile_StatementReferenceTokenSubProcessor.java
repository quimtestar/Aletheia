/*******************************************************************************
 * Copyright (c) 2017 Quim Testar.
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
package aletheia.parser.term.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.semantic.ParseTreeToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_t", right =
{ "turnstile" })
public class St_turnstile_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected St_turnstile_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(ParseTreeToken token, Context context, Transaction transaction, ReferenceType referenceType) throws TokenProcessorException
	{
		if (context == null)
			throw new TokenProcessorException("Cannot refer to the consequent without a context", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation());
		if (referenceType != ReferenceType.TYPE)
			throw new TokenProcessorException("Invalid reference type to the consequent", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation());
		return context.getConsequent();
	}

}
