package org.near.jsonrpc.client

import kotlinx.serialization.builtins.*
import org.near.jsonrpc.types.*

/**
 * Extension functions for type-safe access to NEAR JSON-RPC methods.
 */

/**
 * [Deprecated] Returns changes for a given account, contract or contract code for given block heigh...
 */
suspend fun NearRpcClient.experimentalChanges(
    params: RpcStateChangesInBlockByTypeRequest,
): RpcStateChangesInBlockResponse =
    call(
        method = "EXPERIMENTAL_changes",
        params = params,
        paramsSerializer = RpcStateChangesInBlockByTypeRequest.serializer(),
        resultSerializer = RpcStateChangesInBlockResponse.serializer(),
    )

/**
 * [Deprecated] Returns changes in block for given block height or hash over all transactions for al...
 */
suspend fun NearRpcClient.experimentalChangesInBlock(
    params: RpcStateChangesInBlockRequest,
): RpcStateChangesInBlockByTypeResponse =
    call(
        method = "EXPERIMENTAL_changes_in_block",
        params = params,
        paramsSerializer = RpcStateChangesInBlockRequest.serializer(),
        resultSerializer = RpcStateChangesInBlockByTypeResponse.serializer(),
    )

/**
 * Queries the congestion level of a shard. More info about congestion [here](https://near.github.io...
 */
suspend fun NearRpcClient.experimentalCongestionLevel(params: RpcCongestionLevelRequest): RpcCongestionLevelResponse =
    call(
        method = "EXPERIMENTAL_congestion_level",
        params = params,
        paramsSerializer = RpcCongestionLevelRequest.serializer(),
        resultSerializer = RpcCongestionLevelResponse.serializer(),
    )

/**
 * [Deprecated] Get initial state and parameters for the genesis block. Consider genesis_config inst...
 */
suspend fun NearRpcClient.experimentalGenesisConfig(params: GenesisConfigRequest): GenesisConfig =
    call(
        method = "EXPERIMENTAL_genesis_config",
        params = params,
        paramsSerializer = GenesisConfigRequest.serializer(),
        resultSerializer = GenesisConfig.serializer(),
    )

/**
 * Returns the proofs for a transaction execution.
 */
suspend fun NearRpcClient.experimentalLightClientBlockProof(
    params: RpcLightClientBlockProofRequest,
): RpcLightClientBlockProofResponse =
    call(
        method = "EXPERIMENTAL_light_client_block_proof",
        params = params,
        paramsSerializer = RpcLightClientBlockProofRequest.serializer(),
        resultSerializer = RpcLightClientBlockProofResponse.serializer(),
    )

/**
 * Returns the proofs for a transaction execution.
 */
suspend fun NearRpcClient.experimentalLightClientProof(
    params: RpcLightClientExecutionProofRequest,
): RpcLightClientExecutionProofResponse =
    call(
        method = "EXPERIMENTAL_light_client_proof",
        params = params,
        paramsSerializer = RpcLightClientExecutionProofRequest.serializer(),
        resultSerializer = RpcLightClientExecutionProofResponse.serializer(),
    )

/**
 * [Deprecated] Returns the future windows for maintenance in current epoch for the specified accoun...
 */
suspend fun NearRpcClient.experimentalMaintenanceWindows(params: RpcMaintenanceWindowsRequest): List<RangeOfUint64> =
    call(
        method = "EXPERIMENTAL_maintenance_windows",
        params = params,
        paramsSerializer = RpcMaintenanceWindowsRequest.serializer(),
        resultSerializer = ListSerializer(RangeOfUint64.serializer()),
    )

/**
 * A configuration that defines the protocol-level parameters such as gas/storage costs, limits, fea...
 */
suspend fun NearRpcClient.experimentalProtocolConfig(params: RpcProtocolConfigRequest): RpcProtocolConfigResponse =
    call(
        method = "EXPERIMENTAL_protocol_config",
        params = params,
        paramsSerializer = RpcProtocolConfigRequest.serializer(),
        resultSerializer = RpcProtocolConfigResponse.serializer(),
    )

/**
 * Fetches a receipt by its ID (as is, without a status or execution outcome)
 */
suspend fun NearRpcClient.experimentalReceipt(params: RpcReceiptRequest): RpcReceiptResponse =
    call(
        method = "EXPERIMENTAL_receipt",
        params = params,
        paramsSerializer = RpcReceiptRequest.serializer(),
        resultSerializer = RpcReceiptResponse.serializer(),
    )

/**
 * Contains the split storage information. More info on split storage [here](https://near-nodes.io/a...
 */
suspend fun NearRpcClient.experimentalSplitStorageInfo(
    params: RpcSplitStorageInfoRequest,
): RpcSplitStorageInfoResponse =
    call(
        method = "EXPERIMENTAL_split_storage_info",
        params = params,
        paramsSerializer = RpcSplitStorageInfoRequest.serializer(),
        resultSerializer = RpcSplitStorageInfoResponse.serializer(),
    )

/**
 * Queries status of a transaction by hash, returning the final transaction result and details of al...
 */
suspend fun NearRpcClient.experimentalTxStatus(params: RpcTransactionStatusRequest): RpcTransactionResponse =
    call(
        method = "EXPERIMENTAL_tx_status",
        params = params,
        paramsSerializer = RpcTransactionStatusRequest.serializer(),
        resultSerializer = RpcTransactionResponse.serializer(),
    )

/**
 * Returns the current epoch validators ordered in the block producer order with repetition. This en...
 */
suspend fun NearRpcClient.experimentalValidatorsOrdered(params: RpcValidatorsOrderedRequest): List<ValidatorStakeView> =
    call(
        method = "EXPERIMENTAL_validators_ordered",
        params = params,
        paramsSerializer = RpcValidatorsOrderedRequest.serializer(),
        resultSerializer = ListSerializer(ValidatorStakeView.serializer()),
    )

/**
 * Returns block details for given height or hash
 */
suspend fun NearRpcClient.block(params: RpcBlockRequest): RpcBlockResponse =
    call(
        method = "block",
        params = params,
        paramsSerializer = RpcBlockRequest.serializer(),
        resultSerializer = RpcBlockResponse.serializer(),
    )

/**
 * Returns changes in block for given block height or hash over all transactions for all the types. ...
 */
suspend fun NearRpcClient.blockEffects(params: RpcStateChangesInBlockRequest): RpcStateChangesInBlockByTypeResponse =
    call(
        method = "block_effects",
        params = params,
        paramsSerializer = RpcStateChangesInBlockRequest.serializer(),
        resultSerializer = RpcStateChangesInBlockByTypeResponse.serializer(),
    )

/**
 * [Deprecated] Sends a transaction and immediately returns transaction hash. Consider using send_tx...
 */
suspend fun NearRpcClient.broadcastTxAsync(params: RpcSendTransactionRequest): CryptoHash =
    call(
        method = "broadcast_tx_async",
        params = params,
        paramsSerializer = RpcSendTransactionRequest.serializer(),
        resultSerializer = CryptoHash.serializer(),
    )

/**
 * [Deprecated] Sends a transaction and waits until transaction is fully complete. (Has a 10 second ...
 */
suspend fun NearRpcClient.broadcastTxCommit(params: RpcSendTransactionRequest): RpcTransactionResponse =
    call(
        method = "broadcast_tx_commit",
        params = params,
        paramsSerializer = RpcSendTransactionRequest.serializer(),
        resultSerializer = RpcTransactionResponse.serializer(),
    )

/**
 * Returns changes for a given account, contract or contract code for given block height or hash.
 */
suspend fun NearRpcClient.changes(params: RpcStateChangesInBlockByTypeRequest): RpcStateChangesInBlockResponse =
    call(
        method = "changes",
        params = params,
        paramsSerializer = RpcStateChangesInBlockByTypeRequest.serializer(),
        resultSerializer = RpcStateChangesInBlockResponse.serializer(),
    )

/**
 * Returns details of a specific chunk. You can run a block details query to get a valid chunk hash.
 */
suspend fun NearRpcClient.chunk(params: RpcChunkRequest): RpcChunkResponse =
    call(
        method = "chunk",
        params = params,
        paramsSerializer = RpcChunkRequest.serializer(),
        resultSerializer = RpcChunkResponse.serializer(),
    )

/**
 * Queries client node configuration
 */
suspend fun NearRpcClient.clientConfig(params: RpcClientConfigRequest): RpcClientConfigResponse =
    call(
        method = "client_config",
        params = params,
        paramsSerializer = RpcClientConfigRequest.serializer(),
        resultSerializer = RpcClientConfigResponse.serializer(),
    )

/**
 * Returns gas price for a specific block_height or block_hash. Using [null] will return the most re...
 */
suspend fun NearRpcClient.gasPrice(params: RpcGasPriceRequest): RpcGasPriceResponse =
    call(
        method = "gas_price",
        params = params,
        paramsSerializer = RpcGasPriceRequest.serializer(),
        resultSerializer = RpcGasPriceResponse.serializer(),
    )

/**
 * Get initial state and parameters for the genesis block
 */
suspend fun NearRpcClient.genesisConfig(params: GenesisConfigRequest): GenesisConfig =
    call(
        method = "genesis_config",
        params = params,
        paramsSerializer = GenesisConfigRequest.serializer(),
        resultSerializer = GenesisConfig.serializer(),
    )

/**
 * Returns the current health status of the RPC node the client connects to.
 */
suspend fun NearRpcClient.health(params: RpcHealthRequest): RpcHealthResponse? =
    call(
        method = "health",
        params = params,
        paramsSerializer = RpcHealthRequest.serializer(),
        resultSerializer = RpcHealthResponse.serializer(),
    )

/**
 * Returns the proofs for a transaction execution.
 */
suspend fun NearRpcClient.lightClientProof(
    params: RpcLightClientExecutionProofRequest,
): RpcLightClientExecutionProofResponse =
    call(
        method = "light_client_proof",
        params = params,
        paramsSerializer = RpcLightClientExecutionProofRequest.serializer(),
        resultSerializer = RpcLightClientExecutionProofResponse.serializer(),
    )

/**
 * Returns the future windows for maintenance in current epoch for the specified account. In the mai...
 */
suspend fun NearRpcClient.maintenanceWindows(params: RpcMaintenanceWindowsRequest): List<RangeOfUint64> =
    call(
        method = "maintenance_windows",
        params = params,
        paramsSerializer = RpcMaintenanceWindowsRequest.serializer(),
        resultSerializer = ListSerializer(RangeOfUint64.serializer()),
    )

/**
 * Queries the current state of node network connections. This includes information about active pee...
 */
suspend fun NearRpcClient.networkInfo(params: RpcNetworkInfoRequest): RpcNetworkInfoResponse =
    call(
        method = "network_info",
        params = params,
        paramsSerializer = RpcNetworkInfoRequest.serializer(),
        resultSerializer = RpcNetworkInfoResponse.serializer(),
    )

/**
 * Returns the next light client block.
 */
suspend fun NearRpcClient.nextLightClientBlock(
    params: RpcLightClientNextBlockRequest,
): RpcLightClientNextBlockResponse =
    call(
        method = "next_light_client_block",
        params = params,
        paramsSerializer = RpcLightClientNextBlockRequest.serializer(),
        resultSerializer = RpcLightClientNextBlockResponse.serializer(),
    )

/**
 * This module allows you to make generic requests to the network.
 */
suspend fun NearRpcClient.query(params: RpcQueryRequest): RpcQueryResponse =
    call(
        method = "query",
        params = params,
        paramsSerializer = RpcQueryRequest.serializer(),
        resultSerializer = RpcQueryResponse.serializer(),
    )

/**
 * Sends transaction. Returns the guaranteed execution status and the results the blockchain can pro...
 */
suspend fun NearRpcClient.sendTx(params: RpcSendTransactionRequest): RpcTransactionResponse =
    call(
        method = "send_tx",
        params = params,
        paramsSerializer = RpcSendTransactionRequest.serializer(),
        resultSerializer = RpcTransactionResponse.serializer(),
    )

/**
 * Requests the status of the connected RPC node. This includes information about sync status, nearc...
 */
suspend fun NearRpcClient.status(params: RpcStatusRequest): RpcStatusResponse =
    call(
        method = "status",
        params = params,
        paramsSerializer = RpcStatusRequest.serializer(),
        resultSerializer = RpcStatusResponse.serializer(),
    )

/**
 * Queries status of a transaction by hash and returns the final transaction result.
 */
suspend fun NearRpcClient.tx(params: RpcTransactionStatusRequest): RpcTransactionResponse =
    call(
        method = "tx",
        params = params,
        paramsSerializer = RpcTransactionStatusRequest.serializer(),
        resultSerializer = RpcTransactionResponse.serializer(),
    )

/**
 * Queries active validators on the network. Returns details and the state of validation on the bloc...
 */
suspend fun NearRpcClient.validators(params: RpcValidatorRequest): RpcValidatorResponse =
    call(
        method = "validators",
        params = params,
        paramsSerializer = RpcValidatorRequest.serializer(),
        resultSerializer = RpcValidatorResponse.serializer(),
    )
