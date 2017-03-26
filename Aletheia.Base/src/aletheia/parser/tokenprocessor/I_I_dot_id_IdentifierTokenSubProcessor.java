package aletheia.parser.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;

@ProcessorProduction(left = "I", right =
{ "I", "dot", "id" })
public class I_I_dot_id_IdentifierTokenSubProcessor extends IdentifierTokenSubProcessor
{

	protected I_I_dot_id_IdentifierTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Identifier subProcess(NonTerminalToken token, String input) throws TermParserException
	{
		Identifier namespace = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
		String name = ((TaggedTerminalToken) token.getChildren().get(2)).getText();
		try
		{
			return new Identifier(namespace, name);
		}
		catch (InvalidNameException e)
		{
			throw new TermParserException(e, token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
		}

	}

}
