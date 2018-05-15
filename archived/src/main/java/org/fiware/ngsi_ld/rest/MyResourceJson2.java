package org.fiware.ngsi_ld.rest;

import org.fiware.ngsi_ld.CEntity;
import org.fiware.ngsi_ld.CProperty;
import org.fiware.ngsi_ld.impl.EntityImpl;
import org.fiware.ngsi_ld.comp.EntityAdapter;
import org.fiware.ngsi_ld.impl.CPropertyImpl;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource3")
public class MyResourceJson2 {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getIt() {
        JsonbConfig config = new JsonbConfig();

        config.withAdapters(new EntityAdapter());
        Jsonb jsonb = JsonbBuilder.create(config);

        CEntity entity = new EntityImpl("urn:c3im:Vehicle:4567", "Vehicle");
        CProperty propertySt = new CPropertyImpl("speed", 40);

        entity.addProperty(propertySt);

        return jsonb.toJson(entity);
    }
}
