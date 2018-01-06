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

package net.dv8tion.jda.core.requests;

import org.json.JSONObject;

public enum ErrorResponse
{
    UNKNOWN_ACCOUNT(    10001, "Unknown Account"),
    UNKNOWN_APPLICATION(10002, "Unknown Application"),
    UNKNOWN_CHANNEL(    10003, "Unknown Channel"),
    UNKNOWN_GUILD(      10004, "Unknown Guild"),
    UNKNOWN_INTEGRATION(10005, "Unknown Integration"),
    UNKNOWN_INVITE(     10006, "Unknown Invite"),
    UNKNOWN_MEMBER(     10007, "Unknown Member"),
    UNKNOWN_MESSAGE(    10008, "Unknown Message"),
    UNKNOWN_OVERRIDE(   10009, "Unknown Override"),
    UNKNOWN_PROVIDER(   10010, "Unknown Provider"),
    UNKNOWN_ROLE(       10011, "Unknown Role"),
    UNKNOWN_TOKEN(      10012, "Unknown Token"),
    UNKNOWN_USER(       10013, "Unknown User"),
    UNKNOWN_EMOJI(      10014, "Unknown Emoji"),
    UNKNOWN_WEBHOOK(    10015, "Unknown Webhook"),
    BOTS_NOT_ALLOWED(   20001, "Bots cannot use this endpoint"),
    ONLY_BOTS_ALLOWED(  20002, "Only bots can use this endpoint"),
    MAX_GUILDS(         30001, "Maximum number of Guilds reached (100)"),
    MAX_FRIENDS(        30002, "Maximum number of Friends reached (1000)"),
    MAX_MESSAGE_PINS(   30003, "Maximum number of pinned messages reached (50)"),
    MAX_USERS_PER_DM(   30004, "Maximum number of recipients reached. (10)"),
    MAX_ROLES_PER_GUILD(30005, "Maximum number of guild roles reached (250)"),
    TOO_MANY_REACTIONS( 30010, "Too many reactions"),
    UNAUTHORIZED(       40001, "Unauthorized"),
    MISSING_ACCESS(     50001, "Missing Access"),
    INVALID_ACCOUNT_TYPE(50002,"Invalid Account Type"),
    INVALID_DM_ACTION(  50003, "Cannot execute action on a DM channel"),
    EMBED_DISABLED(     50004, "Embed Disabled"),
    INVALID_AUTHOR_EDIT(50005, "Cannot edit a message authored by another user"),
    EMPTY_MESSAGE(      50006, "Cannot send an empty message"),
    CANNOT_SEND_TO_USER(50007, "Cannot send messages to this user"),
    CANNOT_MESSAGE_VC(  50008, "Cannot send messages in a voice channel"),
    VERIFICATION_ERROR( 50009, "Channel verification level is too high"),
    OAUTH_NOT_BOT(      50010, "OAuth2 application does not have a bot"),
    MAX_OAUTH_APPS(     50011, "OAuth2 application limit reached"),
    INVALID_OAUTH_STATE(50012, "Invalid OAuth state"),
    MISSING_PERMISSIONS(50013, "Missing Permissions"),
    INVALID_TOKEN(      50014, "Invalid Authentication Token"),
    NOTE_TOO_LONG(      50015, "Note is too long"),
    INVALID_BULK_DELETE(50016, "Provided too few or too many messages to delete. Must provided at least 2 and fewer than 100 messages to delete"),
    INVALID_MFA_LEVEL(  50017, "Provided MFA level was invalid."),
    INVALID_PASSWORD(   50018, "Provided password was invalid"),
    INVALID_PIN(        50019, "A message can only be pinned to the channel it was sent in"),
    INVALID_MESSAGE_TARGET(50021, "Cannot execute action on a system message"),
    INVALID_BULK_DELETE_MESSAGE_AGE(50034, "A Message provided to bulk_delete was older than 2 weeks"),
    MFA_NOT_ENABLED(    60003, "MFA auth required but not enabled"),
    REACTION_BLOCKED(   90001, "Reaction Blocked"),

    SERVER_ERROR(           0, "Discord encountered an internal server error! Not good!");


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

    public String getMeaning()
    {
        return meaning;
    }

    public static ErrorResponse fromCode(int code)
    {
        for (ErrorResponse error : values())
        {
            if (code == error.getCode())
                return error;
        }
        return SERVER_ERROR;
    }

    public static ErrorResponse fromJSON(JSONObject obj)
    {
        if (obj == null || obj.isNull("code"))
            return SERVER_ERROR;
        else
        {
            return ErrorResponse.fromCode(obj.getInt("code"));
        }
    }
}
