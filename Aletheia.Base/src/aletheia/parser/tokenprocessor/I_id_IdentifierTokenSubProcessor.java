package aletheia.parser.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;

@ProcessorProduction(left = "I", right =
{ "id" })
public class I_id_IdentifierTokenSubProcessor extends IdentifierTokenSubProcessor
{

	protected I_id_IdentifierTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Identifier subProcess(NonTerminalToken token, String input) throws TermParserException
	{
		String name = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
		try
		{
			return new Identifier(name);
		}
		catch (InvalidNameException e)
		{
			throw new TermParserException(e, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
		}
	}

}
