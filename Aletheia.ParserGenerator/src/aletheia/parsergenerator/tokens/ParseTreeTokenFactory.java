package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;

public class ParseTreeTokenFactory extends NonTerminalTokenFactory<ParseTreeToken>
{

	@Override
	public ParseTreeToken reduceToken(List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
	{
		return new ParseTreeToken(production, reducees);
	}

}
