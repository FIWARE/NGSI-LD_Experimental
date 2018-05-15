package org.fiware.ngsi_ld.impl;

import org.fiware.ngsi_ld.CProperty;

/**
 *
 *   A CProperty implementation
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class CPropertyImpl extends CObjectImpl implements CProperty {
    private Object value;
    private String propertyId;
    private String dataType;

    @Override
    public String getPropertyId() {
        return propertyId;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    public CPropertyImpl(String propertyId, Object value) {
        this.propertyId = propertyId;
        this.value = value;
    }

    public CPropertyImpl(String propertyId, Object value, String dataType) {
        this(propertyId, value);
        this.dataType = dataType;
    }
}
