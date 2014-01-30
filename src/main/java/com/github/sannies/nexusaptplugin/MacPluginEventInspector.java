
package com.github.sannies.nexusaptplugin;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;

/**
 * EventInspector that listens to registry events, repo addition and removal, and simply "hooks" in the generated
 * Packages.gz file to their root.
 *
 * @author cstamas
 */
@Singleton
@Named("deb")
public class MacPluginEventInspector
        implements EventSubscriber {
    public static final String PACKAGES_ARCHETYPE_PATH = "/Packages";
    public static final String PACKAGES_GZ_ARCHETYPE_PATH = "/Packages.gz";
    public static final String RELEASE_ARCHETYPE_PATH = "/Release";
    public static final String RELEASE_GPG_ARCHETYPE_PATH = "/Release.gpg";
    public static final String PUBLIC_KEY_ARCHETYPE_PATH = "/apt-key.gpg.key";

    public static final ImmutableMap<String, String> PATHS_TO_GENERATOR_MAP =
            new ImmutableMap.Builder<String, String>()
                    .put(PACKAGES_ARCHETYPE_PATH, PackagesContentGenerator.ID)
                    .put(PACKAGES_GZ_ARCHETYPE_PATH, PackagesGzContentGenerator.ID)
                    .put(RELEASE_ARCHETYPE_PATH, ReleaseContentGenerator.ID)
                    .put(RELEASE_GPG_ARCHETYPE_PATH, ReleaseGPGContentGenerator.ID)
                    .put(PUBLIC_KEY_ARCHETYPE_PATH, SignKeyContentGenerator.ID)
                    .build();

    private final Logger logger;

    private final ContentClass maven2ContentClass;

    @Inject
    public MacPluginEventInspector(Logger logger, @Named("maven2") ContentClass maven2ContentClass) {
        this.logger = logger;
        this.maven2ContentClass = maven2ContentClass;
    }

    @Subscribe
    public void onRepositoryRegistryEventAdd(RepositoryRegistryEventAdd evt) {
        installArcheTypeCatalogIfCompatible(evt.getRepository());
    }

    @Subscribe
    public void onRepositoryEventLocalStatusChanged(RepositoryEventLocalStatusChanged evt) {
        if (!isLocalStatusInService(evt)) {
            return;
        }
        installArcheTypeCatalogIfCompatible(evt.getRepository());
    }

    private boolean isLocalStatusInService(RepositoryEventLocalStatusChanged evt) {
        return LocalStatus.IN_SERVICE.equals(evt.getNewLocalStatus());
    }

    private void installArcheTypeCatalogIfCompatible(Repository repository) {
        installArchetypeCatalog(repository);
    }

    @Subscribe
    public void onRepositoryItemEventStore(RepositoryItemEventStore evt) {
        updateDebItemInRepository(evt.getRepository(), evt.getItem());
    }

    @Subscribe
    public void onRepositoryItemEventDelete(RepositoryItemEventDelete evt) {
        updateDebItemInRepository(evt.getRepository(), evt.getItem());
    }

    private void updateDebItemInRepository(Repository repository, StorageItem item) {
        if (item.getName().toLowerCase().endsWith(".deb")) {
            updateFileModificationDate(repository);
        }
    }

    private boolean isRepositoryCompatible(Repository repository) {
        // check is it a maven2 content, and either a "hosted", "proxy" or "group" repository
        return maven2ContentClass.isCompatible(repository.getRepositoryContentClass())
                && (repository.getRepositoryKind().isFacetAvailable(HostedRepository.class)
                || repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class) || repository.getRepositoryKind().isFacetAvailable(
                GroupRepository.class));
    }

    public void installArchetypeCatalog(Repository repository) {
        if (!isRepositoryCompatible(repository)) {
            return;
        }

        // new repo added or enabled, "install" the archetype catalogs
        try {
            for (Map.Entry<String, String> entry : PATHS_TO_GENERATOR_MAP.entrySet()) {
                // Packages.gz
                DefaultStorageFileItem file =
                        new DefaultStorageFileItem(repository,
                                new ResourceStoreRequest(entry.getKey()), true, false,
                                new StringContentLocator(entry.getValue()));
                file.setContentGeneratorId(entry.getValue());
                repository.storeItem(false, file);
            }
        }
        catch (RepositoryNotAvailableException e) {
            logger.info("Unable to install the generated archetype catalog, repository \""
                    + e.getRepository().getId() + "\" is out of service.");
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.info("Unable to install the generated archetype catalog!", e);
            }
            else {
                logger.info("Unable to install the generated archetype catalog:" + e.getMessage());
            }
        }
    }

    /**
     * Update the modification time of all items stored by the plugin
     *
     * @param repository The repository to update files in
     */
    public void updateFileModificationDate(Repository repository) {
        if (!isRepositoryCompatible(repository)) {
            return;
        }

        for (String path : PATHS_TO_GENERATOR_MAP.keySet()) {
            try {
                // Retrieve the item, update the modification time and save it
                StorageItem item = repository.retrieveItem(new ResourceStoreRequest(path));
                item.getRepositoryItemAttributes().setModified(System.currentTimeMillis());
                repository.storeItem(false, item);
            }
            catch (StorageException e) {
                logger.error("e", e);
            }
            catch (AccessDeniedException e) {
                logger.error("e", e);
            }
            catch (ItemNotFoundException e) {
                logger.error("e", e);
            }
            catch (IllegalOperationException e) {
                logger.error("e", e);
            }
            catch (UnsupportedStorageOperationException e) {
                logger.error("e", e);
            }
        }
    }
}
