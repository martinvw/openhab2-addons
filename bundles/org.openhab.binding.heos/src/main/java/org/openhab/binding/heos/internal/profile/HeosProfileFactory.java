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

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.*;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.heos.internal.HeosBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines and provides all profiles and their types of this binding.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
@Component
public class HeosProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(HeosProfileFactory.class);

    static final ProfileTypeUID UID_PLAY = new ProfileTypeUID(HeosBindingConstants.BINDING_ID, "play");

    private static final TriggerProfileType PLAYLIST_TYPE = ProfileTypeBuilder.newTrigger(UID_PLAY, "Play Action")
            .withSupportedItemTypes(CoreItemFactory.STRING)
            .withSupportedChannelTypeUIDs(HeosBindingConstants.CH_TYPE_PLAYLISTS).build();

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Stream.of(UID_PLAY).collect(Collectors.toSet());
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Stream.of(PLAYLIST_TYPE).collect(Collectors.toSet());
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channel.getChannelTypeUID(), itemType);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channelType.getUID(), itemType);
    }

    private @Nullable ProfileTypeUID getSuggestedProfileTypeUID(@Nullable ChannelTypeUID channelTypeUID,
            @Nullable String itemType) {
        logger.debug("Suggested profiles: {}, {}", channelTypeUID, itemType);
        if (HeosBindingConstants.CH_TYPE_PLAYLISTS.equals(channelTypeUID) && itemType != null) {
            if (CoreItemFactory.STRING.equals(itemType)) {
                return UID_PLAY;
            }
            return null;
        }
        return null;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        logger.debug("Create profile: {}", profileTypeUID);
        if (UID_PLAY.equals(profileTypeUID)) {
            return new HeosPlaylistProfile(callback, profileContext);
        } else {
            return null;
        }
    }
}
