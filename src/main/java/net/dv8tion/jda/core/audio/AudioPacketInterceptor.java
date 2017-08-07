/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.audio;

import net.dv8tion.jda.core.entities.User;

/**
 * Semi-internal API, used to intercept decrypted AudioPackets before they get processed by the 
 * receiver thread of {@link net.dv8tion.jda.core.audio.AudioConnection AudioConnection}.
 */
public interface AudioPacketInterceptor 
{
    
     /**
     * This method is called whenever the {@link net.dv8tion.jda.core.audio.AudioConnection AudioConnection} receive 
     * thread has successfully decrypted an audio packet, but before it is passed trough the OPUS decoder and the dispatch
     * mechanism of JDA.
     * For further documentation on the internals, refer to {@link net.dv8tion.jda.core.audio.AudioPacket AudioPacket}.
     * <p>
     * <b>Be vary that JDA does not guarantee the order and timing of incoming packets, implementors are responsible
     * for reassembling audio streams themselves.</b>
     * <p>
     * The method is called from the network receive thread, just like {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(UserAudio) AudioReceiveHandler.handleUserAudio}
     * <p>
     * Note that by returning true, JDA will discard the packet without further processing.
     *
     * @param decryptedPacket Audio packet that has been received, and successfully decrypted
     * @param user User the packet originated from
     *
     * @return If true, the packet is discarded, and won't be decoded or passed to the registered {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler}
     */
    boolean handleDecryptedPacket(AudioPacket decryptedPacket, User user);
    
}
