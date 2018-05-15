package org.fiware.ngsi_ld;

import java.net.URI;

/**
 *
 *   CRelationship
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public interface CRelationship extends CObject {
    public URI getObject();
    public String getRelationshipId();
}
