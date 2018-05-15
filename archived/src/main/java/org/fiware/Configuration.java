package org.fiware;

/**
 *
 *   Misc configuration parameters
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class Configuration {
    public static String ORION_BROKER = "http://localhost:1026/v2";

    public static String LINK_HEADER_VALUE =
            "<http://json-ld.org/contexts/ngsi-ld.jsonld>; " +
                    "rel=\"http://www.w3.org/ns/json-ld#context\"; " +
                    "type=\"application/ld+json\"";
}
