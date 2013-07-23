package com.github.sannies.nexusaptplugin;


import javax.inject.Named;

/**
 * @author Raniz
 */
@Named(ReleaseGPGContentGenerator.ID)
public class ReleaseGPGContentGenerator
	extends AbstractContentGenerator {
    public static final String ID = "ReleaseGPGContentGenerator";


	public ReleaseGPGContentGenerator()
	{
		super("application/text", "Release.gpg");
	}

    @Override
    public String getGeneratorId() {
        return ID;
    }
}