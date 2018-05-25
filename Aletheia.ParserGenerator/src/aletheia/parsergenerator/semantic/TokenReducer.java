package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public abstract class TokenReducer<T extends NonTerminalToken>
{
	public abstract T reduce(List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees) throws SemanticException;
}
