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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Collectibles;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollectiblesImpl implements Collectibles {
    public static final Collectibles EMPTY = new CollectiblesImpl(DataObject.empty());

    private final NameplateImpl nameplate;

    private CollectiblesImpl(DataObject o) {
        this.nameplate = o.optObject("nameplate").map(NameplateImpl::new).orElse(null);
    }

    @Nonnull
    public static Collectibles extractFrom(@Nonnull DataObject o) {
        if (o.isNull("collectibles")) {
            return EMPTY;
        }
        DataObject object = o.getObject("collectibles");
        // Avoid allocations if there are no collectibles
        if (object.isNull("nameplate")) {
            return EMPTY;
        }
        return new CollectiblesImpl(object);
    }

    @Nullable
    @Override
    public NameplateImpl getNameplate() {
        return nameplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectiblesImpl)) {
            return false;
        }

        CollectiblesImpl that = (CollectiblesImpl) o;
        return Objects.equals(nameplate, that.nameplate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nameplate);
    }

    @Override
    public String toString() {
        return new EntityString(this).addMetadata("nameplate", nameplate).toString();
    }

    public static class NameplateImpl implements Nameplate {
        private final String sku, asset, palette;

        public NameplateImpl(DataObject o) {
            this.sku = o.getString("sku_id");
            this.asset = o.getString("asset");
            this.palette = o.getString("palette");
        }

        @Nonnull
        @Override
        public String getSkuId() {
            return sku;
        }

        @Nonnull
        @Override
        public String getAssetPath() {
            return asset;
        }

        @Nonnull
        @Override
        public String getPalette() {
            return palette;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NameplateImpl)) {
                return false;
            }

            NameplateImpl nameplate = (NameplateImpl) o;
            return Objects.equals(sku, nameplate.sku)
                    && Objects.equals(asset, nameplate.asset)
                    && Objects.equals(palette, nameplate.palette);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sku, asset, palette);
        }

        @Override
        public String toString() {
            return new EntityString(this)
                    .addMetadata("asset_path", asset)
                    .addMetadata("palette", palette)
                    .toString();
        }
    }

    public static class Effective implements Collectibles {
        private final Member member;

        public Effective(Member member) {
            this.member = member;
        }

        @Nullable
        @Override
        public Nameplate getNameplate() {
            Nameplate memberNameplate = member.getCollectibles().getNameplate();
            return memberNameplate != null
                    ? memberNameplate
                    : member.getUser().getCollectibles().getNameplate();
        }
    }
}
