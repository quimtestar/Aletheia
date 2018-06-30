package aletheia.model.term;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;

public class FoldingCastTypeTerm extends CastTypeTerm
{
	private static final long serialVersionUID = -9215582833035602649L;
	private final static int hashPrime = 2964509;

	private final IdentifiableVariableTerm variable;
	private final Term value;

	public static class FoldingCastTypeException extends CastTypeException
	{
		private static final long serialVersionUID = 6533505214214035548L;

		protected FoldingCastTypeException()
		{
			super();
		}

		protected FoldingCastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected FoldingCastTypeException(String message)
		{
			super(message);
		}

		protected FoldingCastTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	public FoldingCastTypeTerm(Term term, Term type, IdentifiableVariableTerm variable, Term value) throws FoldingCastTypeException
	{
		super(type, term);
		this.variable = variable;
		this.value = value;
		try
		{
			if (!type.replace(variable, value).equals(term.getType()))
				throw new FoldingCastTypeException();
		}
		catch (ReplaceTypeException e)
		{
			throw new FoldingCastTypeException(e);
		}
	}

	public IdentifiableVariableTerm getVariable()
	{
		return variable;
	}

	public Term getValue()
	{
		return value;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + variable.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + value.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof FoldingCastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		FoldingCastTypeTerm castTypeTerm = (FoldingCastTypeTerm) obj;
		if (!variable.equals(castTypeTerm.variable))
			return false;
		if (!value.equals(castTypeTerm.value))
			return false;
		return true;
	}

	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return new FoldingCastTypeTerm(getTerm().replace(replaces, exclude), getType().replace(replaces, exclude), variable, value);
		}
		catch (FoldingCastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		try
		{
			return new FoldingCastTypeTerm(getTerm().replace(replaces), getType().replace(replaces), variable, value);
		}
		catch (FoldingCastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}

	}

	@Override
	protected void stringAppend(StringAppender stringAppender, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		stringAppender.append("(");
		getTerm().stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.append(":");
		getType().stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.append(" | ");
		value.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.append(" <- ");
		variable.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.append(")");
	}

	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		super.freeVariables(freeVars, localVars);
		if (!localVars.contains(variable))
			freeVars.add(variable);

	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return super.isFreeVariable(variable) || this.variable.equals(variable);
	}

}
