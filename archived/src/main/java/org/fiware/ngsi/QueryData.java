package org.fiware.ngsi;

import java.util.ArrayList;
import java.util.List;

public class QueryData {
    public String queryExpression;

    // Comma separated list of entity ids
    public String entityIds;

    // Comma separated list of types
    public String types;

    // Comma separated list of attributes
    public String attrs;

    public GeoQueryData geoQuery;

    public QueryData() {
        types = "";
        entityIds = "";
        queryExpression = "";
        attrs = "";
        geoQuery = new GeoQueryData();
    }
}
