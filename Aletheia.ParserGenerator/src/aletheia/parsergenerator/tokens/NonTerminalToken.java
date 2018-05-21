package aletheia.parsergenerator.tokens;

import aletheia.parsergenerator.symbols.NonTerminalSymbol;

public abstract class NonTerminalToken extends Token<NonTerminalSymbol>
{

	public NonTerminalToken(NonTerminalSymbol symbol, Location startLocation, Location stopLocation)
	{
		super(symbol, startLocation, stopLocation);
	}

	public NonTerminalToken(NonTerminalSymbol symbol, Location location)
	{
		super(symbol, location);
	}

}
