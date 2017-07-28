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

package com.digitalpetri.opcua.stack.core.util;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.security.SecurityAlgorithm;

public class SignatureUtil {

    /**
     * Sign the contents of the provided buffers using the provided {@link SecurityAlgorithm}.
     * Note that only the bytes between position and limit of each buffer are considered.
     *
     * @param securityAlgorithm the {@link SecurityAlgorithm}.
     * @param privateKey        the {@link PrivateKey} to sign with.
     * @param buffers           the data to sign.
     * @return the signature bytes.
     * @throws UaException
     */
    public static byte[] sign(SecurityAlgorithm securityAlgorithm,
                              PrivateKey privateKey,
                              ByteBuffer... buffers) throws UaException {

        String transformation = securityAlgorithm.getTransformation();

        try {
            Signature signature = Signature.getInstance(transformation);
            signature.initSign(privateKey);

            for (ByteBuffer buffer : buffers) {
                signature.update(buffer);
            }

            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new UaException(StatusCodes.Bad_InternalError, e);
        }
    }

    /**
     * Compute the HMAC of the provided buffers.
     *
     * @param securityAlgorithm the {@link SecurityAlgorithm} that provides the transformation for
     *                          {@link Mac#getInstance(String)}}.
     * @param secretKey         the secret key.
     * @param buffers           the buffers to use.
     * @return the computed HMAC.
     * @throws UaException
     */
    public static byte[] hmac(SecurityAlgorithm securityAlgorithm,
                              byte[] secretKey,
                              ByteBuffer... buffers) throws UaException {

        String transformation = securityAlgorithm.getTransformation();

        try {
            Mac mac = Mac.getInstance(transformation);
            mac.init(new SecretKeySpec(secretKey, transformation));

            for (ByteBuffer buffer : buffers) {
                mac.update(buffer);
            }

            return mac.doFinal();
        } catch (GeneralSecurityException e) {
            throw new UaException(StatusCodes.Bad_SecurityChecksFailed, e);
        }
    }

}
