package aletheia.parser.term.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ValuedNonTerminalToken;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class IntegerToken extends ValuedNonTerminalToken<Integer>
{

	public IntegerToken(Production production, List<Token<? extends Symbol>> reducees, int value)
	{
		super(production, reducees, value);
	}

	public int getInt()
	{
		return getValue();
	}

}