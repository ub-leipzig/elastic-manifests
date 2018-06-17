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

package de.ubleipzig.elastic.manifests.generator;

import static de.ubleipzig.elastic.manifests.generator.Constants.domainAttribution;
import static de.ubleipzig.elastic.manifests.generator.Constants.domainLogo;
import static de.ubleipzig.elastic.manifests.generator.Constants.searchService;
import static de.ubleipzig.elastic.manifests.generator.Constants.searchServiceContext;
import static de.ubleipzig.elastic.manifests.generator.Constants.searchServiceId;
import static de.ubleipzig.elastic.manifests.generator.Constants.trellisAnnotationBase;
import static de.ubleipzig.elastic.manifests.generator.Constants.trellisBodyBase;
import static de.ubleipzig.elastic.manifests.generator.Constants.trellisManifestBase;
import static de.ubleipzig.elastic.manifests.generator.Constants.trellisSequenceBase;
import static de.ubleipzig.elastic.manifests.generator.Constants.trellisTargetBase;

import de.ubleipzig.elastic.manifests.templates.AtomicElasticResponse;
import de.ubleipzig.elastic.manifests.templates.Body;
import de.ubleipzig.elastic.manifests.templates.Canvas;
import de.ubleipzig.elastic.manifests.templates.ElasticResponse;
import de.ubleipzig.elastic.manifests.templates.Hits;
import de.ubleipzig.elastic.manifests.templates.ImageServiceResponse;
import de.ubleipzig.elastic.manifests.templates.Manifest;
import de.ubleipzig.elastic.manifests.templates.Metadata;
import de.ubleipzig.elastic.manifests.templates.MetadataMap;
import de.ubleipzig.elastic.manifests.templates.PaintingAnnotation;
import de.ubleipzig.elastic.manifests.templates.Sequence;
import de.ubleipzig.elastic.manifests.templates.Service;
import de.ubleipzig.iiif.vocabulary.IIIFEnum;
import de.ubleipzig.iiif.vocabulary.SC;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AtomicManifestBuilder extends AbstractSerializer {

    private final InputStream body;

    /**
     * @param body String
     */
    public AtomicManifestBuilder(final InputStream body) {
        this.body = body;
    }

    /**
     * @param graph graph
     * @return List
     */
    public List<Sequence> getSequence(final List<Canvas> graph) {
        final String id = trellisSequenceBase + UUID.randomUUID();
        final List<Sequence> sequences = new ArrayList<>();
        final Sequence sequence = new Sequence(id, graph);
        sequence.setViewingHint("paged");
        sequences.add(sequence);
        return sequences;
    }

    /**
     * @param metadata List
     * @param sequences List
     * @return Manifest
     */
    public Manifest getManifest(final List<Sequence> sequences, final List<Metadata> metadata) {
        final String id = trellisManifestBase + UUID.randomUUID();
        final Manifest manifest = new Manifest();
        manifest.setContext(SC.CONTEXT);
        manifest.setId(id);
        manifest.setLogo(domainLogo);
        manifest.setAttribution(domainAttribution);
        manifest.setMetadata(metadata);
        manifest.setSequences(sequences);
        final Service service = new Service();
        service.setContext(searchServiceContext);
        service.setId(searchServiceId);
        service.setProfile(searchService);
        manifest.setService(service);
        return manifest;
    }

    private List<Metadata> buildMetadataFromFirstHit(final Map<String, String> metadata) {
        //buildMetadata
        final List<Metadata> md = new ArrayList<>();
        metadata.forEach((key, value) -> {
            final Metadata meta = buildMetadataObject(key, value);
            md.add(meta);
        });
        return md;
    }

    private Metadata buildMetadataObject(final String label, final String value) {
        final Metadata metadata = new Metadata();
        metadata.setLabel(label);
        metadata.setValue(value);
        return metadata;
    }

    /**
     * @return String
     */
    public String build() {
        final Deserializer deserializer = new Deserializer();
        final AtomicElasticResponse response = deserializer.mapAtomicElasticResponse(body);
        final List<AtomicElasticResponse.Hits> hits = response.getHits().getHits();
        final List<Canvas> canvases = new ArrayList<>();
        if (!hits.isEmpty()) {
            final List<Metadata> md = buildMetadataFromFirstHit(hits.get(0).getSource().getMetadata());
            hits.forEach(h -> {
                final Integer index = h.getSource().getImageIndex();
                final String imageService = h.getSource().getIiifService();

                //getDimensionsFromImageService
                InputStream is = null;
                try {
                    is = new URL(imageService + "/info.json").openStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final ImageServiceResponse ir = deserializer.mapServiceResponse(is);
                final Integer height = ir.getHeight();
                final Integer width = ir.getWidth();

                //createServiceObject
                final Service service = new Service();
                service.setContext(IIIFEnum.IMAGE_CONTEXT.IRIString());
                service.setProfile(IIIFEnum.SERVICE_PROFILE.IRIString());
                service.setId(imageService);

                //createBody
                final Body body = new Body();
                body.setService(service);
                body.setResourceHeight(height);
                body.setResourceWidth(width);
                body.setResourceType("dctypes:Image");
                body.setResourceFormat("image/jpeg");
                body.setResourceId(trellisBodyBase + index + ".jpg");

                //createAnnotation
                final List<PaintingAnnotation> annotations = new ArrayList<>();
                final PaintingAnnotation anno = new PaintingAnnotation();
                final String annoId = trellisAnnotationBase + UUID.randomUUID();
                anno.setId(annoId);
                anno.setBody(body);
                anno.setTarget(trellisTargetBase + index.toString());
                annotations.add(anno);

                //createCanvas
                final Canvas canvas = new Canvas();
                canvas.setId(trellisTargetBase + index.toString());
                canvas.setHeight(height);
                canvas.setWidth(width);
                canvas.setImages(annotations);
                canvas.setLabel(String.format("%08d", index));
                canvases.add(canvas);
            });
            canvases.sort(Comparator.comparing(Canvas::getLabel));
            final List<Sequence> sequences = getSequence(canvases);
            final Manifest manifest = getManifest(sequences, md);
            final Optional<String> json = serialize(manifest);
            return json.orElse(null);
        } else {
            throw new RuntimeException("no hits for query, manifest builder process stopped");
        }
    }
}
