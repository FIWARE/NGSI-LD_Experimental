package org.fiware.ngsi_ld.impl;

import org.fiware.ngsi_ld.CEntity;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *   NGSI-LD Entity Implementation
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class EntityImpl extends CObjectImpl implements CEntity {
    // Entity id
    private String id;
    // Entity type
    private String type;

    // JSON-LD Context
    private Map context = new HashMap<String, String>();

    public EntityImpl(String id, String type, Map<String,String> ctx) {
        this.id = id;
        this.type = type;
        this.context = ctx;
    }

    public EntityImpl(String id, String type) {
        this(id, type, null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String,String> jsonLdContext() {
        return context;
    }

    public void jsonLdContext(Map<String,String> context) {
        this.context = context;
    }
}
