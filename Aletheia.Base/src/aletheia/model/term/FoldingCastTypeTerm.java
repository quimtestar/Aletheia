package aletheia.model.term;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;

public class FoldingCastTypeTerm extends CastTypeTerm
{
	private static final long serialVersionUID = -9215582833035602649L;
	private final static int hashPrime = 2964509;

	private final Term value;
	private final IdentifiableVariableTerm variable;

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

	private static Term computeType(Term term, Term value, IdentifiableVariableTerm variable) throws FoldingCastTypeException
	{
		Term oldType = term.getType();
		Term.Match match = value.match(value.parameters(), oldType);
		if (match == null)
			throw new FoldingCastTypeException("No match");
		Term type = variable;
		for (ParameterVariableTerm parameter : value.parameters())
			try
			{
				type = type.compose(match.getAssignMapLeft().get(parameter));
			}
			catch (ComposeTypeException e)
			{
				throw new FoldingCastTypeException(e);
			}
		return type;
	}

	public FoldingCastTypeTerm(Term term, Term value, IdentifiableVariableTerm variable) throws FoldingCastTypeException
	{
		super(computeType(term, value, variable), term);
		this.value = value;
		this.variable = variable;
	}

	public Term getValue()
	{
		return value;
	}

	public IdentifiableVariableTerm getVariable()
	{
		return variable;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + value.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + variable.hashCode(hasher *= hashPrime);
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
		if (!value.equals(castTypeTerm.value))
			return false;
		if (!variable.equals(castTypeTerm.variable))
			return false;
		return true;
	}

	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return new FoldingCastTypeTerm(getTerm().replace(replaces, exclude), value, variable);
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
			return new FoldingCastTypeTerm(getTerm().replace(replaces), value, variable);
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
		getTerm().stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.append(" | ");
		value.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.append(" <- ");
		variable.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
	}

	// TODO: Still doesn't work 
	public static Term castToTargetType(Term term, Term targetType, Term value, IdentifiableVariableTerm variable) throws CastTypeException
	{
		Term type = term.getType();
		if (type.equals(targetType))
			return term;
		if (targetType.equals(variable))
			return new FoldingCastTypeTerm(term, value, variable);
		if (term instanceof FunctionTerm)
		{
			try
			{
				ParameterVariableTerm parameter = ((FunctionTerm) term).getParameter();
				Term body = ((FunctionTerm) term).getBody();
				ParameterVariableTerm parameter_ = new ParameterVariableTerm(castToTargetType(parameter.getType(), targetType.domain(), value, variable));
				Term body_ = castToTargetType(body.replace(parameter, parameter_), targetType.compose(parameter_), value, variable);
				return new FunctionTerm(parameter_, body_);
			}
			catch (DomainTypeException | ReplaceTypeException | ComposeTypeException e)
			{
				throw new CastTypeException(e);
			}
		}
		else if (term instanceof CompositionTerm)
		{
			try
			{
				SimpleTerm head = ((CompositionTerm) term).getHead();
				Term tail = ((CompositionTerm) term).getTail();
				Term head_ = castToTargetType(head, new FunctionTerm(new ParameterVariableTerm(tail.getType()), targetType), value, variable);
				return head_.compose(tail);
			}
			catch (ComposeTypeException e)
			{
				throw new CastTypeException(e);
			}
		}
		else if (term instanceof VariableTerm)
			return term;
		else
			throw new CastTypeException();

	}

}
