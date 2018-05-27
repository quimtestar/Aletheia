package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "B", right =
{ "B", "bar", "Q" })
public class B__B_bar_Q_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Term oldTerm = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		ParameterVariableTerm param = new ParameterVariableTerm(oldTerm.getType());
		try
		{
			return new FunctionTerm(param, term.replaceSubterm(oldTerm, param));
		}
		catch (ReplaceTypeException e)
		{
			throw new SemanticException(reducees, e);
		}

	}

}