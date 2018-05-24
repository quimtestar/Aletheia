package aletheia.parsergenerator.tokens;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aletheia.parsergenerator.parser.Production;

public class ProductionReducerClassMap<T extends NonTerminalToken, R extends TokenReducer<? super T>>
		extends AbstractMap<Production, Class<? extends ProductionReducer<T, R>>>
{

	private final Map<Production, Class<? extends ProductionReducer<T, R>>> inner;

	public ProductionReducerClassMap()
	{
		super();
		this.inner = new HashMap<>();
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public Class<? extends ProductionReducer<T, R>> get(Object key)
	{
		return inner.get(key);
	}

	@Override
	public Set<Production> keySet()
	{
		return Collections.unmodifiableSet(inner.keySet());
	}

	@Override
	public Collection<Class<? extends ProductionReducer<T, R>>> values()
	{
		return Collections.unmodifiableCollection(inner.values());
	}

	@Override
	public Set<Entry<Production, Class<? extends ProductionReducer<T, R>>>> entrySet()
	{
		return Collections.unmodifiableSet(inner.entrySet());
	}

}
