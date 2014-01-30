package com.github.sannies.nexusaptplugin.capabilities;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.capability.support.CapabilitySupport;

import com.github.sannies.nexusaptplugin.sign.AptSigningConfiguration;

@Named( AptCapabilityDescriptor.TYPE_ID )
public class AptCapability
    extends CapabilitySupport<AptCapabilityConfiguration>
{
	private final AptSigningConfiguration signingConfiguration;

    private AptCapabilityConfiguration configuration;

    @Inject
    public AptCapability(AptSigningConfiguration signingConfiguration) {
    	this.signingConfiguration = signingConfiguration;
    }

    @Override
    protected AptCapabilityConfiguration createConfig(Map<String, String> properties) throws Exception {
        return new AptCapabilityConfiguration(properties);
    }

    @Override
    protected void onCreate(AptCapabilityConfiguration config) {
        configuration = config;
    }

    @Override
    protected void onLoad(AptCapabilityConfiguration config) throws Exception {
        configuration = config;
    }

    @Override
    protected void onUpdate(AptCapabilityConfiguration config) throws Exception {
        configuration = config;
    }

    @Override
    protected void onRemove(AptCapabilityConfiguration config) throws Exception {
        configuration = config;
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
}
