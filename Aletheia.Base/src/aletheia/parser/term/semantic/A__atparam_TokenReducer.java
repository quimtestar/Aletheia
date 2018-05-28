package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.parameterRef.NumberedParameterRef;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "atparam" })
public class A__atparam_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		String atParam = TaggedTerminalToken.getTextFromTokenList(reducees, 0);
		VariableTerm variable = resolveReference(globals, antecedents, new NumberedParameterRef(atParam));
		if (variable == null)
			throw new SemanticException(reducees.get(0), "Parameter:" + "'" + atParam + "'" + " not defined");
		return variable;
	}

}