package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.parser.term.TermParser.ContextTransaction;
import aletheia.parser.term.tokens.IdentifierToken;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.ProductionTokenReducer;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "I", right =
{ "id" })
public class I__id_TokenReducer extends ProductionTokenReducer<ContextTransaction, IdentifierToken>
{

	@Override
	public IdentifierToken reduce(ContextTransaction contextTransaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		String name = ((TaggedTerminalToken) reducees.get(0)).getText();
		try
		{
			return new IdentifierToken(production, reducees, new Identifier(name));
		}
		catch (InvalidNameException e)
		{
			throw new SemanticException(reducees.get(0).getStartLocation(), reducees.get(0).getStopLocation());
		}
	}

}