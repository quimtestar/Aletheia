/*******************************************************************************
 * Copyright (c) 2018, 2019 Quim Testar
 * 
 * This file is part of the Aletheia Proof Assistant.
 * 
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.parsergenerator.semantic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
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
			if ((obj == null) || (getClass() != obj.getClass()))
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

	public static abstract class TrivialProductionTokenPayloadReducer<G, P> extends ProductionTokenPayloadReducer<G, P>
	{
		private final int position;

		public TrivialProductionTokenPayloadReducer(int position)
		{
			this.position = position;
		}

		public TrivialProductionTokenPayloadReducer()
		{
			this(0);
		}

		@Override
		public P reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return NonTerminalToken.getPayloadFromTokenList(reducees, position);
		}

	}

	public static abstract class ConstantProductionTokenPayloadReducer<G, P> extends ProductionTokenPayloadReducer<G, P>
	{
		private final P value;

		public ConstantProductionTokenPayloadReducer(P value)
		{
			super();
			this.value = value;
		}

		@Override
		public P reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return value;
		}

	}

	public static abstract class NullProductionTokenPayloadReducer<G> extends ConstantProductionTokenPayloadReducer<G, Void>
	{
		public NullProductionTokenPayloadReducer()
		{
			super(null);
		}
	}

	public static abstract class TerminalWrapperProductionTokenPayloadReducer<G, P> extends ProductionTokenPayloadReducer<G, P>
	{
		private final int position;

		public TerminalWrapperProductionTokenPayloadReducer(int position)
		{
			this.position = position;
		}

		public TerminalWrapperProductionTokenPayloadReducer()
		{
			this(0);
		}

		@Override
		public P reduce(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return wrap((TerminalToken) reducees.get(position));
		}

		public abstract P wrap(TerminalToken token) throws SemanticException;
	}

	public static class IntegerTerminalWrapperProductionTokenPayloadReducer<G> extends TerminalWrapperProductionTokenPayloadReducer<G, Integer>
	{

		public IntegerTerminalWrapperProductionTokenPayloadReducer()
		{
			super();
		}

		public IntegerTerminalWrapperProductionTokenPayloadReducer(int position)
		{
			super(position);
		}

		@Override
		public Integer wrap(TerminalToken token) throws SemanticException
		{
			try
			{
				return Integer.parseInt(token.getText());
			}
			catch (NumberFormatException e)
			{
				throw new SemanticException(token, e);
			}
		}
	}

	public static class UUIDTerminalWrapperProductionTokenPayloadReducer<G> extends TerminalWrapperProductionTokenPayloadReducer<G, UUID>
	{

		public UUIDTerminalWrapperProductionTokenPayloadReducer()
		{
			super();
		}

		public UUIDTerminalWrapperProductionTokenPayloadReducer(int position)
		{
			super(position);
		}

		@Override
		public UUID wrap(TerminalToken token) throws SemanticException
		{
			try
			{
				return UUID.fromString(token.getText());
			}
			catch (IllegalArgumentException e)
			{
				throw new SemanticException(token, e);
			}
		}
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
						productionTokenPayloadReducerClass.getDeclaredConstructor().newInstance());
				if (old != null)
					throw new ProductionManagedTokenPayloadReducerException("Production collision for classes " + productionTokenPayloadReducerClass.getName()
							+ " and " + old.getClass().getName() + " (" + associatedProductionKey + ")");
			}
			catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException e)
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
			throw new ProductionManagedTokenPayloadReducerException("No token payload reducer class declared for production: " + production);
		return reducer.reduce(globals, antecedents, production, reducees);
	}

}
