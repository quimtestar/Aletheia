package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.TauTerm;
import aletheia.parser.term.TermParser.ProductionTokenReducer;
import aletheia.parser.term.tokens.TermToken;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "A", right =
{ "tau" })
public class A__tau_TokenReducer extends ProductionTokenReducer<TermToken>
{

	@Override
	public TermToken reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		return new TermToken(production, reducees, TauTerm.instance);
	}

}