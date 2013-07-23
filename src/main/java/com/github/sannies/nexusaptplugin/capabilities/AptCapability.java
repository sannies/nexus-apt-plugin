package com.github.sannies.nexusaptplugin.capabilities;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;

import com.github.sannies.nexusaptplugin.sign.AptSigningConfiguration;

@Named( AptCapabilityDescriptor.TYPE_ID )
public class AptCapability
    extends CapabilitySupport
{
	private final AptSigningConfiguration signingConfiguration;

    private AptCapabilityConfiguration configuration;

    @Inject
    public AptCapability(AptSigningConfiguration signingConfiguration) {
    	this.signingConfiguration = signingConfiguration;
    }

    @Override
    public void onCreate() throws Exception {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onLoad() throws Exception {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onUpdate() throws Exception {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onRemove() throws Exception {
        configuration = null;
    }

    @Override
    public void onActivate() {
    	signingConfiguration.setKeyring(configuration.getKeyring());
    	signingConfiguration.setKey(configuration.getKey());
    	signingConfiguration.setPassphrase(configuration.getPassphrase());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    AptCapabilityConfiguration createConfiguration( final Map<String, String> properties ) {
        return new AptCapabilityConfiguration( properties );
    }

}
