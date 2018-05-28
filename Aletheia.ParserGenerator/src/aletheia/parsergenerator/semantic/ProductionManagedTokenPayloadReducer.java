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
import aletheia.parsergenerator.tokens.Token;

public class ProductionManagedTokenPayloadReducer<G, P> extends TokenPayloadReducer<G, P>
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

	public static class ProductionManagedTokenPayloadReducerException extends RuntimeException
	{
		private static final long serialVersionUID = -7943873023809664017L;

		private ProductionManagedTokenPayloadReducerException()
		{
			super();
		}

		private ProductionManagedTokenPayloadReducerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private ProductionManagedTokenPayloadReducerException(String message)
		{
			super(message);
		}

		private ProductionManagedTokenPayloadReducerException(Throwable cause)
		{
			super(cause);
		}

	}

	public static abstract class ProductionTokenPayloadReducer<G, P>
	{
		public abstract P reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException;
	}

	private final Map<AssociatedProductionKey, ProductionTokenPayloadReducer<G, ? extends P>> productionTokenPayloadReducerMap;

	public ProductionManagedTokenPayloadReducer(
			Collection<? extends Class<? extends ProductionTokenPayloadReducer<G, ? extends P>>> productionTokenPayloadReducerClasses)
			throws ProductionManagedTokenPayloadReducerException
	{
		super();
		this.productionTokenPayloadReducerMap = new HashMap<>();
		for (Class<? extends ProductionTokenPayloadReducer<G, ? extends P>> productionTokenPayloadReducerClass : productionTokenPayloadReducerClasses)
		{
			AssociatedProduction associatedProduction = productionTokenPayloadReducerClass.getAnnotation(AssociatedProduction.class);
			if (associatedProduction == null)
				throw new ProductionManagedTokenPayloadReducerException("A ProductionTokenPayloadReducer class must be annotated with an AssociatedProduction: "
						+ productionTokenPayloadReducerClass.getName());
			AssociatedProductionKey associatedProductionKey = new AssociatedProductionKey(associatedProduction);
			try
			{
				ProductionTokenPayloadReducer<G, ? extends P> old = productionTokenPayloadReducerMap.put(associatedProductionKey,
						productionTokenPayloadReducerClass.newInstance());
				if (old != null)
					throw new ProductionManagedTokenPayloadReducerException("Production collision for classes " + productionTokenPayloadReducerClass.getName()
							+ " and " + old.getClass().getName() + " (" + associatedProductionKey + ")");
			}
			catch (SecurityException | InstantiationException | IllegalAccessException e)
			{
				throw new ProductionManagedTokenPayloadReducerException(e);
			}
		}
	}

	@Override
	public P reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		ProductionTokenPayloadReducer<G, ? extends P> reducer = productionTokenPayloadReducerMap.get(new AssociatedProductionKey(production));
		if (reducer == null)
			throw new ProductionManagedTokenPayloadReducerException("No token class declared for production: " + production);
		return reducer.reduce(globals, antecedents, production, reducees);
	}

}