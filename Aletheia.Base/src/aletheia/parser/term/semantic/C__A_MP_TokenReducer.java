package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.parser.term.TermParser.ProductionTokenReducer;
import aletheia.parser.term.tokens.IntegerToken;
import aletheia.parser.term.tokens.TermToken;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "C", right =
{ "A", "MP" })
public class C__A_MP_TokenReducer extends ProductionTokenReducer<TermToken>
{

	@Override
	public TermToken reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Term term = ((TermToken) reducees.get(0)).getTerm();
		int n = ((IntegerToken) reducees.get(1)).getInt();
		try
		{
			return new TermToken(production, reducees, term.project(n));
		}
		catch (ProjectionTypeException e)
		{
			throw new SemanticException(reducees, e);
		}

	}

}