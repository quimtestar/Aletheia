package aletheia.parser.term.tokens;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class IdentifierToken extends NonTerminalToken
{
	private final Identifier identifier;

	public IdentifierToken(Production production, List<Token<? extends Symbol>> reducees, Identifier identifier)
	{
		super(production, reducees);
		this.identifier = identifier;
	}

	public Identifier getIdentifier()
	{
		return identifier;
	}

}