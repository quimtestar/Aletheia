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
package aletheia.parser.term.tokenprocessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefList;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
import aletheia.persistence.Transaction;

public class TokenProcessor
{
	//@formatter:off
	private final static List<Class<? extends TokenSubProcessor<?,?>>> subProcessorClasses=Arrays.asList(
			T_T_B_TermTokenSubProcessor.class,
			T_B_TermTokenSubProcessor.class,
			B_B_bar_Q_TermTokenSubProcessor.class,
			B_Q_TermTokenSubProcessor.class,
			Q_C_question_C_TermTokenSubProcessor.class,
			Q_C_TermTokenSubProcessor.class,
			C_A_MP_TermTokenSubProcessor.class,
			A_ttype_TermTokenSubProcessor.class,
			A_I_TermTokenSubProcessor.class,
			A_atparam_TermTokenSubProcessor.class,
			A_F_TermTokenSubProcessor.class,
			A_U_TermTokenSubProcessor.class,
			A_R_TermTokenSubProcessor.class,
			A_hexref_TermTokenSubProcessor.class,
			A_A_tilde_TermTokenSubProcessor.class,
			A_A_percent_TermTokenSubProcessor.class,
			A_A_sharp_TermTokenSubProcessor.class,
			A_A_apostrophe_TermTokenSubProcessor.class,
			A_A_bang_I_TermTokenSubProcessor.class,
			A_equals_bang_I_TermTokenSubProcessor.class,
			A_openpar_T_closepar_TermTokenSubProcessor.class,
			A_opensq_T_closesq_TermTokenSubProcessor.class,
			A_opencur_T_closecur_TermTokenSubProcessor.class,
			A_A_number_M_TermTokenSubProcessor.class,
			A_A_semicolon_number_M_TermTokenSubProcessor.class,
			AN_asterisk_IntegerTokenSubProcessor.class,
			AN_asterisk_number_IntegerTokenSubProcessor.class,
			M_hyphen_BooleanTokenSubProcessor.class,
			M_BooleanTokenSubProcessor.class,
			F_openfun_TPL_arrow_T_closefun_TermTokenSubProcessor.class,
			TP_P_colon_T_ParameterRefTokenSubProcessor.class,
			TP_T_ParameterRefTokenSubProcessor.class,
			TP_P_assignment_T_ParameterRefTokenSubProcessor.class,
			TPL_ParameterRefListTokenSubProcessor.class,
			TPL_TP_ParameterRefListTokenSubProcessor.class,
			TPL_TPL_comma_TP_ParameterRefListTokenSubProcessor.class,
			P_I_ParameterRefTokenSubProcessor.class,
			P_atparam_ParameterRefTokenSubProcessor.class,
			I_I_dot_id_IdentifierTokenSubProcessor.class,
			I_id_IdentifierTokenSubProcessor.class,
			R_Rt_Sts_ReferenceTokenSubProcessor.class,
			R_Rt_openpar_Sps_closepar_ReferenceTokenSubProcessor.class,
			Rt_ampersand_ReferenceTokenSubProcessor.class,
			Rt_ampersand_caret_ReferenceTokenSubProcessor.class,
			Rt_ampersand_bang_ReferenceTokenSubProcessor.class,
			Sts_uuid_StatementReferenceTokenSubProcessor.class,
			Sts_St_StatementReferenceTokenSubProcessor.class,
			St_S_StatementReferenceTokenSubProcessor.class,
			St_turnstile_StatementReferenceTokenSubProcessor.class,
			Sps_bar_Sp_StatementReferenceTokenSubProcessor.class,
			Sps_Sp_StatementReferenceTokenSubProcessor.class,
			Sps_uuid_bar_Sp_StatementReferenceTokenSubProcessor.class,
			Sp_S_bar_Sp_StatementReferenceTokenSubProcessor.class,
			Sp_St_StatementReferenceTokenSubProcessor.class,
			S_I_StatementTokenSubProcessor.class,
			S_hexref_StatementTokenSubProcessor.class,
			MP_IntegerTokenSubProcessor.class,
			MP_MP_AN_IntegerTokenSubProcessor.class
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

	public Term process(NonTerminalToken token, Context context, Transaction transaction, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TokenProcessorException
	{
		return processTerm(token, context, transaction, new HashMap<>(), parameterIdentifiers);
	}

	protected Term processTerm(NonTerminalToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException
	{
		TermTokenSubProcessor processor = getProcessor(TermTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No TermTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction, tempParameterTable, parameterIdentifiers);
	}

	protected Term processTerm(NonTerminalToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable)
			throws TokenProcessorException
	{
		TermTokenSubProcessor processor = getProcessor(TermTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No TermTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction, tempParameterTable);
	}

	protected ParameterRef processParameterRef(NonTerminalToken token) throws TokenProcessorException
	{
		ParameterRefTokenSubProcessor processor = getProcessor(ParameterRefTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No ParameterRefTokenSubProcessor found for production.");
		return processor.subProcess(token);
	}

	protected TypedParameterRef processTypedParameterRef(NonTerminalToken token, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable) throws TokenProcessorException
	{
		TypedParameterRefTokenSubProcessor processor = getProcessor(TypedParameterRefTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No TypedParameterRefTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction, tempParameterTable);
	}

	protected TypedParameterRefList processTypedParameterRefList(NonTerminalToken token, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TokenProcessorException
	{
		TypedParameterRefListTokenSubProcessor processor = getProcessor(TypedParameterRefListTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No TypedParameterRefListTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction, tempParameterTable, parameterIdentifiers);
	}

	protected Identifier processIdentifier(NonTerminalToken token) throws TokenProcessorException
	{
		IdentifierTokenSubProcessor processor = getProcessor(IdentifierTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No IdentifierTokenSubProcessor found for production.");
		return processor.subProcess(token);
	}

	protected Term processReference(NonTerminalToken token, Context context, Transaction transaction) throws TokenProcessorException
	{
		ReferenceTokenSubProcessor processor = getProcessor(ReferenceTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No ReferenceTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction);
	}

	protected ReferenceType processReferenceType(NonTerminalToken token)
	{
		ReferenceTypeTokenSubProcessor processor = getProcessor(ReferenceTypeTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No ReferenceTypeTokenSubProcessor found for production.");
		return processor.subProcess(token);
	}

	protected UUID processUuid(TerminalToken token) throws TokenProcessorException
	{
		try
		{
			return UUID.fromString(((TaggedTerminalToken) token).getText());
		}
		catch (IllegalArgumentException e)
		{
			throw new TokenProcessorException("Bad UUID string", token.getStartLocation(), token.getStopLocation());
		}
	}

	protected Term processStatementReference(NonTerminalToken token, Context context, Transaction transaction, ReferenceType referenceType)
			throws TokenProcessorException
	{
		StatementReferenceTokenSubProcessor processor = getProcessor(StatementReferenceTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No StatementReferenceTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction, referenceType);
	}

	protected Statement processStatement(NonTerminalToken token, Context context, Transaction transaction) throws TokenProcessorException
	{
		StatementTokenSubProcessor processor = getProcessor(StatementTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No StatementTokenSubProcessor found for production.");
		return processor.subProcess(token, context, transaction);
	}

	protected boolean processBoolean(NonTerminalToken token) throws TokenProcessorException
	{
		BooleanTokenSubProcessor processor = getProcessor(BooleanTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No BooleanTokenSubProcessor found for production.");
		return processor.subProcess(token);
	}

	protected int processInteger(NonTerminalToken token) throws TokenProcessorException
	{
		IntegerTokenSubProcessor processor = getProcessor(IntegerTokenSubProcessor.class, token.getProduction());
		if (processor == null)
			throw new Error("No IntegerTokenSubProcessor found for production.");
		return processor.subProcess(token);
	}

}
