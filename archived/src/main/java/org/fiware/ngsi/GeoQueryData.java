package org.fiware.ngsi;

public class GeoQueryData {
    public String geoRel;

    // Comma separated list of entity ids
    public String geometry;

    // Comma separated list of types
    public String coords;

    public GeoQueryData() {
        geoRel = "";
        geometry = "";
        coords = "";
    }
}
