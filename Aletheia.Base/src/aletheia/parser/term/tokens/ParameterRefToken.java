package aletheia.parser.term.tokens;

import java.util.List;

import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ValuedNonTerminalToken;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class ParameterRefToken extends ValuedNonTerminalToken<ParameterRef>
{

	public ParameterRefToken(Production production, List<Token<? extends Symbol>> reducees, ParameterRef parameterRef)
	{
		super(production, reducees, parameterRef);
	}

	public ParameterRef getParameterRef()
	{
		return getValue();
	}

}