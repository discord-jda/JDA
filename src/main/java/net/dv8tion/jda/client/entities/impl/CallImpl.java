/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CallImpl implements Call
{
    private final CallableChannel callableChannel;
    private final long messageId;

    private final TLongObjectMap<CallUser> callUsers = MiscUtil.newLongMap();
    private final TLongObjectMap<CallUser> callUserHistory = MiscUtil.newLongMap();

    private Region region;

    public CallImpl(CallableChannel callableChannel, long messageId)
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
        return Long.toUnsignedString(messageId);
    }

    @Override
    public long getMessageIdLong()
    {
        return messageId;
    }

    @Override
    public List<CallUser> getRingingUsers()
    {
        return Collections.unmodifiableList(callUsers.valueCollection().stream()
                .filter(CallUser::isRinging)
                .collect(Collectors.toList()));
    }

    @Override
    public List<CallUser> getConnectedUsers()
    {
        return Collections.unmodifiableList(callUsers.valueCollection().stream()
                .filter(cu -> cu.getVoiceState().isInCall())
                .collect(Collectors.toList()));
    }

    @Override
    public List<CallUser> getCallUserHistory()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(callUserHistory.valueCollection()));
    }

    @Override
    public List<CallUser> getAllCallUsers()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(callUsers.valueCollection()));
    }

    @Override
    public long getIdLong()
    {
        return callableChannel.getIdLong();
    }

    @Override
    public String toString()
    {
        return "Call(" + getIdLong() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Call))
            return false;

        Call oCall = (Call) o;
        return getIdLong() == oCall.getIdLong() && messageId == oCall.getMessageIdLong();
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

    public TLongObjectMap<CallUser> getCallUserMap()
    {
        return callUsers;
    }

    public TLongObjectMap<CallUser> getCallUserHistoryMap()
    {
        return callUserHistory;
    }
}
