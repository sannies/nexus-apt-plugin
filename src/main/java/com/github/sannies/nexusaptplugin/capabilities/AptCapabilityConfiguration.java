/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.github.sannies.nexusaptplugin.capabilities;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Configuration adapter for {@link AptCapability}.
 *
 * @since 3.0
 */
public class AptCapabilityConfiguration
{

    public static final String KEYRING = "keyring";

    public static final String KEY = "key";

    public static final String PASSPHRASE = "passphrase";

    private String keyring;

    private String key;

    private String passphrase;

    public AptCapabilityConfiguration()
    {
    	this(null, null, null);
    }

    public AptCapabilityConfiguration(String keyring, String key, String passphrase) {
    	this.keyring = keyring == null ? "" : keyring;
    	this.key = key == null ? "" : key;
    	this.passphrase = passphrase == null ? "" : passphrase;
    }

    public AptCapabilityConfiguration(final Map<String, String> properties ) {
    	this(properties.get(KEYRING), properties.get(KEY), properties.get(PASSPHRASE));
    }

	public String getKeyring()
	{
		return keyring;
	}

	public void setKeyring(String keyring)
	{
		this.keyring = keyring;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getPassphrase()
	{
		return passphrase;
	}

	public void setPassphrase(String passphrase)
	{
		this.passphrase = passphrase;
	}

	public Map<String, String> asMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(KEYRING, keyring);
        map.put(KEY, key);
        map.put(PASSPHRASE, passphrase);

        return map;
    }

}
