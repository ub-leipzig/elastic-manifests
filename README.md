## Elastic Manifests

This creates dynamic IIIF manifests from elastic search result sets.

### Prerequisites
JDK10 or higher

* `--add-modules jdk.incubator.httpclient` is required to run tests

The index must contain a document that looks like this:

```json
{
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

```
The required fields are `imageIndex` and `imageServiceIRI`.

### Building

`buildtools/src/install/install-jpms.sh`

### API

The Camel Jetty API accepts the elastic search standard query object in a GET or POST request:

Example:

```
GET http://localhost:9090/generator?q=${JSON_QUERY}
```

```json
{
  "query": {
    "multi_match": {
      "query": "1801 Sommerhalbjahr",
      "type": "cross_fields",
      "operator": "and"
    }
  },
  "size": 500
}
```

OR

```
POST http://localhost:9090/generator
```