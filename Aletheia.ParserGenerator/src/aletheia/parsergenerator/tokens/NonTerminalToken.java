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

}
