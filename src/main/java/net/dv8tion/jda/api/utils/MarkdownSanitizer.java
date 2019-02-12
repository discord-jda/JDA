/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;

public class MarkdownSanitizer
{
    public static final int NORMAL =     0;
    public static final int BOLD =      1 << 0; // **x**
    public static final int ITALICS_U = 1 << 1; // _x_
    public static final int ITALICS_A = 1 << 2; // *x*
    public static final int MONO =      1 << 3; // `x`
    public static final int MONO_TWO =  1 << 4; // ``x``
    public static final int BLOCK =     1 << 5; // ```x```
    public static final int SPOILER =   1 << 6; // ||x||
    public static final int UNDERLINE = 1 << 7; // __x__
    public static final int STRIKE =    1 << 8; // ~~x~~ TODO I FORGOT OK


    private final Deque<Integer> modeStack = new ArrayDeque<>();
    private int ignored = 0;
    private boolean escape = false;
    private CharSequence sequence;
    private SanitizationStrategy strategy = SanitizationStrategy.REMOVE;

    private MarkdownSanitizer(CharSequence sequence)
    {
        this.sequence = sequence;
    }

    public static MarkdownSanitizer sanitizer(CharSequence sequence)
    {
        Checks.notNull(sequence, "Input");
        return new MarkdownSanitizer(sequence);
    }

    public MarkdownSanitizer withStrategy(int flags, SanitizationStrategy strategy)
    {
        //TODO: strategy per mode?
        return this;
    }

    public MarkdownSanitizer withStrategy(SanitizationStrategy strategy)
    {
        this.strategy = strategy;
        return this;
    }

    public MarkdownSanitizer ignore(int flags)
    {
        ignored = flags;
        return this;
    }

    private int getNextState(char x, int index, int mode) // TODO: Handle unclosed region? like "`clear" which has no mono end
    {
        char next1 = index + 1 < sequence.length() ? sequence.charAt(index + 1) : ' ';
        char next2 = index + 2 < sequence.length() ? sequence.charAt(index + 2) : ' ';
        char next3 = index + 3 < sequence.length() ? sequence.charAt(index + 3) : ' ';
        switch (x)
        {
            default:
                return mode;
            case '*':
                if (mode == ITALICS_A)
                    return next1 == '*' ? BOLD : -1;        // *ab**c or *ab*
                if (mode == BOLD)
                    return next1 == '*' ? -1 : ITALICS_A;   // **ab** or **ab*c
                if (mode == MONO || mode == MONO_TWO || mode == BLOCK)
                    return mode;                               // `ab* or ``ab* or ```ab*
                return next1 == '*' ? BOLD : ITALICS_A;
            case '_':
                if (mode == ITALICS_U)
                    return next1 == '_' ? UNDERLINE : -1;   // _ab__c or _ab_
                if (mode == MONO || mode == MONO_TWO || mode == BLOCK)
                    return mode;                               // `ab_ or ``ab_ or ```ab_
                return next1 == '_' ? UNDERLINE : ITALICS_U;
            case '|':
                if (mode == SPOILER)
                    return next1 == '|' ? -1 : mode;
                if (mode == MONO || mode == MONO_TWO || mode == BLOCK)
                    return mode;
                return next1 == '|' ? SPOILER : mode;
            case '`':
                if (mode == MONO)
                    return -1;
                if (mode == MONO_TWO)
                    return next1 == '`' ? -1 : mode;
                if (mode == BLOCK)
                    return next1 == '`' && next2 == '`' ? -1 : mode;
                if (next1 == '`')
                    if (next2 == '`')
                        return BLOCK;
                    else
                        return MONO_TWO;
                return MONO;
            case '\\': //TODO escaping modes? Handle stuff like "\\*test*" one end escaped? related to unclosed region
                if (next1 == '`')
                    return Integer.MIN_VALUE | (next2 == '`' ? (next3 == '`' ? BLOCK : MONO_TWO) : MONO);
                if (next1 == '*')
                    return Integer.MIN_VALUE | (next2 == '*' ? BOLD : ITALICS_A);
                if (next1 == '_')
                    return Integer.MIN_VALUE | (next2 == '_' ? UNDERLINE : ITALICS_U);
                if (next1 == '|')
                    return next2 == '|' ? Integer.MIN_VALUE | SPOILER : mode;
        }
        return mode;
    }

    private int getDelta(int state)
    {
        switch (state)
        {
            case BLOCK:
                return 3;
            case BOLD:
            case UNDERLINE:
            case MONO_TWO:
            case SPOILER:
                return 2;
            case MONO:
            case ITALICS_A:
            case ITALICS_U:
                return 1;
        }
        return 0;
    }

    private int appendToken(char c, int mode, StringBuilder builder)
    {
        int delta = 1;
        builder.append(c);
        switch (mode)
        {
            case BOLD:
                builder.append("*");
                delta++;
            case ITALICS_A:
                break;
            case UNDERLINE:
                builder.append("_");
                delta++;
            case ITALICS_U:
                break;
            case SPOILER:
                builder.append("|");
                delta++;
                break;
            case BLOCK:
                builder.append("`");
                delta++;
            case MONO_TWO:
                builder.append("`");
                delta++;
            case MONO:
                break;
            case STRIKE:
                builder.append("~");
                delta++;
                break;
        }
        return delta;
    }

    private boolean cleanupStack(int nextState)
    {
        if (modeStack.contains(nextState))
        {
            while (!modeStack.isEmpty() && modeStack.peek() != nextState)
                modeStack.pop(); //TODO: Handle unclosed region here too
            return true;
        }
        return false;
    }

    public String compute()
    {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sequence.length();)
        {
            char c = sequence.charAt(i);
            int state = modeStack.isEmpty() ? NORMAL : modeStack.peek();
            int nextState = getNextState(c, i, state);
            if (nextState == -1)
            {
                int delta = getDelta(state);
                i += delta;
                strategy.compute.accept(state, builder);
                modeStack.pop();
            }
            else if (nextState != state && (nextState & ignored) == 0)
            {
                strategy.compute.accept(nextState, builder);
                if (cleanupStack(nextState))
                {
                    i += getDelta(nextState);
                    continue;
                }
                if (nextState != NORMAL)
                    modeStack.push(nextState);
                int delta = getDelta(nextState);
                if (delta == 0)
                    i++;
                else
                    i += delta;
            }
            else if ((nextState & ignored) != 0)
            {
                i += appendToken(c, nextState, builder);
            }
            else
            {
                builder.append(c);
                i++;
            }
        }
        return builder.toString();
    }

    public enum SanitizationStrategy
    {
        REMOVE((m, b) -> {}),
        ESCAPE((m, b) -> {
            if (m == NORMAL)
                return;
            b.append('\\');
            switch (m)
            {
                case BOLD:
                    b.append("**");
                    break;
                case ITALICS_A:
                    b.append('*');
                    break;
                case UNDERLINE:
                    b.append('_');
                case ITALICS_U:
                    b.append('_');
                    break;
                case BLOCK:
                    b.append('`');
                case MONO_TWO:
                    b.append('`');
                case MONO:
                    b.append('`');
                    break;
                case SPOILER:
                    b.append("||");
                    break;
            }
        });

        private final BiConsumer<Integer, StringBuilder> compute;

        SanitizationStrategy(BiConsumer<Integer, StringBuilder> compute)
        {
            this.compute = compute;
        }
    }
}
