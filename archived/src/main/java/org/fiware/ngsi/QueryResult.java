package org.fiware.ngsi;

import javax.json.JsonArray;

public class QueryResult {
    public int status;
    public JsonArray result;

    public QueryResult(int status, JsonArray result) {
        this.status = status;
        this.result = result;
    }

    public QueryResult(int status) {
        this.status = status;
    }
}
