package com.github.sannies.nexusaptplugin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0    
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IndexerField;
import org.apache.maven.index.IndexerFieldVersion;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.locator.Md5Locator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import com.github.sannies.nexusaptplugin.deb.DebControlParser;
import com.github.sannies.nexusaptplugin.deb.GetControl;


/**
 * A Maven Archetype index creator used to detect and correct the artifact packaging to "maven-archetype" if the
 * inspected JAR is an Archetype. Since packaging is already handled by Minimal creator, this Creator only alters the
 * supplied ArtifactInfo packaging field during processing, but does not interferes with Lucene document fill-up or the
 * ArtifactInfo fill-up (the update* methods are empty).
 *
 * @author cstamas
 */
@Component(role = IndexCreator.class, hint = DebianIndexCreator.ID)
public class DebianIndexCreator
        extends AbstractIndexCreator {
    Md5Locator md5Locator = new Md5Locator();

    public static final String ID = "debian-package";

    public static final IndexerField PACKAGE = new IndexerField(DEBIAN.PACKAGE, IndexerFieldVersion.V1, "deb_package",
            DEBIAN.PACKAGE.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField ARCHITECTURE = new IndexerField(DEBIAN.ARCHITECTURE, IndexerFieldVersion.V1, "deb_architecture",
            DEBIAN.ARCHITECTURE.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField INSTALLED_SIZE = new IndexerField(DEBIAN.INSTALLED_SIZE, IndexerFieldVersion.V1, "deb_installed_size",
            DEBIAN.INSTALLED_SIZE.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField MAINTAINER = new IndexerField(DEBIAN.MAINTAINER, IndexerFieldVersion.V1, "deb_maintainer",
            DEBIAN.MAINTAINER.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField VERSION = new IndexerField(DEBIAN.VERSION, IndexerFieldVersion.V1, "deb_version",
            DEBIAN.VERSION.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField DEPENDS = new IndexerField(DEBIAN.DEPENDS, IndexerFieldVersion.V1, "deb_depends",
            DEBIAN.DEPENDS.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField SECTION = new IndexerField(DEBIAN.SECTION, IndexerFieldVersion.V1, "deb_section",
            DEBIAN.SECTION.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField PRIORITY = new IndexerField(DEBIAN.PRIORITY, IndexerFieldVersion.V1, "deb_priority",
            DEBIAN.PRIORITY.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField DESCRIPTION = new IndexerField(DEBIAN.DESCRIPTION, IndexerFieldVersion.V1, "deb_description",
            DEBIAN.DESCRIPTION.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField MD5 = new IndexerField(DEBIAN.MD5, IndexerFieldVersion.V1, "deb_md5",
            DEBIAN.MD5.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField FILENAME = new IndexerField(DEBIAN.FILENAME, IndexerFieldVersion.V1, "deb_filename",
            DEBIAN.FILENAME.getDescription(), Field.Store.YES, Field.Index.NO);


    public DebianIndexCreator() {
        super(ID, Arrays.asList(MinimalArtifactInfoIndexCreator.ID));
    }

    public void populateArtifactInfo(ArtifactContext ac) throws IOException {
        if (ac.getArtifact() != null && "deb".equals(ac.getArtifactInfo().packaging)) {
            List<String> control = GetControl.doGet(ac.getArtifact());
            ac.getArtifactInfo().getAttributes().putAll(DebControlParser.parse(control));
            ac.getArtifactInfo().getAttributes().put("Filename", getRelativeFileNameOfArtifact(ac));
            File md5 = md5Locator.locate(ac.getArtifact());
            if (md5.exists()) {
                try {
                    ac.getArtifactInfo().md5 = StringUtils.chomp(FileUtils.fileRead(md5)).trim().split(" ")[0];
                } catch (IOException e) {
                    ac.addError(e);
                }
            }
        }
    }

    private String getRelativeFileNameOfArtifact(ArtifactContext ac) {
        return "./" + ac.getArtifactInfo().groupId.replace(".", "/") + "/" + ac.getArtifactInfo().artifactId + "/" + ac.getArtifactInfo().version + "/" + ac.getArtifactInfo().fname;
    }


    public void updateDocument(ArtifactInfo ai, Document doc) {
        if ("deb".equals(ai.packaging)) {
            if (ai.getAttributes().get(PACKAGE.getOntology().getFieldName()) != null) {
                doc.add(PACKAGE.toField(ai.getAttributes().get(PACKAGE.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(ARCHITECTURE.getOntology().getFieldName()) != null) {
                doc.add(ARCHITECTURE.toField(ai.getAttributes().get(ARCHITECTURE.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(INSTALLED_SIZE.getOntology().getFieldName()) != null) {
                doc.add(INSTALLED_SIZE.toField(ai.getAttributes().get(INSTALLED_SIZE.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(MAINTAINER.getOntology().getFieldName()) != null) {
                doc.add(MAINTAINER.toField(ai.getAttributes().get(MAINTAINER.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(VERSION.getOntology().getFieldName()) != null) {
                doc.add(VERSION.toField(ai.getAttributes().get(VERSION.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(DEPENDS.getOntology().getFieldName()) != null) {
                doc.add(DEPENDS.toField(ai.getAttributes().get(DEPENDS.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(SECTION.getOntology().getFieldName()) != null) {
                doc.add(SECTION.toField(ai.getAttributes().get(SECTION.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(PRIORITY.getOntology().getFieldName()) != null) {
                doc.add(PRIORITY.toField(ai.getAttributes().get(PRIORITY.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(DESCRIPTION.getOntology().getFieldName()) != null) {
                doc.add(DESCRIPTION.toField(ai.getAttributes().get(DESCRIPTION.getOntology().getFieldName())));
            }
            if (ai.getAttributes().get(FILENAME.getOntology().getFieldName()) != null) {
                doc.add(FILENAME.toField(ai.getAttributes().get(FILENAME.getOntology().getFieldName())));
            }
            if (ai.md5 != null) {
                doc.add(MD5.toField(ai.md5));
            }
        }
    }

    public boolean updateArtifactInfo(Document doc, ArtifactInfo ai) {
        String filename = doc.get(FILENAME.getKey());
        if(filename != null && filename.endsWith(".deb")) {

            ai.getAttributes().put(PACKAGE.getOntology().getFieldName(), doc.get(PACKAGE.getKey()));
            ai.getAttributes().put(ARCHITECTURE.getOntology().getFieldName(), doc.get(ARCHITECTURE.getKey()));
            ai.getAttributes().put(INSTALLED_SIZE.getOntology().getFieldName(), doc.get(INSTALLED_SIZE.getKey()));
            ai.getAttributes().put(MAINTAINER.getOntology().getFieldName(), doc.get(MAINTAINER.getKey()));
            ai.getAttributes().put(VERSION.getOntology().getFieldName(), doc.get(VERSION.getKey()));
            ai.getAttributes().put(DEPENDS.getOntology().getFieldName(), doc.get(DEPENDS.getKey()));
            ai.getAttributes().put(SECTION.getOntology().getFieldName(), doc.get(SECTION.getKey()));
            ai.getAttributes().put(PRIORITY.getOntology().getFieldName(), doc.get(PRIORITY.getKey()));
            ai.getAttributes().put(DESCRIPTION.getOntology().getFieldName(), doc.get(DESCRIPTION.getKey()));
            ai.getAttributes().put(FILENAME.getOntology().getFieldName(), doc.get(FILENAME.getKey()));
            ai.md5 = doc.get(MD5.getKey());
            return true;
        }
        return false;
    }

    // ==

    @Override
    public String toString() {
        return ID;
    }

    public Collection<IndexerField> getIndexerFields() {
        // it does not "add" any new field, it actually updates those already maintained by minimal creator.
        return Collections.emptyList();
    }
}
