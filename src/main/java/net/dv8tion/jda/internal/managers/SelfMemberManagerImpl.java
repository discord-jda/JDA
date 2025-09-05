/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.managers.SelfMemberManager;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SelfMemberManagerImpl extends ManagerBase<SelfMemberManager> implements SelfMemberManager
{
    protected final SelfMember selfMember;

    protected String nickname;
    protected Icon avatar;
    protected Icon banner;
    protected String bio;

    public SelfMemberManagerImpl(@Nonnull SelfMember selfMember)
    {
        super(selfMember.getJDA(), Route.Guilds.MODIFY_SELF.compile(selfMember.getGuild().getId()));
        this.selfMember = selfMember;
    }

    @Nonnull
    @Override
    public SelfMember getMember()
    {
        return selfMember;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NICKNAME) == NICKNAME)
            avatar = null;
        if ((fields & AVATAR) == AVATAR)
            avatar = null;
        if ((fields & BANNER) == BANNER)
            banner = null;
        if ((fields & BIO) == BIO)
            banner = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl reset(@Nonnull long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl reset()
    {
        super.reset();
        nickname = null;
        avatar = null;
        banner = null;
        bio = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setNickname(@Nullable String nickname)
    {
        Checks.notBlank(nickname, "Nickname");
        Checks.notLonger(nickname, Member.MAX_NICKNAME_LENGTH, "Nickname");
        this.nickname = nickname;
        set |= NICKNAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setAvatar(@Nullable Icon avatar)
    {
        this.avatar = avatar;
        set |= AVATAR;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setBanner(@Nullable Icon banner)
    {
        this.banner = banner;
        set |= BANNER;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setBio(@Nullable String bio)
    {
        Checks.notBlank(bio, "Bio");
        Checks.notLonger(bio, SelfMember.MAX_BIO_LENGTH, "Bio");
        this.bio = bio;
        set |= BIO;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty();

        if (shouldUpdate(NICKNAME))
            body.put("nick", nickname);
        if (shouldUpdate(AVATAR))
            body.put("avatar", avatar == null ? null : avatar.getEncoding());
        if (shouldUpdate(BANNER))
            body.put("banner", banner == null ? null : banner.getEncoding());
        if (shouldUpdate(BIO))
            body.put("bio", bio);

        reset();
        return getRequestBody(body);
    }

    @Override
    protected void handleSuccess(Response response, Request<Void> request)
    {
        request.onSuccess(null);
    }
}
