/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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
package aletheia.test.replacement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.StatementAuthority;
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

public class ReplacementTest0003 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ReplacementTest0003()
	{
		super();
		setReadOnly(false);
	}

	@SuppressWarnings("unused")
	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		if (true)
			throw new Error("Unfinished");
		enterPassphrase(persistenceManager);

		Bijection<UUID, Statement> uuidBijection = new Bijection<>()
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
		Map<Statement, Statement> statementMap = new BijectionKeyMap<>(uuidBijection, new BijectionMap<>(uuidBijection, uuidStatementMap));
		Map<UUID, int[]> uuidInstanceOrderMap = new HashMap<>();
		Map<Statement, int[]> instanceOrderMap = new BijectionKeyMap<>(uuidBijection, uuidInstanceOrderMap);
		{
			Map<String, String> stringMap = new HashMap<>();
			Map<String, int[]> stringInstanceOrderMap = new HashMap<>();

			stringMap.put("Real.Function.Continuous.th.constant.tmp", "Real.Function.Continuous.th.constant");
			stringInstanceOrderMap.put("Real.Function.Continuous.th.constant", new int[]
			{ 0, 1, 2 });

			Context choiceCtx = persistenceManager.getContext(transaction, UUID.fromString("42cc8199-8159-5567-b65c-db023f95eaa3"));
			for (Map.Entry<String, String> e : stringMap.entrySet())
			{
				Statement k = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getKey()));
				if (e.getValue() != null)
				{
					Statement v = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getValue()));
					statementMap.put(k, v);
					if (v != null)
					{
						int[] instanceOrder = stringInstanceOrderMap.get(e.getKey());
						if (instanceOrder != null)
							instanceOrderMap.put(v, instanceOrder);
					}
				}
				else
					statementMap.put(k, null);
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
					Statement statement = persistenceManager.getStatement(transaction, uuid);
					StatementAuthority statementAuthority = statement.getAuthority(transaction);
					if (statementAuthority != null)
						statementAuthority.clearSignatures(transaction);
					stack.addAll(new BijectionCollection<>(new InverseBijection<>(uuidBijection), statement.dependents(transaction)));
					if (statement instanceof Context)
						stack.addAll(
								new BijectionCollection<>(new InverseBijection<>(uuidBijection), ((Context) statement).localStatements(transaction).values()));
				}
			}
			System.out.println("loading: " + stack.size() + ": " + dependents.size());
		}

		for (Statement st : statementMap.keySet())
			if (st instanceof Context)
				for (Statement d : ((Context) st).descendentStatements(transaction))
					dependents.remove(d);

		Bijection<Statement, IdentifiableVariableTerm> statementBijection = new Bijection<>()
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
		Map<VariableTerm, Term> variableMap = new AdaptedMap<>(new BijectionKeyMap<>(statementBijection, new BijectionMap<>(statementBijection, statementMap)));

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

				Statement newSt = null;
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
					Statement oldGeneral = ((Specialization) oldSt).getGeneral(transaction);
					Statement newGeneral = statementMap.getOrDefault(oldGeneral, oldGeneral);
					int[] instanceOrder = instanceOrderMap.get(newGeneral);
					int instancePos = instanceOrder == null || instanceOrder.length > 0 ? instanceOrder[0] : 0;
					Specialization spc = (Specialization) oldSt;
					for (; instancePos > 0; instancePos--)
					{
						spc = (Specialization) spc.getGeneral(transaction);
						instancePos--;
					}
					Term instance = spc.getInstance();
					Statement instanceProof = ((Specialization) oldSt).getInstanceProof(transaction);
					newSt = newCtx.specialize(transaction, newGeneral, instance.replace(variableMap), statementMap.getOrDefault(instanceProof, instanceProof));
					if (instanceOrder != null)
					{
						if (instancePos == 0)
						{
							if (instanceOrder.length > 0)
							{
								int[] newInstanceOrder = new int[instanceOrder.length - 1];
								for (int i = 0; i < newInstanceOrder.length; i++)
									newInstanceOrder[i] = instanceOrder[i + 1] - 1;
								instanceOrderMap.put(newSt, newInstanceOrder);
							}
						}
						else
						{
							if (instanceOrder.length > 0)
							{
								int[] newInstanceOrder = new int[instanceOrder.length - 1];
								for (int i = 0; i < newInstanceOrder.length; i++)
									newInstanceOrder[i] = instanceOrder[i + 1];
								instanceOrderMap.put(newSt, newInstanceOrder);
								oldSt = oldGeneral;
							}
							else
							{
								// UNFINISHED
							}
						}

					}
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
