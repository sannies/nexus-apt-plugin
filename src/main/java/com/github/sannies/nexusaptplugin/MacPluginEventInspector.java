
package com.github.sannies.nexusaptplugin;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
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
import org.sonatype.plexus.appevents.Event;

import com.github.sannies.nexusaptplugin.sign.AptSigningConfiguration;
import com.google.common.collect.ImmutableMap;

/**
 * EventInspector that listens to registry events, repo addition and removal, and simply "hooks" in the generated
 * Packages.gz file to their root.
 *
 * @author cstamas
 */
public class MacPluginEventInspector
        implements EventInspector
{
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

    @Inject
    private Logger logger;

    @Inject
    @Named( "maven2" )
    private ContentClass maven2ContentClass;

    @Inject
    private AptSigningConfiguration signingConfiguration;

    public boolean accepts( Event<?> evt )
    {
		Repository repository;
		if( evt instanceof RepositoryItemEventStore
			|| evt instanceof RepositoryItemEventDelete
			|| evt instanceof RepositoryEventLocalStatusChanged )
		{
			repository = ((RepositoryEvent)evt).getRepository();
		}
		else if(evt instanceof RepositoryRegistryEventAdd)
		{
			repository = ((RepositoryRegistryEventAdd)evt).getRepository();
		}
		else
		{
			// Not interested in this event type
			return false;
		}

		// Check that the repository is compatible
        if( !( maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
                && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable(
                GroupRepository.class ) ) ) )
        {
        	// Not compatible with this repository
        	return false;
        }

        // Check that the repository is in service
        if( evt instanceof RepositoryEventLocalStatusChanged
            && !LocalStatus.IN_SERVICE.equals( ((RepositoryEventLocalStatusChanged)evt).getNewLocalStatus() ) )
        {
        	// Repository is not local
        	return false;
        }

        // If we haven't returned anything yet we're interested in the event
        return true;
    }

    public void inspect( Event<?> evt )
    {
        Repository repository = null;

        if ( evt instanceof RepositoryRegistryEventAdd )
        {
            repository = ((RepositoryRegistryEventAdd)evt).getRepository();
        }
        else if ( evt instanceof RepositoryEvent )
        {
            repository = ((RepositoryEvent)evt).getRepository();
        }
        else
        {
            // huh?
            return;
        }
        
        if ( evt instanceof RepositoryRegistryEventAdd
        		|| evt instanceof RepositoryEventLocalStatusChanged )
        {
        	installArchetypeCatalog(repository);
        }
        else if ( evt instanceof RepositoryItemEvent )
        {
        	StorageItem item = ((RepositoryItemEvent) evt).getItem();
        	if( item.getName().toLowerCase().endsWith(".deb") )
        	{
        		updateFileModificationDate( repository );
        	}
        }
    }
    
    public void installArchetypeCatalog( Repository repository)
    {
        // check is it a maven2 content, and either a "hosted", "proxy" or "group" repository
        if ( maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
                && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable(
                GroupRepository.class ) ) )
        {
            // new repo added or enabled, "install" the archetype catalogs
            try
            {
            	for(Map.Entry<String, String> entry : PATHS_TO_GENERATOR_MAP.entrySet())
            	{
	            	// Packages.gz
	                DefaultStorageFileItem file =
	                        new DefaultStorageFileItem( repository,
	                        		new ResourceStoreRequest( entry.getKey() ), true, false,
	                                new StringContentLocator( entry.getValue() ) );
	                file.setContentGeneratorId( entry.getValue() );
	                repository.storeItem( false, file );
            	}
            }
            catch ( RepositoryNotAvailableException e )
            {
                logger.info( "Unable to install the generated archetype catalog, repository \""
                        + e.getRepository().getId() + "\" is out of service." );
            }
            catch ( Exception e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.info( "Unable to install the generated archetype catalog!", e );
                }
                else
                {
                    logger.info( "Unable to install the generated archetype catalog:" + e.getMessage() );
                }
            }
        }
    }
    
    /**
     * Update the modification time of all items stored by the plugin
     * 
     * @param repository The repository to update files in
     */
    public void updateFileModificationDate( Repository repository )
    {
    	for(String path : PATHS_TO_GENERATOR_MAP.keySet())
    	{
    		try
    		{
    			// Retrieve the item, update the modification time and save it
				StorageItem item = repository.retrieveItem(new ResourceStoreRequest(path));
				item.getRepositoryItemAttributes().setModified(System.currentTimeMillis());
				repository.storeItem(false, item);
			}
    		catch (StorageException e)
    		{
    			logger.error("e");
			}
    		catch (AccessDeniedException e)
    		{
    			logger.error("e");
			}
    		catch (ItemNotFoundException e)
    		{
    			logger.error("e");
			}
    		catch (IllegalOperationException e)
    		{
    			logger.error("e");
			}
    		catch (UnsupportedStorageOperationException e)
    		{
    			logger.error("e");
			}
    	}
    }
}
