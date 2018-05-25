package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;

public abstract class NonTerminalToken extends Token<NonTerminalSymbol>
{
	public NonTerminalToken(NonTerminalSymbol symbol, Location startLocation, Location stopLocation)
	{
		super(symbol, startLocation, stopLocation);
	}

	public NonTerminalToken(Production production, List<Token<? extends Symbol>> reducees)
	{
		this(production.getLeft(), startLocationFromList(reducees), stopLocationFromList(reducees));
	}

}
