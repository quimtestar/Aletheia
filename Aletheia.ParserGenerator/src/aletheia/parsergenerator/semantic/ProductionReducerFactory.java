package aletheia.parsergenerator.semantic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public class ProductionReducerFactory<T extends NonTerminalToken, R extends ProductionManagedTokenReducer<T>>
{

	public static class ProductionReducerFactoryException extends RuntimeException
	{
		private static final long serialVersionUID = -3592221390509656679L;

		private ProductionReducerFactoryException()
		{
			super();
		}

		private ProductionReducerFactoryException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private ProductionReducerFactoryException(String message)
		{
			super(message);
		}

		private ProductionReducerFactoryException(Throwable cause)
		{
			super(cause);
		}

	}

	private static class AssociatedProductionKey
	{
		private final String left;
		private final String[] right;

		private AssociatedProductionKey(AssociatedProduction associatedProduction)
		{
			super();
			this.left = associatedProduction.left();
			this.right = associatedProduction.right();
		}

		private AssociatedProductionKey(Production production)
		{
			super();
			this.left = production.getLeft().toString();
			this.right = new String[production.getRight().size()];
			int i = 0;
			for (Symbol s : production.getRight())
				this.right[i++] = s.toString();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((left == null) ? 0 : left.hashCode());
			result = prime * result + Arrays.hashCode(right);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AssociatedProductionKey other = (AssociatedProductionKey) obj;
			if (left == null)
			{
				if (other.left != null)
					return false;
			}
			else if (!left.equals(other.left))
				return false;
			if (!Arrays.equals(right, other.right))
				return false;
			return true;
		}
	}

	private final Map<AssociatedProductionKey, Class<? extends ProductionReducer<T, R>>> map;

	public ProductionReducerFactory(List<Class<? extends ProductionReducer<T, R>>> productionReducerClasses) throws ProductionReducerFactoryException
	{
		super();
		this.map = new HashMap<>();
		for (Class<? extends ProductionReducer<T, R>> reducerClass : productionReducerClasses)
		{
			AssociatedProduction associatedProduction = reducerClass.getAnnotation(AssociatedProduction.class);
			if (associatedProduction == null)
				throw new ProductionReducerFactoryException("Unannotated parser TokenSubProcessor class");
			map.put(new AssociatedProductionKey(associatedProduction), reducerClass);
		}
	}

	public ProductionReducer<T, R> create(Production production, R tokenReducer) throws ProductionReducerFactoryException
	{
		try
		{
			Class<? extends ProductionReducer<T, R>> reducerClass = map.get(new AssociatedProductionKey(production));
			if (reducerClass == null)
				throw new ProductionReducerFactoryException("No reducer class declared for production: " + production.toString());
			Constructor<? extends ProductionReducer<T, R>> constructor = reducerClass.getDeclaredConstructor(Production.class, tokenReducer.getClass());
			ProductionReducer<T, R> reducer = constructor.newInstance(production, tokenReducer);
			return reducer;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			throw new ProductionReducerFactoryException(e);
		}
	}

}
