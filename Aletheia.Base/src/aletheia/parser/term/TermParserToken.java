package aletheia.parser.term;

import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public class TermParserToken extends NonTerminalToken
{

	public TermParserToken(NonTerminalSymbol symbol, Location startLocation, Location stopLocation)
	{
		super(symbol, startLocation, stopLocation);
	}

	public TermParserToken(NonTerminalSymbol symbol, Location location)
	{
		super(symbol, location);
	}

}
