package org.fiware.ngsi_ld.impl;

import org.fiware.ngsi_ld.GeoProperty;

public class GeoPropertyImpl extends CPropertyImpl implements GeoProperty {
    public GeoPropertyImpl(String propertyId, Object value) {
       super(propertyId, value);
    }

    public GeoPropertyImpl(String propertyId, Object value, String dataType) {
        super(propertyId, value, dataType);
    }
}
