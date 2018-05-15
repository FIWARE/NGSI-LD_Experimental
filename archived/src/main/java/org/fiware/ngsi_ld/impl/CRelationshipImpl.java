package org.fiware.ngsi_ld.impl;

import org.fiware.ngsi_ld.CRelationship;

import java.net.URI;

/**
 *
 *   CRelationship implementation
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class CRelationshipImpl extends CObjectImpl implements CRelationship {
    private String relationshipId;
    private String object;

    public CRelationshipImpl(String relationshipId, URI object) {
        this.relationshipId = relationshipId;
        this.object = object.toString();
    }

    public CRelationshipImpl(String relationshipId, String object) {
        this.relationshipId = relationshipId;
        this.object = object;
    }

    @Override
    public String getRelationshipId() {
        return relationshipId;
    }

    @Override
    public URI getObject() {
        return URI.create(object);
    }

    public String getObjectStr() {
        return object;
    }
}
