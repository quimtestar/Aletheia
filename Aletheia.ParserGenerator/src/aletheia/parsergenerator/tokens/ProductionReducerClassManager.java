package aletheia.parsergenerator.tokens;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;

public class ProductionReducerClassManager<T extends NonTerminalToken, R extends TokenReducer<? super T>>
{
	
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

	public ProductionReducerClassManager(Grammar grammar,List<Class<? extends ProductionReducer<T, R>>> productionReducerClasses)
	{
		super();
		this.map = new HashMap<>();
		for (Class<? extends ProductionReducer<T, R>> reducerClass:productionReducerClasses)
		{
			AssociatedProduction associatedProduction = reducerClass.getAnnotation(AssociatedProduction.class);
			if (associatedProduction == null)
				throw new RuntimeException("Unannotated parser TokenSubProcessor class");
			map.put(new AssociatedProductionKey(associatedProduction),reducerClass);
		}
	}

	public Class<? extends ProductionReducer<T, R>> get(Production production)
	{
		return map.get(new AssociatedProductionKey(production));
	}


}
