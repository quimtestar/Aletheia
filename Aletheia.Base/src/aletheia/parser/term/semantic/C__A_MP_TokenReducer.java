package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "C", right =
{ "A", "MP" })
public class C__A_MP_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		int n = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		try
		{
			return term.project(n);
		}
		catch (ProjectionTypeException e)
		{
			throw new SemanticException(reducees, e);
		}

	}

}