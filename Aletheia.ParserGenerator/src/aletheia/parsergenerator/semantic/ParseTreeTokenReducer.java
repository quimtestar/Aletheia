package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class ParseTreeTokenReducer extends TokenReducer<ParseTreeToken>
{

	@Override
	public ParseTreeToken reduce(List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
	{
		return new ParseTreeToken(production, reducees);
	}

}
