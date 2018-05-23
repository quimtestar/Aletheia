package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.symbols.Symbol;

public abstract class ProductionReducer<T extends NonTerminalToken>
{
	private final TokenReducer<? super T> tokenReducer;

	public ProductionReducer(TokenReducer<? super T> tokenReducer)
	{
		super();
		this.tokenReducer = tokenReducer;
	}

	public TokenReducer<? super T> getTokenReducer()
	{
		return tokenReducer;
	}

	public abstract T reduce(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees);

}
