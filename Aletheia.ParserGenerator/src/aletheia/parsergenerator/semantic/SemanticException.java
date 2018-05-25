package aletheia.parsergenerator.semantic;

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.tokens.Location;

public class SemanticException extends ParserLexerException
{
	private static final long serialVersionUID = -292833502687472137L;

	public SemanticException(Location startLocation, Location stopLocation, String message, Throwable cause)
	{
		super(startLocation, stopLocation, message, cause);
	}

	public SemanticException(Location startLocation, Location stopLocation, String message)
	{
		super(startLocation, stopLocation, message);
	}

	public SemanticException(Location startLocation, Location stopLocation, Throwable cause)
	{
		super(startLocation, stopLocation, cause);
	}

	public SemanticException(Location startLocation, Location stopLocation)
	{
		super(startLocation, stopLocation);
	}

}
