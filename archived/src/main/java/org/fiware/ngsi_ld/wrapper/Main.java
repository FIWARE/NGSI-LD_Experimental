package org.fiware.ngsi_ld.wrapper;

import org.fiware.Configuration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8080/ngsi-ld/v1/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS rest defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS rest and providers
        final ResourceConfig rc = new ResourceConfig().packages("org.fiware.ngsi_ld.rest");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: ngsi-ld_wrapper <orion_end_point>");
            System.exit(-1);
        }

        Configuration.ORION_BROKER = args[0] + "/v2";

        System.out.println("Using Orion: " + Configuration.ORION_BROKER);

        Logger log = Logger.getLogger(Main.class.getName());

        log.log(Level.WARNING,"Starting server .....");

        Logger log2 = Logger.getLogger("org.glassfish");
        log2.setLevel(Level.ALL);
        log2.addHandler(new java.util.logging.ConsoleHandler());

        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl", BASE_URI));
    }
}

