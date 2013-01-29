package com.github.sannies.nexusaptplugin;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.lucene.search.Query;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.proxy.item.ContentLocator;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * A content locator to generate archetype catalog. This way, the actual work (search, archetype catalog model fillup
 * from results, converting it to string and flushing it as byte array backed stream) is postponed to very last moment,
 * when the content itself is asked for.
 *
 * @author cstamas
 */
public class DebianContentLocator
        implements ContentLocator {

    private final String repositoryId;

    private final IndexingContext indexingContext;

    private final ArtifactInfoFilter artifactInfoFilter;

    private NexusIndexer indexer;

    private volatile byte[] payload;

    /* Taken from http://www.debian.org/doc/manuals/debian-reference/ch02.en.html#_package_dependencies */
    private static final String[] PACKAGE_DEPENDENCIES_FIELD = new String[] {
        "Depends", "Pre-Depends", "Recommends", "Suggests", "Enhances",
        "Breaks", "Conflicts", "Replaces", "Provides"
    };

    public DebianContentLocator(String repositoryId, IndexingContext indexingContext,
                                ArtifactInfoFilter artifactInfoFilter, NexusIndexer indexer) {
        this.repositoryId = repositoryId;
        this.indexingContext = indexingContext;
        this.artifactInfoFilter = artifactInfoFilter;
        this.indexer = indexer;
    }

    @Override
    public InputStream getContent()
            throws IOException {
        if (payload == null) {
            Query pq = indexer.constructQuery(MAVEN.PACKAGING, "deb", SearchType.EXACT);

            IteratorSearchRequest sreq = new IteratorSearchRequest(pq, indexingContext);
            IteratorSearchResponse hits = indexer.searchIterator(sreq);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(new GZIPOutputStream(baos));

            for (ArtifactInfo hit : hits) {
                w.write("Package: " + hit.getAttributes().get("Package") + "\n");
                w.write("Version: " + hit.getAttributes().get("Version") + "\n");
                w.write("Architecture: " + hit.getAttributes().get("Architecture") + "\n");
                w.write("Maintainer: " + hit.getAttributes().get("Maintainer") + "\n");
                w.write("Installed-Size: " + hit.getAttributes().get("Installed-Size") + "\n");
                /* Those are not mandatory */
                for(String fieldName : PACKAGE_DEPENDENCIES_FIELD) {
                	writeIfNonEmpty(w, hit, fieldName);
                }
                w.write("Filename: " + hit.getAttributes().get("Filename") + "\n");
                w.write("Size: " + hit.size + "\n");
                w.write("MD5sum: " + hit.md5 + "\n");
                w.write("SHA1: " + hit.sha1 + "\n");
                w.write("Section: " + hit.getAttributes().get("Section") + "\n");
                w.write("Priority: " + hit.getAttributes().get("Priority") + "\n");
                w.write("Description: " + (hit.getAttributes().get("Description") != null ? (hit.getAttributes().get("Description").replace("\n", "\n ")) : "<no desc>") + "\n");
                w.write("\n");
            }
            w.close();
            payload = baos.toByteArray();

        }

        return new ByteArrayInputStream(payload);
    }

    private void writeIfNonEmpty(OutputStreamWriter w, ArtifactInfo hit, String fieldName)
        throws IOException {
        String fieldValue = hit.getAttributes().get(fieldName);
        if(fieldValue != null && !fieldValue.isEmpty()) {
            w.write(fieldName + ": " + fieldValue + "\n");
        }
    }

    @Override
    public String getMimeType() {
        return "application/x-gzip";
    }

    @Override
    public boolean isReusable() {
        return true;
    }
}
