package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.CompositionTerm.CompositionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "T", right =
{ "T", "A" })
public class T_T_A_ParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected T_T_A_ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		ParameterIdentification head = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(0), input);
		ParameterIdentification tail = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(1), input);
		if (head instanceof CompositionParameterIdentification)
			return new CompositionParameterIdentification((CompositionParameterIdentification) head, tail);
		else if (head == null)
			return tail;
		else
			return new CompositionParameterIdentification(null,
					new CompositionParameterIdentification(new CompositionParameterIdentification(null, head), tail));
	}

}
