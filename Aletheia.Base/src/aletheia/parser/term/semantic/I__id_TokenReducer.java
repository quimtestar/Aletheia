package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.parser.term.TermParser.ProductionTokenReducer;
import aletheia.parser.term.tokens.IdentifierToken;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "I", right =
{ "id" })
public class I__id_TokenReducer extends ProductionTokenReducer<IdentifierToken>
{

	@Override
	public IdentifierToken reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		String name = ((TaggedTerminalToken) reducees.get(0)).getText();
		try
		{
			return new IdentifierToken(production, reducees, new Identifier(name));
		}
		catch (InvalidNameException e)
		{
			throw new SemanticException(reducees.get(0), e);
		}
	}

}