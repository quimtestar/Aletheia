package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "R", right =
{ "R_t", "openpar", "S_ps", "closepar" })
public class R__Rt_openpar_Sps_closepar_TokenReducer extends ProductionTokenPayloadReducer<Term>
{
	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		for (IdentifiableVariableTerm v : term.freeIdentifiableVariables())
		{
			if (globals.getContext() == null)
				throw new SemanticException(reducees.get(2), "Referenced term contains free variables");
			else if (!globals.getContext().statements(globals.getTransaction()).containsKey(v))
			{
				throw new SemanticException(reducees.get(2), "Referenced term contains free variables not of this context");
			}
		}
		return term;
	}

}