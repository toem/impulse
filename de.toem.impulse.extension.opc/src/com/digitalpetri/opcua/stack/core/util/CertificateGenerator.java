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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.Permission;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

/**
 * Generates an {@link X509Certificate} and stores it in a keystore.
 * <p>
 * Must be running under a JVM with keytool () on the classpath.
 */
public class CertificateGenerator {

    private static final String KEY_TOOL_CLASS_NAME = "sun.security.tools.keytool.Main";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String keyStorePath;
    private final String keyStorePassword;
    private final String keyStoreType;
    private final String certificateAlias;
    private final String certificatePassword;

    /**
     * @param keyStorePath        absolute path to a keystore. If a keystore does not exist at this location it will be
     *                            created.
     * @param keyStorePassword    password for the keystore.
     * @param keyStoreType        type of keystore ("jks", "pkcs12").
     * @param certificateAlias    alias to store the certificate under.
     * @param certificatePassword password to use for the certificate entry.
     */
    public CertificateGenerator(String keyStorePath,
                                String keyStorePassword,
                                String keyStoreType,
                                String certificateAlias,
                                String certificatePassword) {

        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreType = keyStoreType;
        this.certificateAlias = certificateAlias;
        this.certificatePassword = certificatePassword;
    }

    public X509Certificate generateSelfSignedCertificate(String commonName,
                                                         String organizationName,
                                                         String localityName,
                                                         String stateName,
                                                         String country,
                                                         String applicationUri,
                                                         List<String> dnsNames,
                                                         List<String> ipAddresses) throws Exception {

        return generateSelfSignedCertificate(
                commonName, "", organizationName, localityName, stateName,
                country, 365, applicationUri, dnsNames, ipAddresses);
    }

    public X509Certificate generateSelfSignedCertificate(String commonName,
                                                         String organizationUnit,
                                                         String organizationName,
                                                         String localityName,
                                                         String stateName,
                                                         String country,
                                                         int validity,
                                                         String applicationUri,
                                                         List<String> dnsNames,
                                                         List<String> ipAddresses) throws Exception {

        String subject = String.format("cn=%s, ou=%s, o=%s, l=%s, st=%s, c=%s",
                commonName, organizationUnit, organizationName, localityName, stateName, country);

        List<String> args = buildKeyToolArgs(subject, validity, applicationUri, dnsNames, ipAddresses);

        invokeKeyTool(args.toArray(new String[args.size()]));

        KeyStore keyStore = KeyStore.getInstance(keyStoreType);

        try (FileInputStream fis = new FileInputStream(new File(keyStorePath))) {
            
            keyStore.load(fis, keyStorePassword.toCharArray());

            return (X509Certificate) keyStore.getCertificate(certificateAlias);
        }
    }

    private List<String> buildKeyToolArgs(String subject,
                                          int validity,
                                          String applicationUri,
                                          List<String> dnsNames,
                                          List<String> ipAddresses) {

        List<String> args = new ArrayList<>();
        args.add("-selfcert");
        args.add("-genkey");
        args.add("-validity");
        args.add(String.valueOf(validity));

        args.add("-ext");
        args.add("BC=ca:true");

        args.add("-ext");
        args.add("KeyUsage=digitalSignature,nonRepudiation,keyEncipherment,dataEncipherment,keyAgreement");

        args.add("-ext");
        args.add("ExtendedKeyUsage=serverAuth");

        args.add("-ext");
        StringBuilder sb = new StringBuilder();
        sb.append("SubjectAlternativeName=URI:").append(applicationUri);
        for (String dnsName : dnsNames) {
            sb.append(",DNS:").append(dnsName);
        }
        for (String ipAddress : ipAddresses) {
            sb.append(",IP:").append(ipAddress);
        }
        args.add(sb.toString());

        args.add("-keystore");
        args.add(String.format("%s", keyStorePath));
        args.add("-storetype");
        args.add(keyStoreType);
        args.add("-storepass");
        args.add(String.format("%s", keyStorePassword));

        args.add("-keyalg");
        args.add("RSA");
        args.add("-keysize");
        args.add("2048");

        args.add("-alias");
        args.add(String.format("%s", certificateAlias));
        args.add("-keypass");
        args.add(String.format("%s", certificatePassword));

        args.add("-dname");
        args.add(String.format("%s", subject));
        return args;
    }

    private void invokeKeyTool(String[] args) throws Exception {
        try {
            logger.debug("keytool args: {}", Arrays.toString(args));

            disableSystemExitCall();

            Class<?> c = Class.forName(KEY_TOOL_CLASS_NAME);
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, new Object[]{args});
        } catch (Throwable t) {
            throw new Exception("error invoking keytool", t);
        } finally {
            enableSystemExitCall();
        }
    }

    private static class ExitTrappedException extends SecurityException {}

    private static final SecurityManager SYSTEM_SECURITY_MANAGER = System.getSecurityManager();

    private static synchronized void disableSystemExitCall() {
        SecurityManager securityManager = new SecurityManager() {
            public void checkPermission(Permission permission) {
                if (permission.getName().startsWith("exitVM")) {
                    throw new ExitTrappedException();
                }
            }
        };
        System.setSecurityManager(securityManager);
    }

    private static synchronized void enableSystemExitCall() {
        System.setSecurityManager(SYSTEM_SECURITY_MANAGER);
    }

}
