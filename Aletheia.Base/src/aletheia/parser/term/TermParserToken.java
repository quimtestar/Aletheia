package aletheia.parser.term;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public abstract class TermParserToken extends NonTerminalToken
{
	public TermParserToken(Production production, List<Token<? extends Symbol>> children)
	{
		super(production, children);
	}

}
