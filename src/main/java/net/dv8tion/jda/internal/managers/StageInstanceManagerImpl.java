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

import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.managers.StageInstanceManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StageInstanceManagerImpl extends ManagerBase<StageInstanceManager> implements StageInstanceManager
{
    private final StageInstance instance;

    private String topic;
    private StageInstance.PrivacyLevel privacyLevel;

    public StageInstanceManagerImpl(StageInstance instance)
    {
        super(instance.getChannel().getJDA(), Route.StageInstances.UPDATE_INSTANCE.compile(instance.getChannel().getId()));
        this.instance = instance;
    }

    @Nonnull
    @Override
    public StageInstance getStageInstance()
    {
        return instance;
    }

    @Nonnull
    @Override
    public StageInstanceManager setTopic(@Nullable String topic)
    {
        if (topic != null)
        {
            topic = topic.trim();
            Checks.notLonger(topic, 120, "Topic");
            if (topic.isEmpty())
                topic = null;
        }
        this.topic = topic;
        set |= TOPIC;
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public StageInstanceManager setPrivacyLevel(@Nonnull StageInstance.PrivacyLevel level)
    {
        Checks.notNull(level, "PrivacyLevel");
        Checks.check(level != StageInstance.PrivacyLevel.UNKNOWN, "PrivacyLevel must not be UNKNOWN!");
        Checks.check(level != StageInstance.PrivacyLevel.PUBLIC, "Cannot create PUBLIC stage instances anymore.");
        this.privacyLevel = level;
        set |= PRIVACY_LEVEL;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty();
        if (shouldUpdate(TOPIC) && topic != null)
            body.put("topic", topic);
        if (shouldUpdate(PRIVACY_LEVEL))
            body.put("privacy_level", privacyLevel.getKey());
        return getRequestBody(body);
    }
}
