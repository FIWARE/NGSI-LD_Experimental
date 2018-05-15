package org.fiware.ngsi_ld.comp;

import org.fiware.JsonUtilities;
import org.fiware.ngsi.NgsiTerms;
import org.fiware.ngsi_ld.*;
import org.fiware.ngsi_ld.impl.*;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Transforms NGSI data to NGSI-LD
 * <p>
 * Copyright (c) 2017-2018 FIWARE Foundation e.V.
 * <p>
 * LICENSE: MIT
 */
public class Ngsi2NGSILD {

    /**
     * Converts a Json normalized representation of NGSIv2 to a NGSI-LD Entity
     *
     * @param obj
     * @return
     */
    public static CEntity toNGSILD(JsonObject obj) {
        String id = obj.getString("id");
        String type = obj.getString("type");

        CEntity ent = new EntityImpl(id, type);

        if (obj.containsKey(NgsiTerms.DATE_CREATED)) {
            JsonValue value = obj.get(NgsiTerms.DATE_CREATED);
            ent.setCreatedAt(value.asJsonObject().getString("value"));
        }

        if (obj.containsKey(NgsiTerms.DATE_MODIFIED)) {
            JsonValue value = obj.get(NgsiTerms.DATE_MODIFIED);
            ent.setModifiedAt(value.asJsonObject().getString("value"));
        }

        for (String key : obj.keySet()) {
            if (key.equals(Vocabulary.ID) || key.equals(Vocabulary.TYPE)
                    || key.equals(NgsiTerms.DATE_CREATED) || key.equals(NgsiTerms.DATE_MODIFIED)) {
                continue;
            }

            JsonValue value;
            JsonObject ngsiStructure = null;
            String attrType = null;
            String timestamp = null;


            ngsiStructure = obj.getJsonObject(key);
            value = ngsiStructure.get(Vocabulary.VALUE);
            attrType = ngsiStructure.getString(Vocabulary.TYPE);

            CObject c3imObj;
            if (attrType != null && !attrType.equals("Reference")) {
                if (attrType.equals("geo:json")) {
                    c3imObj = new GeoPropertyImpl(key, value);
                    ent.addProperty((CProperty) c3imObj);
                } else {
                    c3imObj = new CPropertyImpl(key, value);
                    ent.addProperty((CProperty) c3imObj);
                }
            } else {
                String valStr = ngsiStructure.getString("value");
                System.out.println(valStr);
                c3imObj = new CRelationshipImpl(key, valStr);
                ent.addRelationship((CRelationship) c3imObj);
            }

            JsonObject metadata = ngsiStructure.getJsonObject("metadata");
            if (metadata != null) {
                for (String mKey : metadata.keySet()) {
                    JsonObject metadataStructure = metadata.getJsonObject(mKey);
                    if (mKey.equals("timestamp") || mKey.equals(Vocabulary.OBSERVED_AT)) {
                        c3imObj.setTimestamp(metadataStructure.getString("value"));
                        continue;
                    }
                    CProperty metaPropertySt = new CPropertyImpl(mKey, metadataStructure.get("value"));
                    c3imObj.addProperty(metaPropertySt);
                }
            }
        }


        return ent;
    }

    /**
     *
     *   Converts a simplified representation in NGSI to NGSI-LD
     *
     *   This is basically needed to translate dateCreated / dateModified
     *
     *
     * @param obj
     * @return
     */
    public static JsonObject toNGSILDKeyValues(JsonObject obj) {
        String id = obj.getString("id");
        String type = obj.getString("type");

        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add(Vocabulary.ID, id);
        builder.add(Vocabulary.TYPE, type);

        if (obj.containsKey(NgsiTerms.DATE_CREATED)) {
            builder.add(Vocabulary.CREATED_AT, obj.get(NgsiTerms.DATE_CREATED));
        }

        if (obj.containsKey(NgsiTerms.DATE_MODIFIED)) {
            builder.add(Vocabulary.MODIFIED_AT, obj.get(NgsiTerms.DATE_MODIFIED));
        }

        for (String key : obj.keySet()) {
            if (key.equals(Vocabulary.ID) || key.equals(Vocabulary.TYPE)
                    || key.equals(NgsiTerms.DATE_CREATED) || key.equals(NgsiTerms.DATE_MODIFIED)) {
                continue;
            }

            builder.add(key, obj.get(key));
        }

        return builder.build();
    }


    /**
     * Converts a NGSI-LD Entity to a normalized NGSIv2 JSON representation
     *
     * @param ent
     * @return
     */
    public static JsonObject toNgsi(CEntity ent) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", ent.getId());
        builder.add("type", ent.getType());

        Map<String, CProperty> properties = ent.getProperties();

        for (CProperty prop : properties.values()) {
            JsonObjectBuilder valueBuilder = Json.createObjectBuilder();
            String attrName = prop.getPropertyId();
            JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();

            if (prop.getTimestamp() != null) {
                JsonObjectBuilder timestampValueBuilder = Json.createObjectBuilder();
                timestampValueBuilder.add(Vocabulary.TYPE, "DateTime");
                timestampValueBuilder.add(Vocabulary.VALUE, prop.getTimestamp());
                metadataBuilder.add("timestamp", timestampValueBuilder.build());
            }

            Map<String, CProperty> propsOfProps = prop.getProperties();
            addProperties(propsOfProps, metadataBuilder);

            Map<String, CRelationship> relsOfProps = prop.getRelationships();
            addRelationships(relsOfProps, metadataBuilder);

            valueBuilder.add("metadata", metadataBuilder.build());
            builder.add(attrName, toNgsiAttr(valueBuilder, prop));
        }

        Map<String, CRelationship> rels = ent.getRelationships();

        for (CRelationship rel : rels.values()) {
            JsonObjectBuilder valueBuilder = Json.createObjectBuilder();
            String attrName = rel.getRelationshipId();
            JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();

            Map<String, CProperty> propsOfRels = rel.getProperties();
            addProperties(propsOfRels, metadataBuilder);

            Map<String, CRelationship> relsOfRels = rel.getRelationships();
            addRelationships(relsOfRels, metadataBuilder);

            valueBuilder.add("metadata", metadataBuilder.build());
            builder.add(attrName, toNgsiAttr(valueBuilder, rel));
        }

        return builder.build();
    }

    private static void addProperties(Map<String, CProperty> props, JsonObjectBuilder builder) {
        for (CProperty prop : props.values()) {
            JsonObjectBuilder metadataValueBuilder = Json.createObjectBuilder();
            String metadataName = prop.getPropertyId();
            builder.add(metadataName, toNgsiAttr(metadataValueBuilder, prop));
        }
    }

    private static void addRelationships(Map<String, CRelationship> rels, JsonObjectBuilder builder) {
        for (CRelationship relOfProp : rels.values()) {
            JsonObjectBuilder metadataValueBuilder = Json.createObjectBuilder();
            String metadataName = relOfProp.getRelationshipId();
            builder.add(metadataName, toNgsiAttr(metadataValueBuilder, relOfProp));
        }
    }

    private static JsonObject toNgsiAttr(JsonObjectBuilder builder, CProperty prop) {
        Object value = prop.getValue();
        JsonUtilities.addValue(builder, "value", value);

        if (prop.getDataType() != null) {
            builder.add("type", prop.getDataType());
        }

        // TODO: Do this with the JSON-LD context
        if (prop instanceof GeoProperty) {
            builder.add("type", "geo:json");
        }

        return builder.build();
    }

    private static JsonObject toNgsiAttr(JsonObjectBuilder builder, CRelationship rel) {
        String object = rel.getObject().toString();
        JsonUtilities.addValue(builder, "value", object);

        builder.add("type", "Reference");

        return builder.build();
    }


    /**
     *
     *  TODO: Move this to a unit test
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        CEntity ent = new EntityImpl("urn:ngsi-ld:Test:abcde", "Test");

        CProperty propSt = new CPropertyImpl("testProperty", 45);
        propSt.setTimestamp("2017-10-10T12:00:00");

        CRelationship relSt = new CRelationshipImpl("testRel", "urn:ngsi-ld:Test2:abcdef");
        ent.addRelationship(relSt);
        ent.addProperty(propSt);

        CRelationship relst2 = new CRelationshipImpl("relOfProp",
                "urn:ngsi-ld:Test3:xxxxx");

        propSt.addProperty(new CPropertyImpl("observedAt",
                "2017-10-22T12:00:00", "DateTime"));
        propSt.addRelationship(relst2);

        relSt.addProperty(new CPropertyImpl("propOfRel", "TestValue"));
        relSt.addProperty(new CPropertyImpl("observedAt",
                "2017-10-22T12:00:00", "DateTime"));
        propSt.addRelationship(relst2);

        JsonObject obj = Ngsi2NGSILD.toNgsi(ent);

        JsonWriter writer = Json.createWriterFactory(
                Collections.singletonMap(
                        JsonGenerator.PRETTY_PRINTING,
                        true
                )
        ).createWriter(System.out);

        writer.writeObject(obj);
    }
}
