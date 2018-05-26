package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.parser.term.TermParser.ProductionTokenReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parser.term.tokens.IdentifierToken;
import aletheia.parser.term.tokens.ParameterRefToken;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "P", right =
{ "I" })
public class P__I_TokenReducer extends ProductionTokenReducer<ParameterRefToken>
{

	@Override
	public ParameterRefToken reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Identifier identifier = ((IdentifierToken) reducees.get(0)).getIdentifier();
		return new ParameterRefToken(production, reducees, new IdentifierParameterRef(identifier));
	}

}