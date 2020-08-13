/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 487913, 488067
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExtendedReflectiveColumnPropertyAccessor<R> extends ReflectiveColumnPropertyAccessor<R> {

    private static final Log log = LogFactory.getLog(ExtendedReflectiveColumnPropertyAccessor.class);

    /**
     * @param propertyNames
     *            of the members of the row bean
     */
    public ExtendedReflectiveColumnPropertyAccessor(String... propertyNames) {
        super(propertyNames);
    }

    /**
     * @param propertyNames
     *            of the members of the row bean
     * @since 1.4
     */
    public ExtendedReflectiveColumnPropertyAccessor(List<String> propertyNames) {
        super(propertyNames);
    }

    @Override
    public Object getDataValue(R rowObj, int columnIndex) {
        String propertyName = getColumnProperty(columnIndex);
        if (propertyName.contains(".")) { //$NON-NLS-1$
            return getPropertyValue(rowObj, propertyName);
        } else {
            return super.getDataValue(rowObj, columnIndex);
        }
    }

    @Override
    public void setDataValue(R rowObj, int columnIndex, Object newValue) {
        String propertyName = getColumnProperty(columnIndex);
        if (propertyName.contains(".")) { //$NON-NLS-1$
            setPropertyValue(rowObj, propertyName, newValue);
        } else {
            super.setDataValue(rowObj, columnIndex, newValue);
        }
    };

    /**
     * Reads the value of a property out of a given bean via reflection.
     *
     * @param object
     *            the bean out of which the property value should be read
     * @param propertyName
     *            the name of the property which value should be read
     * @return the property value of the bean
     */
    private Object getPropertyValue(Object object, String propertyName) {
        assert object != null : "object can not be null!"; //$NON-NLS-1$

        String[] propertyChain = null;
        if (propertyName.contains(".")) { //$NON-NLS-1$
            propertyChain = propertyName.split("\\."); //$NON-NLS-1$
        } else {
            propertyChain = new String[] { propertyName };
        }

        Object child = object;
        Class<?> objectClass = object.getClass();
        String getterName = null;
        Method getterMethod = null;
        for (String pc : propertyChain) {
            getterName = "get" + pc.substring(0, 1).toUpperCase() + pc.substring(1); //$NON-NLS-1$
            try {
                getterMethod = objectClass.getMethod(getterName);
                child = getterMethod.invoke(child);
            } catch (NoSuchMethodException e) {
                try {
                    getterName = "is" + pc.substring(0, 1).toUpperCase() + pc.substring(1); //$NON-NLS-1$
                    getterMethod = objectClass.getMethod(getterName);
                    child = getterMethod.invoke(child);
                } catch (Exception e1) {
                    log.error("Error on reflective accessing the data model", e1); //$NON-NLS-1$
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                log.error("Error on reflective accessing the data model", e); //$NON-NLS-1$
                throw new RuntimeException(e);
            }

            if (child != null) {
                objectClass = child.getClass();
            } else {
                // null is returned by reflection, therefore we can not go
                // further
                // and null is the correct return value
                break;
            }
        }

        return child;
    }

    /**
     * Sets a value to the property of a bean via reflection. Also supports dot
     * separated property names to access properties anywhere within the object
     * graph.
     *
     * @param object
     *            the bean where the property value should be set
     * @param propertyName
     *            the name of the property which should be set
     * @param value
     *            the value to set
     */
    private void setPropertyValue(Object object, String propertyName, Object value) {
        assert object != null : "object can not be null!"; //$NON-NLS-1$

        try {
            Object singlePropertyObject = null;
            String singlePropertyName = null;
            if (propertyName.contains(".")) { //$NON-NLS-1$
                singlePropertyObject = getPropertyValue(
                        object,
                        propertyName.substring(0, propertyName.lastIndexOf("."))); //$NON-NLS-1$
                singlePropertyName = propertyName.substring(propertyName.lastIndexOf(".") + 1); //$NON-NLS-1$
            } else {
                singlePropertyObject = object;
                singlePropertyName = propertyName;
            }

            String setterName = "set" //$NON-NLS-1$
                    + singlePropertyName.substring(0, 1).toUpperCase()
                    + singlePropertyName.substring(1);
            Method setterMethod = null;
            if (value != null) {
                setterMethod = singlePropertyObject.getClass().getMethod(
                        setterName, new Class<?>[] { value.getClass() });
            } else {
                // as the value is null we can not access the setter method
                // directly and have to search for the method
                Method[] methods = singlePropertyObject.getClass().getMethods();
                for (Method m : methods) {
                    if (m.getName().equals(setterName)) {
                        setterMethod = m;
                    }
                }
            }
            setterMethod.invoke(singlePropertyObject, value);
        } catch (Exception e) {
            log.error("Error on reflective accessing the data model", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

}
