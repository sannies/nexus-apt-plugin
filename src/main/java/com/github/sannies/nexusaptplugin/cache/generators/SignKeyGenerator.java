package com.github.sannies.nexusaptplugin.cache.generators;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Named;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import com.github.sannies.nexusaptplugin.cache.FileGenerator;
import com.github.sannies.nexusaptplugin.cache.RepositoryData;
import com.github.sannies.nexusaptplugin.sign.AptSigningConfiguration;
import com.github.sannies.nexusaptplugin.sign.PGPSigner;
import com.google.inject.Inject;


@Named
public class SignKeyGenerator
	implements FileGenerator
{
    private final AptSigningConfiguration configuration;

    @Inject
	public SignKeyGenerator(AptSigningConfiguration configuration)
	{
    	this.configuration = configuration;
	}

	@Override
	public byte[] generateFile(RepositoryData data)
		throws Exception
	{
		// Extract the key and return it
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PGPSigner signer = configuration.getSigner();
		PGPPublicKey publicKey = signer.getSecretKey().getPublicKey();

		BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(baos));
		publicKey.encode(out);

		out.close();
		baos.close();

		return baos.toByteArray();
	}

}
