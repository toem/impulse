/*
 * Copyright 2015 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.opcua.stack.core.channel;

import java.util.Optional;

import com.digitalpetri.opcua.stack.core.security.SecurityAlgorithm;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.structured.ChannelSecurityToken;
import com.digitalpetri.opcua.stack.core.util.PShaUtil;

public class ChannelSecurity {

    private final SecuritySecrets currentKeys;
    private final ChannelSecurityToken currentToken;

    private final Optional<SecuritySecrets> previousKeys;
    private final Optional<ChannelSecurityToken> previousToken;

    public ChannelSecurity(SecuritySecrets currentSecuritySecrets, ChannelSecurityToken currentToken) {
        this(currentSecuritySecrets, currentToken, null, null);
    }

    public ChannelSecurity(SecuritySecrets currentKeys,
                           ChannelSecurityToken currentToken,
                           SecuritySecrets previousKeys,
                           ChannelSecurityToken previousToken) {

        this.currentKeys = currentKeys;
        this.currentToken = currentToken;
        this.previousKeys = Optional.ofNullable(previousKeys);
        this.previousToken = Optional.ofNullable(previousToken);
    }

    public SecuritySecrets getCurrentKeys() {
        return currentKeys;
    }

    public ChannelSecurityToken getCurrentToken() {
        return currentToken;
    }

    public Optional<SecuritySecrets> getPreviousKeys() {
        return previousKeys;
    }

    public Optional<ChannelSecurityToken> getPreviousToken() {
        return previousToken;
    }

    public static SecuritySecrets generateKeyPair(SecureChannel channel,
                                                 ByteString clientNonce,
                                                 ByteString serverNonce) {

        SecurityAlgorithm keyDerivation = channel.getSecurityPolicy().getKeyDerivationAlgorithm();

        int signatureKeySize = channel.getSymmetricSignatureKeySize();
        int encryptionKeySize = channel.getSymmetricEncryptionKeySize();
        int cipherTextBlockSize = channel.getSymmetricCipherTextBlockSize();

        assert (clientNonce != null);
        assert (serverNonce != null);

        byte[] clientSignatureKey = (keyDerivation == SecurityAlgorithm.PSha1) ?
                PShaUtil.createPSha1Key(serverNonce.bytes(), clientNonce.bytes(), 0, signatureKeySize) :
                PShaUtil.createPSha256Key(serverNonce.bytes(), clientNonce.bytes(), 0, signatureKeySize);

        byte[] clientEncryptionKey = (keyDerivation == SecurityAlgorithm.PSha1) ?
                PShaUtil.createPSha1Key(serverNonce.bytes(), clientNonce.bytes(), signatureKeySize, encryptionKeySize) :
                PShaUtil.createPSha256Key(serverNonce.bytes(), clientNonce.bytes(), signatureKeySize, encryptionKeySize);

        byte[] clientInitializationVector = (keyDerivation == SecurityAlgorithm.PSha1) ?
                PShaUtil.createPSha1Key(serverNonce.bytes(), clientNonce.bytes(), signatureKeySize + encryptionKeySize, cipherTextBlockSize) :
                PShaUtil.createPSha256Key(serverNonce.bytes(), clientNonce.bytes(), signatureKeySize + encryptionKeySize, cipherTextBlockSize);

        byte[] serverSignatureKey = (keyDerivation == SecurityAlgorithm.PSha1) ?
                PShaUtil.createPSha1Key(clientNonce.bytes(), serverNonce.bytes(), 0, signatureKeySize) :
                PShaUtil.createPSha256Key(clientNonce.bytes(), serverNonce.bytes(), 0, signatureKeySize);

        byte[] serverEncryptionKey = (keyDerivation == SecurityAlgorithm.PSha1) ?
                PShaUtil.createPSha1Key(clientNonce.bytes(), serverNonce.bytes(), signatureKeySize, encryptionKeySize) :
                PShaUtil.createPSha256Key(clientNonce.bytes(), serverNonce.bytes(), signatureKeySize, encryptionKeySize);

        byte[] serverInitializationVector = (keyDerivation == SecurityAlgorithm.PSha1) ?
                PShaUtil.createPSha1Key(clientNonce.bytes(), serverNonce.bytes(), signatureKeySize + encryptionKeySize, cipherTextBlockSize) :
                PShaUtil.createPSha256Key(clientNonce.bytes(), serverNonce.bytes(), signatureKeySize + encryptionKeySize, cipherTextBlockSize);

        return new SecuritySecrets(
                new SecretKeys(clientSignatureKey, clientEncryptionKey, clientInitializationVector),
                new SecretKeys(serverSignatureKey, serverEncryptionKey, serverInitializationVector)
        );
    }

    public static class SecuritySecrets {
        private final SecretKeys clientKeys;
        private final SecretKeys serverKeys;

        public SecuritySecrets(SecretKeys clientKeys, SecretKeys serverKeys) {
            this.clientKeys = clientKeys;
            this.serverKeys = serverKeys;
        }

        public SecretKeys getClientKeys() {
            return clientKeys;
        }

        public SecretKeys getServerKeys() {
            return serverKeys;
        }
    }

    public static class SecretKeys {
        private final byte[] signatureKey;
        private final byte[] encryptionKey;
        private final byte[] initializationVector;

        public SecretKeys(byte[] signatureKey, byte[] encryptionKey, byte[] initializationVector) {
            this.signatureKey = signatureKey;
            this.encryptionKey = encryptionKey;
            this.initializationVector = initializationVector;
        }

        public byte[] getSignatureKey() {
            return signatureKey;
        }

        public byte[] getEncryptionKey() {
            return encryptionKey;
        }

        public byte[] getInitializationVector() {
            return initializationVector;
        }
    }
}
