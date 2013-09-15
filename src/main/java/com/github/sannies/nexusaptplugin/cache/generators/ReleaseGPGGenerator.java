package com.github.sannies.nexusaptplugin.cache.generators;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;

import com.github.sannies.nexusaptplugin.cache.DebianFileManager;
import com.github.sannies.nexusaptplugin.cache.FileGenerator;
import com.github.sannies.nexusaptplugin.cache.RepositoryData;
import com.github.sannies.nexusaptplugin.sign.AptSigningConfiguration;
import com.github.sannies.nexusaptplugin.sign.PGPSigner;


@Named
public class ReleaseGPGGenerator
	implements FileGenerator
{
	private final DebianFileManager fileManager;

	private final AptSigningConfiguration signingConfiguration;


	@Inject
	public ReleaseGPGGenerator(DebianFileManager fileManager,
		AptSigningConfiguration signingConfiguration)
	{
		this.fileManager = fileManager;
		this.signingConfiguration = signingConfiguration;
	}

	@Override
	public byte[] generateFile(RepositoryData data)
		throws Exception
	{
		// Read Release
		byte[] release = fileManager.getFile("Release", data);

		// Get the key and sign the Release file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		PGPSigner signer = signingConfiguration.getSigner();
		PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(signer.getSecretKey().getPublicKey().getAlgorithm(), PGPUtil.SHA1));
		signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, signer.getPrivateKey());

		BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(baos));
		signatureGenerator.update(release);
		signatureGenerator.generate().encode(out);

		out.close();
		baos.close();

		return baos.toByteArray();
	}
}
