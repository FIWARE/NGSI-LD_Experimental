package org.fiware.ngsi_ld.impl;


import org.fiware.ngsi_ld.CObject;
import org.fiware.ngsi_ld.CProperty;
import org.fiware.ngsi_ld.CRelationship;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *   NGSI-LD Object implementation
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public abstract class CObjectImpl implements CObject {
    // observedAt
    private String timestamp;
    private String createdAt;
    private String modifiedAt;

    // Properties map
    private Map<String, CProperty> properties = new HashMap<String, CProperty>();
    private Map<String, CRelationship> relationships = new HashMap<String, CRelationship>();

    public CObjectImpl() {

    }

    @Override
    public CProperty getProperty(String id) {
        return properties.get(id);
    }

    @Override
    public CRelationship getRelationship(String id) {
        return relationships.get(id);
    }

    @Override
    public Object getPropertyValue(String id) {
        Object out = null;

        CProperty propertySt = properties.get(id);

        if (propertySt != null) {
            out = propertySt.getValue();
        }

        return out;
    }

    @Override
    public URI getRelationshipObject(String id) {
        URI out = null;

        CRelationship relationshipSt = relationships.get(id);

        if (relationshipSt != null) {
            out = relationshipSt.getObject();
        }

        return out;
    }

    @Override
    public void addProperty(CProperty prop) {
        properties.put(prop.getPropertyId(), prop);
    }

    @Override
    public void addRelationship(CRelationship rel) {
        relationships.put(rel.getRelationshipId(), rel);
    }

    @Override
    public Map<String,CProperty> getProperties() {
        return properties;
    }

    @Override
    public Map<String,CRelationship> getRelationships() {
        return relationships;
    }

    @Override
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public void setCreatedAt(String timestamp) {
        this.createdAt = timestamp;
    }

    @Override
    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setModifiedAt(String timestamp) {
        this.modifiedAt = timestamp;
    }

    @Override
    public String getModifiedAt() {
        return modifiedAt;
    }
}
