## Elastic Manifests

This creates dynamic IIIF manifests from elasticsearch result sets.

### Prerequisites

The index must contain a document that looks like this:

```json
{
    "_index": "vp4",
    "_type": "_doc",
    "_id": "af1a65c0-91cc-4cc5-8d5e-5143b963f797",
    "_score": 8.180616,
    "_source": {
      "imageIndex": 1079,
      "imageServiceIRI": "http://workspaces.ub.uni-leipzig.de:8182/iiif/2/08d0bd2b-f7c9-5222-a819-72bde9c940ea",
      "metadataMap": {
        "tag1": "Catalogus lectionum 1774 - 1849 (ab 1830 Catalogus Scholarum)",
        "tag2": "1774 - 1800",
        "tag3": "1799",
        "tag4": "",
        "tag5": "Sommerhalbjahr",
        "tag6": "",
        "tag7": "",
        "tag8": ""
      }
    }
}
```
The required fields are `imageIndex` and `imageServiceIRI`.

### Building

`buildtools/src/install/install-jpms.sh`