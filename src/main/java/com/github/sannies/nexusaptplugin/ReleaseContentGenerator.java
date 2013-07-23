package com.github.sannies.nexusaptplugin;


import javax.inject.Named;

/**
 * @author Raniz
 */
@Named(ReleaseContentGenerator.ID)
public class ReleaseContentGenerator
	extends AbstractContentGenerator {
    public static final String ID = "ReleaseContentGenerator";


	public ReleaseContentGenerator()
	{
		super("application/text", "Release");
	}

    @Override
    public String getGeneratorId() {
        return ID;
    }
}