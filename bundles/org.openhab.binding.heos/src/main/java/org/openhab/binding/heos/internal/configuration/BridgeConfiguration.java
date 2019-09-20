/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.heos.internal.configuration;

/**
 * Configuration wrapper for bridge configuration
 *
 * @author Martin van Wingerden - Initial Contribution
 */
public class BridgeConfiguration {

    /**
     * Network address of the HEOS bridge
     */
    public String ipAddress;

    /**
     * Username for login to the HEOS account.
     */
    public String username;

    /**
     * Password for login to the HEOS account
     */
    public String password;

    /**
     * The time in seconds for the HEOS Heartbeat (default = 60 s)
     */
    public int heartbeat;

}
