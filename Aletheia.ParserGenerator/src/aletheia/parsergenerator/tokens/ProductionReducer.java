package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.symbols.Symbol;

public abstract class ProductionReducer<T extends NonTerminalToken, R extends TokenReducer<? super T>>
{
	private final R tokenReducer;

	public ProductionReducer(R tokenReducer)
	{
		super();
		this.tokenReducer = tokenReducer;
	}

	public R getTokenReducer()
	{
		return tokenReducer;
	}

	public abstract T reduce(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees);

}
