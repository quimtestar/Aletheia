package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class ValuedNonTerminalToken<V> extends NonTerminalToken
{
	private final V value;

	public ValuedNonTerminalToken(NonTerminalSymbol symbol, Location startLocation, Location stopLocation, V value)
	{
		super(symbol, startLocation, stopLocation);
		this.value = value;
	}

	public ValuedNonTerminalToken(Production production, List<Token<? extends Symbol>> reducees, V value)
	{
		super(production, reducees);
		this.value = value;
	}

	public V getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return super.toString() + ":" + value;
	}

}
