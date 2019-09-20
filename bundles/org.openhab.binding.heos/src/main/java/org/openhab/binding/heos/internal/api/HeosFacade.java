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
package org.openhab.binding.heos.internal.api;

import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosPlayer;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * The {@link HeosFacade} is the interface for handling commands, which are
 * sent to the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosFacade {

    private final HeosSystem heosSystem;
    private final HeosEventController eventController;

    public HeosFacade(HeosSystem heosSystem, HeosEventController eventController) {
        this.heosSystem = heosSystem;
        this.eventController = eventController;
    }

    public List<Map<String, String>> getFavorites() {
        return heosSystem.getFavorites();
    }

    public HeosPlayer getPlayerState(String pid) {
        return heosSystem.getPlayerState(pid);
    }

    /**
     * Pauses the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void pause(String pid) {
        heosSystem.send(heosSystem.command().setPlayStatePause(pid));
    }

    /**
     * Starts the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void play(String pid) {
        heosSystem.send(heosSystem.command().setPlayStatePlay(pid));
    }

    /**
     * Stops the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void stop(String pid) {
        heosSystem.send(heosSystem.command().setPlayStateStop(pid));
    }

    /**
     * Jumps to the next song on the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void next(String pid) {
        heosSystem.send(heosSystem.command().playNext(pid));
    }

    /**
     * Jumps to the previous song on the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void previous(String pid) {
        heosSystem.send(heosSystem.command().playPrevious(pid));
    }

    /**
     * Toggles the mute state the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void mute(String pid) {
        heosSystem.send(heosSystem.command().setMuteToggle(pid));
    }

    /**
     * Mutes the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void muteON(String pid) {
        heosSystem.send(heosSystem.command().setMuteOn(pid));
    }

    /**
     * Un-mutes the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void muteOFF(String pid) {
        heosSystem.send(heosSystem.command().setMuteOff(pid));
    }

    /**
     * Set the play mode of the player or group
     *
     * @param pid  The PID of the dedicated player or group
     * @param mode The shuffle mode: Allowed commands: on; off
     */
    public void setShuffleMode(String pid, String mode) {
        heosSystem.send(heosSystem.command().setShuffleMode(pid, mode));
    }

    /**
     * Sets the repeat mode of the player or group
     *
     * @param pid  The ID of the dedicated player or group
     * @param mode The repeat mode. Allowed commands: on_all; on_one; off
     */
    public void setRepeatMode(String pid, String mode) {
        heosSystem.send(heosSystem.command().setRepeatMode(pid, mode));
    }

    /**
     * Set the HEOS player to a dedicated volume
     *
     * @param vol The volume the player shall be set to (value between 0 -100)
     * @param pid The ID of the dedicated player or group
     */
    public void setVolume(String vol, String pid) {
        heosSystem.send(heosSystem.command().setVolume(vol, pid));
    }

    /**
     * Increases the HEOS player volume 1 Step
     *
     * @param pid The ID of the dedicated player or group
     */
    public void increaseVolume(String pid) {
        heosSystem.send(heosSystem.command().volumeUp(pid));
    }

    /**
     * Decreases the HEOS player volume 1 Step
     *
     * @param pid The ID of the dedicated player or group
     */
    public void decreaseVolume(String pid) {
        heosSystem.send(heosSystem.command().volumeDown(pid));
    }

    /**
     * Toggles mute state of the HEOS group
     *
     * @param gid The GID of the group
     */
    public void muteGroup(String gid) {
        heosSystem.send(heosSystem.command().setMuteToggle(gid));
    }

    /**
     * Mutes the HEOS group
     *
     * @param gid The GID of the group
     */
    public void muteGroupON(String gid) {
        heosSystem.send(heosSystem.command().setGroupMuteOn(gid));
    }

    /**
     * Un-mutes the HEOS group
     *
     * @param gid The GID of the group
     */
    public void muteGroupOFF(String gid) {
        heosSystem.send(heosSystem.command().setGroupMuteOff(gid));
    }

    /**
     * Set the volume of the group to a specific level
     *
     * @param vol The volume the group shall be set to (value between 0-100)
     * @param gid The GID of the group
     */
    public void volumeGroup(String vol, String gid) {
        heosSystem.send(heosSystem.command().setGroupVolume(vol, gid));
    }

    /**
     * Increases the HEOS group volume 1 Step
     *
     * @param gid The ID of the dedicated player or group
     */
    public void increaseGroupVolume(String gid) {
        heosSystem.send(heosSystem.command().setGroupVolumeUp(gid));
    }

    /**
     * Decreases the HEOS group volume 1 Step
     *
     * @param gid The ID of the dedicated player or group
     */
    public void decreaseGroupVolume(String gid) {
        heosSystem.send(heosSystem.command().setGroupVolumeDown(gid));
    }

    /**
     * Un-Group the HEOS group to single player
     *
     * @param gid The GID of the group
     */
    public void ungroupGroup(String gid) {
        String[] pid = new String[]{gid};
        heosSystem.send(heosSystem.command().setGroup(pid));
    }

    /**
     * Builds a group from single players
     *
     * @param pids The single player IDs of the player which shall be grouped
     */
    public void groupPlayer(String[] pids) {
        heosSystem.send(heosSystem.command().setGroup(pids));
    }

    /**
     * Browses through a HEOS source. Currently no response
     *
     * @param sid The source sid which shall be browsed
     */
    public void browseSource(String sid) {
        heosSystem.send(heosSystem.command().browseSource(sid));
    }

    /**
     * Adds a media container to the queue and plays the media directly
     * Information of the sid and cid has to be obtained via the browse function
     *
     * @param pid The player ID where the media object shall be played
     * @param sid The source ID where the media is located
     * @param cid The container ID of the media
     */
    public void addContainerToQueuePlayNow(String pid, String sid, String cid) {
        heosSystem.send(heosSystem.command().addContainerToQueuePlayNow(pid, sid, cid));
    }

    /**
     * Reboot the bridge to which the connection is established
     */
    public void reboot() {
        heosSystem.sendWithoutResponse(heosSystem.command().rebootSystem());
    }

    /**
     * Login in via the bridge to the HEOS account
     *
     * @param name     The username
     * @param password The password of the user
     */
    public void logIn(String name, String password) {
        heosSystem.send(heosSystem.command().signIn(name, password));
    }

    /**
     * Plays a specific station on the HEOS player
     *
     * @param pid  The player ID
     * @param sid  The source ID where the media is located
     * @param cid  The container ID of the media
     * @param mid  The media ID of the media
     * @param name Station name returned by 'browse' command.
     */
    public void playStation(String pid, String sid, String cid, String mid, String name) {
        heosSystem.send(heosSystem.command().playStation(pid, sid, cid, mid, name));
    }

    /**
     * Plays a specific station on the HEOS player
     *
     * @param pid The player ID
     * @param sid The source ID where the media is located
     * @param mid The media ID of the media
     */
    public void playStation(String pid, String sid, String mid) {
        heosSystem.send(heosSystem.command().playStation(pid, sid, mid));
    }

    /**
     * Plays a specified local input source on the player.
     * Input name as per specified in HEOS CLI Protocol
     *
     * @param pid
     * @param input
     */
    public void playInputSource(String pid, String input) {
        heosSystem.send(heosSystem.command().playInputSource(pid, pid, input));
    }

    /**
     * Plays a specified input source from another player on the selected player.
     * Input name as per specified in HEOS CLI Protocol
     *
     * @param des_pid    the PID where the source shall be played
     * @param source_pid the PID where the source is located.
     * @param input      the input name
     */
    public void playInputSource(String des_pid, String source_pid, String input) {
        heosSystem.send(heosSystem.command().playInputSource(des_pid, source_pid, input));
    }

    /**
     * Plays a file from a URL
     *
     * @param pid the PID where the file shall be played
     * @param url the complete URL the file is located
     */
    public void playURL(String pid, URL url) {
        heosSystem.send(heosSystem.command().playURL(pid, url.toString()));
    }

    /**
     * Gets the information like mid, sid and so on of the
     * currently playing media. Response is handled via the
     * HeosEventController
     *
     * @param pid The player ID the media is playing on
     */
    public void getPlayingMediaInfo(String pid) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(pid));
    }

    /**
     * Deletes a media from the queue
     *
     * @param pid The player ID the media is playing on
     * @param qid The queue ID of the media. (starts by 1)
     */
    public void deleteMediaFromQueue(String pid, String qid) {
        heosSystem.send(heosSystem.command().deleteQueueItem(pid, qid));
    }

    /**
     * Plays a specific media file from the queue
     *
     * @param pid The player ID the media shall be played on
     * @param qid The queue ID of the media. (starts by 1)
     */
    public void playMediafromQueue(String pid, String qid) {
        heosSystem.send(heosSystem.command().playQueueItem(pid, qid));
    }

    /**
     * Asks for the actual state of the player. The result has
     * to be handled by the event controller. The system returns {@link HeosConstants.PLAY},
     * {@link HeosConstants.PAUSE} or {@link HeosConstants.STOP}.
     *
     * @param id The player ID the state shall get for
     */
    public void getHeosPlayState(String id) {
        heosSystem.send(heosSystem.command().getPlayState(id));
    }

    /**
     * Ask for the actual mute state of the player. The result has
     * to be handled by the event controller. The HEOS system returns {@link HeosConstants.ON}
     * or {@link HeosConstants.OFF}.
     *
     * @param id The player id the mute state shall get for
     */
    public void getHeosPlayerMuteState(String id) {
        heosSystem.send(heosSystem.command().getMute(id));
    }

    /**
     * Ask for the actual volume the player. The result has
     * to be handled by the event controller. The HEOS system returns
     * a value between 0 and 100
     *
     * @param id The player id the volume shall get for
     */
    public void getHeosPlayerVolume(String id) {
        heosSystem.send(heosSystem.command().getVolume(id));
    }

    /**
     * Ask for the actual shuffle mode of the player. The result has
     * to be handled by the event controller. The HEOS system returns {@link HeosConstants.ON},
     * {@link HeosConstants.HEOS_REPEAT_ALL} or {@link HeosConstants.HEOS_REPEAT_ONE}
     *
     * @param id The player id the shuffle mode shall get for
     */
    public void getHeosPlayerShuffleMode(String id) {
        heosSystem.send(heosSystem.command().getPlayMode(id));
    }

    public void getHeosPlayerRepeatMode(String id) {
        heosSystem.send(heosSystem.command().getPlayMode(id));
    }

    public void getHeosGroupeMuteState(String id) {
        heosSystem.send(heosSystem.command().getGroupMute(id));
    }

    public void getHeosGroupVolume(String id) {
        heosSystem.send(heosSystem.command().getGroupVolume(id));
    }

    public void getNowPlayingMedia(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaArtist(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaSong(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaAlbum(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaImageUrl(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaImageQid(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaMid(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaAlbumID(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaStation(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    public void getNowPlayingMediaType(String id) {
        heosSystem.send(heosSystem.command().getNowPlayingMedia(id));
    }

    /**
     * Sends a RAW command to the HEOS bridge. The command has to be
     * in accordance with the HEOS CLI specification
     *
     * @param command to send
     */
    public void sendRawCommand(String command) {
        heosSystem.send(command);
    }

    /**
     * Register an {@link HeosEventListener} to get notification of system events
     *
     * @param listener The HeosEventListener
     */
    public void registerForChangeEvents(HeosEventListener listener) {
        eventController.addListener(listener);
    }

    /**
     * Unregister an {@link HeosEventListener} to get notification of system events
     *
     * @param listener The HeosEventListener
     */
    public void unregisterForChangeEvents(HeosEventListener listener) {
        eventController.removeListener(listener);
    }
}
