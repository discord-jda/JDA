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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Constants for easy use in {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseException} and {@link net.dv8tion.jda.api.exceptions.ErrorHandler ErrorHandler}.
 *
 * @see RestAction
 * @see net.dv8tion.jda.api.exceptions.ErrorHandler ErrorHandler
 * @see <a href="https://discord.com/developers/docs/topics/opcodes-and-status-codes#json">Discord Error Codes</a>
 */
public enum ErrorResponse
{
    UNKNOWN_ACCOUNT(                10001, "Unknown Account"),
    UNKNOWN_APPLICATION(            10002, "Unknown Application"),
    UNKNOWN_CHANNEL(                10003, "Unknown Channel"),
    UNKNOWN_GUILD(                  10004, "Unknown Guild"),
    UNKNOWN_INTEGRATION(            10005, "Unknown Integration"),
    UNKNOWN_INVITE(                 10006, "Unknown Invite"),
    UNKNOWN_MEMBER(                 10007, "Unknown Member"),
    UNKNOWN_MESSAGE(                10008, "Unknown Message"),
    UNKNOWN_OVERRIDE(               10009, "Unknown Override"),
    UNKNOWN_PROVIDER(               10010, "Unknown Provider"),
    UNKNOWN_ROLE(                   10011, "Unknown Role"),
    UNKNOWN_TOKEN(                  10012, "Unknown Token"),
    UNKNOWN_USER(                   10013, "Unknown User"),
    UNKNOWN_EMOJI(                  10014, "Unknown Emoji"),
    UNKNOWN_WEBHOOK(                10015, "Unknown Webhook"),
    UNKNOWN_BAN(                    10026, "Unknown Ban"),
    UNKNOWN_SKU(                    10027, "Unknown SKU"),
    UNKNOWN_STORE_LISTING(          10028, "Unknown Store Listing"),
    UNKNOWN_ENTITLEMENT(            10029, "Unknown Entitlement"),
    UNKNOWN_BUILD(                  10030, "Unknown Build"),
    UNKNOWN_LOBBY(                  10031, "Unknown Lobby"),
    UNKNOWN_BRANCH(                 10032, "Unknown Branch"),
    UNKNOWN_REDISTRIBUTABLE(        10036, "Unknown Redistributable"),
    UNKNOWN_GUILD_TEMPLATE(         10057, "Unknown Guild Template"),
    UNKNOWN_INTERACTION(            10062, "Unknown Interaction"),
    UNKNOWN_COMMAND(                10063, "Unknown application command"),
    UNKNOWN_COMMAND_PERMISSIONS(    10066, "Unknown application command permissions"),
    UNKNOWN_STAGE_INSTANCE(         10067, "Unknown Stage Instance"),
    BOTS_NOT_ALLOWED(               20001, "Bots cannot use this endpoint"),
    ONLY_BOTS_ALLOWED(              20002, "Only bots can use this endpoint"),
    MAX_GUILDS(                     30001, "Maximum number of Guilds reached (100)"),
    MAX_FRIENDS(                    30002, "Maximum number of Friends reached (1000)"),
    MAX_MESSAGE_PINS(               30003, "Maximum number of pinned messages reached (50)"),
    MAX_USERS_PER_DM(               30004, "Maximum number of recipients reached. (10)"),
    MAX_ROLES_PER_GUILD(            30005, "Maximum number of guild roles reached (250)"),
    MAX_WEBHOOKS(                   30007, "Maximum number of webhooks reached (10)"),
    TOO_MANY_REACTIONS(             30010, "Maximum number of reactions reached (20)"),
    MAX_CHANNELS(                   30013, "Maximum number of guild channels reached (500)"),
    MAX_INVITES(                    30016, "Maximum number of invites reached (1000)"),
    ALREADY_HAS_TEMPLATE(           30031, "Guild already has a template"),
    UNAUTHORIZED(                   40001, "Unauthorized"),
    REQUEST_ENTITY_TOO_LARGE(       40005, "Request entity too large"),
    USER_NOT_CONNECTED(             40032, "Target user is not connected to voice."),
    ALREADY_CROSSPOSTED(            40033, "This message has already been crossposted."),
    MISSING_ACCESS(                 50001, "Missing Access"),
    INVALID_ACCOUNT_TYPE(           50002, "Invalid Account Type"),
    INVALID_DM_ACTION(              50003, "Cannot execute action on a DM channel"),
    EMBED_DISABLED(                 50004, "Widget Disabled"),
    INVALID_AUTHOR_EDIT(            50005, "Cannot edit a message authored by another user"),
    EMPTY_MESSAGE(                  50006, "Cannot send an empty message"),
    CANNOT_SEND_TO_USER(            50007, "Cannot send messages to this user"),
    CANNOT_MESSAGE_VC(              50008, "Cannot send messages in a voice channel"),
    VERIFICATION_ERROR(             50009, "Channel verification level is too high"),
    OAUTH_NOT_BOT(                  50010, "OAuth2 application does not have a bot"),
    MAX_OAUTH_APPS(                 50011, "OAuth2 application limit reached"),
    INVALID_OAUTH_STATE(            50012, "Invalid OAuth state"),
    MISSING_PERMISSIONS(            50013, "Missing Permissions"),
    INVALID_TOKEN(                  50014, "Invalid Authentication Token"),
    NOTE_TOO_LONG(                  50015, "Note is too long"),
    INVALID_BULK_DELETE(            50016, "Provided too few or too many messages to delete. Must provided at least 2 and fewer than 100 messages to delete"),
    INVALID_MFA_LEVEL(              50017, "Provided MFA level was invalid."),
    INVALID_PASSWORD(               50018, "Provided password was invalid"),
    INVALID_PIN(                    50019, "A message can only be pinned to the channel it was sent in"),
    INVITE_CODE_INVALID(            50020, "Invite code is either invalid or taken"),
    INVALID_MESSAGE_TARGET(         50021, "Cannot execute action on a system message"),
    INVALID_OAUTH_ACCESS_TOKEN(     50025, "Invalid OAuth2 access token"),
    INVALID_WEBHOOK_TOKEN(          50027, "Invalid Webhook Token"),
    INVALID_BULK_DELETE_MESSAGE_AGE(50034, "A Message provided to bulk_delete was older than 2 weeks"),
    INVALID_FORM_BODY(              50035, "Invalid Form Body"),
    INVITE_FOR_UNKNOWN_GUILD(       50036, "An invite was accepted to a guild the application's bot is not in"),
    INVALID_API_VERSION(            50041, "Invalid API version"),
    MFA_NOT_ENABLED(                60003, "MFA auth required but not enabled"),
    REACTION_BLOCKED(               90001, "Reaction Blocked"),
    RESOURCES_OVERLOADED(          130000, "Resource overloaded"),
    STAGE_ALREADY_OPEN(            150006, "The Stage is already open"),

    SERVER_ERROR(                       0, "Discord encountered an internal server error! Not good!");


    private final int code;
    private final String meaning;

    ErrorResponse(int code, String meaning)
    {
        this.code = code;
        this.meaning = meaning;
    }

    public int getCode()
    {
        return code;
    }

    @Nonnull
    public String getMeaning()
    {
        return meaning;
    }

    /**
     * Tests whether the given throwable is an {@link ErrorResponseException} with {@link ErrorResponseException#getErrorResponse()} equal to this.
     * <br>This is very useful in combination with {@link RestAction#onErrorMap(Predicate, Function)} and {@link RestAction#onErrorFlatMap(Predicate, Function)}!
     *
     * @param  throwable
     *         The throwable to test
     *
     * @return True, if the error response is equal to this
     */
    public boolean test(Throwable throwable)
    {
        return throwable instanceof ErrorResponseException && ((ErrorResponseException) throwable).getErrorResponse() == this;
    }

    /**
     * Provides a tests whether a given throwable is an {@link ErrorResponseException} with {@link ErrorResponseException#getErrorResponse()} being one of the provided responses.
     * <br>This is very useful in combination with {@link RestAction#onErrorMap(Predicate, Function)} and {@link RestAction#onErrorFlatMap(Predicate, Function)}!
     *
     * @param  responses
     *         The responses to test for
     *
     * @return {@link Predicate} which returns true, if the error response is equal to this
     */
    @Nonnull
    public static Predicate<Throwable> test(@Nonnull ErrorResponse... responses)
    {
        Checks.noneNull(responses, "ErrorResponse");
        EnumSet<ErrorResponse> set = EnumSet.noneOf(ErrorResponse.class);
        Collections.addAll(set, responses);
        return test(set);
    }

    /**
     * Provides a tests whether a given throwable is an {@link ErrorResponseException} with {@link ErrorResponseException#getErrorResponse()} being one of the provided responses.
     * <br>This is very useful in combination with {@link RestAction#onErrorMap(Predicate, Function)} and {@link RestAction#onErrorFlatMap(Predicate, Function)}!
     *
     * @param  responses
     *         The responses to test for
     *
     * @return {@link Predicate} which returns true, if the error response is equal to this
     */
    @Nonnull
    public static Predicate<Throwable> test(@Nonnull Collection<ErrorResponse> responses)
    {
        Checks.noneNull(responses, "ErrorResponse");
        EnumSet<ErrorResponse> set = EnumSet.copyOf(responses);
        return (error) -> error instanceof ErrorResponseException && set.contains(((ErrorResponseException) error).getErrorResponse());

    }

    @Nonnull
    public static ErrorResponse fromCode(int code)
    {
        for (ErrorResponse error : values())
        {
            if (code == error.getCode())
                return error;
        }
        return SERVER_ERROR;
    }

    @Nonnull
    public static ErrorResponse fromJSON(@Nullable DataObject obj)
    {
        if (obj == null || obj.isNull("code"))
            return SERVER_ERROR;
        return ErrorResponse.fromCode(obj.getInt("code"));
    }
}
