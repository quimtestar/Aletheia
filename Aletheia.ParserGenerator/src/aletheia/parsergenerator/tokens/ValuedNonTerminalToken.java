package aletheia.parsergenerator.tokens;

import aletheia.parsergenerator.symbols.NonTerminalSymbol;

//TODO: will this be really useful?
public class ValuedNonTerminalToken<V> extends NonTerminalToken
{
	private final V value;

	public ValuedNonTerminalToken(NonTerminalSymbol symbol, Location startLocation, Location stopLocation, V value)
	{
		super(symbol, startLocation, stopLocation);
		this.value = value;
	}

	public ValuedNonTerminalToken(NonTerminalSymbol symbol, Location location, V value)
	{
		super(symbol, location);
		this.value = value;
	}

	public V getValue()
	{
		return value;
	}

}
