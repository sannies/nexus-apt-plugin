package com.github.sannies.nexusaptplugin;


import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.DefaultIndexerManager;
import org.sonatype.nexus.index.IndexArtifactFilter;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Archetype catalog content generator.
 *
 * @author cstamas
 */
@Named(PackagesContentGenerator.ID)
public class PackagesContentGenerator
        implements ContentGenerator {
    public static final String ID = "PackagesGzContentGenerator";

    @Inject
    private IndexerManager indexerManager;

    @Inject
    private IndexArtifactFilter indexArtifactFilter;

    @Requirement
    private NexusIndexer indexer;

    @Override
    public String getGeneratorId() {
        return ID;
    }

    @Override
    public ContentLocator generateContent(Repository repository, String path, StorageFileItem item)
            throws IllegalOperationException, ItemNotFoundException, LocalStorageException {
        // make length unknown (since it will be known only in the moment of actual content pull)
        item.setLength(-1);


        return new PackagesContentLocator(repository.getId(),
                ((DefaultIndexerManager) indexerManager).getRepositoryIndexContext(repository),
                new ArtifactInfoFilter() {
                    public boolean accepts(IndexingContext ctx, ArtifactInfo ai) {
                        return indexArtifactFilter.filterArtifactInfo(ai);
                    }
                }, indexer);
    }
}