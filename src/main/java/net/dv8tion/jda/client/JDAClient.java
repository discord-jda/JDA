/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.client;

import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public interface JDAClient
{
    JDA getJDA();

    List<Group> getGroups();
    List<Group> getGroupsByName(String name, boolean ignoreCase);
    Group getGroupById(String id);

    List<Relationship> getRelationships();
    List<Relationship> getRelationships(RelationshipType type);
    List<Relationship> getRelationships(RelationshipType type, String name, boolean ignoreCase);
    List<Relationship> getRelationshipsByName(String name, boolean ignoreCase);
    Relationship getRelationship(User user);
    Relationship getRelationship(Member member);
    Relationship getRelationshipById(String id);
    Relationship getRelationshipById(String id, RelationshipType type);

    List<Friend> getFriends();
    List<Friend> getFriendsByName(String name, boolean ignoreCase);
    Friend getFriend(User user);
    Friend getFriend(Member member);
    Friend getFriendById(String id);

    UserSettings getSettings();
}
