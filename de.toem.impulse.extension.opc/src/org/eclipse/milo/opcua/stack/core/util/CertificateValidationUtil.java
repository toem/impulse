/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.util;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathChecker;
import java.security.cert.CertStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class CertificateValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateValidationUtil.class);

    private static final String KEY_USAGE_OID = "2.5.29.15";

    private static final int SUBJECT_ALT_NAME_URI = 6;
    private static final int SUBJECT_ALT_NAME_DNS_NAME = 2;
    private static final int SUBJECT_ALT_NAME_IP_ADDRESS = 7;

    /**
     * Verify that a chain of trust can be established for a certificate or chain of certificates.
     * <p>
     * The chain must begin with the end-entity certificate at index 0 followed by the remaining certificates in the
     * chain, if any, in the correct order.
     * <p>
     * If the end-entity certificate is present in the {@code trustedCertificates} set then trust is immediately
     * verified. Otherwise, an attempt to build a path to a trusted anchor is made using the provided
     * {@code issuerCertificates} as the anchors.
     *
     * @param certificateChain    the certificate chain to verify.
     * @param trustedCertificates the set of known-trusted certificates.
     * @param issuerCertificates  the set of CA certificates to use as trust anchors.
     * @throws UaException if a chain of trust could not be established.
     */
    public static void verifyTrustChain(
        List<X509Certificate> certificateChain,
        Set<X509Certificate> trustedCertificates,
        Set<X509Certificate> issuerCertificates
    ) throws UaException {

        verifyTrustChain(
            certificateChain,
            trustedCertificates,
            Collections.emptySet(),
            issuerCertificates,
            Collections.emptySet()
        );
    }

    /**
     * Verify that a chain of trust can be established for a certificate or chain of certificates.
     * <p>
     * The chain must begin with the end-entity certificate at index 0 followed by the remaining certificates in the
     * chain, if any, in the correct order.
     * <p>
     * If the end-entity certificate is present in the {@code trustedCertificates} set then trust is immediately
     * verified. Otherwise, an attempt to build a path to a trust anchor is made using the root CAs in
     * {@code trustedCertificates} and the root CAs in {@code issuerCertificates} as the trust anchors.
     * <p>
     * Once a valid certificate path has been established, at least one component of that path must be present in the
     * {@code trustedCertificates} list.
     *
     * @param certificateChain    the certificate chain to verify.
     * @param trustedCertificates a collection of known-trusted certificates and CAs. Root CAs are used as Trust
     *                            Anchors.
     * @param trustedCrls         a collection of {@link X509CRL}s for CAs in {@code trustedCertificates}, if any.
     * @param issuerCertificates  a collection of intermediate and root CA certificates used the purpose of path
     *                            validation, but that aren't trusted issuers. Root CAs are used as Trust Anchors.
     * @param issuerCrls          a collection of {@link X509CRL}s for CAs in {@code issuerCertificates}, if any.
     * @throws UaException if a chain of trust could not be established.
     */
    public static void verifyTrustChain(
        List<X509Certificate> certificateChain,
        Collection<X509Certificate> trustedCertificates,
        Collection<X509CRL> trustedCrls,
        Collection<X509Certificate> issuerCertificates,
        Collection<X509CRL> issuerCrls
    ) throws UaException {

        Preconditions.checkArgument(!certificateChain.isEmpty(), "certificateChain must not be empty");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("certificateChain: {}", certificateChain);
            LOGGER.trace("trustedCertificates: {}", trustedCertificates);
            LOGGER.trace("trustedCrls: {}", trustedCrls);
            LOGGER.trace("issuerCertificates: {}", issuerCertificates);
            LOGGER.trace("issuerCrls: {}", issuerCrls);
        }

        X509Certificate certificate = certificateChain.get(0);

        try {
            Set<TrustAnchor> trustAnchors = new HashSet<>();
            for (X509Certificate c : trustedCertificates) {
                if (certificateIsCa(c) && certificateIsSelfSigned(c)) {
                    trustAnchors.add(new TrustAnchor(c, null));
                }
            }
            for (X509Certificate c : issuerCertificates) {
                if (certificateIsCa(c) && certificateIsSelfSigned(c)) {
                    trustAnchors.add(new TrustAnchor(c, null));
                }
            }

            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(certificate);

            PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, selector);

            // Add a CertStore containing any intermediate certs and CRLs
            Collection<Object> collection = Lists.newArrayList();
            collection.addAll(certificateChain.subList(1, certificateChain.size()));

            for (X509Certificate c : trustedCertificates) {
                if (certificateIsCa(c) && !certificateIsSelfSigned(c)) {
                    collection.add(c);
                }
            }
            for (X509Certificate c : issuerCertificates) {
                if (certificateIsCa(c) && !certificateIsSelfSigned(c)) {
                    collection.add(c);
                }
            }

            collection.addAll(trustedCrls);
            collection.addAll(issuerCrls);

            if (collection.size() > 0) {
                CertStore certStore = CertStore.getInstance(
                    "Collection",
                    new CollectionCertStoreParameters(collection)
                );

                params.addCertStore(certStore);
            }

            // Only enable revocation checking if the CRL list is non-empty
            params.setRevocationEnabled(!trustedCrls.isEmpty() || !issuerCrls.isEmpty());

            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");

            // Set up revocation options regardless of whether it's actually enabled
            CertPathChecker revocationChecker = builder.getRevocationChecker();

            if (revocationChecker instanceof PKIXRevocationChecker) {
                ((PKIXRevocationChecker) revocationChecker).setOptions(Sets.newHashSet(
                    PKIXRevocationChecker.Option.NO_FALLBACK,
                    PKIXRevocationChecker.Option.PREFER_CRLS,
                    PKIXRevocationChecker.Option.SOFT_FAIL
                ));
            }

            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder.build(params);

            List<X509Certificate> certificatePath = new ArrayList<>();

            result.getCertPath().getCertificates()
                .stream()
                .map(X509Certificate.class::cast)
                .forEach(certificatePath::add);

            certificatePath.add(result.getTrustAnchor().getTrustedCert());

            LOGGER.debug("certificate path: {}", certificatePath);

            if (certificatePath.stream().noneMatch(trustedCertificates::contains)) {
                throw new UaException(StatusCodes.Bad_SecurityChecksFailed,
                    "certificate path did not contain a trusted certificate");
            }
        } catch (UaException e) {
            throw e;
        } catch (Throwable t) {
            LOGGER.debug("certificate path validation failed: {}", t.getMessage());

            throw new UaException(
                StatusCodes.Bad_SecurityChecksFailed,
                "certificate path validation failed");
        }
    }

    /**
     * Check if {@code certificate}'s KeyUsage and/or BasicConstraints extensions indicate it is a CA.
     *
     * @param certificate the {@link X509Certificate} to check.
     * @return {@code true} if {@code certificate}'s KeyUsage and/or BasicConstraints extensions indicates it is a CA.
     */
    static boolean certificateIsCa(X509Certificate certificate) {
        boolean[] keyUsage = certificate.getKeyUsage();
        int basicConstraints = certificate.getBasicConstraints();

        if (keyUsage == null) {
            // no KeyUsage, just check if the cA BasicConstraint is set.
            return basicConstraints >= 0;
        } else {
            if (keyUsage[5] && basicConstraints == -1) {
                // KeyUsage is present and the keyCertSign bit is set.
                // According to RFC 5280 the BasicConstraint cA bit must also
                // be set, but it's not!
                LOGGER.warn(
                    "'{}' violates RFC 5280: KeyUsage keyCertSign " +
                        "bit set without BasicConstraint cA bit set",
                    certificate.getSubjectX500Principal().getName()
                );
            }

            return keyUsage[5] || basicConstraints >= 0;
        }
    }

    /**
     * Return {@code true} if a given {@link X509Certificate} is self-signed.
     *
     * @return {@code true} if a given {@link X509Certificate} is self-signed.
     */
    private static boolean certificateIsSelfSigned(X509Certificate cert) throws UaException {
        try {
            // Verify certificate signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);

            // Check that subject and issuer are the same
            return Objects.equals(cert.getSubjectX500Principal(), cert.getIssuerX500Principal());
        } catch (SignatureException | InvalidKeyException sigEx) {
            // Invalid signature or key: not self-signed
            return false;
        } catch (Exception e) {
            throw new UaException(StatusCodes.Bad_CertificateInvalid, e);
        }
    }

    public static void validateCertificateValidity(X509Certificate certificate) throws UaException {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException e) {
            throw new UaException(StatusCodes.Bad_CertificateTimeInvalid,
                String.format("certificate is expired: %s - %s",
                    certificate.getNotBefore(), certificate.getNotAfter()));
        } catch (CertificateNotYetValidException e) {
            throw new UaException(StatusCodes.Bad_CertificateTimeInvalid,
                String.format("certificate not yet valid: %s - %s",
                    certificate.getNotBefore(), certificate.getNotAfter()));
        }
    }

    /**
     * Validate that one of {@code hostnames} matches a SubjectAltName DNSName or IPAddress entry in the certificate.
     *
     * @param certificate the certificate to validate against.
     * @param hostnames   the hostnames to look for.
     * @throws UaException if there is no matching DNSName or IPAddress entry.
     */
    public static void validateHostnameOrIpAddress(
        X509Certificate certificate, String... hostnames) throws UaException {

        boolean dnsNameMatch = Arrays.stream(hostnames).anyMatch(n -> {
            try {
                return validateSubjectAltNameField(
                    certificate, SUBJECT_ALT_NAME_DNS_NAME, n::equals);
            } catch (Throwable t) {
                return false;
            }
        });

        boolean ipAddressMatch = Arrays.stream(hostnames).anyMatch(n -> {
            try {
                return validateSubjectAltNameField(
                    certificate, SUBJECT_ALT_NAME_IP_ADDRESS, n::equals);
            } catch (Throwable t) {
                return false;
            }
        });

        if (!(dnsNameMatch || ipAddressMatch)) {
            throw new UaException(StatusCodes.Bad_CertificateHostNameInvalid);
        }
    }

    /**
     * Validate that the application URI matches the SubjectAltName URI in the given certificate.
     *
     * @param certificate    the certificate to validate against.
     * @param applicationUri the URI to validate.
     * @throws UaException if the certificate is invalid, does not contain a uri, or contains a uri that does not match.
     */
    public static void validateApplicationUri(X509Certificate certificate, String applicationUri) throws
        UaException {
        if (!validateSubjectAltNameField(certificate, SUBJECT_ALT_NAME_URI, applicationUri::equals)) {
            throw new UaException(StatusCodes.Bad_CertificateUriInvalid);
        }
    }

    public static void validateApplicationCertificateUsage(X509Certificate certificate) throws UaException {
        Set<String> criticalExtensions = certificate.getCriticalExtensionOIDs();
        if (criticalExtensions == null) criticalExtensions = new HashSet<>();

        if (criticalExtensions.contains(KEY_USAGE_OID)) {
            boolean[] keyUsage = certificate.getKeyUsage();
            boolean digitalSignature = keyUsage[0];
            boolean nonRepudiation = keyUsage[1];
            boolean keyEncipherment = keyUsage[2];
            boolean dataEncipherment = keyUsage[3];

            if (!digitalSignature) {
                throw new UaException(StatusCodes.Bad_CertificateUseNotAllowed,
                    "required KeyUsage 'digitalSignature' not found");
            }

            if (!nonRepudiation) {
                throw new UaException(StatusCodes.Bad_CertificateUseNotAllowed,
                    "required KeyUsage 'nonRepudiation' not found");
            }

            if (!keyEncipherment) {
                throw new UaException(StatusCodes.Bad_CertificateUseNotAllowed,
                    "required KeyUsage 'keyEncipherment' not found");
            }

            if (!dataEncipherment) {
                throw new UaException(StatusCodes.Bad_CertificateUseNotAllowed,
                    "required KeyUsage 'dataEncipherment' not found");
            }
        }
    }

    public static boolean validateSubjectAltNameField(X509Certificate certificate, int field,
                                                      Predicate<Object> fieldValidator) throws UaException {

        try {
            Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) subjectAltNames = Collections.emptyList();

            for (List<?> idAndValue : subjectAltNames) {
                if (idAndValue != null && idAndValue.size() == 2) {
                    if (idAndValue.get(0).equals(field)) {
                        if (fieldValidator.test(idAndValue.get(1))) {
                            return true;
                        }
                    }
                }
            }

            return false;
        } catch (CertificateParsingException e) {
            throw new UaException(StatusCodes.Bad_CertificateInvalid, e);
        }
    }

    public static String getSubjectAltNameUri(X509Certificate certificate) throws UaException {
        try {
            Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) subjectAltNames = Collections.emptyList();

            for (List<?> idAndValue : subjectAltNames) {
                if (idAndValue != null && idAndValue.size() == 2) {
                    if (idAndValue.get(0).equals(SUBJECT_ALT_NAME_URI)) {
                        Object uri = idAndValue.get(1);
                        return uri != null ? uri.toString() : null;
                    }
                }
            }

            return null;
        } catch (CertificateParsingException e) {
            throw new UaException(StatusCodes.Bad_CertificateInvalid, e);
        }
    }

}
