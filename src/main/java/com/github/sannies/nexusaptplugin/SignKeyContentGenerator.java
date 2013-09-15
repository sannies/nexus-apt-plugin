package com.github.sannies.nexusaptplugin;


import javax.inject.Named;

/**
 * @author Raniz
 */
@Named(SignKeyContentGenerator.ID)
public class SignKeyContentGenerator
	extends AbstractContentGenerator {
    public static final String ID = "SignKeyContentGenerator";


	public SignKeyContentGenerator()
	{
		super("application/text", "apt-key.gpg.key");
	}

    @Override
    public String getGeneratorId() {
        return ID;
    }
}