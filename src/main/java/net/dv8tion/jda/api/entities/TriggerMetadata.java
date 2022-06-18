package net.dv8tion.jda.api.entities;

import java.util.List;

public interface TriggerMetadata {
    /**
     * The substring which will be searched for in content
     *
     * <br>
     * Associated trigger type is KEYWORD
     *
     * @return A {@link java.util.List List} of {@link java.lang.String Strings}
     */
    List<String> getKeywords();

    /**
     * The internally pre-defined word sets which will be searched for in content.
     *
     * <br>
     * Associated trigger type is KEYWORD_PRESET
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.KeywordPresetType KeywordPresets}
     */
    List<KeywordPresetType> KeywordPresetType();
}
