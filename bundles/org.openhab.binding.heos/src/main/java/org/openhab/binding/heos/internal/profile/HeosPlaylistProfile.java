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
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.heos.internal.resources.AddCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosPlaylistProfile} class implements the behavior when being linked to a Playlist item.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosPlaylistProfile implements TriggerProfile {
    private final Logger logger = LoggerFactory.getLogger(HeosPlaylistProfile.class);

    private static final String ACTION = "action";

    private final ProfileCallback profileCallback;
    private final ProfileContext context;

    public HeosPlaylistProfile(ProfileCallback callback, ProfileContext context) {
        this.profileCallback = callback;
        this.context = context;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return HeosProfileFactory.UID_PLAY;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        @Nullable
        AddCriteria addCriteria = getAddCriteria();
        if (addCriteria != null) {
            logger.debug("Forwarding '{}' with criteria {}", state, addCriteria);
            profileCallback.handleCommand(new HeosPlayCommandType(state.toString(), addCriteria));
        } else {
            logger.debug("Not forwarding command, because of missing/invalid add-criteria");
        }
    }

    @Override
    public void onTriggerFromHandler(String selected) {
        // ignore
    }

    private @Nullable AddCriteria getAddCriteria() {
        String action = (String) context.getConfiguration().get(ACTION);

        if ("IGNORE".equals(action)) {
            return null;
        } else if (action == null) {
            return AddCriteria.PLAY_NOW;
        }

        try {
            return AddCriteria.valueOf(action);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to map requested action: {}", action);
            return null;
        }
    }
}
