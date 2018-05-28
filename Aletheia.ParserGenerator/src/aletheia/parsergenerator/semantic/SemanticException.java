package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class SemanticException extends ParserBaseException
{
	private static final long serialVersionUID = -292833502687472137L;

	public SemanticException(List<Token<? extends Symbol>> tokens, String message, Throwable cause)
	{
		super(Token.startLocationFromList(tokens), Token.stopLocationFromList(tokens), message, cause);
	}

	public SemanticException(List<Token<? extends Symbol>> tokens, String message)
	{
		super(Token.startLocationFromList(tokens), Token.stopLocationFromList(tokens), message);
	}

	public SemanticException(List<Token<? extends Symbol>> tokens, Throwable cause)
	{
		super(Token.startLocationFromList(tokens), Token.stopLocationFromList(tokens), cause);
	}

	public SemanticException(List<Token<? extends Symbol>> tokens)
	{
		super(Token.startLocationFromList(tokens), Token.stopLocationFromList(tokens));
	}

	public SemanticException(Token<? extends Symbol> token, String message, Throwable cause)
	{
		super(token.getStartLocation(), token.getStopLocation(), message, cause);
	}

	public SemanticException(Token<? extends Symbol> token, String message)
	{
		super(token.getStartLocation(), token.getStopLocation(), message);
	}

	public SemanticException(Token<? extends Symbol> token, Throwable cause)
	{
		super(token.getStartLocation(), token.getStopLocation(), cause);
	}

	public SemanticException(Token<? extends Symbol> token)
	{
		super(token.getStartLocation(), token.getStopLocation());
	}

}
