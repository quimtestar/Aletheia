/*******************************************************************************
 * Copyright (c) 2018, 2023 Quim Testar
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
import aletheia.model.statement.Specialization.BadInstanceException;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
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

public class ReplacementTest0002 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ReplacementTest0002()
	{
		super();
		setReadOnly(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
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
		{
			Map<String, String> stringMap = new HashMap<>();

			stringMap.put("Real.Sequence.Convergent.old", "Real.Sequence.Convergent");
			stringMap.put("Real.Sequence.Convergent.old.def", "Real.Sequence.Convergent.def.bad");
			stringMap.put("Real.Sequence.Convergent.old.th", "Real.Sequence.Convergent.th");
			stringMap.put("Real.Sequence.Convergent.old.th.absolute", "Real.Sequence.Convergent.th.absolute");
			stringMap.put("Real.Sequence.Convergent.old.th.constant", "Real.Sequence.Convergent.th.constant");
			stringMap.put("Real.Sequence.Convergent.old.th.displace", "Real.Sequence.Convergent.th.displace");
			stringMap.put("Real.Sequence.Convergent.old.th.displace.inv", "Real.Sequence.Convergent.th.displace.inv");
			stringMap.put("Real.Sequence.Convergent.old.th.harmonic", "Real.Sequence.Convergent.th.harmonic");
			stringMap.put("Real.Sequence.Convergent.old.th.identical", "Real.Sequence.Convergent.th.identical");
			stringMap.put("Real.Sequence.Convergent.old.th.identical.tail", "Real.Sequence.Convergent.th.identical.tail");
			stringMap.put("Real.Sequence.Convergent.old.th.increasing.bounded", "Real.Sequence.Convergent.th.increasing.bounded");
			stringMap.put("Real.Sequence.Convergent.old.th.inv", "Real.Sequence.Convergent.th.inv");
			stringMap.put("Real.Sequence.Convergent.old.th.inverse", "Real.Sequence.Convergent.th.inverse");
			stringMap.put("Real.Sequence.Convergent.old.th.inverse.unbounded", "Real.Sequence.Convergent.th.inverse.unbounded");
			stringMap.put("Real.Sequence.Convergent.old.th.inverse", "Real.Sequence.Convergent.th.inverse");
			stringMap.put("Real.Sequence.Convergent.old.th.opposite", "Real.Sequence.Convergent.th.opposite");
			stringMap.put("Real.Sequence.Convergent.old.th.product", "Real.Sequence.Convergent.th.product");
			stringMap.put("Real.Sequence.Convergent.old.th.sum", "Real.Sequence.Convergent.th.sum");
			stringMap.put("Real.Sequence.Convergent.old.th.summation", "Real.Sequence.Convergent.th.summation");

			stringMap.put("Real.Sequence.limit.old", "Real.Sequence.limit");
			stringMap.put("Real.Sequence.limit.old.th.Equal.alt", "Real.Sequence.limit.th.Equal.alt");
			stringMap.put("Real.Sequence.limit.old.th.LesserOrEqual", "Real.Sequence.limit.th.LesserOrEqual");
			stringMap.put("Real.Sequence.limit.old.th.Real", "Real.Sequence.limit.th.Real");
			stringMap.put("Real.Sequence.limit.old.th.absolute", "Real.Sequence.limit.th.absolute");
			stringMap.put("Real.Sequence.limit.old.th.bounds", "Real.Sequence.limit.th.bounds.bad");
			stringMap.put("Real.Sequence.limit.old.th.bounds.lft", "Real.Sequence.limit.th.bounds.bad.lft");
			stringMap.put("Real.Sequence.limit.old.th.bounds.rgt", "Real.Sequence.limit.th.bounds.bad.rgt");
			stringMap.put("Real.Sequence.limit.old.th.constant", "Real.Sequence.limit.th.constant");
			stringMap.put("Real.Sequence.limit.old.th.displace", "Real.Sequence.limit.th.displace");
			stringMap.put("Real.Sequence.limit.old.th.harmonic", "Real.Sequence.limit.th.harmonic");
			stringMap.put("Real.Sequence.limit.old.th.identical", "Real.Sequence.limit.th.identical");
			stringMap.put("Real.Sequence.limit.old.th.identical.tail", "Real.Sequence.limit.th.identical.tail");
			stringMap.put("Real.Sequence.limit.old.th.increasing.Convergent.bounded", "Real.Sequence.limit.th.increasing.Convergent.bounded.bad");
			stringMap.put("Real.Sequence.limit.old.th.increasing.bounded.greater", "Real.Sequence.limit.th.increasing.bounded.greater.bad");
			stringMap.put("Real.Sequence.limit.old.th.increasing.bounded.smaller", "Real.Sequence.limit.th.increasing.bounded.smaller.bad");
			stringMap.put("Real.Sequence.limit.old.th.inferior", "Real.Sequence.limit.th.inferior.bad");
			stringMap.put("Real.Sequence.limit.old.th.infinity.Exists.Equal.floor", "Real.Sequence.limit.th.infinity.Exists.Equal.floor");
			stringMap.put("Real.Sequence.limit.old.th.inverse", "Real.Sequence.limit.th.inverse");
			stringMap.put("Real.Sequence.limit.old.th.inverse.unbounded", "Real.Sequence.limit.th.inverse.unbounded");
			stringMap.put("Real.Sequence.limit.old.th.opposite", "Real.Sequence.limit.th.opposite");
			stringMap.put("Real.Sequence.limit.old.th.product", "Real.Sequence.limit.th.product");
			stringMap.put("Real.Sequence.limit.old.th.sum", "Real.Sequence.limit.th.sum");
			stringMap.put("Real.Sequence.limit.old.th.summation", "Real.Sequence.limit.th.summation");
			stringMap.put("Real.Sequence.limit.old.th.superior", "Real.Sequence.limit.th.superior");

			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal", "Real.Sequence.Convergent.th.limit.Equal");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.absolute", "Real.Sequence.Convergent.th.limit.Equal.absolute");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.constant", "Real.Sequence.Convergent.th.limit.Equal.constant");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.displace", "Real.Sequence.Convergent.th.limit.Equal.displace");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.displace.inv", "Real.Sequence.Convergent.th.limit.Equal.displace.inv");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.displace.lft", "Real.Sequence.Convergent.th.limit.Equal.displace.lft");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.harmonic", "Real.Sequence.Convergent.th.limit.Equal.harmonic");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.identical", "Real.Sequence.Convergent.th.limit.Equal.identical");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.identical.tail", "Real.Sequence.Convergent.th.limit.Equal.identical.tail");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.inverse", "Real.Sequence.Convergent.th.limit.Equal.inverse");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.inverse.unbounded", "Real.Sequence.Convergent.th.limit.Equal.inverse.unbounded");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.opposite", "Real.Sequence.Convergent.th.limit.Equal.opposite");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.product", "Real.Sequence.Convergent.th.limit.Equal.product");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.rev", "Real.Sequence.Convergent.th.limit.Equal.rev");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.sum", "Real.Sequence.Convergent.th.limit.Equal.sum");
			stringMap.put("Real.Sequence.Convergent.old.th.limit.Equal.summation", "Real.Sequence.Convergent.th.limit.Equal.summation");

			Context choiceCtx = persistenceManager.getContext(transaction, UUID.fromString("42cc8199-8159-5567-b65c-db023f95eaa3"));
			for (Map.Entry<String, String> e : stringMap.entrySet())
			{
				Statement k = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getKey()));
				if (k == null)
					throw new Exception(e.toString());
				if (e.getValue() != null)
				{
					Statement v = choiceCtx.identifierToStatement(transaction).get(Identifier.parse(e.getValue()));
					if (v == null)
						throw new Exception(e.toString());
					statementMap.put(k, v);
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
					Identifier oldId = new Identifier(newId, "old_ReplacementTest0002");
					try
					{
						oldSt.identify(transaction, oldId);
					}
					catch (Exception e)
					{
						throw e;
					}
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
					Term instance = ((Specialization) oldSt).getInstance();
					Statement instanceProof = ((Specialization) oldSt).getInstanceProof(transaction);
					try
					{
						newSt = newCtx.specialize(transaction, newGeneral, instance.replace(variableMap),
								statementMap.getOrDefault(instanceProof, instanceProof));
					}
					catch (BadInstanceException e)
					{
						if (subsumed(newGeneral.getTerm(), oldSt.getTerm().replace(variableMap)))
						{
							newSt = newGeneral;
							newSt.unidentify(transaction);
						}
						else if (subsumed(oldGeneral.getTerm().replace(variableMap), newGeneral.getTerm()))
						{
							Term domain = newGeneral.getTerm().domain();
							Statement instanceProof_ = newCtx.suitableForInstanceProofStatementByTerm(transaction, domain, false);
							if (instanceProof_ == null)
							{
								instanceProof_ = newCtx.openSubContext(transaction, domain);
								if (newId != null)
									instanceProof_.identify(transaction, new Identifier(newId, "lc"));
							}
							oldSt = oldGeneral;
							newSt = newCtx.specialize(transaction, newGeneral, instanceProof_.getVariable(), instanceProof_);
							newId = new Identifier(newId, "lr");
							stack.push(oldUuid);
						}
						else
						{
							throw e;
						}
					}
					catch (Exception e)
					{
						throw e;
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

	private boolean subsumed(Term a, Term b) throws ReplaceTypeException
	{
		if (a.equals(b))
			return true;
		else if (b instanceof FunctionTerm)
		{
			FunctionTerm fb = (FunctionTerm) b;
			if (a instanceof FunctionTerm)
			{
				FunctionTerm fa = (FunctionTerm) a;
				if (!fa.domain().equals(fb.domain()))
					return subsumed(a, fb.getBody());
				else
					return subsumed(fa.getBody(), fb.getBody().replace(fb.getParameter(), fa.getParameter()));
			}
			else
				return subsumed(a, fb.getBody());
		}
		else
			return false;
	}

}
