package aletheia.parser.term;

import java.util.List;

import aletheia.model.term.Term;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class TermTermParserToken extends TermParserToken
{
	private final Term term;

	public TermTermParserToken(Production production, List<Token<? extends Symbol>> children, Term term)
	{
		super(production, children);
		this.term = term;
	}

	public Term getTerm()
	{
		return term;
	}

}
