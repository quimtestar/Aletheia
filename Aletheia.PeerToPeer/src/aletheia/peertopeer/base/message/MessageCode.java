/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 * 
 * This file is part of the Aletheia Proof Assistant.
 * 
 * The Aletheia Proof Assistant is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * The Aletheia Proof Assistant is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.base.message;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aletheia.peertopeer.base.protocol.MessageSubProtocol;
import aletheia.peertopeer.conjugal.message.LoopConjugalDialogTypeAcknowledgeMessage;
import aletheia.peertopeer.conjugal.message.LoopConjugalDialogTypeRequestMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionAcceptedMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionSocketAddressMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionErrorMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionExpectedPeerNodeUuidMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionSplicedConnectionIdMessage;
import aletheia.peertopeer.conjugal.message.UpdateMaleNodeUuidsMessage;
import aletheia.peertopeer.ephemeral.message.LoopEphemeralDialogTypeAcknowledgeMessage;
import aletheia.peertopeer.ephemeral.message.LoopEphemeralDialogTypeRequestMessage;
import aletheia.peertopeer.ephemeral.message.PersonInfoMessage;
import aletheia.peertopeer.ephemeral.message.RootContextStatementSignaturesResponseMessage;
import aletheia.peertopeer.ephemeral.message.RootContextsRequestMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageAddressRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageContentMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessagePortRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageUuidMessage;
import aletheia.peertopeer.ephemeral.message.SignatureRequestConfirmMessage;
import aletheia.peertopeer.ephemeral.message.SignatureRequestMessage;
import aletheia.peertopeer.ephemeral.message.TransmitDeferredMessagesConfirmMessage;
import aletheia.peertopeer.ephemeral.message.TransmitDeferredMessagesMessage;
import aletheia.peertopeer.network.message.BeltConfirmMessage;
import aletheia.peertopeer.network.message.BeltConnectMessage;
import aletheia.peertopeer.network.message.BeltDisconnectMessage;
import aletheia.peertopeer.network.message.BeltRequestMessage;
import aletheia.peertopeer.network.message.ComplementingInvitationMessage;
import aletheia.peertopeer.network.message.DeferredMessageInfoMessage;
import aletheia.peertopeer.network.message.DeferredMessageQueueMessage;
import aletheia.peertopeer.network.message.DeferredMessageRemovalMessage;
import aletheia.peertopeer.network.message.DeferredMessageRequestMessage;
import aletheia.peertopeer.network.message.DeferredMessageResponseMessage;
import aletheia.peertopeer.network.message.InitialNetworkPhaseTypeMessage;
import aletheia.peertopeer.network.message.LoopNetworkDialogTypeAcknowledgeMessage;
import aletheia.peertopeer.network.message.LoopNetworkDialogTypeRequestMessage;
import aletheia.peertopeer.network.message.NeighbourCumulationMessage;
import aletheia.peertopeer.network.message.NeighbourCumulationValueMessage;
import aletheia.peertopeer.network.message.RedirectAddressMessage;
import aletheia.peertopeer.network.message.RequestRouterCumulationValueMessage;
import aletheia.peertopeer.network.message.ResourceTreeNodeMessage;
import aletheia.peertopeer.network.message.RouteableMessage;
import aletheia.peertopeer.network.message.RouteableProcessedMessage;
import aletheia.peertopeer.network.message.RouterCumulationMessage;
import aletheia.peertopeer.network.message.RouterCumulationValueMessage;
import aletheia.peertopeer.network.message.RouterSetMessage;
import aletheia.peertopeer.network.message.RouterSetNeighbourMessage;
import aletheia.peertopeer.network.message.UpdateBindPortMessage;
import aletheia.peertopeer.spliced.message.ConnectionIdMessage;
import aletheia.peertopeer.statement.message.AvailableProofsMessage;
import aletheia.peertopeer.statement.message.ContextProofRequestMessage;
import aletheia.peertopeer.statement.message.ContextStatementSignaturesResponseMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerRequestMessage;
import aletheia.peertopeer.statement.message.DelegateAuthorizerResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeDelegateDependencyResponseMessage;
import aletheia.peertopeer.statement.message.DelegateTreeInfoMessage;
import aletheia.peertopeer.statement.message.DelegateTreeRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyRequestMessage;
import aletheia.peertopeer.statement.message.DelegateTreeSuccessorDependencyResponseMessage;
import aletheia.peertopeer.statement.message.LoopStatementDialogTypeAcknowledgeMessage;
import aletheia.peertopeer.statement.message.LoopStatementDialogTypeRequestMessage;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonRequisiteMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementRequestMessage;
import aletheia.peertopeer.statement.message.StatementRequisiteMessage;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.peertopeer.statement.message.StatementsSubscribeConfirmationMessage;
import aletheia.peertopeer.statement.message.StatementsSubscribeMessage;
import aletheia.peertopeer.statement.message.SubscriptionContextsMessage;
import aletheia.peertopeer.statement.message.SubscriptionSubContextsMessage;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.enumerate.ShortExportableEnum;

@ExportableEnumInfo(availableVersions =
{ 0, 1 })
public enum MessageCode implements ShortExportableEnum<MessageCode>
{
	//@formatter:off
	_ContextProofRequest((short)0x0001, ContextProofRequestMessage.class, 0, 0),
	_SubscriptionRootContexts((short)0x0004, SubscriptionContextsMessage.class, 1, 1),
	_SubscriptionSubContexts((short)0x0005, SubscriptionSubContextsMessage.class, 0, 0 ),
	_ContextStatementSignaturesResponse((short)0x0006, ContextStatementSignaturesResponseMessage.class, 1, 1),
	_StatementRequisite((short)0x0007, StatementRequisiteMessage.class, 0, 0),
	_StatementRequest((short)0x0008, StatementRequestMessage.class, 0, 0),
	_StatementResponse((short)0x0009, StatementResponseMessage.class, 0, 0),
	_PersonRequisite((short)0x000a, PersonRequisiteMessage.class, 0, 0),
	_PersonRequest((short)0x000b, PersonRequestMessage.class, 0, 0),
	_PersonResponse((short)0x000c, PersonResponseMessage.class, 0, 0),
	_DelegateTreeInfo((short)0x000d, DelegateTreeInfoMessage.class, 0, 0),
	_DelegateTreeRequest((short)0x000e, DelegateTreeRequestMessage.class, 0, 0),
	_DelegateTreeSuccessorDependencyRequest((short)0x000f, DelegateTreeSuccessorDependencyRequestMessage.class, 0, 0),
	_DelegateTreeSuccessorDependencyResponse((short)0x0010, DelegateTreeSuccessorDependencyResponseMessage.class,0,  0),
	_DelegateTreeDelegateDependencyRequest((short)0x0011, DelegateTreeDelegateDependencyRequestMessage.class, 0, 0),
	_DelegateTreeDelegateDependencyResponse((short)0x0012, DelegateTreeDelegateDependencyResponseMessage.class, 0, 0),
	_DelegateAuthorizerRequest((short)0x0013, DelegateAuthorizerRequestMessage.class, 0, 0),
	_DelegateAuthorizerResponse((short)0x0014, DelegateAuthorizerResponseMessage.class, 0, 0),
	_AvailableProofs((short)0x0015, AvailableProofsMessage.class, 0, 0),
	_LoopStatementDialogRequest((short) 0x0016, LoopStatementDialogTypeRequestMessage.class, 0, 0),
	_LoopStatementDialogAcknowledge((short) 0x0017, LoopStatementDialogTypeAcknowledgeMessage.class, 0, 0),
	_StatementsSubscribe((short)0x0018, StatementsSubscribeMessage.class, 0, 0),
	_StatementsSubscribeConfirmation((short)0x0019, StatementsSubscribeConfirmationMessage.class, 0, 0),
	
	_Routeable((short)0x0100, RouteableMessage.class, 0, 0),
	_RouterSet((short)0x0101, RouterSetMessage.class, 0, 0),
	_LoopNetworkDialogRequest((short) 0x0102, LoopNetworkDialogTypeRequestMessage.class,0, 0),
	_LoopNetworkDialogAcknowledge((short) 0x0103, LoopNetworkDialogTypeAcknowledgeMessage.class,0, 0),
	_UpdateBindPort((short) 0x0104, UpdateBindPortMessage.class,0, 0),
	_RedirectAddress((short) 0x0105, RedirectAddressMessage.class,0, 0),
	_InitialNetworkPhaseType((short) 0x0106, InitialNetworkPhaseTypeMessage.class,0, 0),
	_ComplementingInvitation((short) 0x0107, ComplementingInvitationMessage.class,0, 0),
	_RouterSetNeighbour((short) 0x0108, RouterSetNeighbourMessage.class,0, 0),
	_BeltConnect((short) 0x0109, BeltConnectMessage.class,0, 0),
	_BeltConfirm((short) 0x010a, BeltConfirmMessage.class,0, 0),
	_RouteableProcessed((short) 0x010b, RouteableProcessedMessage.class,0, 0),
	_ResourceTree((short) 0x010c, ResourceTreeNodeMessage.class,0, 0),
	_BeltRequest((short) 0x010d, BeltRequestMessage.class,0, 0),
	_BeltDisconnect((short) 0x010e, BeltDisconnectMessage.class,0, 0),
	_DeferredMessageInfo((short) 0x010f, DeferredMessageInfoMessage.class,0, 0),
	_DeferredMessageRequest((short) 0x0110, DeferredMessageRequestMessage.class,0, 0),
	_DeferredMessageResponse((short) 0x0111, DeferredMessageResponseMessage.class,0, 0),
	_DeferredMessageQueue((short) 0x0112, DeferredMessageQueueMessage.class,0, 0),
	_DeferredMessageRemoval((short) 0x0113, DeferredMessageRemovalMessage.class,0, 0),
	_NeighbourCumulationValue((short) 0x0114, NeighbourCumulationValueMessage.class,0, 0),
	_RouterCumulationValue((short) 0x0115, RouterCumulationValueMessage.class,0, 0),
	_NeighbourCumulation((short) 0x0116, NeighbourCumulationMessage.class,0, 0),
	_RouterCumulation((short) 0x0117, RouterCumulationMessage.class,0, 0),
	_RequestRouterCumulationValue((short) 0x0118, RequestRouterCumulationValueMessage.class,0, 0),
	
	_LoopEphemeralDialogAcknowledge((short)0x0200, LoopEphemeralDialogTypeAcknowledgeMessage.class,0, 0),
	_LoopEphemeralDialogRequest((short)0x0201, LoopEphemeralDialogTypeRequestMessage.class,0, 0),
	_RootContextsRequest((short)0x0202, RootContextsRequestMessage.class,0, 0),
	_RootContextStatementSignaturesResponse((short)0x0203, RootContextStatementSignaturesResponseMessage.class, 1,1),
	_SignatureRequest((short)0x0204, SignatureRequestMessage.class,0, 0),
	_SignatureRequestConfirm((short)0x0205, SignatureRequestConfirmMessage.class,0, 0),
	_SendDeferredMessageUuid((short)0x0206,SendDeferredMessageUuidMessage.class,0, 0),
	_SendDeferredMessageContent((short)0x0207,SendDeferredMessageContentMessage.class,0, 0),
	_SendDeferredMessageAddressRedirect((short)0x0208,SendDeferredMessageAddressRedirectMessage.class,0, 0),
	_SendDeferredMessagePortRedirect((short)0x0209,SendDeferredMessagePortRedirectMessage.class,0, 0),
	_TransmitDeferredMessages((short)0x020a,TransmitDeferredMessagesMessage.class,0, 0),
	_TransmitDeferredMessagesConfirm((short)0x020b,TransmitDeferredMessagesConfirmMessage.class,0, 0),
	_PersonInfo((short)0x020c,PersonInfoMessage.class,0, 0),

	_LoopEphemeralConjugalAcknowledge((short)0x0300, LoopConjugalDialogTypeAcknowledgeMessage.class,0, 0),
	_LoopEphemeralConjugalRequest((short)0x0301, LoopConjugalDialogTypeRequestMessage.class,0, 0),
	_OpenConnectionSocketAddress((short)0x0302, OpenConnectionSocketAddressMessage.class,0, 0),
	_OpenConnectionExpectedPeerNodeUuid((short)0x0303, OpenConnectionExpectedPeerNodeUuidMessage.class,0, 0),
	_OpenConnectionSplicedConnectionId((short)0x0304, OpenConnectionSplicedConnectionIdMessage.class,0, 0),
	_OpenConnectionAccepted((short)0x0305, OpenConnectionAcceptedMessage.class,0, 0),
	_OpenConnectionError((short)0x0306, OpenConnectionErrorMessage.class,0, 0),
	_UpdateMaleNodeUuids((short)0x0307, UpdateMaleNodeUuidsMessage.class,0, 0),
	
	_ConnectionId((short)0x0400, ConnectionIdMessage.class,0, 0),

	_Salutation((short)0xff00, SalutationMessage.class,0, 0),
	_MaleSalutation((short)0xff01, MaleSalutationMessage.class,0, 0),
	_Valediction((short)0xff02, ValedictionMessage.class,0, 0),
	_LockInit((short)0xff03, LockInitMessage.class,0, 0),
	_LockRequest((short)0xff04, LockRequestMessage.class,0, 0),
	_LockResponse((short)0xff05, LockResponseMessage.class,0, 0),
	_SubRootPhaseRequest((short)0xff06, SubRootPhaseRequestMessage.class,0, 0),
	_SubRootPhaseResponse((short)0xff07, SubRootPhaseResponseMessage.class,0, 0),
	_ValidPeerNodeUuid((short)0xff08, ValidPeerNodeUuidMessage.class,0, 0),

	@Deprecated
	_ProtocolError((short)0xfff0, ProtocolErrorMessage.class,0, 0),
	@Deprecated
	_RestartDialog((short)0xfff1, RestartDialogMessage.class,0, 0),
	;
	//@formatter:on

	private static final Map<Short, MessageCode> codeMap;
	private static final Map<Class<? extends Message>, MessageCode> classMap;
	private static final Map<Class<? extends Message>, Set<MessageCode>> generalizedClassMap;

	static
	{
		codeMap = new HashMap<Short, MessageCode>();
		classMap = new HashMap<Class<? extends Message>, MessageCode>();
		generalizedClassMap = new HashMap<Class<? extends Message>, Set<MessageCode>>();

		for (MessageCode messageCode : values())
		{
			if (codeMap.put(messageCode.code, messageCode) != null)
				throw new Error("Duplicate symbol code: " + messageCode);
			if (classMap.put(messageCode.clazz, messageCode) != null)
				throw new Error("Duplicate symbol class for code: " + messageCode);
			Class<? extends Message> c = messageCode.clazz;
			while (true)
			{
				Set<MessageCode> set = generalizedClassMap.get(c);
				if (set == null)
				{
					set = EnumSet.of(messageCode);
					generalizedClassMap.put(c, set);
				}
				else
					set.add(messageCode);
				Class<?> s = c.getSuperclass();
				if (!Message.class.isAssignableFrom(s))
					break;
				@SuppressWarnings("unchecked")
				Class<? extends Message> s2 = (Class<? extends Message>) s;
				c = s2;
			}
		}
	}

	private final short code;
	private final Class<? extends Message> clazz;
	private final Class<? extends MessageSubProtocol<? extends Message>> subProtocolClazz;
	private final int enumVersion;
	private final int subProtocolVersion;

	private MessageCode(short code, Class<? extends Message> clazz, int enumVersion, int subProtocolVersion)
	{
		this.code = code;
		this.clazz = clazz;
		MessageSubProtocolInfo messageProtocolInfo = clazz.getAnnotation(MessageSubProtocolInfo.class);
		this.subProtocolClazz = messageProtocolInfo.subProtocolClass();
		this.enumVersion = enumVersion;
		this.subProtocolVersion = subProtocolVersion;
	}

	@Override
	public Short getCode(int version)
	{
		return code;
	}

	public Class<? extends Message> getClazz()
	{
		return clazz;
	}

	public Class<? extends MessageSubProtocol<? extends Message>> getSubProtocolClazz()
	{
		return subProtocolClazz;
	}

	public int getSubProtocolVersion(int version)
	{
		if (version < enumVersion)
			return -1;
		return subProtocolVersion;
	}

	public static MessageCode codeFor(Class<? extends Message> clazz)
	{
		MessageCode code = classMap.get(clazz);
		if (code == null)
			throw new RuntimeException("No code for class: " + clazz.getName());
		return code;
	}

	public static Set<MessageCode> generalizedCodesFor(Class<? extends Message> clazz)
	{
		Set<MessageCode> codes = generalizedClassMap.get(clazz);
		if (codes == null)
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet(codes);
	}

}
