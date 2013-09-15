package com.github.sannies.nexusaptplugin.capabilities;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityBooterSupport;

@Named
@Singleton
public class AptCapabilitiesBooter
    extends CapabilityBooterSupport
{

    @Override
    protected void boot( final CapabilityRegistry registry ) throws Exception {
        maybeAddCapability(
            registry,
            AptCapabilityDescriptor.TYPE,
            true, // enabled
            null, // no notes
            new AptCapabilityConfiguration().asMap()
        );
    }

}
