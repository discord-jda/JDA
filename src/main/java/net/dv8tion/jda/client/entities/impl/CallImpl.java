/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.util.*;
import java.util.stream.Collectors;

public class CallImpl implements Call
{
    private final CallableChannel callableChannel;
    private final String messageId;

    private HashMap<String, CallUser> callUsers = new HashMap<>();
    private HashMap<String, CallUser> callUserHistory = new HashMap<>();

    private Region region;

    public CallImpl(CallableChannel callableChannel, String messageId)
    {
        this.callableChannel = callableChannel;
        this.messageId = messageId;
    }

    @Override
    public Region getRegion()
    {
        return region;
    }

    @Override
    public boolean isGroupCall()
    {
        return callableChannel instanceof Group;
    }

    @Override
    public CallableChannel getCallableChannel()
    {
        return callableChannel;
    }

    @Override
    public Group getGroup()
    {
        return isGroupCall() ? (Group) callableChannel : null;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        return !isGroupCall() ? (PrivateChannel) callableChannel : null;
    }

    @Override
    public String getMessageId()
    {
        return messageId;
    }

    @Override
    public List<CallUser> getRingingUsers()
    {
        return Collections.unmodifiableList(callUsers.values().stream()
                .filter(cu -> cu.isRinging())
                .collect(Collectors.toList()));
    }

    @Override
    public List<CallUser> getConnectedUsers()
    {
        return Collections.unmodifiableList(callUsers.values().stream()
                .filter(cu -> cu.getVoiceState().isInCall())
                .collect(Collectors.toList()));
    }

    @Override
    public List<CallUser> getCallUserHistory()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(callUserHistory.values()));
    }

    @Override
    public List<CallUser> getAllCallUsers()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(callUsers.values()));
    }

    @Override
    public String getId()
    {
        return callableChannel.getId();
    }

    @Override
    public String toString()
    {
        return "Call(" + getId() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Call))
            return false;

        Call oCall = (Call) o;
        return getId().equals(oCall.getId()) && Objects.equals(messageId, oCall.getMessageId());
    }

    @Override
    public int hashCode()
    {
        return ("Call " + getId()).hashCode();
    }

    public CallImpl setRegion(Region region)
    {
        this.region = region;
        return this;
    }

    public HashMap<String, CallUser> getCallUserMap()
    {
        return callUsers;
    }

    public HashMap<String, CallUser> getCallUserHistoryMap()
    {
        return callUserHistory;
    }
}
