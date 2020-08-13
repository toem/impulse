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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.google.common.collect.Maps;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;

public class ManifestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ManifestUtil.class);
    private static final Map<String, String> attributes = load();

    public static Optional<String> read(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    public static boolean exists(String name) {
        return attributes.containsKey(name);
    }

    private static Map<String, String> load() {
        Map<String, String> attributes = Maps.newConcurrentMap();

        for (URI uri : uris()) {
            try {
                attributes.putAll(load(uri.toURL()));
            } catch (Throwable t) {
                logger.error("load(): '{}' failed", uri, t);
            }
        }

        return attributes;
    }

    private static Map<String, String> load(URL url) throws IOException {
        return load(url.openStream());
    }

    private static Map<String, String> load(InputStream stream) {
        Map<String, String> props = Maps.newConcurrentMap();

        try {
            Manifest manifest = new Manifest(stream);
            Attributes attributes = manifest.getMainAttributes();

            for (Object key : attributes.keySet()) {
                String value = attributes.getValue((Attributes.Name) key);
                props.put(key.toString(), value);
            }
        } catch (Throwable t) {
            logger.error("#load(): failed", t);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignored
            }
        }

        return props;
    }

    private static Set<URI> uris() {
        try {
            Enumeration<URL> resources = ManifestUtil.class
                .getClassLoader()
                .getResources("META-INF/MANIFEST.MF");

            Set<URI> uris = new HashSet<>();
            while (resources.hasMoreElements()) {
                uris.add(resources.nextElement().toURI());
            }

            return uris;
        } catch (Throwable t) {
            logger.error("uris() failed", t);
            return Collections.emptySet();
        }
    }

}
