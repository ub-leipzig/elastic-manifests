/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ubleipzig.elastic.manifests.templates;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticResponse {

    @JsonProperty
    private Integer took;

    @JsonProperty
    private Boolean timed_out;

    @JsonProperty
    private Object _shards;

    @JsonProperty
    private ElasticHits hits;

    /**
     *
     * @return ElasticHits
     */
    public ElasticHits getHits() {
        return hits;
    }

    /**
     *
     * @param hits ElasticHits
     */
    public void setHits(final ElasticHits hits) {
        this.hits = hits;
    }
}
