package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;

public abstract class TokenReducer<T extends NonTerminalToken>
{
	public abstract T reduce(List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees);
}
