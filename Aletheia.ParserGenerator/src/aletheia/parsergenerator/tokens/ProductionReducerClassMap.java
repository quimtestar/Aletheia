package aletheia.parsergenerator.tokens;

import java.util.AbstractMap;
import java.util.Set;

import aletheia.parsergenerator.parser.Production;

public class ProductionReducerClassMap<T extends NonTerminalToken> extends AbstractMap<Production, Class<? extends ProductionReducer<? extends T>>>
{

	@Override
	public Set<Entry<Production, Class<? extends ProductionReducer<? extends T>>>> entrySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
