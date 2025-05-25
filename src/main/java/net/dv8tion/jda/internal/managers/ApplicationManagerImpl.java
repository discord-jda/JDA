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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.managers.ApplicationManager;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * When implement new fields, update also {@link #reset(long)}, {@link #reset(long...)}, {@link #reset()} and {@link #finalizeData()}
 */
public class ApplicationManagerImpl extends ManagerBase<ApplicationManager> implements ApplicationManager
{

    protected String description;
    protected Icon icon;
    protected Icon coverImage;
    protected Set<String> tags;
    protected String interactionsEndpointUrl;
    protected String customInstallUrl;
    protected IntegrationTypeConfig installParams;
    protected Map<IntegrationType, IntegrationTypeConfig> integrationTypeConfig;

    public ApplicationManagerImpl(JDA jda)
    {
        super(jda, Route.Applications.EDIT_BOT_APPLICATION.compile());
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationManagerImpl reset(long fields)
    {
        super.reset(fields);

        if((fields & DESCRIPTION) == DESCRIPTION)
            description = null;
        if((fields & ICON) == ICON)
            icon = null;
        if((fields & COVER_IMAGE) == COVER_IMAGE)
            coverImage = null;

        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationManager reset()
    {
        super.reset();
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setDescription(@Nonnull String description)
    {
        Checks.notNull(description, "Description");
        Checks.notLonger(description.trim(), ApplicationInfo.MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description.trim();
        set |= DESCRIPTION;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setIcon(Icon icon)
    {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setCoverImage(Icon coverImage)
    {
        this.coverImage = coverImage;
        set |= COVER_IMAGE;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setTags(@Nonnull Collection<String> tags)
    {
        Checks.noneNull(tags, "Tags");
        Set<String> tagSet = new LinkedHashSet<>();
        for (String tag : tags)
        {
            Checks.notLonger(tag.trim(), ApplicationInfo.MAX_TAG_LENGTH, "Tag");
            Checks.notBlank(tag, "Tag");
            tagSet.add(tag.trim());
        }

        this.tags = tagSet;
        set |= TAGS;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setInteractionsEndpointUrl(@Nullable String interactionsEndpointUrl)
    {
        if (interactionsEndpointUrl != null)
            checkUrl(interactionsEndpointUrl);
        this.interactionsEndpointUrl = interactionsEndpointUrl;
        set |= INTERACTIONS_ENDPOINT_URL;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setCustomInstallUrl(@Nullable String customInstallUrl)
    {
        if (customInstallUrl != null)
            checkUrl(customInstallUrl);
        this.customInstallUrl = customInstallUrl;
        set |= CUSTOM_INSTALL_URL;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setInstallParams(@Nullable ApplicationManager.IntegrationTypeConfig installParams)
    {
        this.installParams = installParams;
        set |= INSTALL_PARAMS;
        return this;
    }

    @Nonnull
    @Override
    public ApplicationManager setIntegrationTypeConfig(@Nullable Map<IntegrationType, IntegrationTypeConfig> config)
    {
        if (config != null)
        {
            Checks.noneNull(config.keySet(), "IntegrationTypeConfig");
            Checks.noneNull(config.values(), "IntegrationTypeConfig");
            Checks.check(!config.keySet().contains(IntegrationType.UNKNOWN), "IntegrationTypeConfig must not be set for UNKNOWN");
        }

        this.integrationTypeConfig = config;
        set |= INTEGRATION_TYPES_CONFIG;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty();

        if (shouldUpdate(DESCRIPTION))
            body.put("description", this.description);
        if (shouldUpdate(ICON))
            body.put("icon", this.icon == null ? null : this.icon.getEncoding());
        if (shouldUpdate(COVER_IMAGE))
            body.put("cover_image", this.coverImage == null ? null : this.coverImage.getEncoding());
        if (shouldUpdate(TAGS))
            body.put("tags", DataArray.fromCollection(this.tags));
        if (shouldUpdate(INTERACTIONS_ENDPOINT_URL))
            body.put("interactions_endpoint_url", this.interactionsEndpointUrl);
        if (shouldUpdate(CUSTOM_INSTALL_URL))
            body.put("custom_install_url", this.customInstallUrl);
        if (shouldUpdate(INSTALL_PARAMS))
            body.put("install_params", this.installParams);
        if (shouldUpdate(INTEGRATION_TYPES_CONFIG))
        {
            DataObject config = DataObject.empty();
            integrationTypeConfig.forEach((key, value) -> config.put(key.name(), DataObject.empty().put("oauth2_install_params", value)));
            body.put("integration_type_config", config);
        }

        reset();
        return getRequestBody(body);
    }

    @Override
    protected void handleSuccess(Response response, Request<Void> request)
    {
        request.onSuccess(null);
    }

    protected void checkUrl(String url)
    {
        Checks.notLonger(url, ApplicationInfo.MAX_URL_LENGTH, "URL");
        Checks.notBlank(url, "URL");
        Checks.noWhitespace(url, "URL");
    }
}
