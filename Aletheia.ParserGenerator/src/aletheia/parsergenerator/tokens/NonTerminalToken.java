package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.utilities.MiscUtilities;

public abstract class NonTerminalToken extends Token<NonTerminalSymbol>
{
	public NonTerminalToken(NonTerminalSymbol symbol, Location startLocation, Location stopLocation)
	{
		super(symbol, startLocation, stopLocation);
	}

	private static Location startLocationFromChildren(List<Token<? extends Symbol>> reducees)
	{
		return reducees.isEmpty() ? null : MiscUtilities.firstFromIterable(reducees).getStartLocation();
	}

	private static Location stopLocationFromChildren(List<Token<? extends Symbol>> reducees)
	{
		return reducees.isEmpty() ? null : MiscUtilities.lastFromList(reducees).getStopLocation();
	}

	public NonTerminalToken(Production production, List<Token<? extends Symbol>> reducees)
	{
		this(production.getLeft(), startLocationFromChildren(reducees), stopLocationFromChildren(reducees));
	}

}
