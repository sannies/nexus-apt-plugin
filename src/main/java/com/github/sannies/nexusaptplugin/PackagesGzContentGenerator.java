package com.github.sannies.nexusaptplugin;


import javax.inject.Named;

/**
 * @author Raniz
 */
@Named(PackagesGzContentGenerator.ID)
public class PackagesGzContentGenerator
	extends AbstractContentGenerator {
    public static final String ID = "PackagesGzContentGenerator";


	public PackagesGzContentGenerator()
	{
		super("application/x-gzip", "Packages.gz");
	}

    @Override
    public String getGeneratorId() {
        return ID;
    }
}