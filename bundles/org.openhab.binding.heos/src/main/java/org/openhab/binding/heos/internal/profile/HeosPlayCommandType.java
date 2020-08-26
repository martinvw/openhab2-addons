/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.profile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.heos.internal.resources.AddCriteria;

/**
 * The {@link HeosPlayCommandType} provides a dedicated command for HEOS playlists.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosPlayCommandType extends StringType {
    private final AddCriteria criteria;

    public HeosPlayCommandType(@Nullable String value, AddCriteria criteria) {
        super(value);
        this.criteria = criteria;
    }

    public AddCriteria getCriteria() {
        return criteria;
    }

    public String getRequestedPlaylist() {
        return super.toString();
    }

    @Override
    public String toString() {
        return "HeosPlaylistCommand{" + "criteria=" + criteria + ',' + "super=" + super.toString() + '}';
    }
}
