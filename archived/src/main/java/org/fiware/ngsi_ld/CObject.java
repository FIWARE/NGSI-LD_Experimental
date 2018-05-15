package org.fiware.ngsi_ld;

import java.net.URI;
import java.util.Map;

/**
 *
 *   CObject
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public interface CObject {
    public CProperty getProperty(String id);

    public Object getPropertyValue(String id);

    public CRelationship getRelationship(String id);

    public URI getRelationshipObject(String id);

    public void addProperty(CProperty prop);

    public void addRelationship(CRelationship rel);

    public Map<String,CRelationship> getRelationships();

    public Map<String,CProperty> getProperties();

    // observedAt
    public void setTimestamp(String timestamp);

    public void setCreatedAt(String timestamp);

    public void setModifiedAt(String timestamp);

    public String getTimestamp();

    public String getCreatedAt();

    public String getModifiedAt();
}
