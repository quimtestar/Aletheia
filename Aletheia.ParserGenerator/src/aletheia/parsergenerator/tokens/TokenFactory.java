package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;

public abstract class TokenFactory<T extends Token<? extends Symbol>>
{

	public TokenFactory()
	{
	}

	public abstract T makeToken(Production production, Location startLocation, Location stopLocation, List<Token<? extends Symbol>> children);

}
