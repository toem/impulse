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

package com.digitalpetri.opcua.stack.core.security;

import java.security.MessageDigest;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;

import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;

public enum SecurityAlgorithm {

    None("", ""),

    /**
     * Symmetric Signature; transformation to be used with {@link Mac#getInstance(String)}.
     */
    HmacSha1("http://www.w3.org/2000/09/xmldsig#hmac-sha1", "HmacSHA1"),

    /**
     * Symmetric Signature; transformation to be used with {@link Mac#getInstance(String)}.
     */
    HmacSha256("http://www.w3.org/2000/09/xmldsig#hmac-sha256", "HmacSHA256"),

    /**
     * Symmetric Encryption; transformation to be used with {@link Cipher#getInstance(String)}.
     */
    Aes128("http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES/CBC/NoPadding"),

    /**
     * Symmetric Encryption; transformation to be used with {@link Cipher#getInstance(String)}.
     */
    Aes256("http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES/CBC/NoPadding"),

    /**
     * Asymmetric Signature; transformation to be used with {@link Signature#getInstance(String)}.
     */
    RsaSha1("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "SHA1withRSA"),

    /**
     * Asymmetric Signature; transformation to be used with {@link Signature#getInstance(String)}.
     */
    RsaSha256("http://www.w3.org/2000/09/xmldsig#rsa-sha256", "SHA256withRSA"),

    /**
     * Asymmetric Encryption; transformation to be used with {@link Cipher#getInstance(String)}.
     */
    Rsa15("http://www.w3.org/2001/04/xmlenc#rsa-1_5", "RSA/ECB/PKCS1Padding"),

    /**
     * Asymmetric Encryption; transformation to be used with {@link Cipher#getInstance(String)}.
     */
    RsaOaep("http://www.w3.org/2001/04/xmlenc#rsa-oaep", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"),

    /**
     * Asymmetric Key Wrap
     */
    KwRsa15("http://www.w3.org/2001/04/xmlenc#rsa-1_5", ""),

    /**
     * Asymmetric Key Wrap
     */
    KwRsaOaep("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", ""),

    /**
     * Key Derivation
     */
    PSha1("http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512/dk/p_sha1", ""),

    /**
     * Key Derivation
     */
    PSha256("http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512/dk/p_sha256", ""),

    /**
     * Cryptographic Hash; transformation to be used with {@link MessageDigest#getInstance(String)}.
     */
    Sha1("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1"),

    /**
     * Cryptographic Hash; transformation to be used with {@link MessageDigest#getInstance(String)}.
     */
    Sha256("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");

    private final String uri;
    private final String transformation;

    SecurityAlgorithm(String uri, String transformation) {
        this.uri = uri;
        this.transformation = transformation;
    }

    /**
     * @return The URI identifying this security algorithm.
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return The transformation string to use with the appropriate provider SPI.
     */
    public String getTransformation() {
        return transformation;
    }

    public static SecurityAlgorithm fromUri(String securityAlgorithmUri) throws UaException {
        for (SecurityAlgorithm algorithm: values()) {
            if (algorithm.getUri().equals(securityAlgorithmUri)) {
                return algorithm;
            }
        }

        throw new UaException(StatusCodes.Bad_SecurityChecksFailed,
                "unknown securityAlgorithmUri: " + securityAlgorithmUri);
    }

}
