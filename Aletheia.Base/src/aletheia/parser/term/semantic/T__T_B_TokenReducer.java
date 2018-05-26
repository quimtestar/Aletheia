package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "T", right =
{ "T", "B" })
public class T__T_B_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Term tail = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		try
		{
			return term.compose(tail);
		}
		catch (ComposeTypeException e)
		{
			throw new SemanticException(reducees.get(0), e);
		}

	}

}