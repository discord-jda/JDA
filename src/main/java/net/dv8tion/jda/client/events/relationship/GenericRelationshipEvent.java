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

package net.dv8tion.jda.client.events.relationship;

import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

public abstract class GenericRelationshipEvent extends Event
{
    protected final Relationship relationship;

    public GenericRelationshipEvent(JDA api, long responseNumber, Relationship relationship)
    {
        super(api, responseNumber);
        this.relationship = relationship;
    }

    public Relationship getRelationship()
    {
        return  relationship;
    }

    public RelationshipType getRelationshipType()
    {
        return relationship.getType();
    }

    public User getUser()
    {
        return relationship.getUser();
    }
}
