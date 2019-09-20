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
package org.openhab.binding.heos.internal.resources;

/**
 * The {@link HeosCommand} provides the available commands for the HEOS network.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosCommands {

    // System Commands
    private final String registerChangeEventOn = "heos://system/register_for_change_events?enable=on";
    private final String registerChangeEventOFF = "heos://system/register_for_change_events?enable=off";
    private final String heosAccountCheck = "heos://system/check_account";
    private final String prettifyJSONon = "heos://system/prettify_json_response?enable=on";
    private final String prettifyJSONoff = "heos://system/prettify_json_response?enable=off";
    private final String rebootSystem = "heos://system/reboot";
    private final String signOut = "heos://system/sign_out";
    private final String heartbeat = "heos://system/heart_beat";

    // Player Commands Control
    private final String setPlayStatePlay = "heos://player/set_play_state?pid=";
    private final String setPlayStatePause = "heos://player/set_play_state?pid=";
    private final String setPlayStateStop = "heos://player/set_play_state?pid=";
    private final String setVolume = "heos://player/set_volume?pid=";
    private final String volumeUp = "heos://player/volume_up?pid=";
    private final String volumeDown = "heos://player/volume_down?pid=";
    private final String setMuteOn = "heos://player/set_mute?pid=";
    private final String setMuteOff = "heos://player/set_mute?pid=";
    private final String setMuteToggle = "heos://player/toggle_mute?pid=";
    private final String playNext = "heos://player/play_next?pid=";
    private final String playPrevious = "heos://player/play_previous?pid=";
    private final String playQueueItem = "heos://player/play_queue?pid=";
    private final String clearQueue = "heos://player/clear_queue?pid=";
    private final String deleteQueueItem = "heos://player/remove_from_queue?pid=";
    private final String setPlayMode = "heos://player/set_play_mode?pid=";

    // Group Commands Control
    private final String getGroups = "heos://group/get_groups";
    private final String getGroupsInfo = "heos://group/get_group_info?gid=";
    private final String setGroup = "heos://group/set_group?pid=";
    private final String getGroupVolume = "heos://group/get_volume?gid=";
    private final String setGroupVolume = "heos://group/set_volume?gid=";
    private final String getGroupMute = "heos://group/get_mute?gid=";
    private final String setGroupMute = "heos://group/set_mute?gid=";
    private final String toggleGroupMute = "heos://group/toggle_mute?gid=";
    private final String groupVolumeUp = "heos://group/volume_up?gid=";
    private final String groupVolumeDown = "heos://group/volume_down?gid=";

    // Player Commands get Information

    private final String getPlayers = "heos://player/get_players";
    private final String getPlayerInfo = "heos://player/get_player_info?pid=";
    private final String getPlayState = "heos://player/get_play_state?pid=";
    private final String getNowPlayingMedia = "heos://player/get_now_playing_media?pid=";
    private final String getVolume = "heos://player/get_volume?pid=";
    private final String getMute = "heos://player/get_mute?pid=";
    private final String getQueue = "heos://player/get_queue?pid=";
    private final String getPlayMode = "heos://player/get_play_mode?pid=";

    // Browse Commands
    private final String getMusicSources = "heos://browse/get_music_sources";
    private final String browseSource = "heos://browse/browse?sid=";
    private final String playStation = "heos://browse/play_stream?pid=";
    private final String addToQueue = "heos://browse/add_to_queue?pid=";
    private final String playInputSource = "heos://browse/play_input?pid=";
    private final String playURL = "heos://browse/play_stream?pid=";

    public String registerChangeEventOn() {
        return registerChangeEventOn;
    }

    public String registerChangeEventOFF() {
        return registerChangeEventOFF;
    }

    public String heosAccountCheck() {
        return heosAccountCheck;
    }

    public String setPlayStatePlay(String pid) {
        return setPlayStatePlay + pid + "&state=play";
    }

    public String setPlayStatePause(String pid) {
        return setPlayStatePause + pid + "&state=pause";
    }

    public String setPlayStateStop(String pid) {
        return setPlayStateStop + pid + "&state=stop";
    }

    public String volumeUp(String pid) {
        return volumeUp + pid + "&step=1";
    }

    public String volumeDown(String pid) {
        return volumeDown + pid + "&step=1";
    }

    public String setMuteOn(String pid) {
        return setMuteOn + pid + "&state=on";
    }

    public String setMuteOff(String pid) {
        return setMuteOff + pid + "&state=off";
    }

    public String setMuteToggle(String pid) {
        return setMuteToggle + pid + "&state=off";
    }

    public String setShuffleMode(String pid, String shuffle) {
        return setPlayMode + pid + "&shuffle=" + shuffle;
    }

    public String setRepeatMode(String pid, String repeat) {
        return setPlayMode + pid + "&repeat=" + repeat;
    }

    public String getPlayMode(String pid) {
        return getPlayMode + pid;
    }

    public String playNext(String pid) {
        return playNext + pid;
    }

    public String playPrevious(String pid) {
        return playPrevious + pid;
    }

    public String setVolume(String vol, String pid) {
        return setVolume + pid + "&level=" + vol;
    }

    public String getPlayers() {
        return getPlayers;
    }

    public String getPlayerInfo(String pid) {
        return getPlayerInfo + pid;
    }

    public String getPlayState(String pid) {
        return getPlayState + pid;
    }

    public String getNowPlayingMedia(String pid) {
        return getNowPlayingMedia + pid;
    }

    public String getVolume(String pid) {
        return getVolume + pid;
    }

    public String getMusicSources() {
        return getMusicSources;
    }

    public String prettifyJSONon() {
        return prettifyJSONon;
    }

    public String prettifyJSONoff() {
        return prettifyJSONoff;
    }

    public String getMute(String pid) {
        return getMute + pid;
    }

    public String getQueue(String pid) {
        return getQueue + pid;
    }

    public String playQueueItem(String pid, String qid) {
        return playQueueItem + pid + "&qid=" + qid;
    }

    public String deleteQueueItem(String pid, String qid) {
        return deleteQueueItem + pid + "&qid=" + qid;
    }

    public String browseSource(String sid) {
        return browseSource + sid;
    }

    public String playStation(String pid) {
        return playStation + pid;
    }

    public String addToQueue(String pid) {
        return addToQueue + pid;
    }

    public String addContainerToQueuePlayNow(String pid, String sid, String cid) {
        return addToQueue + pid + "&sid=" + sid + "&cid=" + cid + "&aid=1";
    }

    public String clearQueue(String pid) {
        return clearQueue + pid;
    }

    public String rebootSystem() {
        return rebootSystem;
    }

    public String playStation(String pid, String sid, String cid, String mid, String name) {
        String newCommand = playStation;
        if (pid != null) {
            newCommand = newCommand + pid;
        }
        if (sid != null) {
            newCommand = newCommand + "&sid=" + sid;
        }
        if (cid != null) {
            newCommand = newCommand + "&cid=" + cid;
        }
        if (mid != null) {
            newCommand = newCommand + "&mid=" + mid;
        }
        if (name != null) {
            newCommand = newCommand + "&name=" + name;
        }
        return newCommand;
    }

    public String playStation(String pid, String sid, String mid) {
        return playStation + pid + "&sid=" + sid + "&mid=" + mid;
    }

    public String playInputSource(String des_pid, String source_pid, String input) {
        return playInputSource + des_pid + "&spid=" + source_pid + "&input=inputs/" + input;
    }

    public String playURL(String pid, String url) {
        return playURL + pid + "&url=" + url;
    }

    public String signIn(String username, String password) {
        return "heos://system/sign_in?un=" + username + "&pw=" + password;
    }

    public String signOut() {
        return signOut;
    }

    public String heartbeat() {
        return heartbeat;
    }

    public String getGroups() {
        return getGroups;
    }

    public String getGroupInfo(String gid) {
        return getGroupsInfo + gid;
    }

    public String setGroup(String[] gid) {
        String players = "";
        for (String player : gid) {
            player = "," + player;
            players = players + player;
        }
        players = players.substring(1);

        return setGroup + players;
    }

    public String getGroupVolume(String gid) {
        return getGroupVolume + gid;
    }

    public String setGroupVolume(String volume, String gid) {
        return setGroupVolume + gid + "&level=" + volume;
    }

    public String setGroupVolumeUp(String gid) {
        return groupVolumeUp + gid + "&step=1";
    }

    public String setGroupVolumeDown(String gid) {
        return groupVolumeDown + gid + "&step=1";
    }

    public String getGroupMute(String gid) {
        return getGroupMute + gid;
    }

    public String setGroupMuteOn(String gid) {
        return setGroupMute + gid + "&state=on";
    }

    public String setGroupMuteOff(String gid) {
        return setGroupMute + gid + "&state=off";
    }

    public String getToggleGroupMute(String gid) {
        return toggleGroupMute + gid;
    }
}
