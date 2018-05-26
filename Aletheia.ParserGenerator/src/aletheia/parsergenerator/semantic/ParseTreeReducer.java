package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class ParseTreeReducer extends TokenPayloadReducer<Void, ParseTree>
{

	@Override
	public ParseTree reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
	{
		return new ParseTree(production, reducees);
	}

}
