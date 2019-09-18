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
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.internal.HeosBindingConstants.CH_TYPE_FAVORITE;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelManager} provides the functions to
 * add and remove channels from the channel list provided by the thing
 * The generation of the individual channels has to be done by the thingHandler
 * itself. Only for the favorites a function is provided which generates the
 * individual channels for each favorite.
 *
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosChannelManager {
    private ThingHandler handler;

    public HeosChannelManager(ThingHandler handler) {
        this.handler = handler;
    }

    public List<Channel> addSingleChannel(Channel channel) {
        ChannelWrapper channels = getChannelsFromThing();
        channels.addChannel(channel);
        return channels.get();
    }

    public List<Channel> addMultipleChannels(List<Channel> channels) {
        ChannelWrapper channelWrapper = getChannelsFromThing();
        channels.forEach(channelWrapper::addChannel);
        return channelWrapper.get();
    }

    public List<Channel> removeSingleChannel(String channelIdentifier) {
        ChannelWrapper channelWrapper = getChannelsFromThing();
        channelWrapper.removeChannel(generateChannelUID(channelIdentifier));
        return channelWrapper.get();
    }

    public List<Channel> addFavoriteChannels(List<Map<String, String>> favoritesList) {
        List<Channel> channelList = new ArrayList<>();
        favoritesList.forEach(element -> channelList.add(generateFavoriteChannel(element)));
        return addMultipleChannels(channelList);
    }

    private Channel generateFavoriteChannel(Map<String, String> properties) {
        return ChannelBuilder.create(generateChannelUID(properties.get(MID)), "Switch")
                .withLabel(properties.get(NAME))
                .withType(CH_TYPE_FAVORITE)
                .withProperties(properties).build();
    }

    /*
     * Gets the channels from the Thing and makes the channel
     * list editable.
     */
    private ChannelWrapper getChannelsFromThing() {
        return new ChannelWrapper(handler.getThing().getChannels());
    }

    private ChannelUID generateChannelUID(String channelIdentifier) {
        return new ChannelUID(handler.getThing().getUID(), channelIdentifier);
    }

    /**
     * Wrap a channel list
     *
     * @author Martin van Wingerden - Initial contribution
     */
    private static class ChannelWrapper {
        private final Logger logger = LoggerFactory.getLogger(ChannelWrapper.class);
        private final List<Channel> channels;

        ChannelWrapper(List<Channel> channels) {
            this.channels = channels;
        }

        private void removeChannel(ChannelUID uid) {
            channels.stream()
                    .filter(channel -> uid.equals(channel.getUID()))
                    .forEach(channels::remove);
        }

        /*
         * Function to add an channel to the channel list.
         * Checks first if channel already exists.
         * If so, updated the channel by removing it first and
         * add it again.
         */
        private void addChannel(Channel channel) {
            // If channel already exists remove it first
            removeChannel(channel.getUID());
            // Then add the new/updated channel to the list
            channels.add(channel);
            logger.debug("Adding Channel: {}", channel.getLabel());
        }

        public List<Channel> get() {
            return channels;
        }
    }
}
