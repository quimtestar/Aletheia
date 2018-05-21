package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;

public class ParseTreeTokenFactory extends TokenFactory<ParseTreeToken>
{

	@Override
	public ParseTreeToken makeToken(Production production, Location startLocation, Location stopLocation, List<Token<? extends Symbol>> children)
	{
		return new ParseTreeToken(production, startLocation, stopLocation, children);
	}

}
