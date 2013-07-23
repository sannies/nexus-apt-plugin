package com.github.sannies.nexusaptplugin.cache;


public interface FileGenerator
{

	/**
	 * Generate the file
	 * 
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	byte[] generateFile(RepositoryData data)
		throws Exception;
}