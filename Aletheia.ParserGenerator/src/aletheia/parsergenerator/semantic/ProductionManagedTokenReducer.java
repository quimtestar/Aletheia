package aletheia.parsergenerator.semantic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class ProductionManagedTokenReducer<G, T extends NonTerminalToken> extends TokenReducer<G, T>
{
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface AssociatedProduction
	{
		String left();

		String[] right();
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
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(left);
			builder.append(" -> ");
			for (String s : right)
			{
				builder.append(s);
				builder.append(" ");
			}
			return builder.toString();
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

	public static class ProductionManagedTokenReducerException extends RuntimeException
	{
		private static final long serialVersionUID = -7943873023809664017L;

		private ProductionManagedTokenReducerException()
		{
			super();
		}

		private ProductionManagedTokenReducerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private ProductionManagedTokenReducerException(String message)
		{
			super(message);
		}

		private ProductionManagedTokenReducerException(Throwable cause)
		{
			super(cause);
		}

	}

	public static abstract class ProductionTokenReducer<G, T extends NonTerminalToken>
	{
		public abstract T reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException;
	}

	private final Map<AssociatedProductionKey, ProductionTokenReducer<G, ? extends T>> productionTokenReducerMap;

	public ProductionManagedTokenReducer(Collection<? extends Class<? extends ProductionTokenReducer<G, ? extends T>>> productionTokenReducerClasses)
			throws ProductionManagedTokenReducerException
	{
		super();
		this.productionTokenReducerMap = new HashMap<>();
		for (Class<? extends ProductionTokenReducer<G, ? extends T>> productionTokenReducerClass : productionTokenReducerClasses)
		{
			AssociatedProduction associatedProduction = productionTokenReducerClass.getAnnotation(AssociatedProduction.class);
			if (associatedProduction == null)
				throw new ProductionManagedTokenReducerException(
						"A ProductionTokenReducer class must be annotated with an AssociatedProduction: " + productionTokenReducerClass.getName());
			AssociatedProductionKey associatedProductionKey = new AssociatedProductionKey(associatedProduction);
			try
			{
				ProductionTokenReducer<G, ? extends T> old = productionTokenReducerMap.put(associatedProductionKey, productionTokenReducerClass.newInstance());
				if (old != null)
					throw new ProductionManagedTokenReducerException("Production collision for classes " + productionTokenReducerClass.getName() + " and "
							+ old.getClass().getName() + " (" + associatedProductionKey + ")");
			}
			catch (SecurityException | InstantiationException | IllegalAccessException e)
			{
				throw new ProductionManagedTokenReducerException(e);
			}
		}
	}

	@Override
	public T reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		ProductionTokenReducer<G, ? extends T> reducer = productionTokenReducerMap.get(new AssociatedProductionKey(production));
		if (reducer == null)
			throw new ProductionManagedTokenReducerException("No token class declared for production: " + production);
		return reducer.reduce(globals, antecedents, production, reducees);
	}

}
