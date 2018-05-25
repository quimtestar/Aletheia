package aletheia.parser.term.tokens;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ValuedNonTerminalToken;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class IdentifierToken extends ValuedNonTerminalToken<Identifier>
{

	public IdentifierToken(Production production, List<Token<? extends Symbol>> reducees, Identifier identifier)
	{
		super(production, reducees, identifier);
	}

	public Identifier getIdentifier()
	{
		return getValue();
	}

}