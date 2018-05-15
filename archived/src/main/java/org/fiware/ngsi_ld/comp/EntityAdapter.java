package org.fiware.ngsi_ld.comp;

import org.fiware.UrnValidator;
import org.fiware.ngsi_ld.CProperty;
import org.fiware.ngsi_ld.CRelationship;
import org.fiware.ngsi_ld.GeoProperty;
import org.fiware.ngsi_ld.impl.*;

import javax.json.*;
import javax.json.bind.adapter.JsonbAdapter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 *
 *   NGSI-LD Entity adapter to JSON
 *
 *   Copyright (c) 2017 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class EntityAdapter implements JsonbAdapter<EntityImpl, JsonObject> {
    @Override
    public JsonObject adaptToJson(EntityImpl e) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        String id = e.getId();
        if (!UrnValidator.isValid(id)) {
            id = "urn" + ":" + "ngsi-ld" + ":" + e.getType() + ":" + e.getId();
        }

        builder.add("id", id);
        builder.add("type", e.getType());

        if (e.getModifiedAt() != null) {
            builder.add(Vocabulary.MODIFIED_AT, e.getModifiedAt());
        }

        if (e.getCreatedAt() != null) {
            builder.add(Vocabulary.CREATED_AT, e.getCreatedAt());
        }

        // Iterate over the properties of the C3IM entity
        Map<String,CProperty> props = e.getProperties();

        Set<String> keys = props.keySet();
        for (String key:keys) {
            CProperty prop = props.get(key);

            adaptPropertyToJson(builder,key, prop);
        }

        // Iterate over the relationships of the C3IM entity
        Map<String,CRelationship> rels = e.getRelationships();

        Set<String> relKeys = rels.keySet();
        for (String relKey:relKeys) {
            CRelationship rel = rels.get(relKey);

            adaptRelationshipToJson(builder,relKey, rel);
        }

        return builder.build();
    }

    @Override
    public EntityImpl adaptFromJson(JsonObject adapted) throws Exception {
        EntityImpl e = new EntityImpl(
                adapted.getString("id"),
                adapted.getString("type")
        );

        for (String key : adapted.keySet()) {
            if(!key.equals("id") && !key.equals("type")) {
                JsonValue val = adapted.get(key);
                String type = val.getValueType().name();
                if (!type.equals("OBJECT")) {
                    throw new Exception("400");
                }
                else {
                    JsonObject obj = val.asJsonObject();
                    String objType = obj.getString("type");
                    if (objType.equals(Vocabulary.C_PROP)) {
                        e.addProperty(fromJsonToProperty(key, obj, null));
                    }
                    else if (objType.equals(Vocabulary.C_REL)) {
                        e.addRelationship(fromJsonToRel(key, obj));
                    }
                    else if (objType.equals(Vocabulary.GEO_PROP)) {
                        e.addProperty(fromJsonToProperty(key,obj,Vocabulary.GEO_PROP));
                    }
                    else {
                        throw new Exception("400");
                    }
                }
            }
        }

        return e;
    }

    private CProperty fromJsonToProperty(String propName, JsonObject obj, String propType) throws Exception {
        String pType = propType;

        if (pType == null) {
            pType = Vocabulary.C_PROP;
        }

        CProperty out;

        if (pType == Vocabulary.GEO_PROP) {
            out = new GeoPropertyImpl(propName, obj.get(Vocabulary.VALUE));
        }
        else {
            out = new CPropertyImpl(propName, obj.get(Vocabulary.VALUE));
        }

        String timestamp = obj.getString(Vocabulary.OBSERVED_AT, "");
        if (timestamp.length() > 0) {
            ((CPropertyImpl)out).setTimestamp(timestamp);
        }

        for(String key: obj.keySet()) {
            if (!key.equals(Vocabulary.TYPE) && !key.equals(Vocabulary.VALUE) && !key.equals(Vocabulary.OBSERVED_AT)) {
                String valueType = obj.get(key).getValueType().name();
                if (valueType.equals("OBJECT")) {
                    JsonObject keyObject = obj.get(key).asJsonObject();
                    String keyType = keyObject.getString(Vocabulary.TYPE);
                    if (keyType != null) {
                        if (keyType.equals(Vocabulary.C_PROP)) {
                            out.addProperty(fromJsonToProperty(key, keyObject, Vocabulary.C_PROP));
                        }
                        else if (keyType.equals(Vocabulary.C_REL)) {
                            out.addRelationship(fromJsonToRel(key, keyObject));
                        }
                        else {
                            CProperty pst = new CPropertyImpl(key, keyObject);
                            out.addProperty(pst);
                        }
                    }

                }
                else {
                    throw new Exception("400");
                }
            }
        }

        return out;
    }

    private CRelationship fromJsonToRel(String relName, JsonObject obj) throws Exception {
        CRelationship out = new CRelationshipImpl(relName, obj.getString(Vocabulary.OBJECT));

        for(String key: obj.keySet()) {
            if (!key.equals(Vocabulary.TYPE) && !key.equals(Vocabulary.OBJECT)) {
                String valueType = obj.get(key).getValueType().name();
                if (valueType.equals("OBJECT")) {
                    JsonObject keyObject = obj.get(key).asJsonObject();
                    String keyType = keyObject.getString(Vocabulary.TYPE);
                    if (keyType != null) {
                        if (keyType.equals(Vocabulary.C_PROP)) {
                            out.addProperty(fromJsonToProperty(key, keyObject, null));
                        }
                        else if (keyType.equals(Vocabulary.C_REL)) {
                            out.addRelationship(fromJsonToRel(key, keyObject));
                        }
                    }
                    else {
                        out.addProperty(new CPropertyImpl(key, keyObject));
                    }
                }
                else {
                    throw new Exception("400");
                }
            }
        }

        return out;
    }

    public static void main(String[] args) throws Exception {
        EntityAdapter adapter = new EntityAdapter();

        InputStream fis = new FileInputStream("/Users/jcantera/work/develop/etsi_java/simple-service/test.json");

        JsonReader reader = Json.createReader(fis);

        JsonObject obj = reader.readObject();

        reader.close();

        EntityImpl entity = adapter.adaptFromJson(obj);

        JsonObject out = adapter.adaptToJson(entity);

        JsonWriter writer = Json.createWriter(System.out);

        writer.writeObject(out);
    }

    private void adaptPropertyToJson(JsonObjectBuilder builder, String key, CProperty pst) {
        Map<String,CProperty> propsOfProp = pst.getProperties();
        Map<String,CRelationship> relsOfProp = pst.getRelationships();

        /*
        if (propsOfProp.size() == 0 && relsOfProp.size() == 0) {
            JsonUtilities.addValue(builder, key, pst.getValue());
        }
        else { */
            JsonObjectBuilder propBuilder = Json.createObjectBuilder();
            if (pst instanceof GeoProperty) {
                propBuilder.add(Vocabulary.TYPE, Vocabulary.GEO_PROP);
            }
            else {
                propBuilder.add(Vocabulary.TYPE, Vocabulary.C_PROP);
            }
            // TODO: This will not always be a JsonValue
            propBuilder.add(Vocabulary.VALUE, (JsonValue)pst.getValue());
            if (pst.getTimestamp() != null) {
                propBuilder.add(Vocabulary.OBSERVED_AT, pst.getTimestamp());
            }

            Set<String> keys = propsOfProp.keySet();
            for (String keyProp : keys) {
                CProperty newSt = propsOfProp.get(keyProp);
                adaptPropertyToJson(propBuilder, keyProp, newSt);
            }
            builder.add(key, propBuilder.build());
        /*} */
    }

    private void adaptRelationshipToJson(JsonObjectBuilder builder, String key, CRelationship relst) {
        Map<String,CRelationship> relsOfRels = relst.getRelationships();
        Map<String,CProperty> propsOfRels = relst.getProperties();
/*
        if (propsOfRels.size() == 0 && relsOfRels.size() == 0) {
            addRelObject(builder, key, relst.getObject().toString());
        }
        else {*/
            JsonObjectBuilder propBuilder = Json.createObjectBuilder();
            propBuilder.add(Vocabulary.TYPE, Vocabulary.C_REL);
            // TODO: This will not always be a JsonValue
            propBuilder.add(Vocabulary.OBJECT, relst.getObject().toString());
            Set<String> keys = propsOfRels.keySet();
            for (String keyProp : keys) {
                CProperty newSt = propsOfRels.get(keyProp);
                adaptPropertyToJson(propBuilder, keyProp, newSt);
            }
            builder.add(key, propBuilder.build());
       /* } */
    }

    private void addRelObject(JsonObjectBuilder obj, String key, String object) {
        obj.add(key, object);
    }
}
