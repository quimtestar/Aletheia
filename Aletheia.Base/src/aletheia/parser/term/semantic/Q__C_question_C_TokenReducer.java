package aletheia.parser.term.semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "Q", right =
{ "C", "question", "C" })
public class Q__C_question_C_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term termMatch = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		List<ParameterVariableTerm> assignable = new ArrayList<>();
		Term.Match match = termMatch.consequent(assignable).match(new HashSet<>(assignable), term);
		if (match == null)
			throw new SemanticException(reducees, "No match.");
		Term assigned = assignable.isEmpty() ? null : match.getAssignMapLeft().get(assignable.get(0));
		if (assigned == null)
			throw new SemanticException(reducees, "Nothing assignable.");
		return assigned;

	}

}