@file:OptIn(ExperimentalSerializationApi::class)

package org.near.jsonrpc.types

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*

@Serializer(forClass = JsonElement::class)
object PolymorphicSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: JsonElement,
    ) = JsonElement.serializer().serialize(encoder, value)

    override fun deserialize(decoder: Decoder): JsonElement = JsonElement.serializer().deserialize(decoder)
}

/**
 * NEAR Account Identifier.
 */
typealias AccountId = String

typealias AccountIdValidityRulesVersion = Long

typealias CryptoHash = String

@Serializable
enum class Direction(val value: String) {
    @SerialName("Left")
    LEFT("Left"),

    @SerialName("Right")
    RIGHT("Right"),
}

@Serializable
enum class Finality(val value: String) {
    @SerialName("optimistic")
    OPTIMISTIC("optimistic"),

    @SerialName("near-final")
    NEAR_FINAL("near-final"),

    @SerialName("final")
    FINAL("final"),
}

/**
 * This type is used to mark function arguments.
 */
typealias FunctionArgs = String

@Serializable
enum class LogSummaryStyle(val value: String) {
    @SerialName("plain")
    PLAIN("plain"),

    @SerialName("colored")
    COLORED("colored"),
}

@Serializable
enum class MethodResolveError(val value: String) {
    @SerialName("MethodEmptyName")
    METHOD_EMPTY_NAME("MethodEmptyName"),

    @SerialName("MethodNotFound")
    METHOD_NOT_FOUND("MethodNotFound"),

    @SerialName("MethodInvalidSignature")
    METHOD_INVALID_SIGNATURE("MethodInvalidSignature"),
}
typealias MutableConfigValue = String

typealias NearGas = Long

typealias NearToken = String

@Serializable
enum class ProtocolVersionCheckConfig(val value: String) {
    @SerialName("Next")
    NEXT("Next"),

    @SerialName("NextNext")
    NEXT_NEXT("NextNext"),
}
typealias PublicKey = String

/**
 * The shard identifier. It may be an arbitrary number - it does not need to be
 */
typealias ShardId = Long

typealias Signature = String

typealias SignedTransaction = String

@Serializable
enum class StorageGetMode(val value: String) {
    @SerialName("FlatStorage")
    FLAT_STORAGE("FlatStorage"),

    @SerialName("Trie")
    TRIE("Trie"),
}

/**
 * This type is used to mark keys (arrays of bytes) that are queried from store.
 */
typealias StoreKey = String

/**
 * This type is used to mark values returned from store (arrays of bytes).
 */
typealias StoreValue = String

@Serializable
enum class SyncCheckpoint(val value: String) {
    @SerialName("genesis")
    GENESIS("genesis"),

    @SerialName("earliest_available")
    EARLIEST_AVAILABLE("earliest_available"),
}
typealias GenesisConfigRequest = Unit

typealias RpcClientConfigRequest = Unit

typealias RpcHealthRequest = Unit

@Serializable(with = RpcHealthResponseSerializer::class)
object RpcHealthResponse

object RpcHealthResponseSerializer : KSerializer<RpcHealthResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("RpcHealthResponse")

    override fun serialize(
        encoder: Encoder,
        value: RpcHealthResponse,
    ) {
        encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): RpcHealthResponse {
        decoder.decodeNull()
        return RpcHealthResponse
    }
}

typealias RpcNetworkInfoRequest = Unit

typealias RpcStatusRequest = Unit

/**
 * Defines permissions for AccessKey
 */
@Serializable(with = AccessKeyPermissionSerializer::class)
sealed interface AccessKeyPermission {
    @Serializable
    data class FunctionCall(
        @SerialName("allowance")
        val allowance: NearToken? = null,
        @SerialName("method_names")
        val methodNames: List<String>,
        @SerialName("receiver_id")
        val receiverId: String,
    ) : AccessKeyPermission

    @Serializable
    @SerialName("FullAccess")
    object Fullaccess : AccessKeyPermission
}

// Custom serializer for AccessKeyPermission to handle NEAR's externally-tagged union format
object AccessKeyPermissionSerializer : KSerializer<AccessKeyPermission> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AccessKeyPermission")

    override fun serialize(
        encoder: Encoder,
        value: AccessKeyPermission,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is AccessKeyPermission.FunctionCall ->
                output.encodeSerializableValue(
                    AccessKeyPermission.FunctionCall.serializer(),
                    value,
                )
            is AccessKeyPermission.Fullaccess ->
                output.encodeJsonElement(
                    buildJsonObject { put("FullAccess", JsonNull) },
                )
        }
    }

    override fun deserialize(decoder: Decoder): AccessKeyPermission {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "FunctionCall" in element ->
                input.json.decodeFromJsonElement(
                    AccessKeyPermission.FunctionCall.serializer(),
                    element["FunctionCall"]!!,
                )
            "FullAccess" in element -> AccessKeyPermission.Fullaccess
            else -> throw SerializationException("Unknown variant in AccessKeyPermission: ${element.keys}")
        }
    }
}

/**
 * Describes the permission scope for an access key. Whether it is a function ca...
 */
@Serializable(with = AccessKeyPermissionViewSerializer::class)
sealed interface AccessKeyPermissionView {
    @Serializable
    @SerialName("FullAccess")
    object Fullaccess : AccessKeyPermissionView

    @Serializable
    data class FunctionCall(
        @SerialName("allowance")
        val allowance: NearToken? = null,
        @SerialName("method_names")
        val methodNames: List<String>,
        @SerialName("receiver_id")
        val receiverId: String,
    ) : AccessKeyPermissionView
}

// Custom serializer for AccessKeyPermissionView to handle NEAR's externally-tagged union format
object AccessKeyPermissionViewSerializer : KSerializer<AccessKeyPermissionView> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AccessKeyPermissionView")

    override fun serialize(
        encoder: Encoder,
        value: AccessKeyPermissionView,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is AccessKeyPermissionView.Fullaccess ->
                output.encodeJsonElement(
                    buildJsonObject { put("FullAccess", JsonNull) },
                )
            is AccessKeyPermissionView.FunctionCall ->
                output.encodeSerializableValue(
                    AccessKeyPermissionView.FunctionCall.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): AccessKeyPermissionView {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "FullAccess" in element -> AccessKeyPermissionView.Fullaccess
            "FunctionCall" in element ->
                input.json.decodeFromJsonElement(
                    AccessKeyPermissionView.FunctionCall.serializer(),
                    element["FunctionCall"]!!,
                )
            else -> throw SerializationException("Unknown variant in AccessKeyPermissionView: ${element.keys}")
        }
    }
}

@Serializable(with = ActionErrorKindSerializer::class)
sealed interface ActionErrorKind {
    @Serializable
    data class AccountAlreadyExists(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class AccountDoesNotExist(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class CreateAccountOnlyByRegistrar(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("predecessor_id")
        val predecessorId: AccountId,
        @SerialName("registrar_account_id")
        val registrarAccountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class CreateAccountNotAllowed(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("predecessor_id")
        val predecessorId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class ActorNoPermission(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("actor_id")
        val actorId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class DeleteKeyDoesNotExist(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : ActionErrorKind

    @Serializable
    data class AddKeyAlreadyExists(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : ActionErrorKind

    @Serializable
    data class DeleteAccountStaking(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class LackBalanceForState(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("amount")
        val amount: NearToken,
    ) : ActionErrorKind

    @Serializable
    data class TriesToUnstake(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class TriesToStake(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("balance")
        val balance: NearToken,
        @SerialName("locked")
        val locked: NearToken,
        @SerialName("stake")
        val stake: NearToken,
    ) : ActionErrorKind

    @Serializable
    data class InsufficientStake(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("minimum_stake")
        val minimumStake: NearToken,
        @SerialName("stake")
        val stake: NearToken,
    ) : ActionErrorKind

    @Serializable
    data class FunctionCallErrorRequest(
        @SerialName("FunctionCallError")
        val functionCallError: FunctionCallError,
    ) : ActionErrorKind

    @Serializable
    data class NewReceiptValidationError(
        @SerialName("NewReceiptValidationError")
        val newReceiptValidationError: ReceiptValidationError,
    ) : ActionErrorKind

    @Serializable
    data class OnlyImplicitAccountCreationAllowed(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    data class DeleteAccountWithLargeState(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionErrorKind

    @Serializable
    @SerialName("DelegateActionInvalidSignature")
    object Delegateactioninvalidsignature : ActionErrorKind

    @Serializable
    data class DelegateActionSenderDoesNotMatchTxReceiver(
        @SerialName("receiver_id")
        val receiverId: AccountId,
        @SerialName("sender_id")
        val senderId: AccountId,
    ) : ActionErrorKind

    @Serializable
    @SerialName("DelegateActionExpired")
    object Delegateactionexpired : ActionErrorKind

    @Serializable
    data class DelegateActionAccessKeyError(
        @SerialName("DelegateActionAccessKeyError")
        val delegateActionAccessKeyError: InvalidAccessKeyError,
    ) : ActionErrorKind

    @Serializable
    data class DelegateActionInvalidNonce(
        @SerialName("ak_nonce")
        val akNonce: Long,
        @SerialName("delegate_nonce")
        val delegateNonce: Long,
    ) : ActionErrorKind

    @Serializable
    data class DelegateActionNonceTooLarge(
        @SerialName("delegate_nonce")
        val delegateNonce: Long,
        @SerialName("upper_bound")
        val upperBound: Long,
    ) : ActionErrorKind

    @Serializable
    data class GlobalContractDoesNotExist(
        @SerialName("identifier")
        val identifier: GlobalContractIdentifier,
    ) : ActionErrorKind
}

// Custom serializer for ActionErrorKind to handle content-based polymorphism
object ActionErrorKindSerializer : KSerializer<ActionErrorKind> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ActionErrorKind")

    override fun serialize(
        encoder: Encoder,
        value: ActionErrorKind,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ActionErrorKind.Delegateactioninvalidsignature ->
                output.encodeJsonElement(
                    JsonPrimitive("DelegateActionInvalidSignature"),
                )
            is ActionErrorKind.Delegateactionexpired -> output.encodeJsonElement(JsonPrimitive("DelegateActionExpired"))
            is ActionErrorKind.AccountAlreadyExists ->
                output.encodeSerializableValue(
                    ActionErrorKind.AccountAlreadyExists.serializer(),
                    value,
                )
            is ActionErrorKind.AccountDoesNotExist ->
                output.encodeSerializableValue(
                    ActionErrorKind.AccountDoesNotExist.serializer(),
                    value,
                )
            is ActionErrorKind.CreateAccountOnlyByRegistrar ->
                output.encodeSerializableValue(
                    ActionErrorKind.CreateAccountOnlyByRegistrar.serializer(),
                    value,
                )
            is ActionErrorKind.CreateAccountNotAllowed ->
                output.encodeSerializableValue(
                    ActionErrorKind.CreateAccountNotAllowed.serializer(),
                    value,
                )
            is ActionErrorKind.ActorNoPermission ->
                output.encodeSerializableValue(
                    ActionErrorKind.ActorNoPermission.serializer(),
                    value,
                )
            is ActionErrorKind.DeleteKeyDoesNotExist ->
                output.encodeSerializableValue(
                    ActionErrorKind.DeleteKeyDoesNotExist.serializer(),
                    value,
                )
            is ActionErrorKind.AddKeyAlreadyExists ->
                output.encodeSerializableValue(
                    ActionErrorKind.AddKeyAlreadyExists.serializer(),
                    value,
                )
            is ActionErrorKind.DeleteAccountStaking ->
                output.encodeSerializableValue(
                    ActionErrorKind.DeleteAccountStaking.serializer(),
                    value,
                )
            is ActionErrorKind.LackBalanceForState ->
                output.encodeSerializableValue(
                    ActionErrorKind.LackBalanceForState.serializer(),
                    value,
                )
            is ActionErrorKind.TriesToUnstake ->
                output.encodeSerializableValue(
                    ActionErrorKind.TriesToUnstake.serializer(),
                    value,
                )
            is ActionErrorKind.TriesToStake ->
                output.encodeSerializableValue(
                    ActionErrorKind.TriesToStake.serializer(),
                    value,
                )
            is ActionErrorKind.InsufficientStake ->
                output.encodeSerializableValue(
                    ActionErrorKind.InsufficientStake.serializer(),
                    value,
                )
            is ActionErrorKind.FunctionCallErrorRequest ->
                output.encodeSerializableValue(
                    ActionErrorKind.FunctionCallErrorRequest.serializer(),
                    value,
                )
            is ActionErrorKind.NewReceiptValidationError ->
                output.encodeSerializableValue(
                    ActionErrorKind.NewReceiptValidationError.serializer(),
                    value,
                )
            is ActionErrorKind.OnlyImplicitAccountCreationAllowed ->
                output.encodeSerializableValue(
                    ActionErrorKind.OnlyImplicitAccountCreationAllowed.serializer(),
                    value,
                )
            is ActionErrorKind.DeleteAccountWithLargeState ->
                output.encodeSerializableValue(
                    ActionErrorKind.DeleteAccountWithLargeState.serializer(),
                    value,
                )
            is ActionErrorKind.DelegateActionSenderDoesNotMatchTxReceiver ->
                output.encodeSerializableValue(
                    ActionErrorKind.DelegateActionSenderDoesNotMatchTxReceiver.serializer(),
                    value,
                )
            is ActionErrorKind.DelegateActionAccessKeyError ->
                output.encodeSerializableValue(
                    ActionErrorKind.DelegateActionAccessKeyError.serializer(),
                    value,
                )
            is ActionErrorKind.DelegateActionInvalidNonce ->
                output.encodeSerializableValue(
                    ActionErrorKind.DelegateActionInvalidNonce.serializer(),
                    value,
                )
            is ActionErrorKind.DelegateActionNonceTooLarge ->
                output.encodeSerializableValue(
                    ActionErrorKind.DelegateActionNonceTooLarge.serializer(),
                    value,
                )
            is ActionErrorKind.GlobalContractDoesNotExist ->
                output.encodeSerializableValue(
                    ActionErrorKind.GlobalContractDoesNotExist.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ActionErrorKind {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "DelegateActionInvalidSignature" -> ActionErrorKind.Delegateactioninvalidsignature
            element is JsonPrimitive && element.content == "DelegateActionExpired" -> ActionErrorKind.Delegateactionexpired
            "AccountAlreadyExists" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.AccountAlreadyExists.serializer(),
                    element,
                )
            "AccountDoesNotExist" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.AccountDoesNotExist.serializer(),
                    element,
                )
            "CreateAccountOnlyByRegistrar" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.CreateAccountOnlyByRegistrar.serializer(),
                    element,
                )
            "CreateAccountNotAllowed" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.CreateAccountNotAllowed.serializer(),
                    element,
                )
            "ActorNoPermission" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.ActorNoPermission.serializer(),
                    element,
                )
            "DeleteKeyDoesNotExist" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DeleteKeyDoesNotExist.serializer(),
                    element,
                )
            "AddKeyAlreadyExists" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.AddKeyAlreadyExists.serializer(),
                    element,
                )
            "DeleteAccountStaking" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DeleteAccountStaking.serializer(),
                    element,
                )
            "LackBalanceForState" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.LackBalanceForState.serializer(),
                    element,
                )
            "TriesToUnstake" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.TriesToUnstake.serializer(),
                    element,
                )
            "TriesToStake" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.TriesToStake.serializer(),
                    element,
                )
            "InsufficientStake" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.InsufficientStake.serializer(),
                    element,
                )
            "FunctionCallError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.FunctionCallErrorRequest.serializer(),
                    element,
                )
            "NewReceiptValidationError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.NewReceiptValidationError.serializer(),
                    element,
                )
            "OnlyImplicitAccountCreationAllowed" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.OnlyImplicitAccountCreationAllowed.serializer(),
                    element,
                )
            "DeleteAccountWithLargeState" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DeleteAccountWithLargeState.serializer(),
                    element,
                )
            "DelegateActionSenderDoesNotMatchTxReceiver" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DelegateActionSenderDoesNotMatchTxReceiver.serializer(),
                    element,
                )
            "DelegateActionAccessKeyError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DelegateActionAccessKeyError.serializer(),
                    element,
                )
            "DelegateActionInvalidNonce" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DelegateActionInvalidNonce.serializer(),
                    element,
                )
            "DelegateActionNonceTooLarge" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.DelegateActionNonceTooLarge.serializer(),
                    element,
                )
            "GlobalContractDoesNotExist" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ActionErrorKind.GlobalContractDoesNotExist.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in ActionErrorKind: $element")
        }
    }
}

@Serializable(with = ActionViewSerializer::class)
sealed interface ActionView {
    @Serializable
    @SerialName("CreateAccount")
    object Createaccount : ActionView

    @Serializable
    data class DeployContract(
        @SerialName("code")
        val code: String,
    ) : ActionView

    @Serializable
    data class FunctionCall(
        @SerialName("args")
        val args: FunctionArgs,
        @SerialName("deposit")
        val deposit: NearToken,
        @SerialName("gas")
        val gas: NearGas,
        @SerialName("method_name")
        val methodName: String,
    ) : ActionView

    @Serializable
    data class Transfer(
        @SerialName("deposit")
        val deposit: NearToken,
    ) : ActionView

    @Serializable
    data class Stake(
        @SerialName("public_key")
        val publicKey: PublicKey,
        @SerialName("stake")
        val stake: NearToken,
    ) : ActionView

    @Serializable
    data class AddKey(
        @SerialName("access_key")
        val accessKey: AccessKeyView,
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : ActionView

    @Serializable
    data class DeleteKey(
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : ActionView

    @Serializable
    data class DeleteAccount(
        @SerialName("beneficiary_id")
        val beneficiaryId: AccountId,
    ) : ActionView

    @Serializable
    data class Delegate(
        @SerialName("delegate_action")
        val delegateAction: DelegateAction,
        @SerialName("signature")
        val signature: Signature,
    ) : ActionView

    @Serializable
    data class DeployGlobalContract(
        @SerialName("code")
        val code: String,
    ) : ActionView

    @Serializable
    data class DeployGlobalContractByAccountId(
        @SerialName("code")
        val code: String,
    ) : ActionView

    @Serializable
    data class UseGlobalContract(
        @SerialName("code_hash")
        val codeHash: CryptoHash,
    ) : ActionView

    @Serializable
    data class UseGlobalContractByAccountId(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : ActionView

    @Serializable
    data class DeterministicStateInit(
        @SerialName("code")
        val code: GlobalContractIdentifierView,
        @SerialName("data")
        val data: Map<String, String>,
        @SerialName("deposit")
        val deposit: NearToken,
    ) : ActionView
}

// Custom serializer for ActionView to handle NEAR's externally-tagged union format
object ActionViewSerializer : KSerializer<ActionView> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ActionView")

    override fun serialize(
        encoder: Encoder,
        value: ActionView,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ActionView.Createaccount -> output.encodeJsonElement(buildJsonObject { put("CreateAccount", JsonNull) })
            is ActionView.DeployContract ->
                output.encodeSerializableValue(
                    ActionView.DeployContract.serializer(),
                    value,
                )
            is ActionView.FunctionCall -> output.encodeSerializableValue(ActionView.FunctionCall.serializer(), value)
            is ActionView.Transfer -> output.encodeSerializableValue(ActionView.Transfer.serializer(), value)
            is ActionView.Stake -> output.encodeSerializableValue(ActionView.Stake.serializer(), value)
            is ActionView.AddKey -> output.encodeSerializableValue(ActionView.AddKey.serializer(), value)
            is ActionView.DeleteKey -> output.encodeSerializableValue(ActionView.DeleteKey.serializer(), value)
            is ActionView.DeleteAccount -> output.encodeSerializableValue(ActionView.DeleteAccount.serializer(), value)
            is ActionView.Delegate -> output.encodeSerializableValue(ActionView.Delegate.serializer(), value)
            is ActionView.DeployGlobalContract ->
                output.encodeSerializableValue(
                    ActionView.DeployGlobalContract.serializer(),
                    value,
                )
            is ActionView.DeployGlobalContractByAccountId ->
                output.encodeSerializableValue(
                    ActionView.DeployGlobalContractByAccountId.serializer(),
                    value,
                )
            is ActionView.UseGlobalContract ->
                output.encodeSerializableValue(
                    ActionView.UseGlobalContract.serializer(),
                    value,
                )
            is ActionView.UseGlobalContractByAccountId ->
                output.encodeSerializableValue(
                    ActionView.UseGlobalContractByAccountId.serializer(),
                    value,
                )
            is ActionView.DeterministicStateInit ->
                output.encodeSerializableValue(
                    ActionView.DeterministicStateInit.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ActionView {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "CreateAccount" in element -> ActionView.Createaccount
            "DeployContract" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.DeployContract.serializer(),
                    element["DeployContract"]!!,
                )
            "FunctionCall" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.FunctionCall.serializer(),
                    element["FunctionCall"]!!,
                )
            "Transfer" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.Transfer.serializer(),
                    element["Transfer"]!!,
                )
            "Stake" in element -> input.json.decodeFromJsonElement(ActionView.Stake.serializer(), element["Stake"]!!)
            "AddKey" in element -> input.json.decodeFromJsonElement(ActionView.AddKey.serializer(), element["AddKey"]!!)
            "DeleteKey" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.DeleteKey.serializer(),
                    element["DeleteKey"]!!,
                )
            "DeleteAccount" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.DeleteAccount.serializer(),
                    element["DeleteAccount"]!!,
                )
            "Delegate" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.Delegate.serializer(),
                    element["Delegate"]!!,
                )
            "DeployGlobalContract" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.DeployGlobalContract.serializer(),
                    element["DeployGlobalContract"]!!,
                )
            "DeployGlobalContractByAccountId" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.DeployGlobalContractByAccountId.serializer(),
                    element["DeployGlobalContractByAccountId"]!!,
                )
            "UseGlobalContract" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.UseGlobalContract.serializer(),
                    element["UseGlobalContract"]!!,
                )
            "UseGlobalContractByAccountId" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.UseGlobalContractByAccountId.serializer(),
                    element["UseGlobalContractByAccountId"]!!,
                )
            "DeterministicStateInit" in element ->
                input.json.decodeFromJsonElement(
                    ActionView.DeterministicStateInit.serializer(),
                    element["DeterministicStateInit"]!!,
                )
            else -> throw SerializationException("Unknown variant in ActionView: ${element.keys}")
        }
    }
}

/**
 * Describes the error for validating a list of actions.
 */
@Serializable(with = ActionsValidationErrorSerializer::class)
sealed interface ActionsValidationError {
    @Serializable
    @SerialName("DeleteActionMustBeFinal")
    object Deleteactionmustbefinal : ActionsValidationError

    @Serializable
    data class TotalPrepaidGasExceeded(
        @SerialName("limit")
        val limit: NearGas,
        @SerialName("total_prepaid_gas")
        val totalPrepaidGas: NearGas,
    ) : ActionsValidationError

    @Serializable
    data class TotalNumberOfActionsExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("total_number_of_actions")
        val totalNumberOfActions: Long,
    ) : ActionsValidationError

    @Serializable
    data class AddKeyMethodNamesNumberOfBytesExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("total_number_of_bytes")
        val totalNumberOfBytes: Long,
    ) : ActionsValidationError

    @Serializable
    data class AddKeyMethodNameLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : ActionsValidationError

    @Serializable
    @SerialName("IntegerOverflow")
    object Integeroverflow : ActionsValidationError

    @Serializable
    data class InvalidAccountId(
        @SerialName("account_id")
        val accountId: String,
    ) : ActionsValidationError

    @Serializable
    data class ContractSizeExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("size")
        val size: Long,
    ) : ActionsValidationError

    @Serializable
    data class FunctionCallMethodNameLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : ActionsValidationError

    @Serializable
    data class FunctionCallArgumentsLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : ActionsValidationError

    @Serializable
    data class UnsuitableStakingKey(
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : ActionsValidationError

    @Serializable
    @SerialName("FunctionCallZeroAttachedGas")
    object Functioncallzeroattachedgas : ActionsValidationError

    @Serializable
    @SerialName("DelegateActionMustBeOnlyOne")
    object Delegateactionmustbeonlyone : ActionsValidationError

    @Serializable
    data class UnsupportedProtocolFeature(
        @SerialName("protocol_feature")
        val protocolFeature: String,
        @SerialName("version")
        val version: Long,
    ) : ActionsValidationError

    @Serializable
    data class InvalidDeterministicStateInitReceiver(
        @SerialName("derived_id")
        val derivedId: AccountId,
        @SerialName("receiver_id")
        val receiverId: AccountId,
    ) : ActionsValidationError

    @Serializable
    data class DeterministicStateInitKeyLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : ActionsValidationError

    @Serializable
    data class DeterministicStateInitValueLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : ActionsValidationError
}

// Custom serializer for ActionsValidationError to handle NEAR's externally-tagged union format
object ActionsValidationErrorSerializer : KSerializer<ActionsValidationError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ActionsValidationError")

    override fun serialize(
        encoder: Encoder,
        value: ActionsValidationError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ActionsValidationError.Deleteactionmustbefinal ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("DeleteActionMustBeFinal", JsonNull)
                    },
                )
            is ActionsValidationError.TotalPrepaidGasExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.TotalPrepaidGasExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.TotalNumberOfActionsExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.TotalNumberOfActionsExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.AddKeyMethodNamesNumberOfBytesExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.AddKeyMethodNamesNumberOfBytesExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.AddKeyMethodNameLengthExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.AddKeyMethodNameLengthExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.Integeroverflow ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("IntegerOverflow", JsonNull)
                    },
                )
            is ActionsValidationError.InvalidAccountId ->
                output.encodeSerializableValue(
                    ActionsValidationError.InvalidAccountId.serializer(),
                    value,
                )
            is ActionsValidationError.ContractSizeExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.ContractSizeExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.FunctionCallMethodNameLengthExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.FunctionCallMethodNameLengthExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.FunctionCallArgumentsLengthExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.FunctionCallArgumentsLengthExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.UnsuitableStakingKey ->
                output.encodeSerializableValue(
                    ActionsValidationError.UnsuitableStakingKey.serializer(),
                    value,
                )
            is ActionsValidationError.Functioncallzeroattachedgas ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("FunctionCallZeroAttachedGas", JsonNull)
                    },
                )
            is ActionsValidationError.Delegateactionmustbeonlyone ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("DelegateActionMustBeOnlyOne", JsonNull)
                    },
                )
            is ActionsValidationError.UnsupportedProtocolFeature ->
                output.encodeSerializableValue(
                    ActionsValidationError.UnsupportedProtocolFeature.serializer(),
                    value,
                )
            is ActionsValidationError.InvalidDeterministicStateInitReceiver ->
                output.encodeSerializableValue(
                    ActionsValidationError.InvalidDeterministicStateInitReceiver.serializer(),
                    value,
                )
            is ActionsValidationError.DeterministicStateInitKeyLengthExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.DeterministicStateInitKeyLengthExceeded.serializer(),
                    value,
                )
            is ActionsValidationError.DeterministicStateInitValueLengthExceeded ->
                output.encodeSerializableValue(
                    ActionsValidationError.DeterministicStateInitValueLengthExceeded.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ActionsValidationError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "DeleteActionMustBeFinal" in element -> ActionsValidationError.Deleteactionmustbefinal
            "TotalPrepaidGasExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.TotalPrepaidGasExceeded.serializer(),
                    element["TotalPrepaidGasExceeded"]!!,
                )
            "TotalNumberOfActionsExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.TotalNumberOfActionsExceeded.serializer(),
                    element["TotalNumberOfActionsExceeded"]!!,
                )
            "AddKeyMethodNamesNumberOfBytesExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.AddKeyMethodNamesNumberOfBytesExceeded.serializer(),
                    element["AddKeyMethodNamesNumberOfBytesExceeded"]!!,
                )
            "AddKeyMethodNameLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.AddKeyMethodNameLengthExceeded.serializer(),
                    element["AddKeyMethodNameLengthExceeded"]!!,
                )
            "IntegerOverflow" in element -> ActionsValidationError.Integeroverflow
            "InvalidAccountId" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.InvalidAccountId.serializer(),
                    element["InvalidAccountId"]!!,
                )
            "ContractSizeExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.ContractSizeExceeded.serializer(),
                    element["ContractSizeExceeded"]!!,
                )
            "FunctionCallMethodNameLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.FunctionCallMethodNameLengthExceeded.serializer(),
                    element["FunctionCallMethodNameLengthExceeded"]!!,
                )
            "FunctionCallArgumentsLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.FunctionCallArgumentsLengthExceeded.serializer(),
                    element["FunctionCallArgumentsLengthExceeded"]!!,
                )
            "UnsuitableStakingKey" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.UnsuitableStakingKey.serializer(),
                    element["UnsuitableStakingKey"]!!,
                )
            "FunctionCallZeroAttachedGas" in element -> ActionsValidationError.Functioncallzeroattachedgas
            "DelegateActionMustBeOnlyOne" in element -> ActionsValidationError.Delegateactionmustbeonlyone
            "UnsupportedProtocolFeature" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.UnsupportedProtocolFeature.serializer(),
                    element["UnsupportedProtocolFeature"]!!,
                )
            "InvalidDeterministicStateInitReceiver" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.InvalidDeterministicStateInitReceiver.serializer(),
                    element["InvalidDeterministicStateInitReceiver"]!!,
                )
            "DeterministicStateInitKeyLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.DeterministicStateInitKeyLengthExceeded.serializer(),
                    element["DeterministicStateInitKeyLengthExceeded"]!!,
                )
            "DeterministicStateInitValueLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    ActionsValidationError.DeterministicStateInitValueLengthExceeded.serializer(),
                    element["DeterministicStateInitValueLengthExceeded"]!!,
                )
            else -> throw SerializationException("Unknown variant in ActionsValidationError: ${element.keys}")
        }
    }
}

@Serializable
data class BandwidthRequests(
    @SerialName("V1")
    val v1: BandwidthRequestsV1,
)

@Serializable(with = BlockIdSerializer::class)
sealed interface BlockId {
    @Serializable
    @JvmInline
    value class IntegerValue(
        val value: Long,
    ) : BlockId

    @Serializable
    @JvmInline
    value class CryptoHashVariant(
        val value: CryptoHash,
    ) : BlockId
}

// Custom serializer for BlockId to handle content-based polymorphism
object BlockIdSerializer : JsonContentPolymorphicSerializer<BlockId>(BlockId::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<BlockId> {
        return when {
            element is JsonPrimitive && element.longOrNull != null -> BlockId.IntegerValue.serializer()
            element is JsonPrimitive && element.isString -> BlockId.CryptoHashVariant.serializer()
            else -> throw SerializationException("Unknown variant in BlockId: type=${element::class.simpleName}")
        }
    }
}

@Serializable(with = CompilationErrorSerializer::class)
sealed interface CompilationError {
    @Serializable
    data class CodeDoesNotExist(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : CompilationError

    @Serializable
    data class PrepareErrorRequest(
        @SerialName("PrepareError")
        val prepareError: PrepareError,
    ) : CompilationError

    @Serializable
    data class WasmerCompileError(
        @SerialName("msg")
        val msg: String,
    ) : CompilationError
}

// Custom serializer for CompilationError to handle content-based polymorphism
object CompilationErrorSerializer : JsonContentPolymorphicSerializer<CompilationError>(CompilationError::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CompilationError> {
        return when {
            "CodeDoesNotExist" in element.jsonObject -> CompilationError.CodeDoesNotExist.serializer()
            "PrepareError" in element.jsonObject -> CompilationError.PrepareErrorRequest.serializer()
            "WasmerCompileError" in element.jsonObject -> CompilationError.WasmerCompileError.serializer()
            else -> throw SerializationException(
                "Unknown variant in CompilationError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable
data class DeterministicAccountStateInit(
    @SerialName("V1")
    val v1: DeterministicAccountStateInitV1,
)

@Serializable(with = ExecutionStatusViewSerializer::class)
sealed interface ExecutionStatusView {
    @Serializable
    @SerialName("Unknown")
    object Unknown : ExecutionStatusView

    @Serializable
    data class Failure(
        @SerialName("Failure")
        val failure: TxExecutionError,
    ) : ExecutionStatusView

    @Serializable
    data class SuccessValue(
        @SerialName("SuccessValue")
        val successValue: String,
    ) : ExecutionStatusView

    @Serializable
    data class SuccessReceiptId(
        @SerialName("SuccessReceiptId")
        val successReceiptId: CryptoHash,
    ) : ExecutionStatusView
}

// Custom serializer for ExecutionStatusView to handle content-based polymorphism
object ExecutionStatusViewSerializer : KSerializer<ExecutionStatusView> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ExecutionStatusView")

    override fun serialize(
        encoder: Encoder,
        value: ExecutionStatusView,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ExecutionStatusView.Unknown -> output.encodeJsonElement(JsonPrimitive("Unknown"))
            is ExecutionStatusView.Failure ->
                output.encodeSerializableValue(
                    ExecutionStatusView.Failure.serializer(),
                    value,
                )
            is ExecutionStatusView.SuccessValue ->
                output.encodeSerializableValue(
                    ExecutionStatusView.SuccessValue.serializer(),
                    value,
                )
            is ExecutionStatusView.SuccessReceiptId ->
                output.encodeSerializableValue(
                    ExecutionStatusView.SuccessReceiptId.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ExecutionStatusView {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "Unknown" -> ExecutionStatusView.Unknown
            "Failure" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ExecutionStatusView.Failure.serializer(),
                    element,
                )
            "SuccessValue" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ExecutionStatusView.SuccessValue.serializer(),
                    element,
                )
            "SuccessReceiptId" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    ExecutionStatusView.SuccessReceiptId.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in ExecutionStatusView: $element")
        }
    }
}

@Serializable(with = ExternalStorageLocationSerializer::class)
sealed interface ExternalStorageLocation {
    @Serializable
    data class S3(
        @SerialName("bucket")
        val bucket: String,
        @SerialName("region")
        val region: String,
    ) : ExternalStorageLocation

    @Serializable
    data class Filesystem(
        @SerialName("root_dir")
        val rootDir: String,
    ) : ExternalStorageLocation

    @Serializable
    data class GCS(
        @SerialName("bucket")
        val bucket: String,
    ) : ExternalStorageLocation
}

// Custom serializer for ExternalStorageLocation to handle NEAR's externally-tagged union format
object ExternalStorageLocationSerializer : KSerializer<ExternalStorageLocation> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ExternalStorageLocation")

    override fun serialize(
        encoder: Encoder,
        value: ExternalStorageLocation,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ExternalStorageLocation.S3 ->
                output.encodeSerializableValue(
                    ExternalStorageLocation.S3.serializer(),
                    value,
                )
            is ExternalStorageLocation.Filesystem ->
                output.encodeSerializableValue(
                    ExternalStorageLocation.Filesystem.serializer(),
                    value,
                )
            is ExternalStorageLocation.GCS ->
                output.encodeSerializableValue(
                    ExternalStorageLocation.GCS.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ExternalStorageLocation {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "S3" in element ->
                input.json.decodeFromJsonElement(
                    ExternalStorageLocation.S3.serializer(),
                    element["S3"]!!,
                )
            "Filesystem" in element ->
                input.json.decodeFromJsonElement(
                    ExternalStorageLocation.Filesystem.serializer(),
                    element["Filesystem"]!!,
                )
            "GCS" in element ->
                input.json.decodeFromJsonElement(
                    ExternalStorageLocation.GCS.serializer(),
                    element["GCS"]!!,
                )
            else -> throw SerializationException("Unknown variant in ExternalStorageLocation: ${element.keys}")
        }
    }
}

@Serializable(with = FinalExecutionStatusSerializer::class)
sealed interface FinalExecutionStatus {
    @Serializable
    @SerialName("NotStarted")
    object Notstarted : FinalExecutionStatus

    @Serializable
    @SerialName("Started")
    object Started : FinalExecutionStatus

    @Serializable
    data class Failure(
        @SerialName("Failure")
        val failure: TxExecutionError,
    ) : FinalExecutionStatus

    @Serializable
    data class SuccessValue(
        @SerialName("SuccessValue")
        val successValue: String,
    ) : FinalExecutionStatus
}

// Custom serializer for FinalExecutionStatus to handle content-based polymorphism
object FinalExecutionStatusSerializer : KSerializer<FinalExecutionStatus> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FinalExecutionStatus")

    override fun serialize(
        encoder: Encoder,
        value: FinalExecutionStatus,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is FinalExecutionStatus.Notstarted -> output.encodeJsonElement(JsonPrimitive("NotStarted"))
            is FinalExecutionStatus.Started -> output.encodeJsonElement(JsonPrimitive("Started"))
            is FinalExecutionStatus.Failure ->
                output.encodeSerializableValue(
                    FinalExecutionStatus.Failure.serializer(),
                    value,
                )
            is FinalExecutionStatus.SuccessValue ->
                output.encodeSerializableValue(
                    FinalExecutionStatus.SuccessValue.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): FinalExecutionStatus {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "NotStarted" -> FinalExecutionStatus.Notstarted
            element is JsonPrimitive && element.content == "Started" -> FinalExecutionStatus.Started
            "Failure" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FinalExecutionStatus.Failure.serializer(),
                    element,
                )
            "SuccessValue" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FinalExecutionStatus.SuccessValue.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in FinalExecutionStatus: $element")
        }
    }
}

/**
 * Serializable version of `near-vm-runner::FunctionCallError`.
 */
@Serializable(with = FunctionCallErrorSerializer::class)
sealed interface FunctionCallError {
    @Serializable
    @SerialName("WasmUnknownError")
    object Wasmunknownerror : FunctionCallError

    @Serializable
    data class CompilationErrorRequest(
        @SerialName("CompilationError")
        val compilationError: CompilationError,
    ) : FunctionCallError

    @Serializable
    data class LinkError(
        @SerialName("msg")
        val msg: String,
    ) : FunctionCallError

    @Serializable
    data class MethodResolveErrorRequest(
        @SerialName("MethodResolveError")
        val methodResolveError: MethodResolveError,
    ) : FunctionCallError

    @Serializable
    data class WasmTrapRequest(
        @SerialName("WasmTrap")
        val wasmTrap: WasmTrap,
    ) : FunctionCallError

    @Serializable
    data class HostErrorRequest(
        @SerialName("HostError")
        val hostError: HostError,
    ) : FunctionCallError

    @Serializable
    data class ExecutionError(
        @SerialName("ExecutionError")
        val executionError: String,
    ) : FunctionCallError
}

// Custom serializer for FunctionCallError to handle content-based polymorphism
object FunctionCallErrorSerializer : KSerializer<FunctionCallError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FunctionCallError")

    override fun serialize(
        encoder: Encoder,
        value: FunctionCallError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is FunctionCallError.Wasmunknownerror -> output.encodeJsonElement(JsonPrimitive("WasmUnknownError"))
            is FunctionCallError.CompilationErrorRequest ->
                output.encodeSerializableValue(
                    FunctionCallError.CompilationErrorRequest.serializer(),
                    value,
                )
            is FunctionCallError.LinkError ->
                output.encodeSerializableValue(
                    FunctionCallError.LinkError.serializer(),
                    value,
                )
            is FunctionCallError.MethodResolveErrorRequest ->
                output.encodeSerializableValue(
                    FunctionCallError.MethodResolveErrorRequest.serializer(),
                    value,
                )
            is FunctionCallError.WasmTrapRequest ->
                output.encodeSerializableValue(
                    FunctionCallError.WasmTrapRequest.serializer(),
                    value,
                )
            is FunctionCallError.HostErrorRequest ->
                output.encodeSerializableValue(
                    FunctionCallError.HostErrorRequest.serializer(),
                    value,
                )
            is FunctionCallError.ExecutionError ->
                output.encodeSerializableValue(
                    FunctionCallError.ExecutionError.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): FunctionCallError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "WasmUnknownError" -> FunctionCallError.Wasmunknownerror
            "CompilationError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FunctionCallError.CompilationErrorRequest.serializer(),
                    element,
                )
            "LinkError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FunctionCallError.LinkError.serializer(),
                    element,
                )
            "MethodResolveError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FunctionCallError.MethodResolveErrorRequest.serializer(),
                    element,
                )
            "WasmTrap" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FunctionCallError.WasmTrapRequest.serializer(),
                    element,
                )
            "HostError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FunctionCallError.HostErrorRequest.serializer(),
                    element,
                )
            "ExecutionError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    FunctionCallError.ExecutionError.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in FunctionCallError: $element")
        }
    }
}

@Serializable(with = GlobalContractDeployModeSerializer::class)
sealed interface GlobalContractDeployMode {
    @Serializable
    @SerialName("CodeHash")
    object Codehash : GlobalContractDeployMode

    @Serializable
    @SerialName("AccountId")
    object Accountid : GlobalContractDeployMode
}

// Custom serializer for GlobalContractDeployMode to handle NEAR's externally-tagged union format
object GlobalContractDeployModeSerializer : KSerializer<GlobalContractDeployMode> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GlobalContractDeployMode")

    override fun serialize(
        encoder: Encoder,
        value: GlobalContractDeployMode,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is GlobalContractDeployMode.Codehash ->
                output.encodeJsonElement(
                    buildJsonObject { put("CodeHash", JsonNull) },
                )
            is GlobalContractDeployMode.Accountid ->
                output.encodeJsonElement(
                    buildJsonObject { put("AccountId", JsonNull) },
                )
        }
    }

    override fun deserialize(decoder: Decoder): GlobalContractDeployMode {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "CodeHash" in element -> GlobalContractDeployMode.Codehash
            "AccountId" in element -> GlobalContractDeployMode.Accountid
            else -> throw SerializationException("Unknown variant in GlobalContractDeployMode: ${element.keys}")
        }
    }
}

@Serializable(with = GlobalContractIdentifierSerializer::class)
sealed interface GlobalContractIdentifier {
    @Serializable
    data class CodeHash(
        @SerialName("CodeHash")
        val codeHash: CryptoHash,
    ) : GlobalContractIdentifier

    @Serializable
    data class AccountIdRequest(
        @SerialName("AccountId")
        val accountId: AccountId,
    ) : GlobalContractIdentifier
}

// Custom serializer for GlobalContractIdentifier to handle content-based polymorphism
object GlobalContractIdentifierSerializer : JsonContentPolymorphicSerializer<GlobalContractIdentifier>(
    GlobalContractIdentifier::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<GlobalContractIdentifier> {
        return when {
            "CodeHash" in element.jsonObject -> GlobalContractIdentifier.CodeHash.serializer()
            "AccountId" in element.jsonObject -> GlobalContractIdentifier.AccountIdRequest.serializer()
            else -> throw SerializationException(
                "Unknown variant in GlobalContractIdentifier: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = GlobalContractIdentifierViewSerializer::class)
sealed interface GlobalContractIdentifierView {
    @Serializable
    @JvmInline
    value class CryptoHashVariant(
        val value: CryptoHash,
    ) : GlobalContractIdentifierView

    @Serializable
    @JvmInline
    value class AccountIdVariant(
        val value: AccountId,
    ) : GlobalContractIdentifierView
}

// Custom serializer for GlobalContractIdentifierView to handle content-based polymorphism
object GlobalContractIdentifierViewSerializer : JsonContentPolymorphicSerializer<GlobalContractIdentifierView>(
    GlobalContractIdentifierView::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<GlobalContractIdentifierView> {
        return when {
            element is JsonPrimitive && element.isString -> GlobalContractIdentifierView.CryptoHashVariant.serializer()
            element is JsonPrimitive && element.isString -> GlobalContractIdentifierView.AccountIdVariant.serializer()
            else -> throw SerializationException(
                "Unknown variant in GlobalContractIdentifierView: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = HostErrorSerializer::class)
sealed interface HostError {
    @Serializable
    @SerialName("BadUTF16")
    object Badutf16 : HostError

    @Serializable
    @SerialName("BadUTF8")
    object Badutf8 : HostError

    @Serializable
    @SerialName("GasExceeded")
    object Gasexceeded : HostError

    @Serializable
    @SerialName("GasLimitExceeded")
    object Gaslimitexceeded : HostError

    @Serializable
    @SerialName("BalanceExceeded")
    object Balanceexceeded : HostError

    @Serializable
    @SerialName("EmptyMethodName")
    object Emptymethodname : HostError

    @Serializable
    data class GuestPanic(
        @SerialName("panic_msg")
        val panicMsg: String,
    ) : HostError

    @Serializable
    @SerialName("IntegerOverflow")
    object Integeroverflow : HostError

    @Serializable
    data class InvalidPromiseIndex(
        @SerialName("promise_idx")
        val promiseIdx: Long,
    ) : HostError

    @Serializable
    @SerialName("CannotAppendActionToJointPromise")
    object Cannotappendactiontojointpromise : HostError

    @Serializable
    @SerialName("CannotReturnJointPromise")
    object Cannotreturnjointpromise : HostError

    @Serializable
    data class InvalidPromiseResultIndex(
        @SerialName("result_idx")
        val resultIdx: Long,
    ) : HostError

    @Serializable
    data class InvalidRegisterId(
        @SerialName("register_id")
        val registerId: Long,
    ) : HostError

    @Serializable
    data class IteratorWasInvalidated(
        @SerialName("iterator_index")
        val iteratorIndex: Long,
    ) : HostError

    @Serializable
    @SerialName("MemoryAccessViolation")
    object Memoryaccessviolation : HostError

    @Serializable
    data class InvalidReceiptIndex(
        @SerialName("receipt_index")
        val receiptIndex: Long,
    ) : HostError

    @Serializable
    data class InvalidIteratorIndex(
        @SerialName("iterator_index")
        val iteratorIndex: Long,
    ) : HostError

    @Serializable
    @SerialName("InvalidAccountId")
    object Invalidaccountid : HostError

    @Serializable
    @SerialName("InvalidMethodName")
    object Invalidmethodname : HostError

    @Serializable
    @SerialName("InvalidPublicKey")
    object Invalidpublickey : HostError

    @Serializable
    data class ProhibitedInView(
        @SerialName("method_name")
        val methodName: String,
    ) : HostError

    @Serializable
    data class NumberOfLogsExceeded(
        @SerialName("limit")
        val limit: Long,
    ) : HostError

    @Serializable
    data class KeyLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : HostError

    @Serializable
    data class ValueLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : HostError

    @Serializable
    data class TotalLogLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : HostError

    @Serializable
    data class NumberPromisesExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("number_of_promises")
        val numberOfPromises: Long,
    ) : HostError

    @Serializable
    data class NumberInputDataDependenciesExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("number_of_input_data_dependencies")
        val numberOfInputDataDependencies: Long,
    ) : HostError

    @Serializable
    data class ReturnedValueLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : HostError

    @Serializable
    data class ContractSizeExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("size")
        val size: Long,
    ) : HostError

    @Serializable
    data class Deprecated(
        @SerialName("method_name")
        val methodName: String,
    ) : HostError

    @Serializable
    data class ECRecoverError(
        @SerialName("msg")
        val msg: String,
    ) : HostError

    @Serializable
    data class AltBn128InvalidInput(
        @SerialName("msg")
        val msg: String,
    ) : HostError

    @Serializable
    data class Ed25519VerifyInvalidInput(
        @SerialName("msg")
        val msg: String,
    ) : HostError
}

// Custom serializer for HostError to handle NEAR's externally-tagged union format
object HostErrorSerializer : KSerializer<HostError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("HostError")

    override fun serialize(
        encoder: Encoder,
        value: HostError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is HostError.Badutf16 -> output.encodeJsonElement(buildJsonObject { put("BadUTF16", JsonNull) })
            is HostError.Badutf8 -> output.encodeJsonElement(buildJsonObject { put("BadUTF8", JsonNull) })
            is HostError.Gasexceeded -> output.encodeJsonElement(buildJsonObject { put("GasExceeded", JsonNull) })
            is HostError.Gaslimitexceeded ->
                output.encodeJsonElement(
                    buildJsonObject { put("GasLimitExceeded", JsonNull) },
                )
            is HostError.Balanceexceeded ->
                output.encodeJsonElement(
                    buildJsonObject { put("BalanceExceeded", JsonNull) },
                )
            is HostError.Emptymethodname ->
                output.encodeJsonElement(
                    buildJsonObject { put("EmptyMethodName", JsonNull) },
                )
            is HostError.GuestPanic -> output.encodeSerializableValue(HostError.GuestPanic.serializer(), value)
            is HostError.Integeroverflow ->
                output.encodeJsonElement(
                    buildJsonObject { put("IntegerOverflow", JsonNull) },
                )
            is HostError.InvalidPromiseIndex ->
                output.encodeSerializableValue(
                    HostError.InvalidPromiseIndex.serializer(),
                    value,
                )
            is HostError.Cannotappendactiontojointpromise ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("CannotAppendActionToJointPromise", JsonNull)
                    },
                )
            is HostError.Cannotreturnjointpromise ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("CannotReturnJointPromise", JsonNull)
                    },
                )
            is HostError.InvalidPromiseResultIndex ->
                output.encodeSerializableValue(
                    HostError.InvalidPromiseResultIndex.serializer(),
                    value,
                )
            is HostError.InvalidRegisterId ->
                output.encodeSerializableValue(
                    HostError.InvalidRegisterId.serializer(),
                    value,
                )
            is HostError.IteratorWasInvalidated ->
                output.encodeSerializableValue(
                    HostError.IteratorWasInvalidated.serializer(),
                    value,
                )
            is HostError.Memoryaccessviolation ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("MemoryAccessViolation", JsonNull)
                    },
                )
            is HostError.InvalidReceiptIndex ->
                output.encodeSerializableValue(
                    HostError.InvalidReceiptIndex.serializer(),
                    value,
                )
            is HostError.InvalidIteratorIndex ->
                output.encodeSerializableValue(
                    HostError.InvalidIteratorIndex.serializer(),
                    value,
                )
            is HostError.Invalidaccountid ->
                output.encodeJsonElement(
                    buildJsonObject { put("InvalidAccountId", JsonNull) },
                )
            is HostError.Invalidmethodname ->
                output.encodeJsonElement(
                    buildJsonObject { put("InvalidMethodName", JsonNull) },
                )
            is HostError.Invalidpublickey ->
                output.encodeJsonElement(
                    buildJsonObject { put("InvalidPublicKey", JsonNull) },
                )
            is HostError.ProhibitedInView ->
                output.encodeSerializableValue(
                    HostError.ProhibitedInView.serializer(),
                    value,
                )
            is HostError.NumberOfLogsExceeded ->
                output.encodeSerializableValue(
                    HostError.NumberOfLogsExceeded.serializer(),
                    value,
                )
            is HostError.KeyLengthExceeded ->
                output.encodeSerializableValue(
                    HostError.KeyLengthExceeded.serializer(),
                    value,
                )
            is HostError.ValueLengthExceeded ->
                output.encodeSerializableValue(
                    HostError.ValueLengthExceeded.serializer(),
                    value,
                )
            is HostError.TotalLogLengthExceeded ->
                output.encodeSerializableValue(
                    HostError.TotalLogLengthExceeded.serializer(),
                    value,
                )
            is HostError.NumberPromisesExceeded ->
                output.encodeSerializableValue(
                    HostError.NumberPromisesExceeded.serializer(),
                    value,
                )
            is HostError.NumberInputDataDependenciesExceeded ->
                output.encodeSerializableValue(
                    HostError.NumberInputDataDependenciesExceeded.serializer(),
                    value,
                )
            is HostError.ReturnedValueLengthExceeded ->
                output.encodeSerializableValue(
                    HostError.ReturnedValueLengthExceeded.serializer(),
                    value,
                )
            is HostError.ContractSizeExceeded ->
                output.encodeSerializableValue(
                    HostError.ContractSizeExceeded.serializer(),
                    value,
                )
            is HostError.Deprecated -> output.encodeSerializableValue(HostError.Deprecated.serializer(), value)
            is HostError.ECRecoverError -> output.encodeSerializableValue(HostError.ECRecoverError.serializer(), value)
            is HostError.AltBn128InvalidInput ->
                output.encodeSerializableValue(
                    HostError.AltBn128InvalidInput.serializer(),
                    value,
                )
            is HostError.Ed25519VerifyInvalidInput ->
                output.encodeSerializableValue(
                    HostError.Ed25519VerifyInvalidInput.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): HostError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "BadUTF16" in element -> HostError.Badutf16
            "BadUTF8" in element -> HostError.Badutf8
            "GasExceeded" in element -> HostError.Gasexceeded
            "GasLimitExceeded" in element -> HostError.Gaslimitexceeded
            "BalanceExceeded" in element -> HostError.Balanceexceeded
            "EmptyMethodName" in element -> HostError.Emptymethodname
            "GuestPanic" in element ->
                input.json.decodeFromJsonElement(
                    HostError.GuestPanic.serializer(),
                    element["GuestPanic"]!!,
                )
            "IntegerOverflow" in element -> HostError.Integeroverflow
            "InvalidPromiseIndex" in element ->
                input.json.decodeFromJsonElement(
                    HostError.InvalidPromiseIndex.serializer(),
                    element["InvalidPromiseIndex"]!!,
                )
            "CannotAppendActionToJointPromise" in element -> HostError.Cannotappendactiontojointpromise
            "CannotReturnJointPromise" in element -> HostError.Cannotreturnjointpromise
            "InvalidPromiseResultIndex" in element ->
                input.json.decodeFromJsonElement(
                    HostError.InvalidPromiseResultIndex.serializer(),
                    element["InvalidPromiseResultIndex"]!!,
                )
            "InvalidRegisterId" in element ->
                input.json.decodeFromJsonElement(
                    HostError.InvalidRegisterId.serializer(),
                    element["InvalidRegisterId"]!!,
                )
            "IteratorWasInvalidated" in element ->
                input.json.decodeFromJsonElement(
                    HostError.IteratorWasInvalidated.serializer(),
                    element["IteratorWasInvalidated"]!!,
                )
            "MemoryAccessViolation" in element -> HostError.Memoryaccessviolation
            "InvalidReceiptIndex" in element ->
                input.json.decodeFromJsonElement(
                    HostError.InvalidReceiptIndex.serializer(),
                    element["InvalidReceiptIndex"]!!,
                )
            "InvalidIteratorIndex" in element ->
                input.json.decodeFromJsonElement(
                    HostError.InvalidIteratorIndex.serializer(),
                    element["InvalidIteratorIndex"]!!,
                )
            "InvalidAccountId" in element -> HostError.Invalidaccountid
            "InvalidMethodName" in element -> HostError.Invalidmethodname
            "InvalidPublicKey" in element -> HostError.Invalidpublickey
            "ProhibitedInView" in element ->
                input.json.decodeFromJsonElement(
                    HostError.ProhibitedInView.serializer(),
                    element["ProhibitedInView"]!!,
                )
            "NumberOfLogsExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.NumberOfLogsExceeded.serializer(),
                    element["NumberOfLogsExceeded"]!!,
                )
            "KeyLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.KeyLengthExceeded.serializer(),
                    element["KeyLengthExceeded"]!!,
                )
            "ValueLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.ValueLengthExceeded.serializer(),
                    element["ValueLengthExceeded"]!!,
                )
            "TotalLogLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.TotalLogLengthExceeded.serializer(),
                    element["TotalLogLengthExceeded"]!!,
                )
            "NumberPromisesExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.NumberPromisesExceeded.serializer(),
                    element["NumberPromisesExceeded"]!!,
                )
            "NumberInputDataDependenciesExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.NumberInputDataDependenciesExceeded.serializer(),
                    element["NumberInputDataDependenciesExceeded"]!!,
                )
            "ReturnedValueLengthExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.ReturnedValueLengthExceeded.serializer(),
                    element["ReturnedValueLengthExceeded"]!!,
                )
            "ContractSizeExceeded" in element ->
                input.json.decodeFromJsonElement(
                    HostError.ContractSizeExceeded.serializer(),
                    element["ContractSizeExceeded"]!!,
                )
            "Deprecated" in element ->
                input.json.decodeFromJsonElement(
                    HostError.Deprecated.serializer(),
                    element["Deprecated"]!!,
                )
            "ECRecoverError" in element ->
                input.json.decodeFromJsonElement(
                    HostError.ECRecoverError.serializer(),
                    element["ECRecoverError"]!!,
                )
            "AltBn128InvalidInput" in element ->
                input.json.decodeFromJsonElement(
                    HostError.AltBn128InvalidInput.serializer(),
                    element["AltBn128InvalidInput"]!!,
                )
            "Ed25519VerifyInvalidInput" in element ->
                input.json.decodeFromJsonElement(
                    HostError.Ed25519VerifyInvalidInput.serializer(),
                    element["Ed25519VerifyInvalidInput"]!!,
                )
            else -> throw SerializationException("Unknown variant in HostError: ${element.keys}")
        }
    }
}

@Serializable(with = InvalidAccessKeyErrorSerializer::class)
sealed interface InvalidAccessKeyError {
    @Serializable
    data class AccessKeyNotFound(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : InvalidAccessKeyError

    @Serializable
    data class ReceiverMismatch(
        @SerialName("ak_receiver")
        val akReceiver: String,
        @SerialName("tx_receiver")
        val txReceiver: AccountId,
    ) : InvalidAccessKeyError

    @Serializable
    data class MethodNameMismatch(
        @SerialName("method_name")
        val methodName: String,
    ) : InvalidAccessKeyError

    @Serializable
    @SerialName("RequiresFullAccess")
    object Requiresfullaccess : InvalidAccessKeyError

    @Serializable
    data class NotEnoughAllowance(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("allowance")
        val allowance: NearToken,
        @SerialName("cost")
        val cost: NearToken,
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : InvalidAccessKeyError

    @Serializable
    @SerialName("DepositWithFunctionCall")
    object Depositwithfunctioncall : InvalidAccessKeyError
}

// Custom serializer for InvalidAccessKeyError to handle NEAR's externally-tagged union format
object InvalidAccessKeyErrorSerializer : KSerializer<InvalidAccessKeyError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InvalidAccessKeyError")

    override fun serialize(
        encoder: Encoder,
        value: InvalidAccessKeyError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is InvalidAccessKeyError.AccessKeyNotFound ->
                output.encodeSerializableValue(
                    InvalidAccessKeyError.AccessKeyNotFound.serializer(),
                    value,
                )
            is InvalidAccessKeyError.ReceiverMismatch ->
                output.encodeSerializableValue(
                    InvalidAccessKeyError.ReceiverMismatch.serializer(),
                    value,
                )
            is InvalidAccessKeyError.MethodNameMismatch ->
                output.encodeSerializableValue(
                    InvalidAccessKeyError.MethodNameMismatch.serializer(),
                    value,
                )
            is InvalidAccessKeyError.Requiresfullaccess ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("RequiresFullAccess", JsonNull)
                    },
                )
            is InvalidAccessKeyError.NotEnoughAllowance ->
                output.encodeSerializableValue(
                    InvalidAccessKeyError.NotEnoughAllowance.serializer(),
                    value,
                )
            is InvalidAccessKeyError.Depositwithfunctioncall ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("DepositWithFunctionCall", JsonNull)
                    },
                )
        }
    }

    override fun deserialize(decoder: Decoder): InvalidAccessKeyError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "AccessKeyNotFound" in element ->
                input.json.decodeFromJsonElement(
                    InvalidAccessKeyError.AccessKeyNotFound.serializer(),
                    element["AccessKeyNotFound"]!!,
                )
            "ReceiverMismatch" in element ->
                input.json.decodeFromJsonElement(
                    InvalidAccessKeyError.ReceiverMismatch.serializer(),
                    element["ReceiverMismatch"]!!,
                )
            "MethodNameMismatch" in element ->
                input.json.decodeFromJsonElement(
                    InvalidAccessKeyError.MethodNameMismatch.serializer(),
                    element["MethodNameMismatch"]!!,
                )
            "RequiresFullAccess" in element -> InvalidAccessKeyError.Requiresfullaccess
            "NotEnoughAllowance" in element ->
                input.json.decodeFromJsonElement(
                    InvalidAccessKeyError.NotEnoughAllowance.serializer(),
                    element["NotEnoughAllowance"]!!,
                )
            "DepositWithFunctionCall" in element -> InvalidAccessKeyError.Depositwithfunctioncall
            else -> throw SerializationException("Unknown variant in InvalidAccessKeyError: ${element.keys}")
        }
    }
}

/**
 * An error happened during TX execution
 */
@Serializable(with = InvalidTxErrorSerializer::class)
sealed interface InvalidTxError {
    @Serializable
    data class InvalidAccessKeyErrorRequest(
        @SerialName("InvalidAccessKeyError")
        val invalidAccessKeyError: InvalidAccessKeyError,
    ) : InvalidTxError

    @Serializable
    data class InvalidSignerId(
        @SerialName("signer_id")
        val signerId: String,
    ) : InvalidTxError

    @Serializable
    data class SignerDoesNotExist(
        @SerialName("signer_id")
        val signerId: AccountId,
    ) : InvalidTxError

    @Serializable
    data class InvalidNonce(
        @SerialName("ak_nonce")
        val akNonce: Long,
        @SerialName("tx_nonce")
        val txNonce: Long,
    ) : InvalidTxError

    @Serializable
    data class NonceTooLarge(
        @SerialName("tx_nonce")
        val txNonce: Long,
        @SerialName("upper_bound")
        val upperBound: Long,
    ) : InvalidTxError

    @Serializable
    data class InvalidReceiverId(
        @SerialName("receiver_id")
        val receiverId: String,
    ) : InvalidTxError

    @Serializable
    @SerialName("InvalidSignature")
    object Invalidsignature : InvalidTxError

    @Serializable
    data class NotEnoughBalance(
        @SerialName("balance")
        val balance: NearToken,
        @SerialName("cost")
        val cost: NearToken,
        @SerialName("signer_id")
        val signerId: AccountId,
    ) : InvalidTxError

    @Serializable
    data class LackBalanceForState(
        @SerialName("amount")
        val amount: NearToken,
        @SerialName("signer_id")
        val signerId: AccountId,
    ) : InvalidTxError

    @Serializable
    @SerialName("CostOverflow")
    object Costoverflow : InvalidTxError

    @Serializable
    @SerialName("InvalidChain")
    object Invalidchain : InvalidTxError

    @Serializable
    @SerialName("Expired")
    object Expired : InvalidTxError

    @Serializable
    data class ActionsValidation(
        @SerialName("ActionsValidation")
        val actionsValidation: ActionsValidationError,
    ) : InvalidTxError

    @Serializable
    data class TransactionSizeExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("size")
        val size: Long,
    ) : InvalidTxError

    @Serializable
    @SerialName("InvalidTransactionVersion")
    object Invalidtransactionversion : InvalidTxError

    @Serializable
    data class StorageErrorRequest(
        @SerialName("StorageError")
        val storageError: StorageError,
    ) : InvalidTxError

    @Serializable
    data class ShardCongested(
        @SerialName("congestion_level")
        val congestionLevel: Double,
        @SerialName("shard_id")
        val shardId: Long,
    ) : InvalidTxError

    @Serializable
    data class ShardStuck(
        @SerialName("missed_chunks")
        val missedChunks: Long,
        @SerialName("shard_id")
        val shardId: Long,
    ) : InvalidTxError
}

// Custom serializer for InvalidTxError to handle content-based polymorphism
object InvalidTxErrorSerializer : KSerializer<InvalidTxError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InvalidTxError")

    override fun serialize(
        encoder: Encoder,
        value: InvalidTxError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is InvalidTxError.Invalidsignature -> output.encodeJsonElement(JsonPrimitive("InvalidSignature"))
            is InvalidTxError.Costoverflow -> output.encodeJsonElement(JsonPrimitive("CostOverflow"))
            is InvalidTxError.Invalidchain -> output.encodeJsonElement(JsonPrimitive("InvalidChain"))
            is InvalidTxError.Expired -> output.encodeJsonElement(JsonPrimitive("Expired"))
            is InvalidTxError.Invalidtransactionversion ->
                output.encodeJsonElement(
                    JsonPrimitive("InvalidTransactionVersion"),
                )
            is InvalidTxError.InvalidAccessKeyErrorRequest ->
                output.encodeSerializableValue(
                    InvalidTxError.InvalidAccessKeyErrorRequest.serializer(),
                    value,
                )
            is InvalidTxError.InvalidSignerId ->
                output.encodeSerializableValue(
                    InvalidTxError.InvalidSignerId.serializer(),
                    value,
                )
            is InvalidTxError.SignerDoesNotExist ->
                output.encodeSerializableValue(
                    InvalidTxError.SignerDoesNotExist.serializer(),
                    value,
                )
            is InvalidTxError.InvalidNonce ->
                output.encodeSerializableValue(
                    InvalidTxError.InvalidNonce.serializer(),
                    value,
                )
            is InvalidTxError.NonceTooLarge ->
                output.encodeSerializableValue(
                    InvalidTxError.NonceTooLarge.serializer(),
                    value,
                )
            is InvalidTxError.InvalidReceiverId ->
                output.encodeSerializableValue(
                    InvalidTxError.InvalidReceiverId.serializer(),
                    value,
                )
            is InvalidTxError.NotEnoughBalance ->
                output.encodeSerializableValue(
                    InvalidTxError.NotEnoughBalance.serializer(),
                    value,
                )
            is InvalidTxError.LackBalanceForState ->
                output.encodeSerializableValue(
                    InvalidTxError.LackBalanceForState.serializer(),
                    value,
                )
            is InvalidTxError.ActionsValidation ->
                output.encodeSerializableValue(
                    InvalidTxError.ActionsValidation.serializer(),
                    value,
                )
            is InvalidTxError.TransactionSizeExceeded ->
                output.encodeSerializableValue(
                    InvalidTxError.TransactionSizeExceeded.serializer(),
                    value,
                )
            is InvalidTxError.StorageErrorRequest ->
                output.encodeSerializableValue(
                    InvalidTxError.StorageErrorRequest.serializer(),
                    value,
                )
            is InvalidTxError.ShardCongested ->
                output.encodeSerializableValue(
                    InvalidTxError.ShardCongested.serializer(),
                    value,
                )
            is InvalidTxError.ShardStuck ->
                output.encodeSerializableValue(
                    InvalidTxError.ShardStuck.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): InvalidTxError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "InvalidSignature" -> InvalidTxError.Invalidsignature
            element is JsonPrimitive && element.content == "CostOverflow" -> InvalidTxError.Costoverflow
            element is JsonPrimitive && element.content == "InvalidChain" -> InvalidTxError.Invalidchain
            element is JsonPrimitive && element.content == "Expired" -> InvalidTxError.Expired
            element is JsonPrimitive && element.content == "InvalidTransactionVersion" -> InvalidTxError.Invalidtransactionversion
            "InvalidAccessKeyError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.InvalidAccessKeyErrorRequest.serializer(),
                    element,
                )
            "InvalidSignerId" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.InvalidSignerId.serializer(),
                    element,
                )
            "SignerDoesNotExist" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.SignerDoesNotExist.serializer(),
                    element,
                )
            "InvalidNonce" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.InvalidNonce.serializer(),
                    element,
                )
            "NonceTooLarge" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.NonceTooLarge.serializer(),
                    element,
                )
            "InvalidReceiverId" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.InvalidReceiverId.serializer(),
                    element,
                )
            "NotEnoughBalance" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.NotEnoughBalance.serializer(),
                    element,
                )
            "LackBalanceForState" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.LackBalanceForState.serializer(),
                    element,
                )
            "ActionsValidation" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.ActionsValidation.serializer(),
                    element,
                )
            "TransactionSizeExceeded" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.TransactionSizeExceeded.serializer(),
                    element,
                )
            "StorageError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.StorageErrorRequest.serializer(),
                    element,
                )
            "ShardCongested" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.ShardCongested.serializer(),
                    element,
                )
            "ShardStuck" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    InvalidTxError.ShardStuck.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in InvalidTxError: $element")
        }
    }
}

@Serializable(with = JsonRpcResponseForArrayOfRangeOfUint64AndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForArrayOfRangeOfUint64AndRpcError {
    @Serializable
    data class Result(
        @SerialName("result")
        val result: List<RangeOfUint64>,
    ) : JsonRpcResponseForArrayOfRangeOfUint64AndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForArrayOfRangeOfUint64AndRpcError
}

// Custom serializer for JsonRpcResponseForArrayOfRangeOfUint64AndRpcError to handle content-based polymorphism
object JsonRpcResponseForArrayOfRangeOfUint64AndRpcErrorSerializer : JsonContentPolymorphicSerializer<JsonRpcResponseForArrayOfRangeOfUint64AndRpcError>(
    JsonRpcResponseForArrayOfRangeOfUint64AndRpcError::class,
) {
    override fun selectDeserializer(
        element: JsonElement,
    ): DeserializationStrategy<JsonRpcResponseForArrayOfRangeOfUint64AndRpcError> {
        return when {
            "result" in element.jsonObject -> JsonRpcResponseForArrayOfRangeOfUint64AndRpcError.Result.serializer()
            "error" in element.jsonObject -> JsonRpcResponseForArrayOfRangeOfUint64AndRpcError.Error.serializer()
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForArrayOfRangeOfUint64AndRpcError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForArrayOfValidatorStakeViewAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError {
    @Serializable
    data class Result(
        @SerialName("result")
        val result: List<ValidatorStakeView>,
    ) : JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError
}

// Custom serializer for JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError to handle content-based polymorphism
object JsonRpcResponseForArrayOfValidatorStakeViewAndRpcErrorSerializer : JsonContentPolymorphicSerializer<JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError>(
    JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError::class,
) {
    override fun selectDeserializer(
        element: JsonElement,
    ): DeserializationStrategy<JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError> {
        return when {
            "result" in element.jsonObject -> JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError.Result.serializer()
            "error" in element.jsonObject -> JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError.Error.serializer()
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForCryptoHashAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForCryptoHashAndRpcError {
    @Serializable
    data class Result(
        @SerialName("result")
        val result: CryptoHash,
    ) : JsonRpcResponseForCryptoHashAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForCryptoHashAndRpcError
}

// Custom serializer for JsonRpcResponseForCryptoHashAndRpcError to handle content-based polymorphism
object JsonRpcResponseForCryptoHashAndRpcErrorSerializer : JsonContentPolymorphicSerializer<JsonRpcResponseForCryptoHashAndRpcError>(
    JsonRpcResponseForCryptoHashAndRpcError::class,
) {
    override fun selectDeserializer(
        element: JsonElement,
    ): DeserializationStrategy<JsonRpcResponseForCryptoHashAndRpcError> {
        return when {
            "result" in element.jsonObject -> JsonRpcResponseForCryptoHashAndRpcError.Result.serializer()
            "error" in element.jsonObject -> JsonRpcResponseForCryptoHashAndRpcError.Error.serializer()
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForCryptoHashAndRpcError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForGenesisConfigAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForGenesisConfigAndRpcError {
    @Serializable
    data class Result(
        @SerialName("avg_hidden_validator_seats_per_shard")
        val avgHiddenValidatorSeatsPerShard: List<Long>,
        @SerialName("block_producer_kickout_threshold")
        val blockProducerKickoutThreshold: Long,
        @SerialName("chain_id")
        val chainId: String,
        @SerialName("chunk_producer_assignment_changes_limit")
        val chunkProducerAssignmentChangesLimit: Long? = null,
        @SerialName("chunk_producer_kickout_threshold")
        val chunkProducerKickoutThreshold: Long,
        @SerialName("chunk_validator_only_kickout_threshold")
        val chunkValidatorOnlyKickoutThreshold: Long? = null,
        @SerialName("dynamic_resharding")
        val dynamicResharding: Boolean,
        @SerialName("epoch_length")
        val epochLength: Long,
        @SerialName("fishermen_threshold")
        val fishermenThreshold: NearToken,
        @SerialName("gas_limit")
        val gasLimit: NearGas,
        @SerialName("gas_price_adjustment_rate")
        val gasPriceAdjustmentRate: List<Int>,
        @SerialName("genesis_height")
        val genesisHeight: Long,
        @SerialName("genesis_time")
        val genesisTime: String,
        @SerialName("max_gas_price")
        val maxGasPrice: NearToken,
        @SerialName("max_inflation_rate")
        val maxInflationRate: List<Int>,
        @SerialName("max_kickout_stake_perc")
        val maxKickoutStakePerc: Long? = null,
        @SerialName("min_gas_price")
        val minGasPrice: NearToken,
        @SerialName("minimum_stake_divisor")
        val minimumStakeDivisor: Long? = null,
        @SerialName("minimum_stake_ratio")
        val minimumStakeRatio: List<Int>? = null,
        @SerialName("minimum_validators_per_shard")
        val minimumValidatorsPerShard: Long? = null,
        @SerialName("num_block_producer_seats")
        val numBlockProducerSeats: Long,
        @SerialName("num_block_producer_seats_per_shard")
        val numBlockProducerSeatsPerShard: List<Long>,
        @SerialName("num_blocks_per_year")
        val numBlocksPerYear: Long,
        @SerialName("num_chunk_only_producer_seats")
        val numChunkOnlyProducerSeats: Long? = null,
        @SerialName("num_chunk_producer_seats")
        val numChunkProducerSeats: Long? = null,
        @SerialName("num_chunk_validator_seats")
        val numChunkValidatorSeats: Long? = null,
        @SerialName("online_max_threshold")
        val onlineMaxThreshold: List<Int>? = null,
        @SerialName("online_min_threshold")
        val onlineMinThreshold: List<Int>? = null,
        @SerialName("protocol_reward_rate")
        val protocolRewardRate: List<Int>,
        @SerialName("protocol_treasury_account")
        val protocolTreasuryAccount: AccountId,
        @SerialName("protocol_upgrade_stake_threshold")
        val protocolUpgradeStakeThreshold: List<Int>? = null,
        @SerialName("protocol_version")
        val protocolVersion: Long,
        @SerialName("shard_layout")
        val shardLayout: ShardLayout? = null,
        @SerialName("shuffle_shard_assignment_for_chunk_producers")
        val shuffleShardAssignmentForChunkProducers: Boolean? = null,
        @SerialName("target_validator_mandates_per_shard")
        val targetValidatorMandatesPerShard: Long? = null,
        @SerialName("total_supply")
        val totalSupply: NearToken,
        @SerialName("transaction_validity_period")
        val transactionValidityPeriod: Long,
        @SerialName("use_production_config")
        val useProductionConfig: Boolean? = null,
        @SerialName("validators")
        val validators: List<AccountInfo>,
    ) : JsonRpcResponseForGenesisConfigAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForGenesisConfigAndRpcError
}

// Custom serializer for JsonRpcResponseForGenesisConfigAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForGenesisConfigAndRpcErrorSerializer : KSerializer<JsonRpcResponseForGenesisConfigAndRpcError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("JsonRpcResponseForGenesisConfigAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForGenesisConfigAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForGenesisConfigAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForGenesisConfigAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForGenesisConfigAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForGenesisConfigAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForGenesisConfigAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForGenesisConfigAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForGenesisConfigAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForGenesisConfigAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForNullableRpcHealthResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForNullableRpcHealthResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("result")
        val result: RpcHealthResponse?,
    ) : JsonRpcResponseForNullableRpcHealthResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForNullableRpcHealthResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForNullableRpcHealthResponseAndRpcError to handle content-based polymorphism
object JsonRpcResponseForNullableRpcHealthResponseAndRpcErrorSerializer : JsonContentPolymorphicSerializer<JsonRpcResponseForNullableRpcHealthResponseAndRpcError>(
    JsonRpcResponseForNullableRpcHealthResponseAndRpcError::class,
) {
    override fun selectDeserializer(
        element: JsonElement,
    ): DeserializationStrategy<JsonRpcResponseForNullableRpcHealthResponseAndRpcError> {
        return when {
            "result" in element.jsonObject -> JsonRpcResponseForNullableRpcHealthResponseAndRpcError.Result.serializer()
            "error" in element.jsonObject -> JsonRpcResponseForNullableRpcHealthResponseAndRpcError.Error.serializer()
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForNullableRpcHealthResponseAndRpcError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcBlockResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcBlockResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("author")
        val author: AccountId,
        @SerialName("chunks")
        val chunks: List<ChunkHeaderView>,
        @SerialName("header")
        val header: BlockHeaderView,
    ) : JsonRpcResponseForRpcBlockResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcBlockResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcBlockResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcBlockResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcBlockResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcBlockResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcBlockResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcBlockResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcBlockResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcBlockResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcBlockResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcBlockResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcBlockResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcBlockResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcBlockResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcChunkResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcChunkResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("author")
        val author: AccountId,
        @SerialName("header")
        val header: ChunkHeaderView,
        @SerialName("receipts")
        val receipts: List<ReceiptView>,
        @SerialName("transactions")
        val transactions: List<SignedTransactionView>,
    ) : JsonRpcResponseForRpcChunkResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcChunkResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcChunkResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcChunkResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcChunkResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcChunkResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcChunkResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcChunkResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcChunkResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcChunkResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcChunkResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcChunkResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcChunkResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcChunkResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcChunkResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcClientConfigResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcClientConfigResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("archive")
        val archive: Boolean,
        @SerialName("block_fetch_horizon")
        val blockFetchHorizon: Long,
        @SerialName("block_header_fetch_horizon")
        val blockHeaderFetchHorizon: Long,
        @SerialName("block_production_tracking_delay")
        val blockProductionTrackingDelay: List<Long>,
        @SerialName("catchup_step_period")
        val catchupStepPeriod: List<Long>,
        @SerialName("chain_id")
        val chainId: String,
        @SerialName("chunk_distribution_network")
        val chunkDistributionNetwork: ChunkDistributionNetworkConfig? = null,
        @SerialName("chunk_request_retry_period")
        val chunkRequestRetryPeriod: List<Long>,
        @SerialName("chunk_validation_threads")
        val chunkValidationThreads: Long,
        @SerialName("chunk_wait_mult")
        val chunkWaitMult: List<Int>,
        @SerialName("client_background_migration_threads")
        val clientBackgroundMigrationThreads: Long,
        @SerialName("cloud_archival_reader")
        val cloudArchivalReader: CloudArchivalReaderConfig? = null,
        @SerialName("cloud_archival_writer")
        val cloudArchivalWriter: CloudArchivalWriterConfig? = null,
        @SerialName("doomslug_step_period")
        val doomslugStepPeriod: List<Long>,
        @SerialName("enable_multiline_logging")
        val enableMultilineLogging: Boolean,
        @SerialName("enable_statistics_export")
        val enableStatisticsExport: Boolean,
        @SerialName("epoch_length")
        val epochLength: Long,
        @SerialName("epoch_sync")
        val epochSync: EpochSyncConfig,
        @SerialName("expected_shutdown")
        val expectedShutdown: MutableConfigValue,
        @SerialName("gc")
        val gc: GCConfig,
        @SerialName("header_sync_expected_height_per_second")
        val headerSyncExpectedHeightPerSecond: Long,
        @SerialName("header_sync_initial_timeout")
        val headerSyncInitialTimeout: List<Long>,
        @SerialName("header_sync_progress_timeout")
        val headerSyncProgressTimeout: List<Long>,
        @SerialName("header_sync_stall_ban_timeout")
        val headerSyncStallBanTimeout: List<Long>,
        @SerialName("log_summary_period")
        val logSummaryPeriod: List<Long>,
        @SerialName("log_summary_style")
        val logSummaryStyle: LogSummaryStyle,
        @SerialName("max_block_production_delay")
        val maxBlockProductionDelay: List<Long>,
        @SerialName("max_block_wait_delay")
        val maxBlockWaitDelay: List<Long>,
        @SerialName("max_gas_burnt_view")
        val maxGasBurntView: NearGas? = null,
        @SerialName("min_block_production_delay")
        val minBlockProductionDelay: List<Long>,
        @SerialName("min_num_peers")
        val minNumPeers: Long,
        @SerialName("num_block_producer_seats")
        val numBlockProducerSeats: Long,
        @SerialName("orphan_state_witness_max_size")
        val orphanStateWitnessMaxSize: Long,
        @SerialName("orphan_state_witness_pool_size")
        val orphanStateWitnessPoolSize: Long,
        @SerialName("produce_chunk_add_transactions_time_limit")
        val produceChunkAddTransactionsTimeLimit: String,
        @SerialName("produce_empty_blocks")
        val produceEmptyBlocks: Boolean,
        @SerialName("protocol_version_check")
        val protocolVersionCheck: ProtocolVersionCheckConfig,
        @SerialName("resharding_config")
        val reshardingConfig: MutableConfigValue,
        @SerialName("rpc_addr")
        val rpcAddr: String? = null,
        @SerialName("save_invalid_witnesses")
        val saveInvalidWitnesses: Boolean,
        @SerialName("save_latest_witnesses")
        val saveLatestWitnesses: Boolean,
        @SerialName("save_trie_changes")
        val saveTrieChanges: Boolean,
        @SerialName("save_tx_outcomes")
        val saveTxOutcomes: Boolean,
        @SerialName("save_untracked_partial_chunks_parts")
        val saveUntrackedPartialChunksParts: Boolean,
        @SerialName("skip_sync_wait")
        val skipSyncWait: Boolean,
        @SerialName("state_request_server_threads")
        val stateRequestServerThreads: Long,
        @SerialName("state_request_throttle_period")
        val stateRequestThrottlePeriod: List<Long>,
        @SerialName("state_requests_per_throttle_period")
        val stateRequestsPerThrottlePeriod: Long,
        @SerialName("state_sync")
        val stateSync: StateSyncConfig,
        @SerialName("state_sync_enabled")
        val stateSyncEnabled: Boolean,
        @SerialName("state_sync_external_backoff")
        val stateSyncExternalBackoff: List<Long>,
        @SerialName("state_sync_external_timeout")
        val stateSyncExternalTimeout: List<Long>,
        @SerialName("state_sync_p2p_timeout")
        val stateSyncP2PTimeout: List<Long>,
        @SerialName("state_sync_retry_backoff")
        val stateSyncRetryBackoff: List<Long>,
        @SerialName("sync_check_period")
        val syncCheckPeriod: List<Long>,
        @SerialName("sync_height_threshold")
        val syncHeightThreshold: Long,
        @SerialName("sync_max_block_requests")
        val syncMaxBlockRequests: Long,
        @SerialName("sync_step_period")
        val syncStepPeriod: List<Long>,
        @SerialName("tracked_shards_config")
        val trackedShardsConfig: TrackedShardsConfig,
        @SerialName("transaction_pool_size_limit")
        val transactionPoolSizeLimit: Long? = null,
        @SerialName("transaction_request_handler_threads")
        val transactionRequestHandlerThreads: Long,
        @SerialName("trie_viewer_state_size_limit")
        val trieViewerStateSizeLimit: Long? = null,
        @SerialName("ttl_account_id_router")
        val ttlAccountIdRouter: List<Long>,
        @SerialName("tx_routing_height_horizon")
        val txRoutingHeightHorizon: Long,
        @SerialName("version")
        val version: Version,
        @SerialName("view_client_threads")
        val viewClientThreads: Long,
    ) : JsonRpcResponseForRpcClientConfigResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcClientConfigResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcClientConfigResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcClientConfigResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcClientConfigResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcClientConfigResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcClientConfigResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcClientConfigResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcClientConfigResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcClientConfigResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcClientConfigResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcClientConfigResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcClientConfigResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcClientConfigResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcClientConfigResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcCongestionLevelResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcCongestionLevelResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("congestion_level")
        val congestionLevel: Double,
    ) : JsonRpcResponseForRpcCongestionLevelResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcCongestionLevelResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcCongestionLevelResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcCongestionLevelResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcCongestionLevelResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcCongestionLevelResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcCongestionLevelResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcCongestionLevelResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcCongestionLevelResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcCongestionLevelResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcCongestionLevelResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcCongestionLevelResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcCongestionLevelResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcCongestionLevelResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcCongestionLevelResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcGasPriceResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcGasPriceResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("gas_price")
        val gasPrice: NearToken,
    ) : JsonRpcResponseForRpcGasPriceResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcGasPriceResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcGasPriceResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcGasPriceResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcGasPriceResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcGasPriceResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcGasPriceResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcGasPriceResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcGasPriceResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcGasPriceResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcGasPriceResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcGasPriceResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcGasPriceResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcGasPriceResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcGasPriceResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("block_header_lite")
        val blockHeaderLite: LightClientBlockLiteView,
        @SerialName("block_proof")
        val blockProof: List<MerklePathItem>,
    ) : JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("block_header_lite")
        val blockHeaderLite: LightClientBlockLiteView,
        @SerialName("block_proof")
        val blockProof: List<MerklePathItem>,
        @SerialName("outcome_proof")
        val outcomeProof: ExecutionOutcomeWithIdView,
        @SerialName("outcome_root_proof")
        val outcomeRootProof: List<MerklePathItem>,
    ) : JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("approvals_after_next")
        val approvalsAfterNext: List<Signature?>? = null,
        @SerialName("inner_lite")
        val innerLite: BlockHeaderInnerLiteView? = null,
        @SerialName("inner_rest_hash")
        val innerRestHash: CryptoHash? = null,
        @SerialName("next_block_inner_hash")
        val nextBlockInnerHash: CryptoHash? = null,
        @SerialName("next_bps")
        val nextBps: List<ValidatorStakeView>? = null,
        @SerialName("prev_block_hash")
        val prevBlockHash: CryptoHash? = null,
    ) : JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcNetworkInfoResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcNetworkInfoResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("active_peers")
        val activePeers: List<RpcPeerInfo>,
        @SerialName("known_producers")
        val knownProducers: List<RpcKnownProducer>,
        @SerialName("num_active_peers")
        val numActivePeers: Long,
        @SerialName("peer_max_count")
        val peerMaxCount: Long,
        @SerialName("received_bytes_per_sec")
        val receivedBytesPerSec: Long,
        @SerialName("sent_bytes_per_sec")
        val sentBytesPerSec: Long,
    ) : JsonRpcResponseForRpcNetworkInfoResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcNetworkInfoResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcNetworkInfoResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcNetworkInfoResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcNetworkInfoResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcNetworkInfoResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcNetworkInfoResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcNetworkInfoResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcNetworkInfoResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcNetworkInfoResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcNetworkInfoResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcNetworkInfoResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcNetworkInfoResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcNetworkInfoResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcNetworkInfoResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcProtocolConfigResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcProtocolConfigResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("avg_hidden_validator_seats_per_shard")
        val avgHiddenValidatorSeatsPerShard: List<Long>,
        @SerialName("block_producer_kickout_threshold")
        val blockProducerKickoutThreshold: Long,
        @SerialName("chain_id")
        val chainId: String,
        @SerialName("chunk_producer_kickout_threshold")
        val chunkProducerKickoutThreshold: Long,
        @SerialName("chunk_validator_only_kickout_threshold")
        val chunkValidatorOnlyKickoutThreshold: Long,
        @SerialName("dynamic_resharding")
        val dynamicResharding: Boolean,
        @SerialName("epoch_length")
        val epochLength: Long,
        @SerialName("fishermen_threshold")
        val fishermenThreshold: NearToken,
        @SerialName("gas_limit")
        val gasLimit: NearGas,
        @SerialName("gas_price_adjustment_rate")
        val gasPriceAdjustmentRate: List<Int>,
        @SerialName("genesis_height")
        val genesisHeight: Long,
        @SerialName("genesis_time")
        val genesisTime: String,
        @SerialName("max_gas_price")
        val maxGasPrice: NearToken,
        @SerialName("max_inflation_rate")
        val maxInflationRate: List<Int>,
        @SerialName("max_kickout_stake_perc")
        val maxKickoutStakePerc: Long,
        @SerialName("min_gas_price")
        val minGasPrice: NearToken,
        @SerialName("minimum_stake_divisor")
        val minimumStakeDivisor: Long,
        @SerialName("minimum_stake_ratio")
        val minimumStakeRatio: List<Int>,
        @SerialName("minimum_validators_per_shard")
        val minimumValidatorsPerShard: Long,
        @SerialName("num_block_producer_seats")
        val numBlockProducerSeats: Long,
        @SerialName("num_block_producer_seats_per_shard")
        val numBlockProducerSeatsPerShard: List<Long>,
        @SerialName("num_blocks_per_year")
        val numBlocksPerYear: Long,
        @SerialName("online_max_threshold")
        val onlineMaxThreshold: List<Int>,
        @SerialName("online_min_threshold")
        val onlineMinThreshold: List<Int>,
        @SerialName("protocol_reward_rate")
        val protocolRewardRate: List<Int>,
        @SerialName("protocol_treasury_account")
        val protocolTreasuryAccount: AccountId,
        @SerialName("protocol_upgrade_stake_threshold")
        val protocolUpgradeStakeThreshold: List<Int>,
        @SerialName("protocol_version")
        val protocolVersion: Long,
        @SerialName("runtime_config")
        val runtimeConfig: RuntimeConfigView,
        @SerialName("shard_layout")
        val shardLayout: ShardLayout,
        @SerialName("shuffle_shard_assignment_for_chunk_producers")
        val shuffleShardAssignmentForChunkProducers: Boolean,
        @SerialName("target_validator_mandates_per_shard")
        val targetValidatorMandatesPerShard: Long,
        @SerialName("transaction_validity_period")
        val transactionValidityPeriod: Long,
    ) : JsonRpcResponseForRpcProtocolConfigResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcProtocolConfigResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcProtocolConfigResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcProtocolConfigResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcProtocolConfigResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcProtocolConfigResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcProtocolConfigResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcProtocolConfigResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcProtocolConfigResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcProtocolConfigResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcProtocolConfigResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcProtocolConfigResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcProtocolConfigResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcProtocolConfigResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcProtocolConfigResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcQueryResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcQueryResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("block_hash")
        val blockHash: CryptoHash,
        @SerialName("block_height")
        val blockHeight: Long,
    ) : JsonRpcResponseForRpcQueryResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcQueryResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcQueryResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcQueryResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcQueryResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcQueryResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcQueryResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcQueryResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcQueryResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcQueryResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcQueryResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcQueryResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcQueryResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcQueryResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcQueryResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcReceiptResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcReceiptResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("predecessor_id")
        val predecessorId: AccountId,
        @SerialName("priority")
        val priority: Long? = null,
        @SerialName("receipt")
        val receipt: ReceiptEnumView,
        @SerialName("receipt_id")
        val receiptId: CryptoHash,
        @SerialName("receiver_id")
        val receiverId: AccountId,
    ) : JsonRpcResponseForRpcReceiptResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcReceiptResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcReceiptResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcReceiptResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcReceiptResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcReceiptResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcReceiptResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcReceiptResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcReceiptResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcReceiptResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcReceiptResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcReceiptResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcReceiptResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcReceiptResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcReceiptResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("cold_head_height")
        val coldHeadHeight: Long? = null,
        @SerialName("final_head_height")
        val finalHeadHeight: Long? = null,
        @SerialName("head_height")
        val headHeight: Long? = null,
        @SerialName("hot_db_kind")
        val hotDbKind: String? = null,
    ) : JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("block_hash")
        val blockHash: CryptoHash,
        @SerialName("changes")
        val changes: List<StateChangeKindView>,
    ) : JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("block_hash")
        val blockHash: CryptoHash,
        @SerialName("changes")
        val changes: List<StateChangeWithCauseView>,
    ) : JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcStatusResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcStatusResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("chain_id")
        val chainId: String,
        @SerialName("detailed_debug_status")
        val detailedDebugStatus: DetailedDebugStatus? = null,
        @SerialName("genesis_hash")
        val genesisHash: CryptoHash,
        @SerialName("latest_protocol_version")
        val latestProtocolVersion: Long,
        @SerialName("node_key")
        val nodeKey: PublicKey? = null,
        @SerialName("node_public_key")
        val nodePublicKey: PublicKey,
        @SerialName("protocol_version")
        val protocolVersion: Long,
        @SerialName("rpc_addr")
        val rpcAddr: String? = null,
        @SerialName("sync_info")
        val syncInfo: StatusSyncInfo,
        @SerialName("uptime_sec")
        val uptimeSec: Long,
        @SerialName("validator_account_id")
        val validatorAccountId: AccountId? = null,
        @SerialName("validator_public_key")
        val validatorPublicKey: PublicKey? = null,
        @SerialName("validators")
        val validators: List<ValidatorInfo>,
        @SerialName("version")
        val version: Version,
    ) : JsonRpcResponseForRpcStatusResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcStatusResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcStatusResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcStatusResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcStatusResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcStatusResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcStatusResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcStatusResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcStatusResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcStatusResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcStatusResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcStatusResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcStatusResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcStatusResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcStatusResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcTransactionResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcTransactionResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("final_execution_status")
        val finalExecutionStatus: TxExecutionStatus,
    ) : JsonRpcResponseForRpcTransactionResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcTransactionResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcTransactionResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcTransactionResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcTransactionResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcTransactionResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcTransactionResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcTransactionResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcTransactionResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcTransactionResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcTransactionResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcTransactionResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcTransactionResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcTransactionResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcTransactionResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

@Serializable(with = JsonRpcResponseForRpcValidatorResponseAndRpcErrorSerializer::class)
sealed interface JsonRpcResponseForRpcValidatorResponseAndRpcError {
    @Serializable
    data class Result(
        @SerialName("current_fishermen")
        val currentFishermen: List<ValidatorStakeView>,
        @SerialName("current_proposals")
        val currentProposals: List<ValidatorStakeView>,
        @SerialName("current_validators")
        val currentValidators: List<CurrentEpochValidatorInfo>,
        @SerialName("epoch_height")
        val epochHeight: Long,
        @SerialName("epoch_start_height")
        val epochStartHeight: Long,
        @SerialName("next_fishermen")
        val nextFishermen: List<ValidatorStakeView>,
        @SerialName("next_validators")
        val nextValidators: List<NextEpochValidatorInfo>,
        @SerialName("prev_epoch_kickout")
        val prevEpochKickout: List<ValidatorKickoutView>,
    ) : JsonRpcResponseForRpcValidatorResponseAndRpcError

    @Serializable
    data class Error(
        @SerialName("cause")
        val cause: JsonElement? = null,
        @SerialName("code")
        val code: Long,
        @SerialName("data")
        val data: JsonElement? = null,
        @SerialName("message")
        val message: String,
        @SerialName("name")
        val name: JsonElement? = null,
    ) : JsonRpcResponseForRpcValidatorResponseAndRpcError
}

// Custom serializer for JsonRpcResponseForRpcValidatorResponseAndRpcError to handle NEAR's externally-tagged union format
object JsonRpcResponseForRpcValidatorResponseAndRpcErrorSerializer : KSerializer<JsonRpcResponseForRpcValidatorResponseAndRpcError> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("JsonRpcResponseForRpcValidatorResponseAndRpcError")

    override fun serialize(
        encoder: Encoder,
        value: JsonRpcResponseForRpcValidatorResponseAndRpcError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is JsonRpcResponseForRpcValidatorResponseAndRpcError.Result ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcValidatorResponseAndRpcError.Result.serializer(),
                    value,
                )
            is JsonRpcResponseForRpcValidatorResponseAndRpcError.Error ->
                output.encodeSerializableValue(
                    JsonRpcResponseForRpcValidatorResponseAndRpcError.Error.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): JsonRpcResponseForRpcValidatorResponseAndRpcError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "result" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcValidatorResponseAndRpcError.Result.serializer(),
                    element["result"]!!,
                )
            "error" in element ->
                input.json.decodeFromJsonElement(
                    JsonRpcResponseForRpcValidatorResponseAndRpcError.Error.serializer(),
                    element["error"]!!,
                )
            else -> throw SerializationException(
                "Unknown variant in JsonRpcResponseForRpcValidatorResponseAndRpcError: ${element.keys}",
            )
        }
    }
}

/**
 * Contexts in which `StorageError::MissingTrieValue` error might occur.
 */
@Serializable(with = MissingTrieValueContextSerializer::class)
sealed interface MissingTrieValueContext {
    @Serializable
    @SerialName("TrieIterator")
    object Trieiterator : MissingTrieValueContext

    @Serializable
    @SerialName("TriePrefetchingStorage")
    object Trieprefetchingstorage : MissingTrieValueContext

    @Serializable
    @SerialName("TrieMemoryPartialStorage")
    object Triememorypartialstorage : MissingTrieValueContext

    @Serializable
    @SerialName("TrieStorage")
    object Triestorage : MissingTrieValueContext
}

// Custom serializer for MissingTrieValueContext to handle NEAR's externally-tagged union format
object MissingTrieValueContextSerializer : KSerializer<MissingTrieValueContext> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MissingTrieValueContext")

    override fun serialize(
        encoder: Encoder,
        value: MissingTrieValueContext,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is MissingTrieValueContext.Trieiterator ->
                output.encodeJsonElement(
                    buildJsonObject { put("TrieIterator", JsonNull) },
                )
            is MissingTrieValueContext.Trieprefetchingstorage ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("TriePrefetchingStorage", JsonNull)
                    },
                )
            is MissingTrieValueContext.Triememorypartialstorage ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("TrieMemoryPartialStorage", JsonNull)
                    },
                )
            is MissingTrieValueContext.Triestorage ->
                output.encodeJsonElement(
                    buildJsonObject { put("TrieStorage", JsonNull) },
                )
        }
    }

    override fun deserialize(decoder: Decoder): MissingTrieValueContext {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "TrieIterator" in element -> MissingTrieValueContext.Trieiterator
            "TriePrefetchingStorage" in element -> MissingTrieValueContext.Trieprefetchingstorage
            "TrieMemoryPartialStorage" in element -> MissingTrieValueContext.Triememorypartialstorage
            "TrieStorage" in element -> MissingTrieValueContext.Triestorage
            else -> throw SerializationException("Unknown variant in MissingTrieValueContext: ${element.keys}")
        }
    }
}

/**
 * An Action that can be included in a transaction or receipt, excluding delegat...
 */
@Serializable(with = NonDelegateActionSerializer::class)
sealed interface NonDelegateAction {
    @Serializable
    data class CreateAccount(
        @SerialName("CreateAccount")
        val createAccount: CreateAccountAction,
    ) : NonDelegateAction

    @Serializable
    data class DeployContract(
        @SerialName("code")
        val code: String,
    ) : NonDelegateAction

    @Serializable
    data class FunctionCall(
        @SerialName("args")
        val args: String,
        @SerialName("deposit")
        val deposit: NearToken,
        @SerialName("gas")
        val gas: NearGas,
        @SerialName("method_name")
        val methodName: String,
    ) : NonDelegateAction

    @Serializable
    data class Transfer(
        @SerialName("deposit")
        val deposit: NearToken,
    ) : NonDelegateAction

    @Serializable
    data class Stake(
        @SerialName("public_key")
        val publicKey: PublicKey,
        @SerialName("stake")
        val stake: NearToken,
    ) : NonDelegateAction

    @Serializable
    data class AddKey(
        @SerialName("access_key")
        val accessKey: AccessKey,
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : NonDelegateAction

    @Serializable
    data class DeleteKey(
        @SerialName("public_key")
        val publicKey: PublicKey,
    ) : NonDelegateAction

    @Serializable
    data class DeleteAccount(
        @SerialName("beneficiary_id")
        val beneficiaryId: AccountId,
    ) : NonDelegateAction

    @Serializable
    data class DeployGlobalContract(
        @SerialName("code")
        val code: String,
        @SerialName("deploy_mode")
        val deployMode: GlobalContractDeployMode,
    ) : NonDelegateAction

    @Serializable
    data class UseGlobalContract(
        @SerialName("contract_identifier")
        val contractIdentifier: GlobalContractIdentifier,
    ) : NonDelegateAction

    @Serializable
    data class DeterministicStateInit(
        @SerialName("deposit")
        val deposit: NearToken,
        @SerialName("state_init")
        val stateInit: DeterministicAccountStateInit,
    ) : NonDelegateAction
}

// Custom serializer for NonDelegateAction to handle content-based polymorphism
object NonDelegateActionSerializer : JsonContentPolymorphicSerializer<NonDelegateAction>(NonDelegateAction::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<NonDelegateAction> {
        return when {
            "CreateAccount" in element.jsonObject -> NonDelegateAction.CreateAccount.serializer()
            "DeployContract" in element.jsonObject -> NonDelegateAction.DeployContract.serializer()
            "FunctionCall" in element.jsonObject -> NonDelegateAction.FunctionCall.serializer()
            "Transfer" in element.jsonObject -> NonDelegateAction.Transfer.serializer()
            "Stake" in element.jsonObject -> NonDelegateAction.Stake.serializer()
            "AddKey" in element.jsonObject -> NonDelegateAction.AddKey.serializer()
            "DeleteKey" in element.jsonObject -> NonDelegateAction.DeleteKey.serializer()
            "DeleteAccount" in element.jsonObject -> NonDelegateAction.DeleteAccount.serializer()
            "DeployGlobalContract" in element.jsonObject -> NonDelegateAction.DeployGlobalContract.serializer()
            "UseGlobalContract" in element.jsonObject -> NonDelegateAction.UseGlobalContract.serializer()
            "DeterministicStateInit" in element.jsonObject -> NonDelegateAction.DeterministicStateInit.serializer()
            else -> throw SerializationException(
                "Unknown variant in NonDelegateAction: type=${element::class.simpleName}",
            )
        }
    }
}

/**
 * Error that can occur while preparing or executing Wasm smart-contract.
 */
@Serializable(with = PrepareErrorSerializer::class)
sealed interface PrepareError {
    @Serializable
    @SerialName("Serialization")
    object Serialization : PrepareError

    @Serializable
    @SerialName("Deserialization")
    object Deserialization : PrepareError

    @Serializable
    @SerialName("InternalMemoryDeclared")
    object Internalmemorydeclared : PrepareError

    @Serializable
    @SerialName("GasInstrumentation")
    object Gasinstrumentation : PrepareError

    @Serializable
    @SerialName("StackHeightInstrumentation")
    object Stackheightinstrumentation : PrepareError

    @Serializable
    @SerialName("Instantiate")
    object Instantiate : PrepareError

    @Serializable
    @SerialName("Memory")
    object Memory : PrepareError

    @Serializable
    @SerialName("TooManyFunctions")
    object Toomanyfunctions : PrepareError

    @Serializable
    @SerialName("TooManyLocals")
    object Toomanylocals : PrepareError

    @Serializable
    @SerialName("TooManyTables")
    object Toomanytables : PrepareError

    @Serializable
    @SerialName("TooManyTableElements")
    object Toomanytableelements : PrepareError
}

// Custom serializer for PrepareError to handle NEAR's externally-tagged union format
object PrepareErrorSerializer : KSerializer<PrepareError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PrepareError")

    override fun serialize(
        encoder: Encoder,
        value: PrepareError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is PrepareError.Serialization ->
                output.encodeJsonElement(
                    buildJsonObject { put("Serialization", JsonNull) },
                )
            is PrepareError.Deserialization ->
                output.encodeJsonElement(
                    buildJsonObject { put("Deserialization", JsonNull) },
                )
            is PrepareError.Internalmemorydeclared ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("InternalMemoryDeclared", JsonNull)
                    },
                )
            is PrepareError.Gasinstrumentation ->
                output.encodeJsonElement(
                    buildJsonObject { put("GasInstrumentation", JsonNull) },
                )
            is PrepareError.Stackheightinstrumentation ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("StackHeightInstrumentation", JsonNull)
                    },
                )
            is PrepareError.Instantiate -> output.encodeJsonElement(buildJsonObject { put("Instantiate", JsonNull) })
            is PrepareError.Memory -> output.encodeJsonElement(buildJsonObject { put("Memory", JsonNull) })
            is PrepareError.Toomanyfunctions ->
                output.encodeJsonElement(
                    buildJsonObject { put("TooManyFunctions", JsonNull) },
                )
            is PrepareError.Toomanylocals ->
                output.encodeJsonElement(
                    buildJsonObject { put("TooManyLocals", JsonNull) },
                )
            is PrepareError.Toomanytables ->
                output.encodeJsonElement(
                    buildJsonObject { put("TooManyTables", JsonNull) },
                )
            is PrepareError.Toomanytableelements ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("TooManyTableElements", JsonNull)
                    },
                )
        }
    }

    override fun deserialize(decoder: Decoder): PrepareError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "Serialization" in element -> PrepareError.Serialization
            "Deserialization" in element -> PrepareError.Deserialization
            "InternalMemoryDeclared" in element -> PrepareError.Internalmemorydeclared
            "GasInstrumentation" in element -> PrepareError.Gasinstrumentation
            "StackHeightInstrumentation" in element -> PrepareError.Stackheightinstrumentation
            "Instantiate" in element -> PrepareError.Instantiate
            "Memory" in element -> PrepareError.Memory
            "TooManyFunctions" in element -> PrepareError.Toomanyfunctions
            "TooManyLocals" in element -> PrepareError.Toomanylocals
            "TooManyTables" in element -> PrepareError.Toomanytables
            "TooManyTableElements" in element -> PrepareError.Toomanytableelements
            else -> throw SerializationException("Unknown variant in PrepareError: ${element.keys}")
        }
    }
}

@Serializable(with = ReceiptEnumViewSerializer::class)
sealed interface ReceiptEnumView {
    @Serializable
    data class Action(
        @SerialName("actions")
        val actions: List<ActionView>,
        @SerialName("gas_price")
        val gasPrice: NearToken,
        @SerialName("input_data_ids")
        val inputDataIds: List<CryptoHash>,
        @SerialName("is_promise_yield")
        val isPromiseYield: Boolean? = null,
        @SerialName("output_data_receivers")
        val outputDataReceivers: List<DataReceiverView>,
        @SerialName("signer_id")
        val signerId: AccountId,
        @SerialName("signer_public_key")
        val signerPublicKey: PublicKey,
    ) : ReceiptEnumView

    @Serializable
    data class Data(
        @SerialName("data")
        val data: String? = null,
        @SerialName("data_id")
        val dataId: CryptoHash,
        @SerialName("is_promise_resume")
        val isPromiseResume: Boolean? = null,
    ) : ReceiptEnumView

    @Serializable
    data class GlobalContractDistribution(
        @SerialName("already_delivered_shards")
        val alreadyDeliveredShards: List<ShardId>,
        @SerialName("code")
        val code: String,
        @SerialName("id")
        val id: GlobalContractIdentifier,
        @SerialName("target_shard")
        val targetShard: ShardId,
    ) : ReceiptEnumView
}

// Custom serializer for ReceiptEnumView to handle NEAR's externally-tagged union format
object ReceiptEnumViewSerializer : KSerializer<ReceiptEnumView> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ReceiptEnumView")

    override fun serialize(
        encoder: Encoder,
        value: ReceiptEnumView,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ReceiptEnumView.Action -> output.encodeSerializableValue(ReceiptEnumView.Action.serializer(), value)
            is ReceiptEnumView.Data -> output.encodeSerializableValue(ReceiptEnumView.Data.serializer(), value)
            is ReceiptEnumView.GlobalContractDistribution ->
                output.encodeSerializableValue(
                    ReceiptEnumView.GlobalContractDistribution.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ReceiptEnumView {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "Action" in element ->
                input.json.decodeFromJsonElement(
                    ReceiptEnumView.Action.serializer(),
                    element["Action"]!!,
                )
            "Data" in element -> input.json.decodeFromJsonElement(ReceiptEnumView.Data.serializer(), element["Data"]!!)
            "GlobalContractDistribution" in element ->
                input.json.decodeFromJsonElement(
                    ReceiptEnumView.GlobalContractDistribution.serializer(),
                    element["GlobalContractDistribution"]!!,
                )
            else -> throw SerializationException("Unknown variant in ReceiptEnumView: ${element.keys}")
        }
    }
}

/**
 * Describes the error for validating a receipt.
 */
@Serializable(with = ReceiptValidationErrorSerializer::class)
sealed interface ReceiptValidationError {
    @Serializable
    data class InvalidPredecessorId(
        @SerialName("account_id")
        val accountId: String,
    ) : ReceiptValidationError

    @Serializable
    data class InvalidReceiverId(
        @SerialName("account_id")
        val accountId: String,
    ) : ReceiptValidationError

    @Serializable
    data class InvalidSignerId(
        @SerialName("account_id")
        val accountId: String,
    ) : ReceiptValidationError

    @Serializable
    data class InvalidDataReceiverId(
        @SerialName("account_id")
        val accountId: String,
    ) : ReceiptValidationError

    @Serializable
    data class ReturnedValueLengthExceeded(
        @SerialName("length")
        val length: Long,
        @SerialName("limit")
        val limit: Long,
    ) : ReceiptValidationError

    @Serializable
    data class NumberInputDataDependenciesExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("number_of_input_data_dependencies")
        val numberOfInputDataDependencies: Long,
    ) : ReceiptValidationError

    @Serializable
    data class ActionsValidation(
        @SerialName("ActionsValidation")
        val actionsValidation: ActionsValidationError,
    ) : ReceiptValidationError

    @Serializable
    data class ReceiptSizeExceeded(
        @SerialName("limit")
        val limit: Long,
        @SerialName("size")
        val size: Long,
    ) : ReceiptValidationError
}

// Custom serializer for ReceiptValidationError to handle content-based polymorphism
object ReceiptValidationErrorSerializer : JsonContentPolymorphicSerializer<ReceiptValidationError>(
    ReceiptValidationError::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ReceiptValidationError> {
        return when {
            "InvalidPredecessorId" in element.jsonObject -> ReceiptValidationError.InvalidPredecessorId.serializer()
            "InvalidReceiverId" in element.jsonObject -> ReceiptValidationError.InvalidReceiverId.serializer()
            "InvalidSignerId" in element.jsonObject -> ReceiptValidationError.InvalidSignerId.serializer()
            "InvalidDataReceiverId" in element.jsonObject -> ReceiptValidationError.InvalidDataReceiverId.serializer()
            "ReturnedValueLengthExceeded" in element.jsonObject -> ReceiptValidationError.ReturnedValueLengthExceeded.serializer()
            "NumberInputDataDependenciesExceeded" in element.jsonObject -> ReceiptValidationError.NumberInputDataDependenciesExceeded.serializer()
            "ActionsValidation" in element.jsonObject -> ReceiptValidationError.ActionsValidation.serializer()
            "ReceiptSizeExceeded" in element.jsonObject -> ReceiptValidationError.ReceiptSizeExceeded.serializer()
            else -> throw SerializationException(
                "Unknown variant in ReceiptValidationError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = RpcBlockRequestSerializer::class)
sealed interface RpcBlockRequest {
    @Serializable
    data class BlockIdRequest(
        @SerialName("block_id")
        val blockId: BlockId,
    ) : RpcBlockRequest

    @Serializable
    data class FinalityRequest(
        @SerialName("finality")
        val finality: Finality,
    ) : RpcBlockRequest

    @Serializable
    data class SyncCheckpointRequest(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
    ) : RpcBlockRequest
}

// Custom serializer for RpcBlockRequest to handle content-based polymorphism
object RpcBlockRequestSerializer : JsonContentPolymorphicSerializer<RpcBlockRequest>(RpcBlockRequest::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcBlockRequest> {
        return when {
            "block_id" in element.jsonObject -> RpcBlockRequest.BlockIdRequest.serializer()
            "finality" in element.jsonObject -> RpcBlockRequest.FinalityRequest.serializer()
            "sync_checkpoint" in element.jsonObject -> RpcBlockRequest.SyncCheckpointRequest.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcBlockRequest: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = RpcChunkRequestSerializer::class)
sealed interface RpcChunkRequest {
    @Serializable
    data class Variant0(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("shard_id")
        val shardId: ShardId,
    ) : RpcChunkRequest

    @Serializable
    data class ChunkId(
        @SerialName("chunk_id")
        val chunkId: CryptoHash,
    ) : RpcChunkRequest
}

// Custom serializer for RpcChunkRequest to handle content-based polymorphism
object RpcChunkRequestSerializer : JsonContentPolymorphicSerializer<RpcChunkRequest>(RpcChunkRequest::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcChunkRequest> {
        return when {
            "block_id" in element.jsonObject && "shard_id" in element.jsonObject -> RpcChunkRequest.Variant0.serializer()
            "chunk_id" in element.jsonObject -> RpcChunkRequest.ChunkId.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcChunkRequest: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = RpcCongestionLevelRequestSerializer::class)
sealed interface RpcCongestionLevelRequest {
    @Serializable
    data class Variant0(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("shard_id")
        val shardId: ShardId,
    ) : RpcCongestionLevelRequest

    @Serializable
    data class ChunkId(
        @SerialName("chunk_id")
        val chunkId: CryptoHash,
    ) : RpcCongestionLevelRequest
}

// Custom serializer for RpcCongestionLevelRequest to handle content-based polymorphism
object RpcCongestionLevelRequestSerializer : JsonContentPolymorphicSerializer<RpcCongestionLevelRequest>(
    RpcCongestionLevelRequest::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcCongestionLevelRequest> {
        return when {
            "block_id" in element.jsonObject && "shard_id" in element.jsonObject -> RpcCongestionLevelRequest.Variant0.serializer()
            "chunk_id" in element.jsonObject -> RpcCongestionLevelRequest.ChunkId.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcCongestionLevelRequest: type=${element::class.simpleName}",
            )
        }
    }
}

/**
 * This struct may be returned from JSON RPC server in case of error
 */
@Serializable
@JsonClassDiscriminator("name")
sealed interface RpcError {
    @Serializable
    @SerialName("REQUEST_VALIDATION_ERROR")
    data class REQUESTVALIDATIONERROR(
        @SerialName("cause")
        val cause: RpcRequestValidationErrorKind,
    ) : RpcError

    @Serializable
    @SerialName("HANDLER_ERROR")
    data class HANDLERERROR(
        @SerialName("cause")
        val cause: JsonElement,
    ) : RpcError

    @Serializable
    @SerialName("INTERNAL_ERROR")
    data class INTERNALERROR(
        @SerialName("cause")
        val cause: JsonElement,
    ) : RpcError
}

@Serializable
@JsonClassDiscriminator("type")
sealed interface RpcLightClientExecutionProofRequest {
    @Serializable
    @SerialName("transaction")
    data class Transaction(
        @SerialName("sender_id")
        val senderId: AccountId,
        @SerialName("transaction_hash")
        val transactionHash: CryptoHash,
    ) : RpcLightClientExecutionProofRequest

    @Serializable
    @SerialName("receipt")
    data class Receipt(
        @SerialName("receipt_id")
        val receiptId: CryptoHash,
        @SerialName("receiver_id")
        val receiverId: AccountId,
    ) : RpcLightClientExecutionProofRequest
}

@Serializable(with = RpcProtocolConfigRequestSerializer::class)
sealed interface RpcProtocolConfigRequest {
    @Serializable
    data class BlockIdRequest(
        @SerialName("block_id")
        val blockId: BlockId,
    ) : RpcProtocolConfigRequest

    @Serializable
    data class FinalityRequest(
        @SerialName("finality")
        val finality: Finality,
    ) : RpcProtocolConfigRequest

    @Serializable
    data class SyncCheckpointRequest(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
    ) : RpcProtocolConfigRequest
}

// Custom serializer for RpcProtocolConfigRequest to handle content-based polymorphism
object RpcProtocolConfigRequestSerializer : JsonContentPolymorphicSerializer<RpcProtocolConfigRequest>(
    RpcProtocolConfigRequest::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcProtocolConfigRequest> {
        return when {
            "block_id" in element.jsonObject -> RpcProtocolConfigRequest.BlockIdRequest.serializer()
            "finality" in element.jsonObject -> RpcProtocolConfigRequest.FinalityRequest.serializer()
            "sync_checkpoint" in element.jsonObject -> RpcProtocolConfigRequest.SyncCheckpointRequest.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcProtocolConfigRequest: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable
sealed interface RpcQueryRequest {
    @Serializable
    data class ViewAccountByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewCodeByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewStateByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("include_proof")
        val includeProof: Boolean? = null,
        @SerialName("prefix_base64")
        val prefixBase64: StoreKey,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccessKeyByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccessKeyListByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class CallFunctionByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("args_base64")
        val argsBase64: FunctionArgs,
        @SerialName("method_name")
        val methodName: String,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewGlobalContractCodeByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("code_hash")
        val codeHash: CryptoHash,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewGlobalContractCodeByAccountIdByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccountByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewCodeByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewStateByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("include_proof")
        val includeProof: Boolean? = null,
        @SerialName("prefix_base64")
        val prefixBase64: StoreKey,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccessKeyByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccessKeyListByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class CallFunctionByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("args_base64")
        val argsBase64: FunctionArgs,
        @SerialName("method_name")
        val methodName: String,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewGlobalContractCodeByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("code_hash")
        val codeHash: CryptoHash,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewGlobalContractCodeByAccountIdByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccountBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewCodeBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewStateBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("include_proof")
        val includeProof: Boolean? = null,
        @SerialName("prefix_base64")
        val prefixBase64: StoreKey,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccessKeyBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewAccessKeyListBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class CallFunctionBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("args_base64")
        val argsBase64: FunctionArgs,
        @SerialName("method_name")
        val methodName: String,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewGlobalContractCodeBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("code_hash")
        val codeHash: CryptoHash,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest

    @Serializable
    data class ViewGlobalContractCodeByAccountIdBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("request_type")
        val requestType: String,
    ) : RpcQueryRequest
}

@Serializable(with = RpcQueryResponseSerializer::class)
sealed interface RpcQueryResponse {
    @Serializable
    @JvmInline
    value class AccountViewVariant(
        val value: AccountView,
    ) : RpcQueryResponse

    @Serializable
    @JvmInline
    value class ContractCodeViewVariant(
        val value: ContractCodeView,
    ) : RpcQueryResponse

    @Serializable
    @JvmInline
    value class ViewStateResultVariant(
        val value: ViewStateResult,
    ) : RpcQueryResponse

    @Serializable
    @JvmInline
    value class CallResultVariant(
        val value: CallResult,
    ) : RpcQueryResponse

    @Serializable
    @JvmInline
    value class AccessKeyViewVariant(
        val value: AccessKeyView,
    ) : RpcQueryResponse

    @Serializable
    @JvmInline
    value class AccessKeyListVariant(
        val value: AccessKeyList,
    ) : RpcQueryResponse
}

// Custom serializer for RpcQueryResponse to handle content-based polymorphism
object RpcQueryResponseSerializer : JsonContentPolymorphicSerializer<RpcQueryResponse>(RpcQueryResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcQueryResponse> {
        return when {
            "amount" in element.jsonObject && "code_hash" in element.jsonObject -> RpcQueryResponse.AccountViewVariant.serializer()
            "code_base64" in element.jsonObject && "hash" in element.jsonObject -> RpcQueryResponse.ContractCodeViewVariant.serializer()
            "proof" in element.jsonObject && "values" in element.jsonObject -> RpcQueryResponse.ViewStateResultVariant.serializer()
            "logs" in element.jsonObject && "result" in element.jsonObject -> RpcQueryResponse.CallResultVariant.serializer()
            "nonce" in element.jsonObject && "permission" in element.jsonObject -> RpcQueryResponse.AccessKeyViewVariant.serializer()
            "keys" in element.jsonObject -> RpcQueryResponse.AccessKeyListVariant.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcQueryResponse: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable
@JsonClassDiscriminator("name")
sealed interface RpcRequestValidationErrorKind {
    @Serializable
    @SerialName("METHOD_NOT_FOUND")
    data class METHODNOTFOUND(
        @SerialName("info")
        val info: METHODNOTFOUNDInfo,
    ) : RpcRequestValidationErrorKind

    @Serializable
    data class METHODNOTFOUNDInfo(
        @SerialName("method_name")
        val methodName: String,
    )

    @Serializable
    @SerialName("PARSE_ERROR")
    data class PARSEERROR(
        @SerialName("info")
        val info: PARSEERRORInfo,
    ) : RpcRequestValidationErrorKind

    @Serializable
    data class PARSEERRORInfo(
        @SerialName("error_message")
        val errorMessage: String,
    )
}

/**
 * It is a [serializable view] of [`StateChangesRequest`].
 */
@Serializable
sealed interface RpcStateChangesInBlockByTypeRequest {
    @Serializable
    data class AccountChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class SingleAccessKeyChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("keys")
        val keys: List<AccountWithPublicKey>,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class SingleGasKeyChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("keys")
        val keys: List<AccountWithPublicKey>,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AllAccessKeyChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AllGasKeyChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class ContractCodeChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class DataChangesByBlockId(
        @SerialName("block_id")
        val blockId: BlockId,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("key_prefix_base64")
        val keyPrefixBase64: StoreKey,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AccountChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class SingleAccessKeyChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("keys")
        val keys: List<AccountWithPublicKey>,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class SingleGasKeyChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("keys")
        val keys: List<AccountWithPublicKey>,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AllAccessKeyChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AllGasKeyChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class ContractCodeChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class DataChangesByFinality(
        @SerialName("finality")
        val finality: Finality,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("key_prefix_base64")
        val keyPrefixBase64: StoreKey,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AccountChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class SingleAccessKeyChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("keys")
        val keys: List<AccountWithPublicKey>,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class SingleGasKeyChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("keys")
        val keys: List<AccountWithPublicKey>,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AllAccessKeyChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class AllGasKeyChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class ContractCodeChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
    ) : RpcStateChangesInBlockByTypeRequest

    @Serializable
    data class DataChangesBySyncCheckpoint(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
        @SerialName("account_ids")
        val accountIds: List<AccountId>,
        @SerialName("changes_type")
        val changesType: String,
        @SerialName("key_prefix_base64")
        val keyPrefixBase64: StoreKey,
    ) : RpcStateChangesInBlockByTypeRequest
}

@Serializable(with = RpcStateChangesInBlockRequestSerializer::class)
sealed interface RpcStateChangesInBlockRequest {
    @Serializable
    data class BlockIdRequest(
        @SerialName("block_id")
        val blockId: BlockId,
    ) : RpcStateChangesInBlockRequest

    @Serializable
    data class FinalityRequest(
        @SerialName("finality")
        val finality: Finality,
    ) : RpcStateChangesInBlockRequest

    @Serializable
    data class SyncCheckpointRequest(
        @SerialName("sync_checkpoint")
        val syncCheckpoint: SyncCheckpoint,
    ) : RpcStateChangesInBlockRequest
}

// Custom serializer for RpcStateChangesInBlockRequest to handle content-based polymorphism
object RpcStateChangesInBlockRequestSerializer : JsonContentPolymorphicSerializer<RpcStateChangesInBlockRequest>(
    RpcStateChangesInBlockRequest::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcStateChangesInBlockRequest> {
        return when {
            "block_id" in element.jsonObject -> RpcStateChangesInBlockRequest.BlockIdRequest.serializer()
            "finality" in element.jsonObject -> RpcStateChangesInBlockRequest.FinalityRequest.serializer()
            "sync_checkpoint" in element.jsonObject -> RpcStateChangesInBlockRequest.SyncCheckpointRequest.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcStateChangesInBlockRequest: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = RpcTransactionResponseSerializer::class)
sealed interface RpcTransactionResponse {
    @Serializable
    @JvmInline
    value class FinalExecutionOutcomeWithReceiptViewVariant(
        val value: FinalExecutionOutcomeWithReceiptView,
    ) : RpcTransactionResponse

    @Serializable
    @JvmInline
    value class FinalExecutionOutcomeViewVariant(
        val value: FinalExecutionOutcomeView,
    ) : RpcTransactionResponse
}

// Custom serializer for RpcTransactionResponse to handle content-based polymorphism
object RpcTransactionResponseSerializer : JsonContentPolymorphicSerializer<RpcTransactionResponse>(
    RpcTransactionResponse::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcTransactionResponse> {
        return when {
            "receipts" in element.jsonObject && "receipts_outcome" in element.jsonObject -> RpcTransactionResponse.FinalExecutionOutcomeWithReceiptViewVariant.serializer()
            "receipts_outcome" in element.jsonObject && "status" in element.jsonObject -> RpcTransactionResponse.FinalExecutionOutcomeViewVariant.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcTransactionResponse: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = RpcTransactionStatusRequestSerializer::class)
sealed interface RpcTransactionStatusRequest {
    @Serializable
    data class SignedTxBase64(
        @SerialName("signed_tx_base64")
        val signedTxBase64: SignedTransaction,
    ) : RpcTransactionStatusRequest

    @Serializable
    data class Variant1(
        @SerialName("sender_account_id")
        val senderAccountId: AccountId,
        @SerialName("tx_hash")
        val txHash: CryptoHash,
    ) : RpcTransactionStatusRequest
}

// Custom serializer for RpcTransactionStatusRequest to handle content-based polymorphism
object RpcTransactionStatusRequestSerializer : JsonContentPolymorphicSerializer<RpcTransactionStatusRequest>(
    RpcTransactionStatusRequest::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RpcTransactionStatusRequest> {
        return when {
            "signed_tx_base64" in element.jsonObject -> RpcTransactionStatusRequest.SignedTxBase64.serializer()
            "sender_account_id" in element.jsonObject && "tx_hash" in element.jsonObject -> RpcTransactionStatusRequest.Variant1.serializer()
            else -> throw SerializationException(
                "Unknown variant in RpcTransactionStatusRequest: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = RpcValidatorRequestSerializer::class)
sealed interface RpcValidatorRequest {
    @Serializable
    @SerialName("latest")
    object Latest : RpcValidatorRequest

    @Serializable
    data class EpochIdRequest(
        @SerialName("epoch_id")
        val epochId: EpochId,
    ) : RpcValidatorRequest

    @Serializable
    data class BlockIdRequest(
        @SerialName("block_id")
        val blockId: BlockId,
    ) : RpcValidatorRequest
}

// Custom serializer for RpcValidatorRequest to handle content-based polymorphism
object RpcValidatorRequestSerializer : KSerializer<RpcValidatorRequest> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("RpcValidatorRequest")

    override fun serialize(
        encoder: Encoder,
        value: RpcValidatorRequest,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is RpcValidatorRequest.Latest -> output.encodeJsonElement(JsonPrimitive("latest"))
            is RpcValidatorRequest.EpochIdRequest ->
                output.encodeSerializableValue(
                    RpcValidatorRequest.EpochIdRequest.serializer(),
                    value,
                )
            is RpcValidatorRequest.BlockIdRequest ->
                output.encodeSerializableValue(
                    RpcValidatorRequest.BlockIdRequest.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): RpcValidatorRequest {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "latest" -> RpcValidatorRequest.Latest
            "epoch_id" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    RpcValidatorRequest.EpochIdRequest.serializer(),
                    element,
                )
            "block_id" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    RpcValidatorRequest.BlockIdRequest.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in RpcValidatorRequest: $element")
        }
    }
}

/**
 * A versioned struct that contains all information needed to assign accounts to...
 */
@Serializable(with = ShardLayoutSerializer::class)
sealed interface ShardLayout {
    @Serializable
    data class V0(
        @SerialName("num_shards")
        val numShards: Long,
        @SerialName("version")
        val version: Long,
    ) : ShardLayout

    @Serializable
    data class V1(
        @SerialName("boundary_accounts")
        val boundaryAccounts: List<AccountId>,
        @SerialName("shards_split_map")
        val shardsSplitMap: List<List<ShardId>>? = null,
        @SerialName("to_parent_shard_map")
        val toParentShardMap: List<ShardId>? = null,
        @SerialName("version")
        val version: Long,
    ) : ShardLayout

    @Serializable
    data class V2(
        @SerialName("boundary_accounts")
        val boundaryAccounts: List<AccountId>,
        @SerialName("id_to_index_map")
        val idToIndexMap: Map<String, Long>,
        @SerialName("index_to_id_map")
        val indexToIdMap: Map<String, ShardId>,
        @SerialName("shard_ids")
        val shardIds: List<ShardId>,
        @SerialName("shards_parent_map")
        val shardsParentMap: Map<String, ShardId>? = null,
        @SerialName("shards_split_map")
        val shardsSplitMap: Map<String, List<ShardId>>? = null,
        @SerialName("version")
        val version: Long,
    ) : ShardLayout
}

// Custom serializer for ShardLayout to handle NEAR's externally-tagged union format
object ShardLayoutSerializer : KSerializer<ShardLayout> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShardLayout")

    override fun serialize(
        encoder: Encoder,
        value: ShardLayout,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ShardLayout.V0 -> output.encodeSerializableValue(ShardLayout.V0.serializer(), value)
            is ShardLayout.V1 -> output.encodeSerializableValue(ShardLayout.V1.serializer(), value)
            is ShardLayout.V2 -> output.encodeSerializableValue(ShardLayout.V2.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): ShardLayout {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "V0" in element -> input.json.decodeFromJsonElement(ShardLayout.V0.serializer(), element["V0"]!!)
            "V1" in element -> input.json.decodeFromJsonElement(ShardLayout.V1.serializer(), element["V1"]!!)
            "V2" in element -> input.json.decodeFromJsonElement(ShardLayout.V2.serializer(), element["V2"]!!)
            else -> throw SerializationException("Unknown variant in ShardLayout: ${element.keys}")
        }
    }
}

/**
 * See crate::types::StateChangeCause for details.
 */
@Serializable
@JsonClassDiscriminator("type")
sealed interface StateChangeCauseView {
    @Serializable
    @SerialName("not_writable_to_disk")
    object NotWritableToDisk : StateChangeCauseView

    @Serializable
    @SerialName("initial_state")
    object InitialState : StateChangeCauseView

    @Serializable
    @SerialName("transaction_processing")
    data class TransactionProcessing(
        @SerialName("tx_hash")
        val txHash: CryptoHash,
    ) : StateChangeCauseView

    @Serializable
    @SerialName("action_receipt_processing_started")
    data class ActionReceiptProcessingStarted(
        @SerialName("receipt_hash")
        val receiptHash: CryptoHash,
    ) : StateChangeCauseView

    @Serializable
    @SerialName("action_receipt_gas_reward")
    data class ActionReceiptGasReward(
        @SerialName("receipt_hash")
        val receiptHash: CryptoHash,
    ) : StateChangeCauseView

    @Serializable
    @SerialName("receipt_processing")
    data class ReceiptProcessing(
        @SerialName("receipt_hash")
        val receiptHash: CryptoHash,
    ) : StateChangeCauseView

    @Serializable
    @SerialName("postponed_receipt")
    data class PostponedReceipt(
        @SerialName("receipt_hash")
        val receiptHash: CryptoHash,
    ) : StateChangeCauseView

    @Serializable
    @SerialName("updated_delayed_receipts")
    object UpdatedDelayedReceipts : StateChangeCauseView

    @Serializable
    @SerialName("validator_accounts_update")
    object ValidatorAccountsUpdate : StateChangeCauseView

    @Serializable
    @SerialName("migration")
    object Migration : StateChangeCauseView

    @Serializable
    @SerialName("bandwidth_scheduler_state_update")
    object BandwidthSchedulerStateUpdate : StateChangeCauseView
}

/**
 * It is a [serializable view] of [`StateChangeKind`].
 */
@Serializable
@JsonClassDiscriminator("type")
sealed interface StateChangeKindView {
    @Serializable
    @SerialName("account_touched")
    data class AccountTouched(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : StateChangeKindView

    @Serializable
    @SerialName("access_key_touched")
    data class AccessKeyTouched(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : StateChangeKindView

    @Serializable
    @SerialName("data_touched")
    data class DataTouched(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : StateChangeKindView

    @Serializable
    @SerialName("contract_code_touched")
    data class ContractCodeTouched(
        @SerialName("account_id")
        val accountId: AccountId,
    ) : StateChangeKindView
}

@Serializable
@JsonClassDiscriminator("type")
sealed interface StateChangeWithCauseView {
    @Serializable
    @SerialName("account_update")
    data class AccountUpdate(
        @SerialName("change")
        val change: AccountUpdateChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class AccountUpdateChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("amount")
        val amount: NearToken,
        @SerialName("code_hash")
        val codeHash: CryptoHash,
        @SerialName("global_contract_account_id")
        val globalContractAccountId: AccountId? = null,
        @SerialName("global_contract_hash")
        val globalContractHash: CryptoHash? = null,
        @SerialName("locked")
        val locked: NearToken,
        @SerialName("storage_paid_at")
        val storagePaidAt: Long? = null,
        @SerialName("storage_usage")
        val storageUsage: Long,
    )

    @Serializable
    @SerialName("account_deletion")
    data class AccountDeletion(
        @SerialName("change")
        val change: AccountDeletionChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class AccountDeletionChange(
        @SerialName("account_id")
        val accountId: AccountId,
    )

    @Serializable
    @SerialName("access_key_update")
    data class AccessKeyUpdate(
        @SerialName("change")
        val change: AccessKeyUpdateChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class AccessKeyUpdateChange(
        @SerialName("access_key")
        val accessKey: AccessKeyView,
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
    )

    @Serializable
    @SerialName("access_key_deletion")
    data class AccessKeyDeletion(
        @SerialName("change")
        val change: AccessKeyDeletionChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class AccessKeyDeletionChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
    )

    @Serializable
    @SerialName("gas_key_update")
    data class GasKeyUpdate(
        @SerialName("change")
        val change: GasKeyUpdateChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class GasKeyUpdateChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("gas_key")
        val gasKey: GasKeyView,
        @SerialName("public_key")
        val publicKey: PublicKey,
    )

    @Serializable
    @SerialName("gas_key_nonce_update")
    data class GasKeyNonceUpdate(
        @SerialName("change")
        val change: GasKeyNonceUpdateChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class GasKeyNonceUpdateChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("index")
        val index: Long,
        @SerialName("nonce")
        val nonce: Long,
        @SerialName("public_key")
        val publicKey: PublicKey,
    )

    @Serializable
    @SerialName("gas_key_deletion")
    data class GasKeyDeletion(
        @SerialName("change")
        val change: GasKeyDeletionChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class GasKeyDeletionChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("public_key")
        val publicKey: PublicKey,
    )

    @Serializable
    @SerialName("data_update")
    data class DataUpdate(
        @SerialName("change")
        val change: DataUpdateChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class DataUpdateChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("key_base64")
        val keyBase64: StoreKey,
        @SerialName("value_base64")
        val valueBase64: StoreValue,
    )

    @Serializable
    @SerialName("data_deletion")
    data class DataDeletion(
        @SerialName("change")
        val change: DataDeletionChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class DataDeletionChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("key_base64")
        val keyBase64: StoreKey,
    )

    @Serializable
    @SerialName("contract_code_update")
    data class ContractCodeUpdate(
        @SerialName("change")
        val change: ContractCodeUpdateChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class ContractCodeUpdateChange(
        @SerialName("account_id")
        val accountId: AccountId,
        @SerialName("code_base64")
        val codeBase64: String,
    )

    @Serializable
    @SerialName("contract_code_deletion")
    data class ContractCodeDeletion(
        @SerialName("change")
        val change: ContractCodeDeletionChange,
    ) : StateChangeWithCauseView

    @Serializable
    data class ContractCodeDeletionChange(
        @SerialName("account_id")
        val accountId: AccountId,
    )
}

/**
 * Errors which may occur during working with trie storages, storing
 */
@Serializable(with = StorageErrorSerializer::class)
sealed interface StorageError {
    @Serializable
    @SerialName("StorageInternalError")
    object Storageinternalerror : StorageError

    @Serializable
    data class MissingTrieValueRequest(
        @SerialName("context")
        val context: MissingTrieValueContext,
        @SerialName("hash")
        val hash: CryptoHash,
    ) : StorageError

    @Serializable
    @SerialName("UnexpectedTrieValue")
    object Unexpectedtrievalue : StorageError

    @Serializable
    data class StorageInconsistentState(
        @SerialName("StorageInconsistentState")
        val storageInconsistentState: String,
    ) : StorageError

    @Serializable
    data class FlatStorageBlockNotSupported(
        @SerialName("FlatStorageBlockNotSupported")
        val flatStorageBlockNotSupported: String,
    ) : StorageError

    @Serializable
    data class MemTrieLoadingError(
        @SerialName("MemTrieLoadingError")
        val memTrieLoadingError: String,
    ) : StorageError
}

// Custom serializer for StorageError to handle content-based polymorphism
object StorageErrorSerializer : KSerializer<StorageError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("StorageError")

    override fun serialize(
        encoder: Encoder,
        value: StorageError,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is StorageError.Storageinternalerror -> output.encodeJsonElement(JsonPrimitive("StorageInternalError"))
            is StorageError.Unexpectedtrievalue -> output.encodeJsonElement(JsonPrimitive("UnexpectedTrieValue"))
            is StorageError.MissingTrieValueRequest ->
                output.encodeSerializableValue(
                    StorageError.MissingTrieValueRequest.serializer(),
                    value,
                )
            is StorageError.StorageInconsistentState ->
                output.encodeSerializableValue(
                    StorageError.StorageInconsistentState.serializer(),
                    value,
                )
            is StorageError.FlatStorageBlockNotSupported ->
                output.encodeSerializableValue(
                    StorageError.FlatStorageBlockNotSupported.serializer(),
                    value,
                )
            is StorageError.MemTrieLoadingError ->
                output.encodeSerializableValue(
                    StorageError.MemTrieLoadingError.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): StorageError {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "StorageInternalError" -> StorageError.Storageinternalerror
            element is JsonPrimitive && element.content == "UnexpectedTrieValue" -> StorageError.Unexpectedtrievalue
            "MissingTrieValue" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    StorageError.MissingTrieValueRequest.serializer(),
                    element,
                )
            "StorageInconsistentState" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    StorageError.StorageInconsistentState.serializer(),
                    element,
                )
            "FlatStorageBlockNotSupported" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    StorageError.FlatStorageBlockNotSupported.serializer(),
                    element,
                )
            "MemTrieLoadingError" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    StorageError.MemTrieLoadingError.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in StorageError: $element")
        }
    }
}

/**
 * Configures how to fetch state parts during state sync.
 */
@Serializable(with = SyncConfigSerializer::class)
sealed interface SyncConfig {
    @Serializable
    @SerialName("Peers")
    object Peers : SyncConfig

    @Serializable
    data class ExternalStorage(
        @SerialName("external_storage_fallback_threshold")
        val externalStorageFallbackThreshold: Long? = null,
        @SerialName("location")
        val location: ExternalStorageLocation,
        @SerialName("num_concurrent_requests")
        val numConcurrentRequests: Long? = null,
        @SerialName("num_concurrent_requests_during_catchup")
        val numConcurrentRequestsDuringCatchup: Long? = null,
    ) : SyncConfig
}

// Custom serializer for SyncConfig to handle NEAR's externally-tagged union format
object SyncConfigSerializer : KSerializer<SyncConfig> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SyncConfig")

    override fun serialize(
        encoder: Encoder,
        value: SyncConfig,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is SyncConfig.Peers -> output.encodeJsonElement(buildJsonObject { put("Peers", JsonNull) })
            is SyncConfig.ExternalStorage ->
                output.encodeSerializableValue(
                    SyncConfig.ExternalStorage.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): SyncConfig {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "Peers" in element -> SyncConfig.Peers
            "ExternalStorage" in element ->
                input.json.decodeFromJsonElement(
                    SyncConfig.ExternalStorage.serializer(),
                    element["ExternalStorage"]!!,
                )
            else -> throw SerializationException("Unknown variant in SyncConfig: ${element.keys}")
        }
    }
}

/**
 * Describes the expected behavior of the node regarding shard tracking.
 */
@Serializable(with = TrackedShardsConfigSerializer::class)
sealed interface TrackedShardsConfig {
    @Serializable
    @SerialName("NoShards")
    object Noshards : TrackedShardsConfig

    @Serializable
    data class Shards(
        @SerialName("Shards")
        val shards: List<ShardUId>,
    ) : TrackedShardsConfig

    @Serializable
    @SerialName("AllShards")
    object Allshards : TrackedShardsConfig

    @Serializable
    data class ShadowValidator(
        @SerialName("ShadowValidator")
        val shadowValidator: AccountId,
    ) : TrackedShardsConfig

    @Serializable
    data class Schedule(
        @SerialName("Schedule")
        val schedule: List<List<ShardId>>,
    ) : TrackedShardsConfig

    @Serializable
    data class Accounts(
        @SerialName("Accounts")
        val accounts: List<AccountId>,
    ) : TrackedShardsConfig
}

// Custom serializer for TrackedShardsConfig to handle content-based polymorphism
object TrackedShardsConfigSerializer : KSerializer<TrackedShardsConfig> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TrackedShardsConfig")

    override fun serialize(
        encoder: Encoder,
        value: TrackedShardsConfig,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is TrackedShardsConfig.Noshards -> output.encodeJsonElement(JsonPrimitive("NoShards"))
            is TrackedShardsConfig.Allshards -> output.encodeJsonElement(JsonPrimitive("AllShards"))
            is TrackedShardsConfig.Shards ->
                output.encodeSerializableValue(
                    TrackedShardsConfig.Shards.serializer(),
                    value,
                )
            is TrackedShardsConfig.ShadowValidator ->
                output.encodeSerializableValue(
                    TrackedShardsConfig.ShadowValidator.serializer(),
                    value,
                )
            is TrackedShardsConfig.Schedule ->
                output.encodeSerializableValue(
                    TrackedShardsConfig.Schedule.serializer(),
                    value,
                )
            is TrackedShardsConfig.Accounts ->
                output.encodeSerializableValue(
                    TrackedShardsConfig.Accounts.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): TrackedShardsConfig {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "NoShards" -> TrackedShardsConfig.Noshards
            element is JsonPrimitive && element.content == "AllShards" -> TrackedShardsConfig.Allshards
            "Shards" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    TrackedShardsConfig.Shards.serializer(),
                    element,
                )
            "ShadowValidator" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    TrackedShardsConfig.ShadowValidator.serializer(),
                    element,
                )
            "Schedule" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    TrackedShardsConfig.Schedule.serializer(),
                    element,
                )
            "Accounts" in element.jsonObject ->
                input.json.decodeFromJsonElement(
                    TrackedShardsConfig.Accounts.serializer(),
                    element,
                )
            else -> throw SerializationException("Unknown variant in TrackedShardsConfig: $element")
        }
    }
}

/**
 * Error returned in the ExecutionOutcome in case of failure
 */
@Serializable(with = TxExecutionErrorSerializer::class)
sealed interface TxExecutionError {
    @Serializable
    data class ActionErrorRequest(
        @SerialName("index")
        val index: Long? = null,
        @SerialName("kind")
        val kind: ActionErrorKind,
    ) : TxExecutionError

    @Serializable
    data class InvalidTxErrorRequest(
        @SerialName("InvalidTxError")
        val invalidTxError: InvalidTxError,
    ) : TxExecutionError
}

// Custom serializer for TxExecutionError to handle content-based polymorphism
object TxExecutionErrorSerializer : JsonContentPolymorphicSerializer<TxExecutionError>(TxExecutionError::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<TxExecutionError> {
        return when {
            "ActionError" in element.jsonObject -> TxExecutionError.ActionErrorRequest.serializer()
            "InvalidTxError" in element.jsonObject -> TxExecutionError.InvalidTxErrorRequest.serializer()
            else -> throw SerializationException(
                "Unknown variant in TxExecutionError: type=${element::class.simpleName}",
            )
        }
    }
}

@Serializable(with = TxExecutionStatusSerializer::class)
sealed interface TxExecutionStatus {
    @Serializable
    @SerialName("NONE")
    object None : TxExecutionStatus

    @Serializable
    @SerialName("INCLUDED")
    object Included : TxExecutionStatus

    @Serializable
    @SerialName("EXECUTED_OPTIMISTIC")
    object Executedoptimistic : TxExecutionStatus

    @Serializable
    @SerialName("INCLUDED_FINAL")
    object Includedfinal : TxExecutionStatus

    @Serializable
    @SerialName("EXECUTED")
    object Executed : TxExecutionStatus

    @Serializable
    @SerialName("FINAL")
    object Final : TxExecutionStatus
}

// Custom serializer for TxExecutionStatus to handle NEAR's externally-tagged union format
object TxExecutionStatusSerializer : KSerializer<TxExecutionStatus> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TxExecutionStatus")

    override fun serialize(
        encoder: Encoder,
        value: TxExecutionStatus,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is TxExecutionStatus.None -> output.encodeJsonElement(buildJsonObject { put("NONE", JsonNull) })
            is TxExecutionStatus.Included -> output.encodeJsonElement(buildJsonObject { put("INCLUDED", JsonNull) })
            is TxExecutionStatus.Executedoptimistic ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("EXECUTED_OPTIMISTIC", JsonNull)
                    },
                )
            is TxExecutionStatus.Includedfinal ->
                output.encodeJsonElement(
                    buildJsonObject { put("INCLUDED_FINAL", JsonNull) },
                )
            is TxExecutionStatus.Executed -> output.encodeJsonElement(buildJsonObject { put("EXECUTED", JsonNull) })
            is TxExecutionStatus.Final -> output.encodeJsonElement(buildJsonObject { put("FINAL", JsonNull) })
        }
    }

    override fun deserialize(decoder: Decoder): TxExecutionStatus {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "NONE" in element -> TxExecutionStatus.None
            "INCLUDED" in element -> TxExecutionStatus.Included
            "EXECUTED_OPTIMISTIC" in element -> TxExecutionStatus.Executedoptimistic
            "INCLUDED_FINAL" in element -> TxExecutionStatus.Includedfinal
            "EXECUTED" in element -> TxExecutionStatus.Executed
            "FINAL" in element -> TxExecutionStatus.Final
            else -> throw SerializationException("Unknown variant in TxExecutionStatus: ${element.keys}")
        }
    }
}

@Serializable(with = VMKindSerializer::class)
sealed interface VMKind {
    @Serializable
    @SerialName("Wasmer0")
    object Wasmer0 : VMKind

    @Serializable
    @SerialName("Wasmtime")
    object Wasmtime : VMKind

    @Serializable
    @SerialName("Wasmer2")
    object Wasmer2 : VMKind

    @Serializable
    @SerialName("NearVm")
    object Nearvm : VMKind
}

// Custom serializer for VMKind to handle NEAR's externally-tagged union format
object VMKindSerializer : KSerializer<VMKind> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("VMKind")

    override fun serialize(
        encoder: Encoder,
        value: VMKind,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is VMKind.Wasmer0 -> output.encodeJsonElement(buildJsonObject { put("Wasmer0", JsonNull) })
            is VMKind.Wasmtime -> output.encodeJsonElement(buildJsonObject { put("Wasmtime", JsonNull) })
            is VMKind.Wasmer2 -> output.encodeJsonElement(buildJsonObject { put("Wasmer2", JsonNull) })
            is VMKind.Nearvm -> output.encodeJsonElement(buildJsonObject { put("NearVm", JsonNull) })
        }
    }

    override fun deserialize(decoder: Decoder): VMKind {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "Wasmer0" in element -> VMKind.Wasmer0
            "Wasmtime" in element -> VMKind.Wasmtime
            "Wasmer2" in element -> VMKind.Wasmer2
            "NearVm" in element -> VMKind.Nearvm
            else -> throw SerializationException("Unknown variant in VMKind: ${element.keys}")
        }
    }
}

/**
 * Reasons for removing a validator from the validator set.
 */
@Serializable(with = ValidatorKickoutReasonSerializer::class)
sealed interface ValidatorKickoutReason {
    @Serializable
    @SerialName("_UnusedSlashed")
    object Unusedslashed : ValidatorKickoutReason

    @Serializable
    data class NotEnoughBlocks(
        @SerialName("expected")
        val expected: Long,
        @SerialName("produced")
        val produced: Long,
    ) : ValidatorKickoutReason

    @Serializable
    data class NotEnoughChunks(
        @SerialName("expected")
        val expected: Long,
        @SerialName("produced")
        val produced: Long,
    ) : ValidatorKickoutReason

    @Serializable
    @SerialName("Unstaked")
    object Unstaked : ValidatorKickoutReason

    @Serializable
    data class NotEnoughStake(
        @SerialName("stake_u128")
        val stakeU128: NearToken,
        @SerialName("threshold_u128")
        val thresholdU128: NearToken,
    ) : ValidatorKickoutReason

    @Serializable
    @SerialName("DidNotGetASeat")
    object Didnotgetaseat : ValidatorKickoutReason

    @Serializable
    data class NotEnoughChunkEndorsements(
        @SerialName("expected")
        val expected: Long,
        @SerialName("produced")
        val produced: Long,
    ) : ValidatorKickoutReason

    @Serializable
    data class ProtocolVersionTooOld(
        @SerialName("network_version")
        val networkVersion: Long,
        @SerialName("version")
        val version: Long,
    ) : ValidatorKickoutReason
}

// Custom serializer for ValidatorKickoutReason to handle NEAR's externally-tagged union format
object ValidatorKickoutReasonSerializer : KSerializer<ValidatorKickoutReason> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ValidatorKickoutReason")

    override fun serialize(
        encoder: Encoder,
        value: ValidatorKickoutReason,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is ValidatorKickoutReason.Unusedslashed ->
                output.encodeJsonElement(
                    buildJsonObject { put("_UnusedSlashed", JsonNull) },
                )
            is ValidatorKickoutReason.NotEnoughBlocks ->
                output.encodeSerializableValue(
                    ValidatorKickoutReason.NotEnoughBlocks.serializer(),
                    value,
                )
            is ValidatorKickoutReason.NotEnoughChunks ->
                output.encodeSerializableValue(
                    ValidatorKickoutReason.NotEnoughChunks.serializer(),
                    value,
                )
            is ValidatorKickoutReason.Unstaked ->
                output.encodeJsonElement(
                    buildJsonObject { put("Unstaked", JsonNull) },
                )
            is ValidatorKickoutReason.NotEnoughStake ->
                output.encodeSerializableValue(
                    ValidatorKickoutReason.NotEnoughStake.serializer(),
                    value,
                )
            is ValidatorKickoutReason.Didnotgetaseat ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("DidNotGetASeat", JsonNull)
                    },
                )
            is ValidatorKickoutReason.NotEnoughChunkEndorsements ->
                output.encodeSerializableValue(
                    ValidatorKickoutReason.NotEnoughChunkEndorsements.serializer(),
                    value,
                )
            is ValidatorKickoutReason.ProtocolVersionTooOld ->
                output.encodeSerializableValue(
                    ValidatorKickoutReason.ProtocolVersionTooOld.serializer(),
                    value,
                )
        }
    }

    override fun deserialize(decoder: Decoder): ValidatorKickoutReason {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "_UnusedSlashed" in element -> ValidatorKickoutReason.Unusedslashed
            "NotEnoughBlocks" in element ->
                input.json.decodeFromJsonElement(
                    ValidatorKickoutReason.NotEnoughBlocks.serializer(),
                    element["NotEnoughBlocks"]!!,
                )
            "NotEnoughChunks" in element ->
                input.json.decodeFromJsonElement(
                    ValidatorKickoutReason.NotEnoughChunks.serializer(),
                    element["NotEnoughChunks"]!!,
                )
            "Unstaked" in element -> ValidatorKickoutReason.Unstaked
            "NotEnoughStake" in element ->
                input.json.decodeFromJsonElement(
                    ValidatorKickoutReason.NotEnoughStake.serializer(),
                    element["NotEnoughStake"]!!,
                )
            "DidNotGetASeat" in element -> ValidatorKickoutReason.Didnotgetaseat
            "NotEnoughChunkEndorsements" in element ->
                input.json.decodeFromJsonElement(
                    ValidatorKickoutReason.NotEnoughChunkEndorsements.serializer(),
                    element["NotEnoughChunkEndorsements"]!!,
                )
            "ProtocolVersionTooOld" in element ->
                input.json.decodeFromJsonElement(
                    ValidatorKickoutReason.ProtocolVersionTooOld.serializer(),
                    element["ProtocolVersionTooOld"]!!,
                )
            else -> throw SerializationException("Unknown variant in ValidatorKickoutReason: ${element.keys}")
        }
    }
}

@Serializable
data class ValidatorStakeView(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("stake")
    val stake: NearToken,
    @SerialName("validator_stake_struct_version")
    val validatorStakeStructVersion: String,
)

/**
 * A kind of a trap happened during execution of a binary
 */
@Serializable(with = WasmTrapSerializer::class)
sealed interface WasmTrap {
    @Serializable
    @SerialName("Unreachable")
    object Unreachable : WasmTrap

    @Serializable
    @SerialName("IncorrectCallIndirectSignature")
    object Incorrectcallindirectsignature : WasmTrap

    @Serializable
    @SerialName("MemoryOutOfBounds")
    object Memoryoutofbounds : WasmTrap

    @Serializable
    @SerialName("CallIndirectOOB")
    object Callindirectoob : WasmTrap

    @Serializable
    @SerialName("IllegalArithmetic")
    object Illegalarithmetic : WasmTrap

    @Serializable
    @SerialName("MisalignedAtomicAccess")
    object Misalignedatomicaccess : WasmTrap

    @Serializable
    @SerialName("IndirectCallToNull")
    object Indirectcalltonull : WasmTrap

    @Serializable
    @SerialName("StackOverflow")
    object Stackoverflow : WasmTrap

    @Serializable
    @SerialName("GenericTrap")
    object Generictrap : WasmTrap
}

// Custom serializer for WasmTrap to handle NEAR's externally-tagged union format
object WasmTrapSerializer : KSerializer<WasmTrap> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WasmTrap")

    override fun serialize(
        encoder: Encoder,
        value: WasmTrap,
    ) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This serializer only works with JSON")
        when (value) {
            is WasmTrap.Unreachable -> output.encodeJsonElement(buildJsonObject { put("Unreachable", JsonNull) })
            is WasmTrap.Incorrectcallindirectsignature ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("IncorrectCallIndirectSignature", JsonNull)
                    },
                )
            is WasmTrap.Memoryoutofbounds ->
                output.encodeJsonElement(
                    buildJsonObject { put("MemoryOutOfBounds", JsonNull) },
                )
            is WasmTrap.Callindirectoob ->
                output.encodeJsonElement(
                    buildJsonObject { put("CallIndirectOOB", JsonNull) },
                )
            is WasmTrap.Illegalarithmetic ->
                output.encodeJsonElement(
                    buildJsonObject { put("IllegalArithmetic", JsonNull) },
                )
            is WasmTrap.Misalignedatomicaccess ->
                output.encodeJsonElement(
                    buildJsonObject {
                        put("MisalignedAtomicAccess", JsonNull)
                    },
                )
            is WasmTrap.Indirectcalltonull ->
                output.encodeJsonElement(
                    buildJsonObject { put("IndirectCallToNull", JsonNull) },
                )
            is WasmTrap.Stackoverflow -> output.encodeJsonElement(buildJsonObject { put("StackOverflow", JsonNull) })
            is WasmTrap.Generictrap -> output.encodeJsonElement(buildJsonObject { put("GenericTrap", JsonNull) })
        }
    }

    override fun deserialize(decoder: Decoder): WasmTrap {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer only works with JSON")
        val element = input.decodeJsonElement().jsonObject
        return when {
            "Unreachable" in element -> WasmTrap.Unreachable
            "IncorrectCallIndirectSignature" in element -> WasmTrap.Incorrectcallindirectsignature
            "MemoryOutOfBounds" in element -> WasmTrap.Memoryoutofbounds
            "CallIndirectOOB" in element -> WasmTrap.Callindirectoob
            "IllegalArithmetic" in element -> WasmTrap.Illegalarithmetic
            "MisalignedAtomicAccess" in element -> WasmTrap.Misalignedatomicaccess
            "IndirectCallToNull" in element -> WasmTrap.Indirectcalltonull
            "StackOverflow" in element -> WasmTrap.Stackoverflow
            "GenericTrap" in element -> WasmTrap.Generictrap
            else -> throw SerializationException("Unknown variant in WasmTrap: ${element.keys}")
        }
    }
}

/**
 * Epoch identifier -- wrapped hash, to make it easier to distinguish.
 */
typealias EpochId = String

/**
 * Peer id is the public key.
 */
typealias PeerId = String

@Serializable
data class AccessKey(
    @SerialName("nonce")
    val nonce: Long,
    @SerialName("permission")
    val permission: AccessKeyPermission,
)

@Serializable
data class AccessKeyCreationConfigView(
    @SerialName("full_access_cost")
    val fullAccessCost: Fee,
    @SerialName("function_call_cost")
    val functionCallCost: Fee,
    @SerialName("function_call_cost_per_byte")
    val functionCallCostPerByte: Fee,
)

@Serializable
data class AccessKeyInfoView(
    @SerialName("access_key")
    val accessKey: AccessKeyView,
    @SerialName("public_key")
    val publicKey: PublicKey,
)

@Serializable
data class AccessKeyList(
    @SerialName("keys")
    val keys: List<AccessKeyInfoView>,
)

@Serializable
data class AccessKeyView(
    @SerialName("nonce")
    val nonce: Long,
    @SerialName("permission")
    val permission: AccessKeyPermissionView,
)

@Serializable
data class AccountCreationConfigView(
    @SerialName("min_allowed_top_level_account_length")
    val minAllowedTopLevelAccountLength: Long,
    @SerialName("registrar_account_id")
    val registrarAccountId: AccountId,
)

@Serializable
data class AccountDataView(
    @SerialName("account_key")
    val accountKey: PublicKey,
    @SerialName("peer_id")
    val peerId: PublicKey,
    @SerialName("proxies")
    val proxies: List<Tier1ProxyView>,
    @SerialName("timestamp")
    val timestamp: String,
)

@Serializable
data class AccountInfo(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("amount")
    val amount: NearToken,
    @SerialName("public_key")
    val publicKey: PublicKey,
)

@Serializable
data class AccountView(
    @SerialName("amount")
    val amount: NearToken,
    @SerialName("code_hash")
    val codeHash: CryptoHash,
    @SerialName("global_contract_account_id")
    val globalContractAccountId: AccountId? = null,
    @SerialName("global_contract_hash")
    val globalContractHash: CryptoHash? = null,
    @SerialName("locked")
    val locked: NearToken,
    @SerialName("storage_paid_at")
    val storagePaidAt: Long? = null,
    @SerialName("storage_usage")
    val storageUsage: Long,
)

@Serializable
data class AccountWithPublicKey(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("public_key")
    val publicKey: PublicKey,
)

@Serializable
data class ActionCreationConfigView(
    @SerialName("add_key_cost")
    val addKeyCost: AccessKeyCreationConfigView,
    @SerialName("create_account_cost")
    val createAccountCost: Fee,
    @SerialName("delegate_cost")
    val delegateCost: Fee,
    @SerialName("delete_account_cost")
    val deleteAccountCost: Fee,
    @SerialName("delete_key_cost")
    val deleteKeyCost: Fee,
    @SerialName("deploy_contract_cost")
    val deployContractCost: Fee,
    @SerialName("deploy_contract_cost_per_byte")
    val deployContractCostPerByte: Fee,
    @SerialName("function_call_cost")
    val functionCallCost: Fee,
    @SerialName("function_call_cost_per_byte")
    val functionCallCostPerByte: Fee,
    @SerialName("stake_cost")
    val stakeCost: Fee,
    @SerialName("transfer_cost")
    val transferCost: Fee,
)

@Serializable
data class ActionError(
    @SerialName("index")
    val index: Long? = null,
    @SerialName("kind")
    val kind: ActionErrorKind,
)

@Serializable
data class AddKeyAction(
    @SerialName("access_key")
    val accessKey: AccessKey,
    @SerialName("public_key")
    val publicKey: PublicKey,
)

@Serializable
data class BandwidthRequest(
    @SerialName("requested_values_bitmap")
    val requestedValuesBitmap: BandwidthRequestBitmap,
    @SerialName("to_shard")
    val toShard: Long,
)

@Serializable
data class BandwidthRequestBitmap(
    @SerialName("data")
    val data: List<Long>,
)

@Serializable
data class BandwidthRequestsV1(
    @SerialName("requests")
    val requests: List<BandwidthRequest>,
)

@Serializable
data class BlockHeaderInnerLiteView(
    @SerialName("block_merkle_root")
    val blockMerkleRoot: CryptoHash,
    @SerialName("epoch_id")
    val epochId: CryptoHash,
    @SerialName("height")
    val height: Long,
    @SerialName("next_bp_hash")
    val nextBpHash: CryptoHash,
    @SerialName("next_epoch_id")
    val nextEpochId: CryptoHash,
    @SerialName("outcome_root")
    val outcomeRoot: CryptoHash,
    @SerialName("prev_state_root")
    val prevStateRoot: CryptoHash,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("timestamp_nanosec")
    val timestampNanosec: String,
)

@Serializable
data class BlockHeaderView(
    @SerialName("approvals")
    val approvals: List<Signature?>,
    @SerialName("block_body_hash")
    val blockBodyHash: CryptoHash? = null,
    @SerialName("block_merkle_root")
    val blockMerkleRoot: CryptoHash,
    @SerialName("block_ordinal")
    val blockOrdinal: Long? = null,
    @SerialName("challenges_result")
    val challengesResult: List<SlashedValidator>,
    @SerialName("challenges_root")
    val challengesRoot: CryptoHash,
    @SerialName("chunk_endorsements")
    val chunkEndorsements: List<List<Long>>? = null,
    @SerialName("chunk_headers_root")
    val chunkHeadersRoot: CryptoHash,
    @SerialName("chunk_mask")
    val chunkMask: List<Boolean>,
    @SerialName("chunk_receipts_root")
    val chunkReceiptsRoot: CryptoHash,
    @SerialName("chunk_tx_root")
    val chunkTxRoot: CryptoHash,
    @SerialName("chunks_included")
    val chunksIncluded: Long,
    @SerialName("epoch_id")
    val epochId: CryptoHash,
    @SerialName("epoch_sync_data_hash")
    val epochSyncDataHash: CryptoHash? = null,
    @SerialName("gas_price")
    val gasPrice: NearToken,
    @SerialName("hash")
    val hash: CryptoHash,
    @SerialName("height")
    val height: Long,
    @SerialName("last_ds_final_block")
    val lastDsFinalBlock: CryptoHash,
    @SerialName("last_final_block")
    val lastFinalBlock: CryptoHash,
    @SerialName("latest_protocol_version")
    val latestProtocolVersion: Long,
    @SerialName("next_bp_hash")
    val nextBpHash: CryptoHash,
    @SerialName("next_epoch_id")
    val nextEpochId: CryptoHash,
    @SerialName("outcome_root")
    val outcomeRoot: CryptoHash,
    @SerialName("prev_hash")
    val prevHash: CryptoHash,
    @SerialName("prev_height")
    val prevHeight: Long? = null,
    @SerialName("prev_state_root")
    val prevStateRoot: CryptoHash,
    @SerialName("random_value")
    val randomValue: CryptoHash,
    @SerialName("rent_paid")
    val rentPaid: NearToken? = null,
    @SerialName("signature")
    val signature: Signature,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("timestamp_nanosec")
    val timestampNanosec: String,
    @SerialName("total_supply")
    val totalSupply: NearToken,
    @SerialName("validator_proposals")
    val validatorProposals: List<ValidatorStakeView>,
    @SerialName("validator_reward")
    val validatorReward: NearToken? = null,
)

@Serializable
data class BlockStatusView(
    @SerialName("hash")
    val hash: CryptoHash,
    @SerialName("height")
    val height: Long,
)

@Serializable
data class CallResult(
    @SerialName("logs")
    val logs: List<String>,
    @SerialName("result")
    val result: List<Long>,
)

@Serializable
data class CatchupStatusView(
    @SerialName("blocks_to_catchup")
    val blocksToCatchup: List<BlockStatusView>,
    @SerialName("shard_sync_status")
    val shardSyncStatus: Map<String, String>,
    @SerialName("sync_block_hash")
    val syncBlockHash: CryptoHash,
    @SerialName("sync_block_height")
    val syncBlockHeight: Long,
)

@Serializable
data class ChunkDistributionNetworkConfig(
    @SerialName("enabled")
    val enabled: Boolean,
    @SerialName("uris")
    val uris: ChunkDistributionUris,
)

@Serializable
data class ChunkDistributionUris(
    @SerialName("get")
    val get: String,
    @SerialName("set")
    val set: String,
)

@Serializable
data class ChunkHeaderView(
    @SerialName("balance_burnt")
    val balanceBurnt: NearToken,
    @SerialName("bandwidth_requests")
    val bandwidthRequests: BandwidthRequests? = null,
    @SerialName("chunk_hash")
    val chunkHash: CryptoHash,
    @SerialName("congestion_info")
    val congestionInfo: CongestionInfoView? = null,
    @SerialName("encoded_length")
    val encodedLength: Long,
    @SerialName("encoded_merkle_root")
    val encodedMerkleRoot: CryptoHash,
    @SerialName("gas_limit")
    val gasLimit: NearGas,
    @SerialName("gas_used")
    val gasUsed: NearGas,
    @SerialName("height_created")
    val heightCreated: Long,
    @SerialName("height_included")
    val heightIncluded: Long,
    @SerialName("outcome_root")
    val outcomeRoot: CryptoHash,
    @SerialName("outgoing_receipts_root")
    val outgoingReceiptsRoot: CryptoHash,
    @SerialName("prev_block_hash")
    val prevBlockHash: CryptoHash,
    @SerialName("prev_state_root")
    val prevStateRoot: CryptoHash,
    @SerialName("rent_paid")
    val rentPaid: NearToken? = null,
    @SerialName("shard_id")
    val shardId: ShardId,
    @SerialName("signature")
    val signature: Signature,
    @SerialName("tx_root")
    val txRoot: CryptoHash,
    @SerialName("validator_proposals")
    val validatorProposals: List<ValidatorStakeView>,
    @SerialName("validator_reward")
    val validatorReward: NearToken? = null,
)

@Serializable
data class CloudArchivalReaderConfig(
    @SerialName("cloud_storage")
    val cloudStorage: CloudStorageConfig,
)

@Serializable
data class CloudArchivalWriterConfig(
    @SerialName("archive_block_data")
    val archiveBlockData: Boolean? = null,
    @SerialName("cloud_storage")
    val cloudStorage: CloudStorageConfig,
    @SerialName("polling_interval")
    val pollingInterval: DurationAsStdSchemaProvider? = null,
)

@Serializable
data class CloudStorageConfig(
    @SerialName("credentials_file")
    val credentialsFile: String? = null,
    @SerialName("storage")
    val storage: ExternalStorageLocation,
)

@Serializable
data class CongestionControlConfigView(
    @SerialName("allowed_shard_outgoing_gas")
    val allowedShardOutgoingGas: NearGas,
    @SerialName("max_congestion_incoming_gas")
    val maxCongestionIncomingGas: NearGas,
    @SerialName("max_congestion_memory_consumption")
    val maxCongestionMemoryConsumption: Long,
    @SerialName("max_congestion_missed_chunks")
    val maxCongestionMissedChunks: Long,
    @SerialName("max_congestion_outgoing_gas")
    val maxCongestionOutgoingGas: NearGas,
    @SerialName("max_outgoing_gas")
    val maxOutgoingGas: NearGas,
    @SerialName("max_tx_gas")
    val maxTxGas: NearGas,
    @SerialName("min_outgoing_gas")
    val minOutgoingGas: NearGas,
    @SerialName("min_tx_gas")
    val minTxGas: NearGas,
    @SerialName("outgoing_receipts_big_size_limit")
    val outgoingReceiptsBigSizeLimit: Long,
    @SerialName("outgoing_receipts_usual_size_limit")
    val outgoingReceiptsUsualSizeLimit: Long,
    @SerialName("reject_tx_congestion_threshold")
    val rejectTxCongestionThreshold: Double,
)

@Serializable
data class CongestionInfoView(
    @SerialName("allowed_shard")
    val allowedShard: Long,
    @SerialName("buffered_receipts_gas")
    val bufferedReceiptsGas: String,
    @SerialName("delayed_receipts_gas")
    val delayedReceiptsGas: String,
    @SerialName("receipt_bytes")
    val receiptBytes: Long,
)

@Serializable
data class ContractCodeView(
    @SerialName("code_base64")
    val codeBase64: String,
    @SerialName("hash")
    val hash: CryptoHash,
)

@Serializable
data class CostGasUsed(
    @SerialName("cost")
    val cost: String,
    @SerialName("cost_category")
    val costCategory: String,
    @SerialName("gas_used")
    val gasUsed: String,
)

@Serializable
object CreateAccountAction

@Serializable
data class CurrentEpochValidatorInfo(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("is_slashed")
    val isSlashed: Boolean,
    @SerialName("num_expected_blocks")
    val numExpectedBlocks: Long,
    @SerialName("num_expected_chunks")
    val numExpectedChunks: Long? = null,
    @SerialName("num_expected_chunks_per_shard")
    val numExpectedChunksPerShard: List<Long>? = null,
    @SerialName("num_expected_endorsements")
    val numExpectedEndorsements: Long? = null,
    @SerialName("num_expected_endorsements_per_shard")
    val numExpectedEndorsementsPerShard: List<Long>? = null,
    @SerialName("num_produced_blocks")
    val numProducedBlocks: Long,
    @SerialName("num_produced_chunks")
    val numProducedChunks: Long? = null,
    @SerialName("num_produced_chunks_per_shard")
    val numProducedChunksPerShard: List<Long>? = null,
    @SerialName("num_produced_endorsements")
    val numProducedEndorsements: Long? = null,
    @SerialName("num_produced_endorsements_per_shard")
    val numProducedEndorsementsPerShard: List<Long>? = null,
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("shards")
    val shards: List<ShardId>,
    @SerialName("shards_endorsed")
    val shardsEndorsed: List<ShardId>? = null,
    @SerialName("stake")
    val stake: NearToken,
)

@Serializable
data class DataReceiptCreationConfigView(
    @SerialName("base_cost")
    val baseCost: Fee,
    @SerialName("cost_per_byte")
    val costPerByte: Fee,
)

@Serializable
data class DataReceiverView(
    @SerialName("data_id")
    val dataId: CryptoHash,
    @SerialName("receiver_id")
    val receiverId: AccountId,
)

@Serializable
data class DelegateAction(
    @SerialName("actions")
    val actions: List<NonDelegateAction>,
    @SerialName("max_block_height")
    val maxBlockHeight: Long,
    @SerialName("nonce")
    val nonce: Long,
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("receiver_id")
    val receiverId: AccountId,
    @SerialName("sender_id")
    val senderId: AccountId,
)

@Serializable
data class DeleteAccountAction(
    @SerialName("beneficiary_id")
    val beneficiaryId: AccountId,
)

@Serializable
data class DeleteKeyAction(
    @SerialName("public_key")
    val publicKey: PublicKey,
)

@Serializable
data class DeployContractAction(
    @SerialName("code")
    val code: String,
)

@Serializable
data class DeployGlobalContractAction(
    @SerialName("code")
    val code: String,
    @SerialName("deploy_mode")
    val deployMode: GlobalContractDeployMode,
)

@Serializable
data class DetailedDebugStatus(
    @SerialName("block_production_delay_millis")
    val blockProductionDelayMillis: Long,
    @SerialName("catchup_status")
    val catchupStatus: List<CatchupStatusView>,
    @SerialName("current_head_status")
    val currentHeadStatus: BlockStatusView,
    @SerialName("current_header_head_status")
    val currentHeaderHeadStatus: BlockStatusView,
    @SerialName("network_info")
    val networkInfo: NetworkInfoView,
    @SerialName("sync_status")
    val syncStatus: String,
)

@Serializable
data class DeterministicAccountStateInitV1(
    @SerialName("code")
    val code: GlobalContractIdentifier,
    @SerialName("data")
    val data: Map<String, String>,
)

@Serializable
data class DeterministicStateInitAction(
    @SerialName("deposit")
    val deposit: NearToken,
    @SerialName("state_init")
    val stateInit: DeterministicAccountStateInit,
)

@Serializable
data class DumpConfig(
    @SerialName("credentials_file")
    val credentialsFile: String? = null,
    @SerialName("iteration_delay")
    val iterationDelay: DurationAsStdSchemaProvider? = null,
    @SerialName("location")
    val location: ExternalStorageLocation,
    @SerialName("restart_dump_for_shards")
    val restartDumpForShards: List<ShardId>? = null,
)

@Serializable
data class DurationAsStdSchemaProvider(
    @SerialName("nanos")
    val nanos: Int,
    @SerialName("secs")
    val secs: Long,
)

@Serializable
data class EpochSyncConfig(
    @SerialName("disable_epoch_sync_for_bootstrapping")
    val disableEpochSyncForBootstrapping: Boolean? = null,
    @SerialName("epoch_sync_horizon")
    val epochSyncHorizon: Long,
    @SerialName("ignore_epoch_sync_network_requests")
    val ignoreEpochSyncNetworkRequests: Boolean? = null,
    @SerialName("timeout_for_epoch_sync")
    val timeoutForEpochSync: DurationAsStdSchemaProvider,
)

@Serializable
data class ExecutionMetadataView(
    @SerialName("gas_profile")
    val gasProfile: List<CostGasUsed>? = null,
    @SerialName("version")
    val version: Long,
)

@Serializable
data class ExecutionOutcomeView(
    @SerialName("executor_id")
    val executorId: AccountId,
    @SerialName("gas_burnt")
    val gasBurnt: NearGas,
    @SerialName("logs")
    val logs: List<String>,
    @SerialName("metadata")
    val metadata: ExecutionMetadataView? = null,
    @SerialName("receipt_ids")
    val receiptIds: List<CryptoHash>,
    @SerialName("status")
    val status: ExecutionStatusView,
    @SerialName("tokens_burnt")
    val tokensBurnt: NearToken,
)

@Serializable
data class ExecutionOutcomeWithIdView(
    @SerialName("block_hash")
    val blockHash: CryptoHash,
    @SerialName("id")
    val id: CryptoHash,
    @SerialName("outcome")
    val outcome: ExecutionOutcomeView,
    @SerialName("proof")
    val proof: List<MerklePathItem>,
)

@Serializable
data class ExtCostsConfigView(
    @SerialName("alt_bn128_g1_multiexp_base")
    val altBn128G1MultiexpBase: NearGas,
    @SerialName("alt_bn128_g1_multiexp_element")
    val altBn128G1MultiexpElement: NearGas,
    @SerialName("alt_bn128_g1_sum_base")
    val altBn128G1SumBase: NearGas,
    @SerialName("alt_bn128_g1_sum_element")
    val altBn128G1SumElement: NearGas,
    @SerialName("alt_bn128_pairing_check_base")
    val altBn128PairingCheckBase: NearGas,
    @SerialName("alt_bn128_pairing_check_element")
    val altBn128PairingCheckElement: NearGas,
    @SerialName("base")
    val base: NearGas,
    @SerialName("bls12381_g1_multiexp_base")
    val bls12381G1MultiexpBase: NearGas,
    @SerialName("bls12381_g1_multiexp_element")
    val bls12381G1MultiexpElement: NearGas,
    @SerialName("bls12381_g2_multiexp_base")
    val bls12381G2MultiexpBase: NearGas,
    @SerialName("bls12381_g2_multiexp_element")
    val bls12381G2MultiexpElement: NearGas,
    @SerialName("bls12381_map_fp2_to_g2_base")
    val bls12381MapFp2ToG2Base: NearGas,
    @SerialName("bls12381_map_fp2_to_g2_element")
    val bls12381MapFp2ToG2Element: NearGas,
    @SerialName("bls12381_map_fp_to_g1_base")
    val bls12381MapFpToG1Base: NearGas,
    @SerialName("bls12381_map_fp_to_g1_element")
    val bls12381MapFpToG1Element: NearGas,
    @SerialName("bls12381_p1_decompress_base")
    val bls12381P1DecompressBase: NearGas,
    @SerialName("bls12381_p1_decompress_element")
    val bls12381P1DecompressElement: NearGas,
    @SerialName("bls12381_p1_sum_base")
    val bls12381P1SumBase: NearGas,
    @SerialName("bls12381_p1_sum_element")
    val bls12381P1SumElement: NearGas,
    @SerialName("bls12381_p2_decompress_base")
    val bls12381P2DecompressBase: NearGas,
    @SerialName("bls12381_p2_decompress_element")
    val bls12381P2DecompressElement: NearGas,
    @SerialName("bls12381_p2_sum_base")
    val bls12381P2SumBase: NearGas,
    @SerialName("bls12381_p2_sum_element")
    val bls12381P2SumElement: NearGas,
    @SerialName("bls12381_pairing_base")
    val bls12381PairingBase: NearGas,
    @SerialName("bls12381_pairing_element")
    val bls12381PairingElement: NearGas,
    @SerialName("contract_compile_base")
    val contractCompileBase: NearGas,
    @SerialName("contract_compile_bytes")
    val contractCompileBytes: NearGas,
    @SerialName("contract_loading_base")
    val contractLoadingBase: NearGas,
    @SerialName("contract_loading_bytes")
    val contractLoadingBytes: NearGas,
    @SerialName("ecrecover_base")
    val ecrecoverBase: NearGas,
    @SerialName("ed25519_verify_base")
    val ed25519VerifyBase: NearGas,
    @SerialName("ed25519_verify_byte")
    val ed25519VerifyByte: NearGas,
    @SerialName("keccak256_base")
    val keccak256Base: NearGas,
    @SerialName("keccak256_byte")
    val keccak256Byte: NearGas,
    @SerialName("keccak512_base")
    val keccak512Base: NearGas,
    @SerialName("keccak512_byte")
    val keccak512Byte: NearGas,
    @SerialName("log_base")
    val logBase: NearGas,
    @SerialName("log_byte")
    val logByte: NearGas,
    @SerialName("promise_and_base")
    val promiseAndBase: NearGas,
    @SerialName("promise_and_per_promise")
    val promiseAndPerPromise: NearGas,
    @SerialName("promise_return")
    val promiseReturn: NearGas,
    @SerialName("read_cached_trie_node")
    val readCachedTrieNode: NearGas,
    @SerialName("read_memory_base")
    val readMemoryBase: NearGas,
    @SerialName("read_memory_byte")
    val readMemoryByte: NearGas,
    @SerialName("read_register_base")
    val readRegisterBase: NearGas,
    @SerialName("read_register_byte")
    val readRegisterByte: NearGas,
    @SerialName("ripemd160_base")
    val ripemd160Base: NearGas,
    @SerialName("ripemd160_block")
    val ripemd160Block: NearGas,
    @SerialName("sha256_base")
    val sha256Base: NearGas,
    @SerialName("sha256_byte")
    val sha256Byte: NearGas,
    @SerialName("storage_has_key_base")
    val storageHasKeyBase: NearGas,
    @SerialName("storage_has_key_byte")
    val storageHasKeyByte: NearGas,
    @SerialName("storage_iter_create_from_byte")
    val storageIterCreateFromByte: NearGas,
    @SerialName("storage_iter_create_prefix_base")
    val storageIterCreatePrefixBase: NearGas,
    @SerialName("storage_iter_create_prefix_byte")
    val storageIterCreatePrefixByte: NearGas,
    @SerialName("storage_iter_create_range_base")
    val storageIterCreateRangeBase: NearGas,
    @SerialName("storage_iter_create_to_byte")
    val storageIterCreateToByte: NearGas,
    @SerialName("storage_iter_next_base")
    val storageIterNextBase: NearGas,
    @SerialName("storage_iter_next_key_byte")
    val storageIterNextKeyByte: NearGas,
    @SerialName("storage_iter_next_value_byte")
    val storageIterNextValueByte: NearGas,
    @SerialName("storage_large_read_overhead_base")
    val storageLargeReadOverheadBase: NearGas,
    @SerialName("storage_large_read_overhead_byte")
    val storageLargeReadOverheadByte: NearGas,
    @SerialName("storage_read_base")
    val storageReadBase: NearGas,
    @SerialName("storage_read_key_byte")
    val storageReadKeyByte: NearGas,
    @SerialName("storage_read_value_byte")
    val storageReadValueByte: NearGas,
    @SerialName("storage_remove_base")
    val storageRemoveBase: NearGas,
    @SerialName("storage_remove_key_byte")
    val storageRemoveKeyByte: NearGas,
    @SerialName("storage_remove_ret_value_byte")
    val storageRemoveRetValueByte: NearGas,
    @SerialName("storage_write_base")
    val storageWriteBase: NearGas,
    @SerialName("storage_write_evicted_byte")
    val storageWriteEvictedByte: NearGas,
    @SerialName("storage_write_key_byte")
    val storageWriteKeyByte: NearGas,
    @SerialName("storage_write_value_byte")
    val storageWriteValueByte: NearGas,
    @SerialName("touching_trie_node")
    val touchingTrieNode: NearGas,
    @SerialName("utf16_decoding_base")
    val utf16DecodingBase: NearGas,
    @SerialName("utf16_decoding_byte")
    val utf16DecodingByte: NearGas,
    @SerialName("utf8_decoding_base")
    val utf8DecodingBase: NearGas,
    @SerialName("utf8_decoding_byte")
    val utf8DecodingByte: NearGas,
    @SerialName("validator_stake_base")
    val validatorStakeBase: NearGas,
    @SerialName("validator_total_stake_base")
    val validatorTotalStakeBase: NearGas,
    @SerialName("write_memory_base")
    val writeMemoryBase: NearGas,
    @SerialName("write_memory_byte")
    val writeMemoryByte: NearGas,
    @SerialName("write_register_base")
    val writeRegisterBase: NearGas,
    @SerialName("write_register_byte")
    val writeRegisterByte: NearGas,
    @SerialName("yield_create_base")
    val yieldCreateBase: NearGas,
    @SerialName("yield_create_byte")
    val yieldCreateByte: NearGas,
    @SerialName("yield_resume_base")
    val yieldResumeBase: NearGas,
    @SerialName("yield_resume_byte")
    val yieldResumeByte: NearGas,
)

@Serializable
data class ExternalStorageConfig(
    @SerialName("external_storage_fallback_threshold")
    val externalStorageFallbackThreshold: Long? = null,
    @SerialName("location")
    val location: ExternalStorageLocation,
    @SerialName("num_concurrent_requests")
    val numConcurrentRequests: Long? = null,
    @SerialName("num_concurrent_requests_during_catchup")
    val numConcurrentRequestsDuringCatchup: Long? = null,
)

@Serializable
data class Fee(
    @SerialName("execution")
    val execution: NearGas,
    @SerialName("send_not_sir")
    val sendNotSir: NearGas,
    @SerialName("send_sir")
    val sendSir: NearGas,
)

@Serializable
data class FinalExecutionOutcomeView(
    @SerialName("receipts_outcome")
    val receiptsOutcome: List<ExecutionOutcomeWithIdView>,
    @SerialName("status")
    val status: FinalExecutionStatus,
    @SerialName("transaction")
    val transaction: SignedTransactionView,
    @SerialName("transaction_outcome")
    val transactionOutcome: ExecutionOutcomeWithIdView,
)

@Serializable
data class FinalExecutionOutcomeWithReceiptView(
    @SerialName("receipts")
    val receipts: List<ReceiptView>,
    @SerialName("receipts_outcome")
    val receiptsOutcome: List<ExecutionOutcomeWithIdView>,
    @SerialName("status")
    val status: FinalExecutionStatus,
    @SerialName("transaction")
    val transaction: SignedTransactionView,
    @SerialName("transaction_outcome")
    val transactionOutcome: ExecutionOutcomeWithIdView,
)

@Serializable
data class FunctionCallAction(
    @SerialName("args")
    val args: String,
    @SerialName("deposit")
    val deposit: NearToken,
    @SerialName("gas")
    val gas: NearGas,
    @SerialName("method_name")
    val methodName: String,
)

@Serializable
data class FunctionCallPermission(
    @SerialName("allowance")
    val allowance: NearToken? = null,
    @SerialName("method_names")
    val methodNames: List<String>,
    @SerialName("receiver_id")
    val receiverId: String,
)

@Serializable
data class GCConfig(
    @SerialName("gc_blocks_limit")
    val gcBlocksLimit: Long? = null,
    @SerialName("gc_fork_clean_step")
    val gcForkCleanStep: Long? = null,
    @SerialName("gc_num_epochs_to_keep")
    val gcNumEpochsToKeep: Long? = null,
    @SerialName("gc_step_period")
    val gcStepPeriod: DurationAsStdSchemaProvider? = null,
)

@Serializable
data class GasKeyView(
    @SerialName("balance")
    val balance: NearToken,
    @SerialName("num_nonces")
    val numNonces: Long,
    @SerialName("permission")
    val permission: AccessKeyPermissionView,
)

@Serializable
data class GenesisConfig(
    @SerialName("avg_hidden_validator_seats_per_shard")
    val avgHiddenValidatorSeatsPerShard: List<Long>,
    @SerialName("block_producer_kickout_threshold")
    val blockProducerKickoutThreshold: Long,
    @SerialName("chain_id")
    val chainId: String,
    @SerialName("chunk_producer_assignment_changes_limit")
    val chunkProducerAssignmentChangesLimit: Long? = null,
    @SerialName("chunk_producer_kickout_threshold")
    val chunkProducerKickoutThreshold: Long,
    @SerialName("chunk_validator_only_kickout_threshold")
    val chunkValidatorOnlyKickoutThreshold: Long? = null,
    @SerialName("dynamic_resharding")
    val dynamicResharding: Boolean,
    @SerialName("epoch_length")
    val epochLength: Long,
    @SerialName("fishermen_threshold")
    val fishermenThreshold: NearToken,
    @SerialName("gas_limit")
    val gasLimit: NearGas,
    @SerialName("gas_price_adjustment_rate")
    val gasPriceAdjustmentRate: List<Int>,
    @SerialName("genesis_height")
    val genesisHeight: Long,
    @SerialName("genesis_time")
    val genesisTime: String,
    @SerialName("max_gas_price")
    val maxGasPrice: NearToken,
    @SerialName("max_inflation_rate")
    val maxInflationRate: List<Int>,
    @SerialName("max_kickout_stake_perc")
    val maxKickoutStakePerc: Long? = null,
    @SerialName("min_gas_price")
    val minGasPrice: NearToken,
    @SerialName("minimum_stake_divisor")
    val minimumStakeDivisor: Long? = null,
    @SerialName("minimum_stake_ratio")
    val minimumStakeRatio: List<Int>? = null,
    @SerialName("minimum_validators_per_shard")
    val minimumValidatorsPerShard: Long? = null,
    @SerialName("num_block_producer_seats")
    val numBlockProducerSeats: Long,
    @SerialName("num_block_producer_seats_per_shard")
    val numBlockProducerSeatsPerShard: List<Long>,
    @SerialName("num_blocks_per_year")
    val numBlocksPerYear: Long,
    @SerialName("num_chunk_only_producer_seats")
    val numChunkOnlyProducerSeats: Long? = null,
    @SerialName("num_chunk_producer_seats")
    val numChunkProducerSeats: Long? = null,
    @SerialName("num_chunk_validator_seats")
    val numChunkValidatorSeats: Long? = null,
    @SerialName("online_max_threshold")
    val onlineMaxThreshold: List<Int>? = null,
    @SerialName("online_min_threshold")
    val onlineMinThreshold: List<Int>? = null,
    @SerialName("protocol_reward_rate")
    val protocolRewardRate: List<Int>,
    @SerialName("protocol_treasury_account")
    val protocolTreasuryAccount: AccountId,
    @SerialName("protocol_upgrade_stake_threshold")
    val protocolUpgradeStakeThreshold: List<Int>? = null,
    @SerialName("protocol_version")
    val protocolVersion: Long,
    @SerialName("shard_layout")
    val shardLayout: ShardLayout? = null,
    @SerialName("shuffle_shard_assignment_for_chunk_producers")
    val shuffleShardAssignmentForChunkProducers: Boolean? = null,
    @SerialName("target_validator_mandates_per_shard")
    val targetValidatorMandatesPerShard: Long? = null,
    @SerialName("total_supply")
    val totalSupply: NearToken,
    @SerialName("transaction_validity_period")
    val transactionValidityPeriod: Long,
    @SerialName("use_production_config")
    val useProductionConfig: Boolean? = null,
    @SerialName("validators")
    val validators: List<AccountInfo>,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALChanges(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcStateChangesInBlockByTypeRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALChangesInBlock(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcStateChangesInBlockRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALCongestionLevel(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcCongestionLevelRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALGenesisConfig(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: GenesisConfigRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALLightClientBlockProof(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcLightClientBlockProofRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALLightClientProof(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcLightClientExecutionProofRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALMaintenanceWindows(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcMaintenanceWindowsRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALProtocolConfig(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcProtocolConfigRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALReceipt(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcReceiptRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALSplitStorageInfo(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcSplitStorageInfoRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALTxStatus(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcTransactionStatusRequest,
)

@Serializable
data class JsonRpcRequestForEXPERIMENTALValidatorsOrdered(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcValidatorsOrderedRequest,
)

@Serializable
data class JsonRpcRequestForBlock(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcBlockRequest,
)

@Serializable
data class JsonRpcRequestForBlockEffects(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcStateChangesInBlockRequest,
)

@Serializable
data class JsonRpcRequestForBroadcastTxAsync(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcSendTransactionRequest,
)

@Serializable
data class JsonRpcRequestForBroadcastTxCommit(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcSendTransactionRequest,
)

@Serializable
data class JsonRpcRequestForChanges(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcStateChangesInBlockByTypeRequest,
)

@Serializable
data class JsonRpcRequestForChunk(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcChunkRequest,
)

@Serializable
data class JsonRpcRequestForClientConfig(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcClientConfigRequest,
)

@Serializable
data class JsonRpcRequestForGasPrice(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcGasPriceRequest,
)

@Serializable
data class JsonRpcRequestForGenesisConfig(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: GenesisConfigRequest,
)

@Serializable
data class JsonRpcRequestForHealth(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcHealthRequest,
)

@Serializable
data class JsonRpcRequestForLightClientProof(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcLightClientExecutionProofRequest,
)

@Serializable
data class JsonRpcRequestForMaintenanceWindows(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcMaintenanceWindowsRequest,
)

@Serializable
data class JsonRpcRequestForNetworkInfo(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcNetworkInfoRequest,
)

@Serializable
data class JsonRpcRequestForNextLightClientBlock(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcLightClientNextBlockRequest,
)

@Serializable
data class JsonRpcRequestForQuery(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcQueryRequest,
)

@Serializable
data class JsonRpcRequestForSendTx(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcSendTransactionRequest,
)

@Serializable
data class JsonRpcRequestForStatus(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcStatusRequest,
)

@Serializable
data class JsonRpcRequestForTx(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcTransactionStatusRequest,
)

@Serializable
data class JsonRpcRequestForValidators(
    @SerialName("id")
    val id: String,
    @SerialName("jsonrpc")
    val jsonrpc: String,
    @SerialName("method")
    val method: String,
    @SerialName("params")
    val params: RpcValidatorRequest,
)

@Serializable
data class KnownProducerView(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("next_hops")
    val nextHops: List<PublicKey>? = null,
    @SerialName("peer_id")
    val peerId: PublicKey,
)

@Serializable
data class LightClientBlockLiteView(
    @SerialName("inner_lite")
    val innerLite: BlockHeaderInnerLiteView,
    @SerialName("inner_rest_hash")
    val innerRestHash: CryptoHash,
    @SerialName("prev_block_hash")
    val prevBlockHash: CryptoHash,
)

@Serializable
data class LimitConfig(
    @SerialName("account_id_validity_rules_version")
    val accountIdValidityRulesVersion: AccountIdValidityRulesVersion? = null,
    @SerialName("initial_memory_pages")
    val initialMemoryPages: Long,
    @SerialName("max_actions_per_receipt")
    val maxActionsPerReceipt: Long,
    @SerialName("max_arguments_length")
    val maxArgumentsLength: Long,
    @SerialName("max_contract_size")
    val maxContractSize: Long,
    @SerialName("max_elements_per_contract_table")
    val maxElementsPerContractTable: Long? = null,
    @SerialName("max_functions_number_per_contract")
    val maxFunctionsNumberPerContract: Long? = null,
    @SerialName("max_gas_burnt")
    val maxGasBurnt: NearGas,
    @SerialName("max_length_method_name")
    val maxLengthMethodName: Long,
    @SerialName("max_length_returned_data")
    val maxLengthReturnedData: Long,
    @SerialName("max_length_storage_key")
    val maxLengthStorageKey: Long,
    @SerialName("max_length_storage_value")
    val maxLengthStorageValue: Long,
    @SerialName("max_locals_per_contract")
    val maxLocalsPerContract: Long? = null,
    @SerialName("max_memory_pages")
    val maxMemoryPages: Long,
    @SerialName("max_number_bytes_method_names")
    val maxNumberBytesMethodNames: Long,
    @SerialName("max_number_input_data_dependencies")
    val maxNumberInputDataDependencies: Long,
    @SerialName("max_number_logs")
    val maxNumberLogs: Long,
    @SerialName("max_number_registers")
    val maxNumberRegisters: Long,
    @SerialName("max_promises_per_function_call_action")
    val maxPromisesPerFunctionCallAction: Long,
    @SerialName("max_receipt_size")
    val maxReceiptSize: Long,
    @SerialName("max_register_size")
    val maxRegisterSize: Long,
    @SerialName("max_stack_height")
    val maxStackHeight: Long,
    @SerialName("max_tables_per_contract")
    val maxTablesPerContract: Long? = null,
    @SerialName("max_total_log_length")
    val maxTotalLogLength: Long,
    @SerialName("max_total_prepaid_gas")
    val maxTotalPrepaidGas: NearGas,
    @SerialName("max_transaction_size")
    val maxTransactionSize: Long,
    @SerialName("max_yield_payload_size")
    val maxYieldPayloadSize: Long,
    @SerialName("per_receipt_storage_proof_size_limit")
    val perReceiptStorageProofSizeLimit: Long,
    @SerialName("registers_memory_limit")
    val registersMemoryLimit: Long,
    @SerialName("yield_timeout_length_in_blocks")
    val yieldTimeoutLengthInBlocks: Long,
)

@Serializable
data class MerklePathItem(
    @SerialName("direction")
    val direction: Direction,
    @SerialName("hash")
    val hash: CryptoHash,
)

@Serializable
data class MissingTrieValue(
    @SerialName("context")
    val context: MissingTrieValueContext,
    @SerialName("hash")
    val hash: CryptoHash,
)

@Serializable
data class NetworkInfoView(
    @SerialName("connected_peers")
    val connectedPeers: List<PeerInfoView>,
    @SerialName("known_producers")
    val knownProducers: List<KnownProducerView>,
    @SerialName("num_connected_peers")
    val numConnectedPeers: Long,
    @SerialName("peer_max_count")
    val peerMaxCount: Long,
    @SerialName("tier1_accounts_data")
    val tier1AccountsData: List<AccountDataView>,
    @SerialName("tier1_accounts_keys")
    val tier1AccountsKeys: List<PublicKey>,
    @SerialName("tier1_connections")
    val tier1Connections: List<PeerInfoView>,
)

@Serializable
data class NextEpochValidatorInfo(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("shards")
    val shards: List<ShardId>,
    @SerialName("stake")
    val stake: NearToken,
)

@Serializable
data class PeerInfoView(
    @SerialName("account_id")
    val accountId: AccountId? = null,
    @SerialName("addr")
    val addr: String,
    @SerialName("archival")
    val archival: Boolean,
    @SerialName("block_hash")
    val blockHash: CryptoHash? = null,
    @SerialName("connection_established_time_millis")
    val connectionEstablishedTimeMillis: Long,
    @SerialName("height")
    val height: Long? = null,
    @SerialName("is_highest_block_invalid")
    val isHighestBlockInvalid: Boolean,
    @SerialName("is_outbound_peer")
    val isOutboundPeer: Boolean,
    @SerialName("last_time_peer_requested_millis")
    val lastTimePeerRequestedMillis: Long,
    @SerialName("last_time_received_message_millis")
    val lastTimeReceivedMessageMillis: Long,
    @SerialName("nonce")
    val nonce: Long,
    @SerialName("peer_id")
    val peerId: PublicKey,
    @SerialName("received_bytes_per_sec")
    val receivedBytesPerSec: Long,
    @SerialName("sent_bytes_per_sec")
    val sentBytesPerSec: Long,
    @SerialName("tracked_shards")
    val trackedShards: List<ShardId>,
)

@Serializable
data class RangeOfUint64(
    @SerialName("end")
    val end: Long,
    @SerialName("start")
    val start: Long,
)

@Serializable
data class ReceiptView(
    @SerialName("predecessor_id")
    val predecessorId: AccountId,
    @SerialName("priority")
    val priority: Long? = null,
    @SerialName("receipt")
    val receipt: ReceiptEnumView,
    @SerialName("receipt_id")
    val receiptId: CryptoHash,
    @SerialName("receiver_id")
    val receiverId: AccountId,
)

@Serializable
data class RpcBlockResponse(
    @SerialName("author")
    val author: AccountId,
    @SerialName("chunks")
    val chunks: List<ChunkHeaderView>,
    @SerialName("header")
    val header: BlockHeaderView,
)

@Serializable
data class RpcChunkResponse(
    @SerialName("author")
    val author: AccountId,
    @SerialName("header")
    val header: ChunkHeaderView,
    @SerialName("receipts")
    val receipts: List<ReceiptView>,
    @SerialName("transactions")
    val transactions: List<SignedTransactionView>,
)

@Serializable
data class RpcClientConfigResponse(
    @SerialName("archive")
    val archive: Boolean,
    @SerialName("block_fetch_horizon")
    val blockFetchHorizon: Long,
    @SerialName("block_header_fetch_horizon")
    val blockHeaderFetchHorizon: Long,
    @SerialName("block_production_tracking_delay")
    val blockProductionTrackingDelay: List<Long>,
    @SerialName("catchup_step_period")
    val catchupStepPeriod: List<Long>,
    @SerialName("chain_id")
    val chainId: String,
    @SerialName("chunk_distribution_network")
    val chunkDistributionNetwork: ChunkDistributionNetworkConfig? = null,
    @SerialName("chunk_request_retry_period")
    val chunkRequestRetryPeriod: List<Long>,
    @SerialName("chunk_validation_threads")
    val chunkValidationThreads: Long,
    @SerialName("chunk_wait_mult")
    val chunkWaitMult: List<Int>,
    @SerialName("client_background_migration_threads")
    val clientBackgroundMigrationThreads: Long,
    @SerialName("cloud_archival_reader")
    val cloudArchivalReader: CloudArchivalReaderConfig? = null,
    @SerialName("cloud_archival_writer")
    val cloudArchivalWriter: CloudArchivalWriterConfig? = null,
    @SerialName("doomslug_step_period")
    val doomslugStepPeriod: List<Long>,
    @SerialName("enable_multiline_logging")
    val enableMultilineLogging: Boolean,
    @SerialName("enable_statistics_export")
    val enableStatisticsExport: Boolean,
    @SerialName("epoch_length")
    val epochLength: Long,
    @SerialName("epoch_sync")
    val epochSync: EpochSyncConfig,
    @SerialName("expected_shutdown")
    val expectedShutdown: MutableConfigValue,
    @SerialName("gc")
    val gc: GCConfig,
    @SerialName("header_sync_expected_height_per_second")
    val headerSyncExpectedHeightPerSecond: Long,
    @SerialName("header_sync_initial_timeout")
    val headerSyncInitialTimeout: List<Long>,
    @SerialName("header_sync_progress_timeout")
    val headerSyncProgressTimeout: List<Long>,
    @SerialName("header_sync_stall_ban_timeout")
    val headerSyncStallBanTimeout: List<Long>,
    @SerialName("log_summary_period")
    val logSummaryPeriod: List<Long>,
    @SerialName("log_summary_style")
    val logSummaryStyle: LogSummaryStyle,
    @SerialName("max_block_production_delay")
    val maxBlockProductionDelay: List<Long>,
    @SerialName("max_block_wait_delay")
    val maxBlockWaitDelay: List<Long>,
    @SerialName("max_gas_burnt_view")
    val maxGasBurntView: NearGas? = null,
    @SerialName("min_block_production_delay")
    val minBlockProductionDelay: List<Long>,
    @SerialName("min_num_peers")
    val minNumPeers: Long,
    @SerialName("num_block_producer_seats")
    val numBlockProducerSeats: Long,
    @SerialName("orphan_state_witness_max_size")
    val orphanStateWitnessMaxSize: Long,
    @SerialName("orphan_state_witness_pool_size")
    val orphanStateWitnessPoolSize: Long,
    @SerialName("produce_chunk_add_transactions_time_limit")
    val produceChunkAddTransactionsTimeLimit: String,
    @SerialName("produce_empty_blocks")
    val produceEmptyBlocks: Boolean,
    @SerialName("protocol_version_check")
    val protocolVersionCheck: ProtocolVersionCheckConfig,
    @SerialName("resharding_config")
    val reshardingConfig: MutableConfigValue,
    @SerialName("rpc_addr")
    val rpcAddr: String? = null,
    @SerialName("save_invalid_witnesses")
    val saveInvalidWitnesses: Boolean,
    @SerialName("save_latest_witnesses")
    val saveLatestWitnesses: Boolean,
    @SerialName("save_trie_changes")
    val saveTrieChanges: Boolean,
    @SerialName("save_tx_outcomes")
    val saveTxOutcomes: Boolean,
    @SerialName("save_untracked_partial_chunks_parts")
    val saveUntrackedPartialChunksParts: Boolean,
    @SerialName("skip_sync_wait")
    val skipSyncWait: Boolean,
    @SerialName("state_request_server_threads")
    val stateRequestServerThreads: Long,
    @SerialName("state_request_throttle_period")
    val stateRequestThrottlePeriod: List<Long>,
    @SerialName("state_requests_per_throttle_period")
    val stateRequestsPerThrottlePeriod: Long,
    @SerialName("state_sync")
    val stateSync: StateSyncConfig,
    @SerialName("state_sync_enabled")
    val stateSyncEnabled: Boolean,
    @SerialName("state_sync_external_backoff")
    val stateSyncExternalBackoff: List<Long>,
    @SerialName("state_sync_external_timeout")
    val stateSyncExternalTimeout: List<Long>,
    @SerialName("state_sync_p2p_timeout")
    val stateSyncP2PTimeout: List<Long>,
    @SerialName("state_sync_retry_backoff")
    val stateSyncRetryBackoff: List<Long>,
    @SerialName("sync_check_period")
    val syncCheckPeriod: List<Long>,
    @SerialName("sync_height_threshold")
    val syncHeightThreshold: Long,
    @SerialName("sync_max_block_requests")
    val syncMaxBlockRequests: Long,
    @SerialName("sync_step_period")
    val syncStepPeriod: List<Long>,
    @SerialName("tracked_shards_config")
    val trackedShardsConfig: TrackedShardsConfig,
    @SerialName("transaction_pool_size_limit")
    val transactionPoolSizeLimit: Long? = null,
    @SerialName("transaction_request_handler_threads")
    val transactionRequestHandlerThreads: Long,
    @SerialName("trie_viewer_state_size_limit")
    val trieViewerStateSizeLimit: Long? = null,
    @SerialName("ttl_account_id_router")
    val ttlAccountIdRouter: List<Long>,
    @SerialName("tx_routing_height_horizon")
    val txRoutingHeightHorizon: Long,
    @SerialName("version")
    val version: Version,
    @SerialName("view_client_threads")
    val viewClientThreads: Long,
)

@Serializable
data class RpcCongestionLevelResponse(
    @SerialName("congestion_level")
    val congestionLevel: Double,
)

@Serializable
data class RpcGasPriceRequest(
    @SerialName("block_id")
    val blockId: BlockId? = null,
)

@Serializable
data class RpcGasPriceResponse(
    @SerialName("gas_price")
    val gasPrice: NearToken,
)

@Serializable
data class RpcKnownProducer(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("addr")
    val addr: String? = null,
    @SerialName("peer_id")
    val peerId: PeerId,
)

@Serializable
data class RpcLightClientBlockProofRequest(
    @SerialName("block_hash")
    val blockHash: CryptoHash,
    @SerialName("light_client_head")
    val lightClientHead: CryptoHash,
)

@Serializable
data class RpcLightClientBlockProofResponse(
    @SerialName("block_header_lite")
    val blockHeaderLite: LightClientBlockLiteView,
    @SerialName("block_proof")
    val blockProof: List<MerklePathItem>,
)

@Serializable
data class RpcLightClientExecutionProofResponse(
    @SerialName("block_header_lite")
    val blockHeaderLite: LightClientBlockLiteView,
    @SerialName("block_proof")
    val blockProof: List<MerklePathItem>,
    @SerialName("outcome_proof")
    val outcomeProof: ExecutionOutcomeWithIdView,
    @SerialName("outcome_root_proof")
    val outcomeRootProof: List<MerklePathItem>,
)

@Serializable
data class RpcLightClientNextBlockRequest(
    @SerialName("last_block_hash")
    val lastBlockHash: CryptoHash,
)

@Serializable
data class RpcLightClientNextBlockResponse(
    @SerialName("approvals_after_next")
    val approvalsAfterNext: List<Signature?>? = null,
    @SerialName("inner_lite")
    val innerLite: BlockHeaderInnerLiteView? = null,
    @SerialName("inner_rest_hash")
    val innerRestHash: CryptoHash? = null,
    @SerialName("next_block_inner_hash")
    val nextBlockInnerHash: CryptoHash? = null,
    @SerialName("next_bps")
    val nextBps: List<ValidatorStakeView>? = null,
    @SerialName("prev_block_hash")
    val prevBlockHash: CryptoHash? = null,
)

@Serializable
data class RpcMaintenanceWindowsRequest(
    @SerialName("account_id")
    val accountId: AccountId,
)

@Serializable
data class RpcNetworkInfoResponse(
    @SerialName("active_peers")
    val activePeers: List<RpcPeerInfo>,
    @SerialName("known_producers")
    val knownProducers: List<RpcKnownProducer>,
    @SerialName("num_active_peers")
    val numActivePeers: Long,
    @SerialName("peer_max_count")
    val peerMaxCount: Long,
    @SerialName("received_bytes_per_sec")
    val receivedBytesPerSec: Long,
    @SerialName("sent_bytes_per_sec")
    val sentBytesPerSec: Long,
)

@Serializable
data class RpcPeerInfo(
    @SerialName("account_id")
    val accountId: AccountId? = null,
    @SerialName("addr")
    val addr: String? = null,
    @SerialName("id")
    val id: PeerId,
)

@Serializable
data class RpcProtocolConfigResponse(
    @SerialName("avg_hidden_validator_seats_per_shard")
    val avgHiddenValidatorSeatsPerShard: List<Long>,
    @SerialName("block_producer_kickout_threshold")
    val blockProducerKickoutThreshold: Long,
    @SerialName("chain_id")
    val chainId: String,
    @SerialName("chunk_producer_kickout_threshold")
    val chunkProducerKickoutThreshold: Long,
    @SerialName("chunk_validator_only_kickout_threshold")
    val chunkValidatorOnlyKickoutThreshold: Long,
    @SerialName("dynamic_resharding")
    val dynamicResharding: Boolean,
    @SerialName("epoch_length")
    val epochLength: Long,
    @SerialName("fishermen_threshold")
    val fishermenThreshold: NearToken,
    @SerialName("gas_limit")
    val gasLimit: NearGas,
    @SerialName("gas_price_adjustment_rate")
    val gasPriceAdjustmentRate: List<Int>,
    @SerialName("genesis_height")
    val genesisHeight: Long,
    @SerialName("genesis_time")
    val genesisTime: String,
    @SerialName("max_gas_price")
    val maxGasPrice: NearToken,
    @SerialName("max_inflation_rate")
    val maxInflationRate: List<Int>,
    @SerialName("max_kickout_stake_perc")
    val maxKickoutStakePerc: Long,
    @SerialName("min_gas_price")
    val minGasPrice: NearToken,
    @SerialName("minimum_stake_divisor")
    val minimumStakeDivisor: Long,
    @SerialName("minimum_stake_ratio")
    val minimumStakeRatio: List<Int>,
    @SerialName("minimum_validators_per_shard")
    val minimumValidatorsPerShard: Long,
    @SerialName("num_block_producer_seats")
    val numBlockProducerSeats: Long,
    @SerialName("num_block_producer_seats_per_shard")
    val numBlockProducerSeatsPerShard: List<Long>,
    @SerialName("num_blocks_per_year")
    val numBlocksPerYear: Long,
    @SerialName("online_max_threshold")
    val onlineMaxThreshold: List<Int>,
    @SerialName("online_min_threshold")
    val onlineMinThreshold: List<Int>,
    @SerialName("protocol_reward_rate")
    val protocolRewardRate: List<Int>,
    @SerialName("protocol_treasury_account")
    val protocolTreasuryAccount: AccountId,
    @SerialName("protocol_upgrade_stake_threshold")
    val protocolUpgradeStakeThreshold: List<Int>,
    @SerialName("protocol_version")
    val protocolVersion: Long,
    @SerialName("runtime_config")
    val runtimeConfig: RuntimeConfigView,
    @SerialName("shard_layout")
    val shardLayout: ShardLayout,
    @SerialName("shuffle_shard_assignment_for_chunk_producers")
    val shuffleShardAssignmentForChunkProducers: Boolean,
    @SerialName("target_validator_mandates_per_shard")
    val targetValidatorMandatesPerShard: Long,
    @SerialName("transaction_validity_period")
    val transactionValidityPeriod: Long,
)

@Serializable
data class RpcReceiptRequest(
    @SerialName("receipt_id")
    val receiptId: CryptoHash,
)

@Serializable
data class RpcReceiptResponse(
    @SerialName("predecessor_id")
    val predecessorId: AccountId,
    @SerialName("priority")
    val priority: Long? = null,
    @SerialName("receipt")
    val receipt: ReceiptEnumView,
    @SerialName("receipt_id")
    val receiptId: CryptoHash,
    @SerialName("receiver_id")
    val receiverId: AccountId,
)

@Serializable
data class RpcSendTransactionRequest(
    @SerialName("signed_tx_base64")
    val signedTxBase64: SignedTransaction,
    @SerialName("wait_until")
    val waitUntil: TxExecutionStatus? = null,
)

@Serializable
object RpcSplitStorageInfoRequest

@Serializable
data class RpcSplitStorageInfoResponse(
    @SerialName("cold_head_height")
    val coldHeadHeight: Long? = null,
    @SerialName("final_head_height")
    val finalHeadHeight: Long? = null,
    @SerialName("head_height")
    val headHeight: Long? = null,
    @SerialName("hot_db_kind")
    val hotDbKind: String? = null,
)

@Serializable
data class RpcStateChangesInBlockByTypeResponse(
    @SerialName("block_hash")
    val blockHash: CryptoHash,
    @SerialName("changes")
    val changes: List<StateChangeKindView>,
)

@Serializable
data class RpcStateChangesInBlockResponse(
    @SerialName("block_hash")
    val blockHash: CryptoHash,
    @SerialName("changes")
    val changes: List<StateChangeWithCauseView>,
)

@Serializable
data class RpcStatusResponse(
    @SerialName("chain_id")
    val chainId: String,
    @SerialName("detailed_debug_status")
    val detailedDebugStatus: DetailedDebugStatus? = null,
    @SerialName("genesis_hash")
    val genesisHash: CryptoHash,
    @SerialName("latest_protocol_version")
    val latestProtocolVersion: Long,
    @SerialName("node_key")
    val nodeKey: PublicKey? = null,
    @SerialName("node_public_key")
    val nodePublicKey: PublicKey,
    @SerialName("protocol_version")
    val protocolVersion: Long,
    @SerialName("rpc_addr")
    val rpcAddr: String? = null,
    @SerialName("sync_info")
    val syncInfo: StatusSyncInfo,
    @SerialName("uptime_sec")
    val uptimeSec: Long,
    @SerialName("validator_account_id")
    val validatorAccountId: AccountId? = null,
    @SerialName("validator_public_key")
    val validatorPublicKey: PublicKey? = null,
    @SerialName("validators")
    val validators: List<ValidatorInfo>,
    @SerialName("version")
    val version: Version,
)

@Serializable
data class RpcValidatorResponse(
    @SerialName("current_fishermen")
    val currentFishermen: List<ValidatorStakeView>,
    @SerialName("current_proposals")
    val currentProposals: List<ValidatorStakeView>,
    @SerialName("current_validators")
    val currentValidators: List<CurrentEpochValidatorInfo>,
    @SerialName("epoch_height")
    val epochHeight: Long,
    @SerialName("epoch_start_height")
    val epochStartHeight: Long,
    @SerialName("next_fishermen")
    val nextFishermen: List<ValidatorStakeView>,
    @SerialName("next_validators")
    val nextValidators: List<NextEpochValidatorInfo>,
    @SerialName("prev_epoch_kickout")
    val prevEpochKickout: List<ValidatorKickoutView>,
)

@Serializable
data class RpcValidatorsOrderedRequest(
    @SerialName("block_id")
    val blockId: BlockId? = null,
)

@Serializable
data class RuntimeConfigView(
    @SerialName("account_creation_config")
    val accountCreationConfig: AccountCreationConfigView,
    @SerialName("congestion_control_config")
    val congestionControlConfig: CongestionControlConfigView,
    @SerialName("storage_amount_per_byte")
    val storageAmountPerByte: NearToken,
    @SerialName("transaction_costs")
    val transactionCosts: RuntimeFeesConfigView,
    @SerialName("wasm_config")
    val wasmConfig: VMConfigView,
    @SerialName("witness_config")
    val witnessConfig: WitnessConfigView,
)

@Serializable
data class RuntimeFeesConfigView(
    @SerialName("action_creation_config")
    val actionCreationConfig: ActionCreationConfigView,
    @SerialName("action_receipt_creation_config")
    val actionReceiptCreationConfig: Fee,
    @SerialName("burnt_gas_reward")
    val burntGasReward: List<Int>,
    @SerialName("data_receipt_creation_config")
    val dataReceiptCreationConfig: DataReceiptCreationConfigView,
    @SerialName("pessimistic_gas_price_inflation_ratio")
    val pessimisticGasPriceInflationRatio: List<Int>,
    @SerialName("storage_usage_config")
    val storageUsageConfig: StorageUsageConfigView,
)

@Serializable
data class ShardLayoutV0(
    @SerialName("num_shards")
    val numShards: Long,
    @SerialName("version")
    val version: Long,
)

@Serializable
data class ShardLayoutV1(
    @SerialName("boundary_accounts")
    val boundaryAccounts: List<AccountId>,
    @SerialName("shards_split_map")
    val shardsSplitMap: List<List<ShardId>>? = null,
    @SerialName("to_parent_shard_map")
    val toParentShardMap: List<ShardId>? = null,
    @SerialName("version")
    val version: Long,
)

@Serializable
data class ShardLayoutV2(
    @SerialName("boundary_accounts")
    val boundaryAccounts: List<AccountId>,
    @SerialName("id_to_index_map")
    val idToIndexMap: Map<String, Long>,
    @SerialName("index_to_id_map")
    val indexToIdMap: Map<String, ShardId>,
    @SerialName("shard_ids")
    val shardIds: List<ShardId>,
    @SerialName("shards_parent_map")
    val shardsParentMap: Map<String, ShardId>? = null,
    @SerialName("shards_split_map")
    val shardsSplitMap: Map<String, List<ShardId>>? = null,
    @SerialName("version")
    val version: Long,
)

@Serializable
data class ShardUId(
    @SerialName("shard_id")
    val shardId: Long,
    @SerialName("version")
    val version: Long,
)

@Serializable
data class SignedDelegateAction(
    @SerialName("delegate_action")
    val delegateAction: DelegateAction,
    @SerialName("signature")
    val signature: Signature,
)

@Serializable
data class SignedTransactionView(
    @SerialName("actions")
    val actions: List<ActionView>,
    @SerialName("hash")
    val hash: CryptoHash,
    @SerialName("nonce")
    val nonce: Long,
    @SerialName("priority_fee")
    val priorityFee: Long? = null,
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("receiver_id")
    val receiverId: AccountId,
    @SerialName("signature")
    val signature: Signature,
    @SerialName("signer_id")
    val signerId: AccountId,
)

@Serializable
data class SlashedValidator(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("is_double_sign")
    val isDoubleSign: Boolean,
)

@Serializable
data class StakeAction(
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("stake")
    val stake: NearToken,
)

@Serializable
data class StateItem(
    @SerialName("key")
    val key: StoreKey,
    @SerialName("value")
    val value: StoreValue,
)

@Serializable
data class StateSyncConfig(
    @SerialName("concurrency")
    val concurrency: SyncConcurrency? = null,
    @SerialName("dump")
    val dump: DumpConfig? = null,
    @SerialName("parts_compression_lvl")
    val partsCompressionLvl: Int? = null,
    @SerialName("sync")
    val sync: SyncConfig? = null,
)

@Serializable
data class StatusSyncInfo(
    @SerialName("earliest_block_hash")
    val earliestBlockHash: CryptoHash? = null,
    @SerialName("earliest_block_height")
    val earliestBlockHeight: Long? = null,
    @SerialName("earliest_block_time")
    val earliestBlockTime: String? = null,
    @SerialName("epoch_id")
    val epochId: EpochId? = null,
    @SerialName("epoch_start_height")
    val epochStartHeight: Long? = null,
    @SerialName("latest_block_hash")
    val latestBlockHash: CryptoHash,
    @SerialName("latest_block_height")
    val latestBlockHeight: Long,
    @SerialName("latest_block_time")
    val latestBlockTime: String,
    @SerialName("latest_state_root")
    val latestStateRoot: CryptoHash,
    @SerialName("syncing")
    val syncing: Boolean,
)

@Serializable
data class StorageUsageConfigView(
    @SerialName("num_bytes_account")
    val numBytesAccount: Long,
    @SerialName("num_extra_bytes_record")
    val numExtraBytesRecord: Long,
)

@Serializable
data class SyncConcurrency(
    @SerialName("apply")
    val apply: Long,
    @SerialName("apply_during_catchup")
    val applyDuringCatchup: Long,
    @SerialName("peer_downloads")
    val peerDownloads: Long,
    @SerialName("per_shard")
    val perShard: Long,
)

@Serializable
data class Tier1ProxyView(
    @SerialName("addr")
    val addr: String,
    @SerialName("peer_id")
    val peerId: PublicKey,
)

@Serializable
data class TransferAction(
    @SerialName("deposit")
    val deposit: NearToken,
)

@Serializable
data class UseGlobalContractAction(
    @SerialName("contract_identifier")
    val contractIdentifier: GlobalContractIdentifier,
)

@Serializable
data class VMConfigView(
    @SerialName("deterministic_account_ids")
    val deterministicAccountIds: Boolean,
    @SerialName("discard_custom_sections")
    val discardCustomSections: Boolean,
    @SerialName("eth_implicit_accounts")
    val ethImplicitAccounts: Boolean,
    @SerialName("ext_costs")
    val extCosts: ExtCostsConfigView,
    @SerialName("fix_contract_loading_cost")
    val fixContractLoadingCost: Boolean,
    @SerialName("global_contract_host_fns")
    val globalContractHostFns: Boolean,
    @SerialName("grow_mem_cost")
    val growMemCost: Long,
    @SerialName("implicit_account_creation")
    val implicitAccountCreation: Boolean,
    @SerialName("limit_config")
    val limitConfig: LimitConfig,
    @SerialName("reftypes_bulk_memory")
    val reftypesBulkMemory: Boolean,
    @SerialName("regular_op_cost")
    val regularOpCost: Long,
    @SerialName("saturating_float_to_int")
    val saturatingFloatToInt: Boolean,
    @SerialName("storage_get_mode")
    val storageGetMode: StorageGetMode,
    @SerialName("vm_kind")
    val vmKind: VMKind,
)

@Serializable
data class ValidatorInfo(
    @SerialName("account_id")
    val accountId: AccountId,
)

@Serializable
data class ValidatorKickoutView(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("reason")
    val reason: ValidatorKickoutReason,
)

@Serializable
data class ValidatorStakeViewV1(
    @SerialName("account_id")
    val accountId: AccountId,
    @SerialName("public_key")
    val publicKey: PublicKey,
    @SerialName("stake")
    val stake: NearToken,
)

@Serializable
data class Version(
    @SerialName("build")
    val build: String,
    @SerialName("commit")
    val commit: String,
    @SerialName("rustc_version")
    val rustcVersion: String? = null,
    @SerialName("version")
    val version: String,
)

@Serializable
data class ViewStateResult(
    @SerialName("proof")
    val proof: List<String>? = null,
    @SerialName("values")
    val values: List<StateItem>,
)

@Serializable
data class WitnessConfigView(
    @SerialName("combined_transactions_size_limit")
    val combinedTransactionsSizeLimit: Long,
    @SerialName("main_storage_proof_size_soft_limit")
    val mainStorageProofSizeSoftLimit: Long,
    @SerialName("new_transactions_validation_state_size_soft_limit")
    val newTransactionsValidationStateSizeSoftLimit: Long,
)

/**
 * SerializersModule for NEAR's externally-tagged unions.
 */
val nearSerializersModule =
    SerializersModule {
        polymorphic(AccessKeyPermission::class) { defaultDeserializer { AccessKeyPermissionSerializer } }
        polymorphic(AccessKeyPermissionView::class) { defaultDeserializer { AccessKeyPermissionViewSerializer } }
        polymorphic(ActionErrorKind::class) { defaultDeserializer { ActionErrorKindSerializer } }
        polymorphic(ActionView::class) { defaultDeserializer { ActionViewSerializer } }
        polymorphic(ActionsValidationError::class) { defaultDeserializer { ActionsValidationErrorSerializer } }
        polymorphic(BlockId::class) { defaultDeserializer { BlockIdSerializer } }
        polymorphic(CompilationError::class) { defaultDeserializer { CompilationErrorSerializer } }
        polymorphic(ExecutionStatusView::class) { defaultDeserializer { ExecutionStatusViewSerializer } }
        polymorphic(ExternalStorageLocation::class) { defaultDeserializer { ExternalStorageLocationSerializer } }
        polymorphic(FinalExecutionStatus::class) { defaultDeserializer { FinalExecutionStatusSerializer } }
        polymorphic(FunctionCallError::class) { defaultDeserializer { FunctionCallErrorSerializer } }
        polymorphic(GlobalContractDeployMode::class) { defaultDeserializer { GlobalContractDeployModeSerializer } }
        polymorphic(GlobalContractIdentifier::class) { defaultDeserializer { GlobalContractIdentifierSerializer } }
        polymorphic(
            GlobalContractIdentifierView::class,
        ) { defaultDeserializer { GlobalContractIdentifierViewSerializer } }
        polymorphic(HostError::class) { defaultDeserializer { HostErrorSerializer } }
        polymorphic(InvalidAccessKeyError::class) { defaultDeserializer { InvalidAccessKeyErrorSerializer } }
        polymorphic(InvalidTxError::class) { defaultDeserializer { InvalidTxErrorSerializer } }
        polymorphic(JsonRpcResponseForArrayOfRangeOfUint64AndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForArrayOfRangeOfUint64AndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForArrayOfValidatorStakeViewAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForArrayOfValidatorStakeViewAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForCryptoHashAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForCryptoHashAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForGenesisConfigAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForGenesisConfigAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForNullableRpcHealthResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForNullableRpcHealthResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcBlockResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcBlockResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcChunkResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcChunkResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcClientConfigResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcClientConfigResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcCongestionLevelResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcCongestionLevelResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcGasPriceResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcGasPriceResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcLightClientBlockProofResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcLightClientExecutionProofResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcLightClientNextBlockResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcNetworkInfoResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcNetworkInfoResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcProtocolConfigResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcProtocolConfigResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcQueryResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcQueryResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcReceiptResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcReceiptResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcSplitStorageInfoResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcStateChangesInBlockByTypeResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcStateChangesInBlockResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcStatusResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcStatusResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcTransactionResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcTransactionResponseAndRpcErrorSerializer }
        }
        polymorphic(JsonRpcResponseForRpcValidatorResponseAndRpcError::class) {
            defaultDeserializer { JsonRpcResponseForRpcValidatorResponseAndRpcErrorSerializer }
        }
        polymorphic(MissingTrieValueContext::class) { defaultDeserializer { MissingTrieValueContextSerializer } }
        polymorphic(NonDelegateAction::class) { defaultDeserializer { NonDelegateActionSerializer } }
        polymorphic(PrepareError::class) { defaultDeserializer { PrepareErrorSerializer } }
        polymorphic(ReceiptEnumView::class) { defaultDeserializer { ReceiptEnumViewSerializer } }
        polymorphic(ReceiptValidationError::class) { defaultDeserializer { ReceiptValidationErrorSerializer } }
        polymorphic(RpcBlockRequest::class) { defaultDeserializer { RpcBlockRequestSerializer } }
        polymorphic(RpcChunkRequest::class) { defaultDeserializer { RpcChunkRequestSerializer } }
        polymorphic(RpcCongestionLevelRequest::class) { defaultDeserializer { RpcCongestionLevelRequestSerializer } }
        polymorphic(RpcProtocolConfigRequest::class) { defaultDeserializer { RpcProtocolConfigRequestSerializer } }
        polymorphic(RpcQueryResponse::class) { defaultDeserializer { RpcQueryResponseSerializer } }
        polymorphic(
            RpcStateChangesInBlockRequest::class,
        ) { defaultDeserializer { RpcStateChangesInBlockRequestSerializer } }
        polymorphic(RpcTransactionResponse::class) { defaultDeserializer { RpcTransactionResponseSerializer } }
        polymorphic(
            RpcTransactionStatusRequest::class,
        ) { defaultDeserializer { RpcTransactionStatusRequestSerializer } }
        polymorphic(RpcValidatorRequest::class) { defaultDeserializer { RpcValidatorRequestSerializer } }
        polymorphic(ShardLayout::class) { defaultDeserializer { ShardLayoutSerializer } }
        polymorphic(StorageError::class) { defaultDeserializer { StorageErrorSerializer } }
        polymorphic(SyncConfig::class) { defaultDeserializer { SyncConfigSerializer } }
        polymorphic(TrackedShardsConfig::class) { defaultDeserializer { TrackedShardsConfigSerializer } }
        polymorphic(TxExecutionError::class) { defaultDeserializer { TxExecutionErrorSerializer } }
        polymorphic(TxExecutionStatus::class) { defaultDeserializer { TxExecutionStatusSerializer } }
        polymorphic(VMKind::class) { defaultDeserializer { VMKindSerializer } }
        polymorphic(ValidatorKickoutReason::class) { defaultDeserializer { ValidatorKickoutReasonSerializer } }
        polymorphic(WasmTrap::class) { defaultDeserializer { WasmTrapSerializer } }
    }
