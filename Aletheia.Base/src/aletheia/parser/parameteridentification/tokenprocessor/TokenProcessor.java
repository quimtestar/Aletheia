/*******************************************************************************
 * Copyright (c) 2017 Quim Testar.
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
package aletheia.parser.parameteridentification.tokenprocessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import aletheia.model.identifier.Identifier;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public class TokenProcessor
{
	//@formatter:off
	private final static List<Class<? extends TokenSubProcessor<?,?>>> subProcessorClasses=Arrays.asList(
			T_T__ParameterIdentificationTokenSubProcessor.class,
			T_ParameterIdentificationTokenSubProcessor.class,
			T__T_F_ParameterIdentificationTokenSubProcessor.class,
			T__T_openpar_T_closepar_ParameterIdentificationTokenSubProcessor.class,
			F_openfun_M_arrow_T__closefun_FunctionParameterIdentificationTokenSubProcessor.class,
			F_openfun_M_closefun_FunctionParameterIdentificationTokenSubProcessor.class,
			M_M_comma_P_ParameterIdentificationTokenSubProcessor.class,
			M_P_ParameterIdentificationTokenSubProcessor.class,
			P_I_IdentifierTokenSubProcessor.class,
			P_IdentifierTokenSubProcessor.class,
			P_I_colon_T_IdentifierTokenSubProcessor.class,
			P_colon_T_IdentifierTokenSubProcessor.class,
			I_I_dot_id_IdentifierTokenSubProcessor.class,
			I_id_IdentifierTokenSubProcessor.class
			);
	//@formatter:on

	private static class SubProcessorClassKey
	{
		private final String left;
		private final String[] right;

		private SubProcessorClassKey(ProcessorProduction processorProduction)
		{
			super();
			this.left = processorProduction.left();
			this.right = processorProduction.right();
		}

		private SubProcessorClassKey(Production production)
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
			SubProcessorClassKey other = (SubProcessorClassKey) obj;
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

	private final static Map<SubProcessorClassKey, Class<? extends TokenSubProcessor<?, ?>>> subProcessorClassMap = new HashMap<>();

	static
	{
		for (Class<? extends TokenSubProcessor<?, ?>> processorClass : subProcessorClasses)
		{
			ProcessorProduction pp = processorClass.getAnnotation(ProcessorProduction.class);
			if (pp == null)
				throw new Error("Unannotated parser TokenSubProcessor class");
			Class<? extends TokenSubProcessor<?, ?>> old = subProcessorClassMap.put(new SubProcessorClassKey(pp), processorClass);
			if (old != null)
				throw new Error("TokenSubProcessor class collission. Check annotations.");
		}
	}

	private class InstanceSubProcessorException extends Exception
	{
		private static final long serialVersionUID = -8341884928282479799L;

		private InstanceSubProcessorException(Throwable cause)
		{
			super(cause);
		}

	}

	private static TokenSubProcessor<?, ?> instanceSubProcessor(TokenProcessor processor, Production production) throws InstanceSubProcessorException
	{
		Class<? extends TokenSubProcessor<?, ?>> class_ = subProcessorClassMap.get(new SubProcessorClassKey(production));
		if (class_ == null)
			return null;
		try
		{
			Constructor<? extends TokenSubProcessor<?, ?>> constructor = class_.getDeclaredConstructor(TokenProcessor.class);
			return constructor.newInstance(processor);
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			throw new Error(e);
		}
	}

	private final Map<Production, TokenSubProcessor<?, ?>> subProcessors;

	public TokenProcessor(Grammar grammar)
	{
		subProcessors = new HashMap<>();
		try
		{
			for (Production p : grammar.productions())
			{
				TokenSubProcessor<?, ?> processor = instanceSubProcessor(this, p);
				if (processor != null)
					subProcessors.put(p, processor);
			}
		}
		catch (InstanceSubProcessorException e)
		{
			throw new Error(e);
		}
	}

	private <S extends TokenSubProcessor<?, ?>> S getProcessor(Class<S> subProcessorClass, Production production)
	{
		TokenSubProcessor<?, ?> subProcessor = subProcessors.get(production);
		if (!subProcessorClass.isInstance(subProcessor))
			return null;
		return subProcessorClass.cast(subProcessor);
	}

	public ParameterIdentification process(NonTerminalToken token) throws TokenProcessorException
	{
		return processParameterIdentification(token);
	}

	protected ParameterIdentification processParameterIdentification(NonTerminalToken token) throws TokenProcessorException
	{
		ParameterIdentificationTokenSubProcessor processor = getProcessor(ParameterIdentificationTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No ParameterIdentificationTokenSubProcessor found for production: " + token.getProduction());
		return processor.subProcess(token);
	}

	protected Identifier processIdentifier(NonTerminalToken token) throws TokenProcessorException
	{
		IdentifierTokenSubProcessor processor = getProcessor(IdentifierTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No IdentifierTokenSubProcessor found for production: " + token.getProduction());
		return processor.subProcess(token);
	}

	protected static class ParameterWithType
	{
		private final Identifier parameter;
		private final ParameterIdentification parameterType;

		protected ParameterWithType(Identifier parameter, ParameterIdentification parameterType)
		{
			super();
			this.parameter = parameter;
			this.parameterType = parameterType;
		}

		public Identifier getParameter()
		{
			return parameter;
		}

		public ParameterIdentification getParameterType()
		{
			return parameterType;
		}

	}

	protected ParameterWithType processParameterWithType(NonTerminalToken token) throws TokenProcessorException
	{
		ParameterWithTypeTokenSubProcessor processor = getProcessor(ParameterWithTypeTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No ParameterWithTypeTokenSubProcessor found for production: " + token.getProduction());
		return processor.subProcess(token);
	}

	protected static class ParameterWithTypeList extends ArrayList<ParameterWithType>
	{
		private static final long serialVersionUID = -6060639962653418191L;

	}

	protected ParameterWithTypeList processParameterWithTypeList(NonTerminalToken token) throws TokenProcessorException
	{
		ParameterWithTypeListTokenSubProcessor processor = getProcessor(ParameterWithTypeListTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No ParameterWithTypeListTokenSubProcessor found for production: " + token.getProduction());
		return processor.subProcess(token);
	}

}
