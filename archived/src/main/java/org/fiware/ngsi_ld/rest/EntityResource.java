package org.fiware.ngsi_ld.rest;

import org.fiware.ngsi.GeoQueryData;
import org.fiware.ngsi.NgsiClient;
import org.fiware.ngsi.QueryData;
import org.fiware.ngsi.QueryResult;
import org.fiware.ngsi_ld.CEntity;
import org.fiware.ngsi_ld.comp.EntityAdapter;
import org.fiware.Configuration;
import org.fiware.ngsi_ld.comp.Ngsi2NGSILD;
import org.fiware.ngsi_ld.impl.EntityImpl;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 *   Entity resource
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
@Path("/entities/")
public class EntityResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Object that will be transformed to application/json response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response getEntity(@PathParam("id") String id,
                              @QueryParam("attrs") String attrs,
                              @QueryParam("options") List<String> options) {
        JsonbConfig config = new JsonbConfig();

        config.withAdapters(new EntityAdapter());
        Jsonb jsonb = JsonbBuilder.create(config);

        QueryData qd = new QueryData();
        qd.entityIds = id;
        if (attrs != null) {
            qd.attrs = attrs;
        }

        QueryResult result = retrieveNgsiEntity(qd, options);

        if (result.status != 200) {
            return Response.status(result.status).build();
        }
        else {
            JsonArray array = result.result.asJsonArray();
            if (array.size() == 0) {
                return Response.status(404).build();
            }

            if (options.indexOf("keyValues") != -1) {
                return Response.status(200).entity(array.getJsonObject(0)).build();
            }

            CEntity c3imEntity = Ngsi2NGSILD.toNGSILD(array.getJsonObject(0));
            return addJsonLinkHeader(Response.ok()).entity(jsonb.toJson(c3imEntity)).build();
        }
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Object that will be transformed to application/json response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntities(@QueryParam("id") String id, @QueryParam("type") String type,
                              @QueryParam("q") String q, @QueryParam("georel") String georel,
                              @QueryParam("geometry") String geometry, @QueryParam("coords") String coords,
                                @QueryParam("attrs") String attrs,
                              @QueryParam("options") List<String> options) {
        QueryData qd = new QueryData();
        if (id != null) {
            qd.entityIds = id;
        }

        if (type != null) {
            qd.types = type;
        }

        if (q != null) {
            qd.queryExpression = q;
        }

        if (georel != null) {
            qd.geoQuery = new GeoQueryData();
            qd.geoQuery.geoRel = georel;
            qd.geoQuery.coords = coords;
            qd.geoQuery.geometry = geometry;
        }

        if (attrs != null) {
            qd.attrs = attrs;
        }

        JsonbConfig config = new JsonbConfig();

        config.withAdapters(new EntityAdapter());
        Jsonb jsonb = JsonbBuilder.create(config);

        QueryResult result = retrieveNgsiEntity(qd, options);

        if (result.status != 200) {
            return Response.status(result.status).build();
        }
        else {
            boolean keyValues = false;

            if (options.indexOf("keyValues") != -1) {
                keyValues = true;
            }

            JsonArray array = result.result.asJsonArray();

            // Workaround to return a list
            StringBuffer stb = new StringBuffer("[");

            for(int j = 0; j < array.size(); j++) {
                JsonObject obj = array.getJsonObject(j);

                if (!keyValues) {
                    CEntity c3imEntity = Ngsi2NGSILD.toNGSILD(obj);
                    stb.append(jsonb.toJson(c3imEntity));
                }
                else {
                    stb.append(Ngsi2NGSILD.toNGSILDKeyValues(obj));
                }

                stb.append(",");
            }

            if (stb.length() > 1) {
                stb.deleteCharAt(stb.length() - 1);
            }

            stb.append("]");

            return addJsonLinkHeader(Response.ok(stb.toString())).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addEntity(String ent) {
        JsonbConfig config = new JsonbConfig();

        config.withAdapters(new EntityAdapter());
        Jsonb jsonb = JsonbBuilder.create(config);

        JsonObject obj = null;

        try {
            CEntity entity = jsonb.fromJson(ent, EntityImpl.class);

            obj = Ngsi2NGSILD.toNgsi(entity);
        }
        catch(Exception ex) {
            if(ex.getCause().getMessage().equals("400")) {
               return Response.status(400).build();
            }
        }

        JsonWriter writer = Json.createWriter(System.out);
        writer.writeObject(obj);

        NgsiClient client = new NgsiClient(Configuration.ORION_BROKER);
        Response res = client.createEntity(obj);

        if(res.getStatus() == 201) {
            res = Response.status(201).location(URI.create(
                    "entities/" + obj.getString("id"))).build();
        }

        return res;
    }

    private Response.ResponseBuilder addJsonLinkHeader(Response.ResponseBuilder rb) {
        return rb.header("Link", Configuration.LINK_HEADER_VALUE);
    }

    private QueryResult retrieveNgsiEntity(QueryData qd, List<String> options) {
        NgsiClient client = new NgsiClient(Configuration.ORION_BROKER);

        return client.query(qd, options);
    }
}
