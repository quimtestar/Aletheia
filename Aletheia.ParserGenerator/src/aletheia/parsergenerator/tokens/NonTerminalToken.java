package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.TokenPayloadReducer;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;

public class NonTerminalToken<G, P> extends Token<NonTerminalSymbol>
{
	private final P payload;

	public NonTerminalToken(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees,
			TokenPayloadReducer<G, ? extends P> reducer) throws SemanticException
	{
		super(production.getLeft(), startLocationFromList(reducees), stopLocationFromList(reducees));
		this.payload = reducer.reduce(globals, antecedents, production, reducees);
	}

	public P getPayload()
	{
		return payload;
	}

	@Override
	public String toString()
	{
		return super.toString() + ":" + payload;
	}

	@SuppressWarnings("unchecked")
	public static <P> P getPayloadFromTokenList(List<Token<? extends Symbol>> list, int i)
	{
		return ((NonTerminalToken<?, ? extends P>) list.get(i)).getPayload();
	}

	public static <P, T extends NonTerminalToken<?, P>> T findFirstInList(List<Token<? extends Symbol>> list, NonTerminalSymbol symbol)
	{
		return Token.findFirstInList(list, symbol);
	}

	public static <P, T extends NonTerminalToken<?, P>> T findLastInList(List<Token<? extends Symbol>> list, NonTerminalSymbol symbol)
	{
		return Token.findLastInList(list, symbol);
	}

	public static <P, T extends NonTerminalToken<?, P>> P findFirstPayloadInList(List<Token<? extends Symbol>> list, NonTerminalSymbol symbol)
	{
		T token = NonTerminalToken.<P, T> findFirstInList(list, symbol);
		return token == null ? null : token.getPayload();
	}

	public static <P, T extends NonTerminalToken<?, P>> P findLastPayloadInList(List<Token<? extends Symbol>> list, NonTerminalSymbol symbol)
	{
		T token = NonTerminalToken.<P, T> findLastInList(list, symbol);
		return token == null ? null : token.getPayload();
	}

}
