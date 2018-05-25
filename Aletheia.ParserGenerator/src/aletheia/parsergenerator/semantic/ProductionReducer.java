package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public abstract class ProductionReducer<T extends NonTerminalToken, R extends ProductionManagedTokenReducer<? super T>>
{
	private final R tokenReducer;
	private final Production production;

	public ProductionReducer(R tokenReducer, Production production)
	{
		super();
		this.tokenReducer = tokenReducer;
		this.production = production;
	}

	public R getTokenReducer()
	{
		return tokenReducer;
	}

	public Production getProduction()
	{
		return production;
	}

	public abstract T reduce(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees) throws SemanticException;

}
