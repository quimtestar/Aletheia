package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;

public class ProductionManagedTokenReducer<T extends NonTerminalToken> extends TokenReducer<T>
{
	private final ProductionReducerFactory<T, ? extends ProductionManagedTokenReducer<T>> productionReducerFactory;

	public ProductionManagedTokenReducer(ProductionReducerFactory<T, ? extends ProductionManagedTokenReducer<T>> productionReducerFactory)
	{
		this.productionReducerFactory = productionReducerFactory;
	}

	@Override
	public T reduce(List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		return productionReducerFactory.create(production, this).reduce(antecedents, reducees);
	}

}
