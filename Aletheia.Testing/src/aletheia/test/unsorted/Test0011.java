package aletheia.test.unsorted;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.gui.cli.command.authority.RevokeSignatures;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BijectionKeyMap;
import aletheia.utilities.collections.BijectionMap;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.InverseBijection;

public class Test0011 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public Test0011()
	{
		super(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		//enterPassphrase(persistenceManager);

		Bijection<UUID, Statement> uuidBijection = new Bijection<UUID, Statement>()
		{

			@Override
			public Statement forward(UUID uuid)
			{
				return uuid != null ? persistenceManager.getStatement(transaction, uuid) : null;
			}

			@Override
			public UUID backward(Statement statement)
			{
				return statement != null ? statement.getUuid() : null;
			}

		};
		Map<UUID, UUID> uuidStatementMap = new HashMap<>();
		Map<Statement, Statement> statementMap = new BijectionKeyMap<>(uuidBijection,
				new BijectionMap<>(uuidBijection, uuidStatementMap));
		{
			Map<String, String> stringMap = new HashMap<>();
			stringMap.put("Aletheia.Term.freeVars", "Aletheia.Term.freeVarsNew");
			stringMap.put("Aletheia.Term.freeVars.def", "Aletheia.Term.freeVarsNew.th.def.old");
			stringMap.put("Aletheia.Term.freeVars.th.tau", "Aletheia.Term.freeVarsNew.th.tau");
			stringMap.put("Aletheia.Term.freeVars.th.variable", "Aletheia.Term.freeVarsNew.th.variable");
			stringMap.put("Aletheia.Term.freeVars.th.composition", "Aletheia.Term.freeVarsNew.th.composition");
			stringMap.put("Aletheia.Term.freeVars.th.function", "Aletheia.Term.freeVarsNew.th.function.subtraction_singleton");
			stringMap.put("Aletheia.Term.freeVars.th.projection", "Aletheia.Term.freeVarsNew.th.projection");
			Context choiceCtx = persistenceManager.getContext(transaction, UUID.fromString("75130b32-91fa-5da5-af6c-744cb4463f64"));
			for (Map.Entry<String, String> e : stringMap.entrySet())
			{
				Statement k = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getKey()));
				Statement v = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getValue()));
				statementMap.put(k, v);
			}
		}

		Set<UUID> uuidDependents = new HashSet<>();
		Set<Statement> dependents = new BijectionSet<>(uuidBijection, uuidDependents);
		{
			Stack<UUID> stack = new Stack<>();
			stack.addAll(uuidStatementMap.keySet());
			while (!stack.isEmpty())
			{
				System.out.println("loading: " + stack.size() + ": " + dependents.size());
				UUID uuid = stack.pop();
				if (uuidDependents.add(uuid))
				{
					Statement st = persistenceManager.getStatement(transaction, uuid);
					if (st.isValidSignature(transaction))
					{
						RevokeSignatures rs = new RevokeSignatures(AletheiaCliConsole.cliConsole(persistenceManager), transaction,
								st.getAuthority(transaction).validSignatureMap(transaction).values());
						Method runMethod = RevokeSignatures.class.getDeclaredMethod("runTransactional");
						runMethod.setAccessible(true);
						runMethod.invoke(rs);
					}
					stack.addAll(new BijectionCollection<>(new InverseBijection<>(uuidBijection), st.dependents(transaction)));
					if (st instanceof Context)
						stack.addAll(new BijectionCollection<>(new InverseBijection<>(uuidBijection),
								((Context) st).localStatements(transaction).values()));
				}
			}
			System.out.println("loading: " + stack.size() + ": " + dependents.size());
		}

		for (Statement st : statementMap.keySet())
			if (st instanceof Context)
				for (Statement d : ((Context) st).descendentStatements(transaction))
					dependents.remove(d);

		Bijection<Statement, IdentifiableVariableTerm> statementBijection = new Bijection<Statement, IdentifiableVariableTerm>()
		{

			@Override
			public IdentifiableVariableTerm forward(Statement statement)
			{
				if (statement == null)
					return null;
				else
					return statement.getVariable();
			}

			@Override
			public Statement backward(IdentifiableVariableTerm variable)
			{
				if (variable == null)
					return null;
				else
					return persistenceManager.getStatement(transaction, variable.getUuid());
			}
		};
		Map<VariableTerm, Term> variableMap = new AdaptedMap<>(new BijectionKeyMap<>(
				statementBijection, new BijectionMap<>(statementBijection, statementMap)));

		Stack<UUID> stack = new Stack<>();
		stack.addAll(uuidDependents);
		loop: while (!stack.isEmpty())
		{
			System.out.println("unloading: " + stack.size() + ": " + statementMap.size());
			UUID oldUuid = stack.peek();
			if (!uuidStatementMap.containsKey(oldUuid))
			{
				Statement oldSt = persistenceManager.getStatement(transaction, oldUuid);
				Context oldCtx = oldSt.getContext(transaction);
				Context newCtx;
				if (dependents.contains(oldCtx))
				{
					newCtx = (Context) statementMap.get(oldCtx);
					if (newCtx == null)
					{
						stack.push(oldCtx.getUuid());
						continue loop;
					}
				}
				else
					newCtx = oldCtx;
				for (Statement oldDep : oldSt.dependencies(transaction))
				{
					if (dependents.contains(oldDep) && !statementMap.containsKey(oldDep))
					{
						stack.push(oldDep.getUuid());
						continue loop;
					}
				}

				Identifier newId = oldSt.unidentify(transaction);
				if (newId != null)
				{
					Identifier oldId = new Identifier(newId, "old_test0011");
					oldSt.identify(transaction, oldId);
				}

				Statement newSt;
				if (oldSt instanceof Assumption)
					newSt = newCtx.assumptions(transaction).get(((Assumption) oldSt).getOrder());
				else if (oldSt instanceof Context)
				{
					Term term = oldSt.getTerm();
					if (oldSt instanceof UnfoldingContext)
					{
						Declaration declaration = ((UnfoldingContext) oldSt).getDeclaration(transaction);
						newSt = newCtx.openUnfoldingSubContext(transaction, term.replace(variableMap),
								(Declaration) statementMap.getOrDefault(declaration, declaration));
					}
					else if (oldSt instanceof RootContext)
						throw new RuntimeException();
					else
						newSt = newCtx.openSubContext(transaction, term.replace(variableMap));
				}
				else if (oldSt instanceof Declaration)
				{
					Term value = ((Declaration) oldSt).getValue();
					Statement valueProof = ((Declaration) oldSt).getValueProof(transaction);
					newSt = newCtx.declare(transaction, value.replace(variableMap), statementMap.getOrDefault(valueProof, valueProof));
				}
				else if (oldSt instanceof Specialization)
				{
					Statement general = ((Specialization) oldSt).getGeneral(transaction);
					Term instance = ((Specialization) oldSt).getInstance();
					Statement instanceProof = ((Specialization) oldSt).getInstanceProof(transaction);
					newSt = newCtx.specialize(transaction, statementMap.getOrDefault(general, general), instance.replace(variableMap),
							statementMap.getOrDefault(instanceProof, instanceProof));
				}
				else
					throw new Error();

				if (newId != null)
					newSt.identify(transaction, newId);
				statementMap.put(oldSt, newSt);
				stack.pop();
			}
			else
				stack.pop();
		}
		System.out.println("done!");

	}

}
