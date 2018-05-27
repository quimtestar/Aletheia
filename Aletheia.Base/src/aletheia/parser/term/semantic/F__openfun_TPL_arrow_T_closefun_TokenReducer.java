package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.FunctionTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefList;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefWithValue;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.utilities.collections.ReverseList;

@AssociatedProduction(left = "F", right =
{ "openfun", "TPL", "arrow", "T", "closefun" })
public class F__openfun_TPL_arrow_T_closefun_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		TypedParameterRefList typedParameterRefList = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 3);
		for (TypedParameterRef typedParameterRef : new ReverseList<>(typedParameterRefList.list()))
		{
			term = new FunctionTerm(typedParameterRef.getParameter(), term);
			if (typedParameterRef instanceof TypedParameterRefWithValue)
				try
				{
					term = term.compose(((TypedParameterRefWithValue) typedParameterRef).getValue());
				}
				catch (ComposeTypeException e)
				{
					throw new SemanticException(reducees, e);
				}
		}
		return term;
	}

}