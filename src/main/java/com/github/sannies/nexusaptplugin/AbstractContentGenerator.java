package com.github.sannies.nexusaptplugin;


import javax.inject.Inject;
import javax.inject.Named;

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

import com.github.sannies.nexusaptplugin.cache.DebianFileManager;
import com.github.sannies.nexusaptplugin.cache.RepositoryData;

/**
 * @author Raniz
 */
public abstract class AbstractContentGenerator
        implements ContentGenerator {

    @Inject
    private IndexerManager indexerManager;

    @Inject
    private IndexArtifactFilter indexArtifactFilter;

    @Requirement
    private NexusIndexer indexer;

    @Inject
    private DebianFileManager fileManager;

    private final String mimeType;

    private final String fileName;

    public AbstractContentGenerator(String mimeType, String fileName)
	{
		this.mimeType = mimeType;
		this.fileName = fileName;
	}


	@Override
    public ContentLocator generateContent(Repository repository, String path, StorageFileItem item)
            throws IllegalOperationException, ItemNotFoundException, LocalStorageException {

        RepositoryData data = new RepositoryData(repository.getId(),
        	((DefaultIndexerManager)indexerManager).getRepositoryIndexContext(repository),
        	new ArtifactInfoFilter()
        	{
	        	@Override
				public boolean accepts(IndexingContext ctx, ArtifactInfo ai) {
	        		return indexArtifactFilter.filterArtifactInfo(ai);
	        	}
        	},
        	indexer);

        return new FileManagerContentLocator(fileManager, mimeType, data, fileName);
    }
}
