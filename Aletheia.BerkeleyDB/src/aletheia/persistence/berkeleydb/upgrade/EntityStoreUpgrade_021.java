/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
package aletheia.persistence.berkeleydb.upgrade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.authority.StatementAuthority.DependentUnpackedSignatureRequests;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.AtomicTerm;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.CompositionTerm.CompositionTypeException;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.TauTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.utilities.SecurityUtilities;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CombinedMap;

public class EntityStoreUpgrade_021 extends EntityStoreUpgrade
{
	private static final Logger logger = LoggerManager.instance.logger();

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(21);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void upgrade() throws UpgradeException
		{
			justPostProcessingUpgrade();
		}

		private Term translateTerm(Map<UUID, UUID> uuidMap, Term term)
		{
			try
			{
				return translateTerm(new HashMap<ParameterVariableTerm, ParameterVariableTerm>(), uuidMap, term);
			}
			catch (Exception e)
			{
				throw e;
			}
		}

		private Term translateTerm(Map<ParameterVariableTerm, ParameterVariableTerm> paramMap, Map<UUID, UUID> uuidMap, Term term)
		{
			try
			{
				if (term instanceof FunctionTerm)
				{
					FunctionTerm fTerm = (FunctionTerm) term;
					ParameterVariableTerm par = new ParameterVariableTerm(translateTerm(paramMap, uuidMap, fTerm.getParameter().getType()));
					ParameterVariableTerm oldPar = paramMap.put(fTerm.getParameter(), par);
					Term body = translateTerm(paramMap, uuidMap, fTerm.getBody());
					if (oldPar == null)
						paramMap.remove(fTerm.getParameter());
					else
						paramMap.put(fTerm.getParameter(), oldPar);
					return new FunctionTerm(par, body);
				}
				else if (term instanceof SimpleTerm)
				{
					if (term instanceof AtomicTerm)
					{
						if (term instanceof ProjectionTerm)
						{
							ProjectionTerm pTerm = (ProjectionTerm) term;
							return new ProjectionTerm((FunctionTerm) translateTerm(paramMap, uuidMap, pTerm.getFunction()));
						}
						else if (term instanceof TauTerm)
						{
							return TauTerm.instance;
						}
						else if (term instanceof VariableTerm)
						{
							if (term instanceof IdentifiableVariableTerm)
							{
								IdentifiableVariableTerm iTerm = (IdentifiableVariableTerm) term;
								return new IdentifiableVariableTerm(translateTerm(paramMap, uuidMap, iTerm.getType()), uuidMap.get(iTerm.getUuid()));
							}
							else if (term instanceof ParameterVariableTerm)
							{
								ParameterVariableTerm pTerm = (ParameterVariableTerm) term;
								return paramMap.get(pTerm);
							}
							else
								throw new Error();

						}
						else
							throw new Error();

					}
					else if (term instanceof CompositionTerm)
					{
						CompositionTerm cTerm = (CompositionTerm) term;
						SimpleTerm headD = (SimpleTerm) translateTerm(paramMap, uuidMap, cTerm.getHead());
						Term tailD = translateTerm(paramMap, uuidMap, cTerm.getTail());
						return new CompositionTerm(headD, tailD);
					}
					else
						throw new Error();
				}
				else
					throw new Error();
			}
			catch (NullParameterTypeException | ProjectionTypeException | CompositionTypeException e)
			{
				throw new RuntimeException(e);
			}
		}

		private UUID translateUuid(UUID uuid)
		{
			class UUIDGenerationProtocol extends Protocol<UUID>
			{
				final StringProtocol stringProtocol = new StringProtocol(0);
				final UUIDProtocol uuidProtocol = new UUIDProtocol(0);

				public UUIDGenerationProtocol()
				{
					super(0);
				}

				@Override
				public void send(DataOutput out, UUID uuid) throws IOException
				{
					stringProtocol.send(out, "EntityStoreUpgrade_021");
					uuidProtocol.send(out, uuid);
				}

				@Override
				public UUID recv(DataInput in) throws IOException, ProtocolException
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
					throw new UnsupportedOperationException();
				}

			}

			return SecurityUtilities.instance.objectToUUID(uuid, new UUIDGenerationProtocol());
		}

		private void copyStatementExtras(Transaction transaction, Statement stO, Statement stD)
		{
			try
			{
				StatementAuthority stAuthO = stO.getAuthority(transaction);
				if (stAuthO != null)
					stD.createAuthorityOverwrite(transaction, stAuthO.getAuthor(transaction), stAuthO.getCreationDate());
				else
				{
					StatementAuthority stAuthD = stD.getAuthority(transaction);
					if (stAuthD != null)
					{
						for (UnpackedSignatureRequest usr : stAuthD.dependentUnpackedSignatureRequests(transaction))
							usr.delete(transaction);
					}
					stD.deleteAuthorityForce(transaction);
				}

			}
			catch (AuthorityWithNoParentException | DependentUnpackedSignatureRequests e)
			{
				throw new RuntimeException(e);
			}

			try
			{
				StatementLocal stLocO = stO.getLocal(transaction);
				if (stLocO != null)
				{
					StatementLocal stLocD = stD.getOrCreateLocal(transaction);
					stLocD.setSubscribeProof(transaction, stLocO.isSubscribeProof());
					if ((stLocO instanceof ContextLocal) && (stLocD instanceof ContextLocal))
					{
						ContextLocal ctxLocO = (ContextLocal) stLocO;
						ContextLocal ctxLocD = (ContextLocal) stLocD;
						ctxLocD.setSubscribeStatements(transaction, ctxLocO.isSubscribeStatements());
					}
				}
				else
					stD.deleteLocal(transaction);

			}
			finally
			{

			}

		}

		private void deleteRootContext(BerkeleyDBPersistenceManager persistenceManager, RootContext rootCtx)
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				int i = 0;
				for (UnpackedSignatureRequest unpackedSignatureRequest : rootCtx.unpackedSignatureRequestSetByPath(transaction))
					unpackedSignatureRequest.delete(transaction);
				Stack<Statement> stack = new Stack<>();
				stack.addAll(rootCtx.localStatements(transaction).values());
				while (!stack.isEmpty())
				{
					logger.trace("---> deleting context: " + rootCtx.getIdentifier() + ": " + stack.size() + ": " + i);
					Statement st = stack.peek();
					if (persistenceManager.statements(transaction).containsKey(st.getVariable()))
					{
						boolean pushed = false;
						if (st instanceof Context)
						{
							Collection<Statement> locals = ((Context) st).localStatements(transaction).values();
							if (!locals.isEmpty())
							{
								stack.addAll(locals);
								pushed = true;
							}
						}
						Set<Statement> dependents = st.dependents(transaction);
						if (!dependents.isEmpty())
						{
							stack.addAll(dependents);
							pushed = true;
						}
						if (!pushed)
						{
							stack.pop();
							st.deleteLocal(transaction);
							StatementAuthority stAuth = st.getAuthority(transaction);
							if (stAuth != null)
								stAuth.deleteNoCheckSignedProof(transaction);
							persistenceManager.deleteStatement(transaction, st);
							i++;
							if (i % 1000 == 0)
							{
								transaction.commit();
								transaction = persistenceManager.beginTransaction();
							}
						}
					}
					else
						stack.pop();
				}
				rootCtx.deleteAuthorityForce(transaction);
				rootCtx.deleteLocal(transaction);
				persistenceManager.deleteStatement(transaction, rootCtx);
				transaction.commit();
			}
			catch (DependentUnpackedSignatureRequests e)
			{
				throw new Error(e);
			}
			finally
			{
				transaction.abort();
			}
		}

		@Override
		protected void postProcessing(BerkeleyDBPersistenceManager persistenceManager)
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{

				Map<UUID, UUID> rootCtxUuidMap;
				{
					rootCtxUuidMap = new HashMap<>();
					Map<UUID, UUID> revmap = new HashMap<>();
					for (RootContext rootCtx : persistenceManager.rootContexts(transaction).values())
					{
						UUID newUuid = translateUuid(rootCtx.getUuid());
						revmap.remove(rootCtxUuidMap.remove(newUuid));
						if (!revmap.containsKey(rootCtx.getUuid()))
						{
							rootCtxUuidMap.put(rootCtx.getUuid(), newUuid);
							revmap.put(newUuid, rootCtx.getUuid());
						}
					}
				}

				for (Map.Entry<UUID, UUID> e0 : rootCtxUuidMap.entrySet())
				{
					RootContext rootCtx = persistenceManager.getRootContext(transaction, e0.getKey());
					RootContext newRootCtx = persistenceManager.getRootContext(transaction, e0.getValue());
					if (newRootCtx == null)
					{
						newRootCtx = RootContext.create(persistenceManager, transaction, translateUuid(rootCtx.getUuid()), rootCtx.getTerm());
						newRootCtx.identify(transaction, new Identifier(rootCtx.getIdentifier(), "generating_EntityStoreUpgrade_021"));
					}
					copyStatementExtras(transaction, rootCtx, newRootCtx);

					class StackEntry
					{
						final Context ctxO;
						final Context ctxD;
						final Map<UUID, UUID> uuidMap;

						public StackEntry(Context ctxO, Context ctxD, Map<UUID, UUID> uuidMap)
						{
							super();
							this.ctxO = ctxO;
							this.ctxD = ctxD;
							this.uuidMap = uuidMap;
						}
					}
					;

					Stack<StackEntry> stack = new Stack<>();
					stack.push(new StackEntry(rootCtx, newRootCtx, Collections.singletonMap(rootCtx.getUuid(), newRootCtx.getUuid())));
					int i = 0;
					while (!stack.isEmpty())
					{
						StackEntry se = stack.pop();
						Map<UUID, UUID> frontUuidMap = new HashMap<>();
						Map<UUID, UUID> uuidMap = new CombinedMap<>(frontUuidMap, se.uuidMap);
						for (Statement stO : new BufferedList<>(se.ctxO.localDependencySortedStatements(transaction)))
						{
							logger.trace("---> transforming " + rootCtx.getIdentifier() + ": " + i);
							UUID uuidD = translateUuid(stO.getUuid());
							Statement stD = persistenceManager.getStatement(transaction, uuidD);
							if (stD == null)
							{
								if (stO instanceof Assumption)
								{
									Assumption asO = (Assumption) stO;
									stD = se.ctxD.assumptions(transaction).get(asO.getOrder());
								}
								else if (stO instanceof Specialization)
								{
									Specialization spcO = (Specialization) stO;
									Statement genD = persistenceManager.getStatement(transaction, uuidMap.get(spcO.getGeneralUuid()));
									Term instanceD = translateTerm(uuidMap, spcO.getInstance());
									stD = se.ctxD.specialize(transaction, uuidD, genD, instanceD);

								}
								else if (stO instanceof Context)
								{
									Context ctxO = (Context) stO;
									Term termD = translateTerm(uuidMap, ctxO.getTerm());
									if (stO instanceof UnfoldingContext)
									{
										UnfoldingContext unfO = (UnfoldingContext) stO;
										Declaration decD = persistenceManager.getDeclaration(transaction, uuidMap.get(unfO.getDeclarationUuid()));
										stD = se.ctxD.openUnfoldingSubContext(transaction, uuidD, termD, decD);
									}
									else if (stO instanceof Declaration)
									{
										Declaration decO = (Declaration) stO;
										Term valD = translateTerm(uuidMap, decO.getValue());
										stD = se.ctxD.declare(transaction, uuidD, valD);
									}
									else if (stO instanceof RootContext)
										throw new RuntimeException();
									else
									{
										stD = se.ctxD.openSubContext(transaction, uuidD, termD);
									}
								}
								else
									throw new Error();
							}
							if (stO instanceof Context)
								stack.push(new StackEntry((Context) stO, (Context) stD, uuidMap));
							frontUuidMap.put(stO.getUuid(), stD.getUuid());
							if (stO.getIdentifier() != null && !stO.getIdentifier().equals(stD.getIdentifier()))
							{
								stD.unidentify(transaction);
								Nomenclator nomenclator = stD.getParentNomenclator(transaction);
								if (nomenclator.isLocalIdentifier(stO.getIdentifier()))
									nomenclator.unidentifyStatement(stO.getIdentifier(), true);
								stD.identify(transaction, stO.getIdentifier());
							}
							copyStatementExtras(transaction, stO, stD);
							i++;
							if (i % 1000 == 0)
							{
								transaction.commit();
								transaction = persistenceManager.beginTransaction();
							}
						}
					}
					transaction.commit();
					transaction = persistenceManager.beginTransaction();
					deleteRootContext(persistenceManager, rootCtx);
					newRootCtx.unidentify(transaction, true);
					newRootCtx.identify(transaction, rootCtx.getIdentifier());
				}
				transaction.commit();
			}
			catch (StatementException | NomenclatorException | InvalidNameException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				transaction.abort();
			}

		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
