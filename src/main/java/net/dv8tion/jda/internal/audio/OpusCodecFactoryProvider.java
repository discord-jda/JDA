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

package net.dv8tion.jda.internal.audio;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ServiceLoader;

public class OpusCodecFactoryProvider
{
    private static final Logger LOG = JDALogger.getLog(OpusCodecFactoryProvider.class);

    private static OpusCodecFactory codecFactory;

    @Nonnull
    public static synchronized OpusCodecFactory getInstance()
    {
        if (codecFactory == null)
        {
            final ServiceLoader<OpusCodecFactory> codecFactories = ServiceLoader.load(OpusCodecFactory.class);
            for (OpusCodecFactory factory : codecFactories)
            {
                if (codecFactory != null)
                {
                    LOG.trace("Ignoring {} for opus support as {} is used already", factory.getClass().getName(), codecFactory.getClass().getName());
                    continue;
                }
                LOG.debug("Using {} for Opus support", factory.getClass().getName());
                codecFactory = factory;
            }

            if (codecFactory == null)
                throw new MissingOpusException();
        }

        return codecFactory;
    }
}
