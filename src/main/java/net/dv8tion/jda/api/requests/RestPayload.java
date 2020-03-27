/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//TODO: Documentation
public class RestPayload implements SerializableData
{
    private final int code;
    private final byte[] data;

    public RestPayload(int code, byte[] data)
    {
        this.code = code;
        this.data = data;
    }

    public int getCode()
    {
        return code;
    }

    public boolean isType(@Nonnull DataType type)
    {
        Checks.notNull(type, "DataType");
        switch (type)
        {
            default:
                return false;
            case OBJECT:
                return data[0] == (byte) '{';
            case ARRAY:
                return data[0] == (byte) '[';
            case NULL:
                return data.length == 0;
        }
    }

    @Nonnull
    public DataType getType()
    {
        if (data.length == 0)
            return DataType.NULL;
        switch (data[0])
        {
            case '{': return DataType.OBJECT;
            case '[': return DataType.ARRAY;
            default:  return DataType.UNKNOWN;
        }
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.fromJson(toInputStream());
    }

    @Nonnull
    public DataArray toArray()
    {
        return DataArray.fromJson(toInputStream());
    }

    @Nonnull
    @Override
    public String toString()
    {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof RestPayload))
            return false;
        RestPayload p = (RestPayload) obj;
        return Arrays.equals(data, p.data);
    }

    @Nonnull
    public byte[] toBytes()
    {
        return data;
    }

    @Nonnull
    public InputStream toInputStream()
    {
        return new ByteArrayInputStream(data);
    }
}
