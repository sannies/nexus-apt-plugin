package com.github.sannies.nexusaptplugin.cache;

import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexingContext;

public class RepositoryData
{
	private final String repositoryId;

	private final IndexingContext indexingContext;

	private final ArtifactInfoFilter artifactInfoFilter;

	private final NexusIndexer indexer;

	public RepositoryData(String repositoryId, IndexingContext indexingContext,
		ArtifactInfoFilter artifactInfoFilter, NexusIndexer indexer)
	{
		this.repositoryId = repositoryId;
		this.indexingContext = indexingContext;
		this.artifactInfoFilter = artifactInfoFilter;
		this.indexer = indexer;
	}

	public String getRepositoryId()
	{
		return repositoryId;
	}

	public IndexingContext getIndexingContext()
	{
		return indexingContext;
	}

	public ArtifactInfoFilter getArtifactInfoFilter()
	{
		return artifactInfoFilter;
	}

	public NexusIndexer getIndexer()
	{
		return indexer;
	}

}