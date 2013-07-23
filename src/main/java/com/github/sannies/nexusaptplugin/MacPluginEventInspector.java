
package com.github.sannies.nexusaptplugin;

import java.io.FileNotFoundException;

import javax.inject.Inject;
import javax.inject.Named;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

import com.github.sannies.nexusaptplugin.sign.AptSigningConfiguration;
import com.github.sannies.nexusaptplugin.sign.PGPSigner;

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
    public static final String RELEASE_ARCHETYPE_PATH = "/Release";
    public static final String RELEASE_GPG_ARCHETYPE_PATH = "/Release.gpg";
    public static final String PUBLIC_KEY_ARCHETYPE_PATH = "/apt-key.gpg.key";

    @Inject
    private Logger logger;

    @Inject
    @Named( "maven2" )
    private ContentClass maven2ContentClass;

    @Inject
    private AptSigningConfiguration signingConfiguration;

    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof RepositoryRegistryEventAdd )
        {
            RepositoryRegistryRepositoryEvent registryEvent = (RepositoryRegistryRepositoryEvent) evt;

            Repository repository = registryEvent.getRepository();

            return maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
                    && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                    || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable(
                    GroupRepository.class ) );
        }
        else if ( evt instanceof RepositoryEventLocalStatusChanged )
        {
            RepositoryEventLocalStatusChanged localStatusEvent = (RepositoryEventLocalStatusChanged) evt;

            // only if put into service
            return LocalStatus.IN_SERVICE.equals( localStatusEvent.getNewLocalStatus() );
        }
        else
        {
            return false;
        }
    }

    public void inspect( Event<?> evt )
    {
        Repository repository = null;

        if ( evt instanceof RepositoryRegistryEventAdd )
        {
            RepositoryRegistryRepositoryEvent registryEvent = (RepositoryRegistryRepositoryEvent) evt;

            repository = registryEvent.getRepository();
        }
        else if ( evt instanceof RepositoryEventLocalStatusChanged )
        {
            RepositoryEventLocalStatusChanged localStatusEvent = (RepositoryEventLocalStatusChanged) evt;

            repository = localStatusEvent.getRepository();
        }
        else
        {
            // huh?
            return;
        }

        // check is it a maven2 content, and either a "hosted", "proxy" or "group" repository
        if ( maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
                && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable(
                GroupRepository.class ) ) )
        {
            // new repo added or enabled, "install" the archetype catalogs
            try
            {
            	// Packages.gz
                DefaultStorageFileItem file =
                        new DefaultStorageFileItem( repository, new ResourceStoreRequest( PACKAGES_ARCHETYPE_PATH ), true, false,
                                new StringContentLocator( PackagesContentGenerator.ID ) );

                file.setContentGeneratorId( PackagesContentGenerator.ID );

                repository.storeItem( false, file );

                // Release
                file =
                        new DefaultStorageFileItem( repository, new ResourceStoreRequest( RELEASE_ARCHETYPE_PATH ), true, false,
                                new StringContentLocator( ReleaseContentGenerator.ID ) );

                file.setContentGeneratorId( ReleaseContentGenerator.ID );

                repository.storeItem( false, file );

                // Signing
                try
                {
                	// See if the key information is correct, if it's not we'll get some sort of exception here
	                PGPSigner signer = signingConfiguration.getSigner();
	
	                // Release.gpg
	                file =
	                        new DefaultStorageFileItem( repository, new ResourceStoreRequest( RELEASE_GPG_ARCHETYPE_PATH ), true, false,
	                                new StringContentLocator( ReleaseGPGContentGenerator.ID ) );
	
	                file.setContentGeneratorId( ReleaseGPGContentGenerator.ID );

	                repository.storeItem( false, file );

	                // apt-key.gpg.key
	                file =
	                        new DefaultStorageFileItem( repository, new ResourceStoreRequest( PUBLIC_KEY_ARCHETYPE_PATH ), true, false,
	                                new StringContentLocator( SignKeyContentGenerator.ID ) );
	
	                file.setContentGeneratorId( SignKeyContentGenerator.ID );
	
	                repository.storeItem( false, file );
                }
                catch(FileNotFoundException e)
                {
                	logger.warn("Signing information is either invalid or unset");
                }
                catch(PGPException e)
                {
                	logger.warn("Signing information is either invalid or unset");
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
}