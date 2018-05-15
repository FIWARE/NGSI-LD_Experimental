package org.fiware.ngsi_ld.rest;

import org.fiware.Configuration;
import org.fiware.ngsi.GeoQueryData;
import org.fiware.ngsi.NgsiClient;
import org.fiware.ngsi.QueryData;
import org.fiware.ngsi.QueryResult;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;

/**
 *
 *   Entity resource
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
@Path("/csources/")
public class CsourceResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCsource(String jsonPayload) {
        JsonReader reader = Json.createReader(new StringReader(jsonPayload));

        JsonObject obj = reader.readObject();

        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", obj.getString("id"));
        builder.add("type", obj.getString("type"));

        for(String key:obj.keySet()) {
            if (!key.equals("id") && !key.equals("type")) {
                JsonObjectBuilder valueBuilder = Json.createObjectBuilder();
                valueBuilder.add("value", obj.get(key));
                if (key.equals("location")) {
                    valueBuilder.add("type", "geo:json");
                }
                builder.add(key, valueBuilder.build());
            }
        }

        NgsiClient client = new NgsiClient(Configuration.ORION_BROKER);

        return client.createEntity(builder.build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCsources(@QueryParam("id") String id,
                                @QueryParam("q") String q, @QueryParam("georel") String georel,
                                @QueryParam("geometry") String geometry, @QueryParam("coords") String coords) {
        QueryData qd = new QueryData();
        if (id != null) {
            qd.entityIds = id;
        }

        qd.types = "ContextSource";

        if (q != null) {
            qd.queryExpression = q;
        }

        if (georel != null) {
            qd.geoQuery = new GeoQueryData();
            qd.geoQuery.geoRel = georel;
            qd.geoQuery.coords = coords;
            qd.geoQuery.geometry = geometry;
        }

        NgsiClient client = new NgsiClient(Configuration.ORION_BROKER);

        QueryResult result = client.query(qd);

        return Response.status(result.status).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCsource(String id) {
        return null;
    }
}
