package com.github.sannies.nexusaptplugin.cache.generators;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64;

import com.github.sannies.nexusaptplugin.cache.DebianFileManager;
import com.github.sannies.nexusaptplugin.cache.FileGenerator;
import com.github.sannies.nexusaptplugin.cache.RepositoryData;


@Named
public class ReleaseGenerator
	implements FileGenerator
{
    /** Enum for the hash algorithms to include included */
    private static enum Algorithm
    {
    	MD5("MD5Sum", "MD5"),
    	SHA1("SHA1", "SHA1"),
    	SHA256("SHA256", "SHA256");

    	final String heading;

    	final String name;

    	private Algorithm(String heading, String name)
    	{
    		this.heading = heading;
    		this.name = name;
    	}
    }

    private final String[] FILES = new String[]{ "Packages", "Packages.gz" };

	private final DebianFileManager fileManager;

	@Inject
	public ReleaseGenerator(DebianFileManager fileManager)
	{
		this.fileManager = fileManager;
	}

	@Override
	public byte[] generateFile(RepositoryData data)
		throws Exception
	{
		// Gather files
		List<File> files = new LinkedList<File>();
		int maxSizeLength = 0;
		for(String name : FILES)
		{
			byte[] contents = fileManager.getFile(name, data);
			File file = new File();
			file.name = name;
			file.contents = contents;
			file.size = String.valueOf(contents.length);
			files.add(file);

			maxSizeLength = Math.max(maxSizeLength, file.size.length());
		}

		// Create Releases
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    OutputStreamWriter w = new OutputStreamWriter(baos);
	    for(Algorithm algorithm : Algorithm.values())
	    {
	        try {
	    		MessageDigest md = MessageDigest.getInstance(algorithm.name);
	    		w.write(algorithm.heading);
	    		w.write(":\n");

	    		for(File file : files)
	    		{
	    			md.reset();
		    		md.update(file.contents);
		    		byte[] digest = md.digest();
		    		w.write(" ");
		    		w.write(Base64.encodeBase64String(digest));
		    		for(int i = 0; i <= maxSizeLength - file.size.length(); i++)
		    		{
		    			w.write(" ");
		    		}
		    		w.write(file.size);
		    		w.write(" ");
		    		w.write(file.name);
		    		w.write("\n");
	    		}
	        }
	        catch(NoSuchAlgorithmException e)
	        {
	        	// guess there's not much we can do...
	        	throw new RuntimeException(e);
	        }
	    }
	    w.close();

	    return baos.toByteArray();
	}

	private static final class File
	{
		private String name;

		private byte[] contents;

		private String size;
	}

}
