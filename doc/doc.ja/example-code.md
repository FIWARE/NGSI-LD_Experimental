# JSON-LD 表現

## Vehicle Entity (車両エンティティ)

```
{
 	"id": "urn:ngsi-ld:Vehicle:A4567",
  	"type": "Vehicle",
 	"brandName": {
   		"type": "Property",
		"value": "Mercedes"
	},
 	"isParked": {
   		"type": "Relationship",
   		"object": "urn:ngsi-ld:OffStreetParking:Downtown1",
   		"observedAt": "2017-07-29T12:00:04",
   		"providedBy": {
        		"type": "Relationship",
        		"object": "urn:ngsi-ld:Person:Bob"
     		} 
 	},
	"@context": [
		"http://uri.etsi.org/ngsi-ld/coreContext.jsonld",
		"http://example.org/cim/commonTerms.jsonld",
		"http://example.org/cim/vehicle.jsonld", 
		"http://example.org/cim/parking.jsonld" 
	]
}
``` 

## Parking Entity (パーキング・エンティティ)

```
{
  	"id": "urn:ngsi-ld:OffStreetParking:Downtown1",
  	"type": "OffStreetParking",
	"name": {
		"type": "Property",
    		"value": "Downtown One"
	},
  	"availableSpotNumber": {
    		"type": "Property",
    		"value": 121,
		"observedAt": "2017-07-29T12:05:02",
    		"reliability": {
      			"type": "Property",
      			"value": 0.7
    		},
    		"providedBy": {
       			"type": "Relationship",
       			"object": "urn:ngsi-ld:Camera:C1"
    		}
	},
	"totalSpotNumber": {
    		"type": "Property",
    		"value": 200
	},
	"location": {
		"type": "GeoProperty",
		"value": {
			"type": "Point",
			"coordinates": [-8.5, 41.2]
		}
	},
	"@context": [
		"http://uri.etsi.org/ngsi-ld/coreContext.jsonld",
		"http://example.org/cim/parking.jsonld"
	]
}
```
