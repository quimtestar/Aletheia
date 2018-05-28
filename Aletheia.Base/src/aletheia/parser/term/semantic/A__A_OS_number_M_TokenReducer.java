package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.CompositionTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.utilities.collections.BufferedList;

@AssociatedProduction(left = "A", right =
{ "A", "OS", "number", "M" })
public class A__A_OS_number_M_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		if (term instanceof CompositionTerm)
		{
			List<Term> components;
			if (NonTerminalToken.<Boolean> getPayloadFromTokenList(reducees, 3))
				components = new BufferedList<>(((CompositionTerm) term).aggregateComponents());
			else
				components = new BufferedList<>(((CompositionTerm) term).components());
			int n = Integer.parseInt(TaggedTerminalToken.getTextFromTokenList(reducees, 2));
			if (n < 0 || n >= components.size())
				throw new SemanticException(reducees.get(2), "Composition coordinate " + n + " out of bounds for term: " + "'"
						+ term.toString(globals.getTransaction(), globals.getContext()) + "'");
			return components.get(n);
		}
		else
			throw new SemanticException(reducees.get(0), "Only can use composition coordinates in compositions");
	}

}