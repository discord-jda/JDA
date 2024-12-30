package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Icon;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Implemented only description, icon and cover_image.
 * <br>For more fields check Discord docs: <a href="https://discord.com/developers/docs/resources/application#edit-current-application">https://discord.com/developers/docs/resources/application#edit-current-application</a>
 */
public interface ApplicationManager extends Manager<ApplicationManager>
{

    /**
     * Used to set description
     */
    long DESCRIPTION = 1;
    /**
     * Used to set icon
     */
    long ICON = 1 << 1;
    /**
     * Used to set cover_image
     */
    long COVER_IMAGE = 1 << 2;

    /**
     * The {@link net.dv8tion.jda.api.entities.ApplicationInfo ApplicationInfo} associated to the bot.
     *
     * @return The corresponding ApplicationInfo
     */
    @Nonnull
    ApplicationInfo getApplicationInfo();

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ApplicationManager.ICON | ApplicationManager.COVER_IMAGE);}
     * <br>For all flags, check {@link ApplicationManager} fields.
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ApplicationManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    ApplicationManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ApplicationManager.ICON, ApplicationManager.COVER_IMAGE);}
     * <br>For all flags, check {@link ApplicationManager} fields.
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ApplicationManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    ApplicationManager reset(long... fields);


    /**
     * Sets the description of the application.
     *
     * @param  description
     *         The new description
     *
     * @return ApplicationManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ApplicationManager setDescription(@Nonnull String description);

    /**
     * Sets the icon of the application.
     *
     * @param  icon
     *         The new icon
     *
     * @return ApplicationManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ApplicationManager setIcon(@Nonnull Icon icon);

    /**
     * Sets the cover image of the application.
     *
     * @param  coverImage
     *         The new coverImage
     *
     * @return ApplicationManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ApplicationManager setCoverImage(@Nonnull Icon coverImage);

}
