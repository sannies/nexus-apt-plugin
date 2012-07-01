package com.github.sannies.nexusaptplugin;

import org.apache.lucene.search.Query;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.proxy.item.ContentLocator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    private volatile String payload;

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
        if (true/*payload == null*/) {
            Query pq = indexer.constructQuery( MAVEN.PACKAGING, "deb", SearchType.EXACT );

            // to have sorted results by version in descending order
            IteratorSearchRequest sreq = new IteratorSearchRequest( pq, indexingContext );



            IteratorSearchResponse hits = indexer.searchIterator( sreq );
            payload = "";
            for (ArtifactInfo hit : hits) {
                payload += hit.toString() + "\n";
            }


        }

        return new ByteArrayInputStream(payload.getBytes());
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public boolean isReusable() {
        return true;
    }
}