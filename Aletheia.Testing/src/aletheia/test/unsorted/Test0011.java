package aletheia.test.unsorted;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class Test0011 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public Test0011()
	{
		super(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		Map<String, String> stringMap = new HashMap<>();
		stringMap.put("Aletheia.Term.freeVars", "Aletheia.Term.freeVarsNew");
		stringMap.put("Aletheia.Term.freeVars.def", "Aletheia.Term.freeVarsNew.th.def.old");
		stringMap.put("Aletheia.Term.freeVars.th.tau", "Aletheia.Term.freeVarsNew.th.tau");
		stringMap.put("Aletheia.Term.freeVars.th.variable", "Aletheia.Term.freeVarsNew.th.tau");
		stringMap.put("Aletheia.Term.freeVars.th.composition", "Aletheia.Term.freeVarsNew.th.tau");
		stringMap.put("Aletheia.Term.freeVars.th.function", "Aletheia.Term.freeVarsNew.th.function.subtraction_singleton");
		stringMap.put("Aletheia.Term.freeVars.th.projection", "Aletheia.Term.freeVarsNew.th.projection");

		Context choiceCtx = persistenceManager.getContext(transaction, UUID.fromString("75130b32-91fa-5da5-af6c-744cb4463f64"));

		Map<Statement, Statement> statementMap = new HashMap<>();
		for (Map.Entry<String, String> e : stringMap.entrySet())
		{
			Statement k = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getKey()));
			Statement v = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getValue()));
			statementMap.put(k, v);
		}

		Set<Statement> pending = new HashSet<>();
		{
			Stack<Statement> stack = new Stack<>();
			stack.addAll(statementMap.keySet());
			while (!stack.isEmpty())
			{
				System.out.println(stack.size());
				Statement st = stack.pop();
				if (pending.add(st))
				{
					stack.addAll(st.dependents(transaction));
					if ((st instanceof Context) && !statementMap.containsKey(st))
						stack.addAll(((Context) st).localStatements(transaction).values());
				}
			}
		}
		System.out.println(pending.size());

	}

}
