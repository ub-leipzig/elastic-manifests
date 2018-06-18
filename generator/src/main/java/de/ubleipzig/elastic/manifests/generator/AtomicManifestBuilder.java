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
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import de.ubleipzig.elastic.manifests.templates.AtomicElasticResponse;
import de.ubleipzig.elastic.manifests.templates.Body;
import de.ubleipzig.elastic.manifests.templates.Canvas;
import de.ubleipzig.elastic.manifests.templates.ImageServiceResponse;
import de.ubleipzig.elastic.manifests.templates.Manifest;
import de.ubleipzig.elastic.manifests.templates.Metadata;
import de.ubleipzig.elastic.manifests.templates.PaintingAnnotation;
import de.ubleipzig.elastic.manifests.templates.Sequence;
import de.ubleipzig.elastic.manifests.templates.Service;
import de.ubleipzig.elastic.manifests.templates.Structure;
import de.ubleipzig.iiif.vocabulary.IIIFEnum;
import de.ubleipzig.iiif.vocabulary.SC;
import de.ubleipzig.iiif.vocabulary.SCEnum;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * @param metadata  List
     * @param sequences List
     * @param structures List
     * @return Manifest
     */
    public Manifest getManifest(final List<Sequence> sequences, final List<Metadata> metadata, final List<Structure>
            structures) {
        final String id = trellisManifestBase + UUID.randomUUID();
        final Optional<Metadata> titleObject = metadata.stream().filter(m -> m.getLabel().equals("Title")).findFirst();
        final Manifest manifest = new Manifest();
        if (titleObject.isPresent()) {
            final String title = titleObject.get().getValue();
            manifest.setLabel(title);
        }
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
        structures.sort(comparing(Structure::getStructureId));
        manifest.setStructures(structures);
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
        final Map<String, String> structuresMap = new HashMap<>();
        final List<Structure> structureList = new ArrayList<>();

        if (!hits.isEmpty()) {
            final List<Metadata> md = buildMetadataFromFirstHit(hits.get(0).getSource().getMetadata());
            hits.forEach(h -> {
                final Integer index = h.getSource().getImageIndex();
                final String imageService = h.getSource().getIiifService();
                final Map<Integer, Structure> structureMap = h.getSource().getStructureMap();

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

                //map canvas to structure
                if (!structureMap.isEmpty()) {
                    structuresMap.put(trellisTargetBase + index.toString(), structureMap.get(1).getStructureId());
                    //create list of structures
                    structureList.add(structureMap.get(1));
                }
            });
            canvases.sort(comparing(Canvas::getLabel));

            //build structures
            final List<Structure> dedupStructures = structureList.stream().collect(
                    collectingAndThen(toCollection(() -> new TreeSet<>(comparing(Structure::getStructureId))),
                            ArrayList::new));
            final List<Structure> structures = new ArrayList<>();
            dedupStructures.forEach(s -> {
                if (s != null) {
                    final List<String> canvasesList = getKeysByValue(structuresMap, s.getStructureId());
                    canvasesList.sort(Comparator.naturalOrder());
                    final Structure structure = new Structure();
                    structure.setStructureId(s.getStructureId());
                    structure.setStructureLabel(s.getStructureLabel());
                    structure.setCanvases(canvasesList);
                    structure.setStructureType(SCEnum.Range.compactedIRI());
                    structures.add(structure);
                }
            });

            final List<Sequence> sequences = getSequence(canvases);
            final Manifest manifest = getManifest(sequences, md, structures);
            final Optional<String> json = serialize(manifest);
            return json.orElse(null);
        } else {
            throw new RuntimeException("no hits for query, manifest builder process stopped");
        }
    }

    private static <T, V> List<T> getKeysByValue(final Map<T, V> map, final V value) {
        return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(
                Map.Entry::getKey).collect(Collectors.toList());
    }
}
