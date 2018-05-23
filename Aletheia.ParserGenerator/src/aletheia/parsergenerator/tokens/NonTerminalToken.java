package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.utilities.MiscUtilities;

public abstract class NonTerminalToken extends Token<NonTerminalSymbol>
{

	private static Location startLocationFromChildren(List<Token<? extends Symbol>> children)
	{
		return children.isEmpty() ? null : MiscUtilities.firstFromIterable(children).getStartLocation();
	}

	private static Location stopLocationFromChildren(List<Token<? extends Symbol>> children)
	{
		return children.isEmpty() ? null : MiscUtilities.lastFromList(children).getStopLocation();
	}

	public NonTerminalToken(Production production, List<Token<? extends Symbol>> children)
	{
		super(production.getLeft(), startLocationFromChildren(children), stopLocationFromChildren(children));
	}

}
