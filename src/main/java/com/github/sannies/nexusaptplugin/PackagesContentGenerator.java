package com.github.sannies.nexusaptplugin;


import javax.inject.Named;

/**
 * @author Raniz
 */
@Named(PackagesContentGenerator.ID)
public class PackagesContentGenerator
	extends AbstractContentGenerator {
    public static final String ID = "PackagesContentGenerator";


	public PackagesContentGenerator()
	{
		super("application/text", "Packages");
	}

    @Override
    public String getGeneratorId() {
        return ID;
    }
}