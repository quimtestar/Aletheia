package aletheia.parser.term.tokens;

import java.util.List;

import aletheia.model.term.Term;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ValuedNonTerminalToken;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class TermToken extends ValuedNonTerminalToken<Term>
{

	public TermToken(Production production, List<Token<? extends Symbol>> reducees, Term term)
	{
		super(production, reducees, term);
	}

	public Term getTerm()
	{
		return getValue();
	}

}