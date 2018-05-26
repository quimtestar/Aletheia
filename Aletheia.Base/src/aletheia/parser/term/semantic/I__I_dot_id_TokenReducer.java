package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "I", right =
{ "I", "dot", "id" })
public class I__I_dot_id_TokenReducer extends ProductionTokenPayloadReducer<Identifier>
{

	@Override
	public Identifier reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Identifier namespace = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		String name = TaggedTerminalToken.getTextFromTokenList(reducees, 2);
		try
		{
			return new Identifier(namespace, name);
		}
		catch (InvalidNameException e)
		{
			throw new SemanticException(reducees.get(0), e);
		}

	}

}