package aletheia.parser.term.tokens;

import java.util.List;

import aletheia.model.term.Term;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class TermToken extends NonTerminalToken
{
	private final Term term;

	public TermToken(Production production, List<Token<? extends Symbol>> reducees, Term term)
	{
		super(production, reducees);
		this.term = term;
	}

	public Term getTerm()
	{
		return term;
	}

}