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

package net.dv8tion.jda.test.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.managers.ApplicationManager;
import net.dv8tion.jda.api.managers.ApplicationManager.IntegrationTypeConfig;
import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.managers.ApplicationManagerImpl;
import net.dv8tion.jda.test.IntegrationTest;
import net.dv8tion.jda.test.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

import static net.dv8tion.jda.test.ChecksHelper.assertChecks;
import static net.dv8tion.jda.test.ChecksHelper.assertStringChecks;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class ApplicationManagerTest extends IntegrationTest
{
    @Test
    void minimalApplicationUpdate()
    {
        ApplicationManager manager = new ApplicationManagerImpl(jda);

        manager.setDescription("test");

        assertThatRequestFrom(manager)
            .hasMethod(Method.PATCH)
            .hasCompiledRoute("applications/@me")
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void fullApplicationUpdate()
    {
        ApplicationManager manager = new ApplicationManagerImpl(jda);

        IntegrationTypeConfig installParams = getInstallParams();

        LinkedHashMap<IntegrationType, IntegrationTypeConfig> integrationTypeConfig = new LinkedHashMap<>();
        for (IntegrationType type : IntegrationType.values())
            if (type != IntegrationType.UNKNOWN)
                integrationTypeConfig.put(type, installParams);

        manager.setDescription("test")
            .setIcon(getLogoIcon())
            .setCoverImage(getLogoIcon())
            .setTags(Collections.singleton("test-tag"))
            .setCustomInstallUrl("https://jda.wiki/install")
            .setInteractionsEndpointUrl("https://jda.wiki/interaction")
            .setInstallParams(installParams)
            .setIntegrationTypeConfig(integrationTypeConfig);

        assertThatRequestFrom(manager)
            .hasMethod(Method.PATCH)
            .hasCompiledRoute("applications/@me")
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void testChecks()
    {
        ApplicationManager manager = new ApplicationManagerImpl(jda);

        assertStringChecks("Description", manager::setDescription)
            .checksNotNull()
            .checksNotLonger(ApplicationInfo.MAX_DESCRIPTION_LENGTH);

        assertStringChecks("Tag", value -> manager.setTags(Collections.singleton(value)))
            .checksNotLonger(ApplicationInfo.MAX_TAG_LENGTH);

        assertChecks("Tags",manager::setTags)
            .checksNotNull();

        assertUrlChecks("URL", manager::setCustomInstallUrl);
        assertUrlChecks("URL", manager::setInteractionsEndpointUrl);

        Map<IntegrationType, IntegrationTypeConfig> config = new HashMap<>();
        config.put(null, IntegrationTypeConfig.of(Collections.emptySet(), Collections.emptySet()));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> manager.setIntegrationTypeConfig(config));

        config.clear();
        config.put(IntegrationType.GUILD_INSTALL, null);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> manager.setIntegrationTypeConfig(config));

        config.clear();
        config.put(IntegrationType.UNKNOWN, IntegrationTypeConfig.of(Collections.emptySet(), Collections.emptySet()));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> manager.setIntegrationTypeConfig(config));
    }

    @Nonnull
    @Override
    protected DataObject normalizeRequestBody(@Nonnull DataObject body)
    {
        String iconEncoding = getLogoIcon().getEncoding();
        for (String key : body.keys())
        {
            if (iconEncoding.equals(body.get(key)))
                body.put(key, "[MASKED]");
        }
        return super.normalizeRequestBody(body);
    }

    private static void assertUrlChecks(String name, ThrowingConsumer<String> consumer)
    {
        assertStringChecks(name, consumer)
            .checksNotBlank(false)
            .checksNoWhitespace()
            .checksNotLonger(ApplicationInfo.MAX_URL_LENGTH);
    }

    private static IntegrationTypeConfig getInstallParams()
    {
        Set<String> scopes = new LinkedHashSet<>();
        scopes.add("bot");
        EnumSet<Permission> permissions = EnumSet.of(Permission.MESSAGE_SEND);
        IntegrationTypeConfig installParams = IntegrationTypeConfig.of(scopes, permissions);
        return installParams;
    }

    private Icon getLogoIcon()
    {
        try
        {
            return Icon.from(getResource(Resources.LOGO_PNG), Icon.IconType.PNG);
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }
}
