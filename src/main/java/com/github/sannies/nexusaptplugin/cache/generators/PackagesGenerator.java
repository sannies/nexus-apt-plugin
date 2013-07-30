package com.github.sannies.nexusaptplugin.cache.generators;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.context.IndexingContext;

import com.github.sannies.nexusaptplugin.cache.FileGenerator;
import com.github.sannies.nexusaptplugin.cache.RepositoryData;


@Named
public class PackagesGenerator
	implements FileGenerator
{
    /* Taken from http://www.debian.org/doc/manuals/debian-reference/ch02.en.html#_package_dependencies */
    private static final String[] PACKAGE_DEPENDENCIES_FIELD = new String[] {
        "Depends", "Pre-Depends", "Recommends", "Suggests", "Enhances",
        "Breaks", "Conflicts", "Replaces", "Provides"
    };

    @Inject
	public PackagesGenerator()
	{
	}

	@Override
	public byte[] generateFile(RepositoryData data)
		throws Exception
	{
		NexusIndexer indexer = data.getIndexer();
		IndexingContext indexingContext = data.getIndexingContext();

        Query pq = indexer.constructQuery(MAVEN.PACKAGING, "deb", SearchType.EXACT);

        IteratorSearchRequest sreq = new IteratorSearchRequest(pq, indexingContext);
        IteratorSearchResponse hits = indexer.searchIterator(sreq);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(baos);

        for (ArtifactInfo hit : hits) {
            Map<String, String> attrs = hit.getAttributes();
            if(attrs.get("Package") == null || attrs.get("Version") == null
                || attrs.get("Filename") == null)
            {
                // This won't produce a real artifact, ignore it
                continue;
            }

            // Verify that this is a valid artifact
            w.write("Package: " + attrs.get("Package") + "\n");
            w.write("Version: " + attrs.get("Version") + "\n");
            w.write("Architecture: " + attrs.get("Architecture") + "\n");
            w.write("Maintainer: " + attrs.get("Maintainer") + "\n");
            w.write("Installed-Size: " + attrs.get("Installed-Size") + "\n");
            /* Those are not mandatory */
            for(String fieldName : PACKAGE_DEPENDENCIES_FIELD) {
                writeIfNonEmpty(w, hit, fieldName);
            }
            w.write("Filename: " + attrs.get("Filename") + "\n");
            w.write("Size: " + hit.size + "\n");
            w.write("MD5sum: " + hit.md5 + "\n");
            w.write("SHA1: " + hit.sha1 + "\n");
            w.write("Section: " + attrs.get("Section") + "\n");
            w.write("Priority: " + attrs.get("Priority") + "\n");
            w.write("Description: " + (attrs.get("Description") != null ? (attrs.get("Description").replace("\n", "\n ")) : "<no desc>") + "\n");
            w.write("\n");
        }
        w.close();
        return baos.toByteArray();
    }

    private void writeIfNonEmpty(OutputStreamWriter w, ArtifactInfo hit, String fieldName)
        throws IOException {
        String fieldValue = hit.getAttributes().get(fieldName);
        if(fieldValue != null && !fieldValue.isEmpty()) {
            w.write(fieldName + ": " + fieldValue + "\n");
        }
    }

}
