package org.fiware.ngsi;



import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;


public class NgsiClient {
    private String endPoint;

    public NgsiClient(String endPoint) {
        this.endPoint = endPoint;
    }

    public QueryResult query(QueryData q) {
        return this.query(q,null);
    }

    public QueryResult query(QueryData q, List<String> options) {
        Client c = ClientBuilder.newClient();

        URI uri = URI.create(endPoint);
        WebTarget target = c.target(uri).path("/entities");

        if (q.entityIds.length() > 0) {
            target = target.queryParam("id",q.entityIds);
        }

        if(q.types.length() > 0) {
            target = target.queryParam("type", q.types);
        }

        if (q.queryExpression.length() > 0) {
            target = target.queryParam("q", q.queryExpression);
        }

        if (q.geoQuery.geoRel.length() > 0) {
            target = target.queryParam("georel", q.geoQuery.geoRel);
            target = target.queryParam("geometry", q.geoQuery.geometry);
            target = target.queryParam("coords", q.geoQuery.coords);
        }

        if (q.attrs.length() > 0) {
            target = target.queryParam("attrs", q.attrs + ",dateCreated,dateModified");
        }
        else {
            target = target.queryParam("attrs", "*,dateCreated,dateModified");
        }

        if (options != null && options.size() > 0) {
            String optionsList = String.join(",", options);
            target = target.queryParam("options",optionsList);
        }

        QueryResult out = null;

        try {
            Response r = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();
            if (r.getStatus() == 200) {
                out = new QueryResult(200, r.readEntity(JsonArray.class));
            }
            else {
                out = new QueryResult(r.getStatus());
            }
        }
        catch(Throwable thr) {
           thr.printStackTrace();
           out = new QueryResult(500);
        }

        return out;
    }

    /**
     *
     *   Invokes the NGSIv2 API to create an entity
     *
     *   @param obj JSON Object encoded using the NGSIv2 normalized format
     *
     */
    public Response createEntity(JsonObject obj) {
        Client c = ClientBuilder.newClient();

        URI uri = URI.create(endPoint);
        WebTarget target = c.target(uri).path("/entities");

        Invocation.Builder invocationBuilder =  target.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.json(obj));

        return response;
    }
}
