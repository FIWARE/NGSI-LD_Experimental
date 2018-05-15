package org.fiware.ngsi_ld;

import java.util.Map;

/**
 *
 *   NGSI-LD Entity
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public interface CEntity extends CObject {
    public String getId();
    public String getType();

    public Map<String,String> jsonLdContext();
    public void jsonLdContext(Map<String,String> context);
}
