/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 450927
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 487913, 488067
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience class which uses java reflection to get/set property names from
 * the row bean. It looks for getter methods for reading and setter methods for
 * writing according to the Java conventions.
 *
 * @param <R>
 *            type of the row object/bean
 */
public class ReflectiveColumnPropertyAccessor<R> implements IColumnPropertyAccessor<R> {

    private static final Log LOG = LogFactory.getLog(ReflectiveColumnPropertyAccessor.class);

    private final List<String> propertyNames;

    private Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptorMap =
            new HashMap<Class<?>, Map<String, PropertyDescriptor>>();

    /**
     * @param propertyNames
     *            of the members of the row bean
     */
    public ReflectiveColumnPropertyAccessor(String... propertyNames) {
        this.propertyNames = Arrays.asList(propertyNames);
    }

    /**
     * @param propertyNames
     *            of the members of the row bean
     * @since 1.4
     */
    public ReflectiveColumnPropertyAccessor(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    @Override
    public int getColumnCount() {
        return this.propertyNames.size();
    }

    @Override
    public Object getDataValue(R rowObj, int columnIndex) {
        try {
            PropertyDescriptor propertyDesc = getPropertyDescriptor(rowObj, columnIndex);
            Method readMethod = propertyDesc.getReadMethod();
            return readMethod.invoke(rowObj);
        } catch (Exception e) {
            LOG.warn(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDataValue(R rowObj, int columnIndex, Object newValue) {
        try {
            PropertyDescriptor propertyDesc = getPropertyDescriptor(rowObj, columnIndex);
            Method writeMethod = propertyDesc.getWriteMethod();
            if (writeMethod == null) {
                throw new RuntimeException(
                        "Setter method not found in backing bean for value at column index: " + columnIndex); //$NON-NLS-1$
            }
            writeMethod.invoke(rowObj, newValue);
        } catch (IllegalArgumentException ex) {
            LOG.error("Data type being set does not match the data type of the setter method in the backing bean", ex); //$NON-NLS-1$
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException("Error while setting data value"); //$NON-NLS-1$
        }
    };

    @Override
    public String getColumnProperty(int columnIndex) {
        return this.propertyNames.get(columnIndex);
    }

    @Override
    public int getColumnIndex(String propertyName) {
        return this.propertyNames.indexOf(propertyName);
    }

    private PropertyDescriptor getPropertyDescriptor(R rowObj, int columnIndex) throws IntrospectionException {
        synchronized (rowObj) {
            if (!this.propertyDescriptorMap.containsKey(rowObj.getClass())) {
                PropertyDescriptor[] propertyDescriptors =
                        Introspector.getBeanInfo(rowObj.getClass()).getPropertyDescriptors();

                Map<String, PropertyDescriptor> propertiesByAttribute = new HashMap<String, PropertyDescriptor>();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    propertiesByAttribute.put(propertyDescriptor.getName(), propertyDescriptor);
                }
                this.propertyDescriptorMap.put(rowObj.getClass(), propertiesByAttribute);
            }
        }

        final String propertyName = this.propertyNames.get(columnIndex);
        return this.propertyDescriptorMap.get(rowObj.getClass()).get(propertyName);
    }

}
