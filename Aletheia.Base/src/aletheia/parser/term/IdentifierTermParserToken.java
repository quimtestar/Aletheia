package aletheia.parser.term;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class IdentifierTermParserToken extends TermParserToken
{
	private final Identifier identifier;

	public IdentifierTermParserToken(Production production, List<Token<? extends Symbol>> children, Identifier identifier)
	{
		super(production, children);
		this.identifier = identifier;
	}

	public Identifier getIdentifier()
	{
		return identifier;
	}

}
