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


    private final Deque<Integer> modeStack = new ArrayDeque<>();
    private int ignored = 0;
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

    private int getNextState(char x, int index, int mode)
    {
        char nextChar = index + 1 < sequence.length() ? sequence.charAt(index + 1) : ' ';
        switch (x)
        {
            default:
                return mode;
            case '*':
                if (mode == ITALICS_A)
                    return nextChar == '*' ? BOLD : -1;        // *ab**c or *ab*
                if (mode == BOLD)
                    return nextChar == '*' ? -1 : ITALICS_A;   // **ab** or **ab*c
                if (mode == MONO || mode == MONO_TWO || mode == BLOCK)
                    return mode;                               // `ab* or ``ab* or ```ab*
                return nextChar == '*' ? BOLD : ITALICS_A;
            case '_':
                if (mode == ITALICS_U)
                    return nextChar == '_' ? UNDERLINE : -1;   // _ab__c or _ab_
                if (mode == MONO || mode == MONO_TWO || mode == BLOCK)
                    return mode;                               // `ab_ or ``ab_ or ```ab_
                return nextChar == '_' ? UNDERLINE : ITALICS_U;
            case '|':
                if (mode == SPOILER)
                    return nextChar == '|' ? -1 : mode;
                if (mode == MONO || mode == MONO_TWO || mode == BLOCK)
                    return mode;
                return nextChar == '|' ? SPOILER : mode;
            case '`':
                char lateNext = index + 2 < sequence.length() ? sequence.charAt(index + 2) : ' ';
                if (mode == MONO)
                    return -1;
                if (mode == MONO_TWO)
                    return nextChar == '`' ? -1 : mode;
                if (mode == BLOCK)
                    return nextChar == '`' && lateNext == '`' ? -1 : mode;
                if (nextChar == '`')
                    if (lateNext == '`')
                        return BLOCK;
                    else
                        return MONO_TWO;
                return MONO;
            case '\\': //TODO escaping modes?
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

    public String compute() // TODO: ignored
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
            else if (nextState != state)
            {
                strategy.compute.accept(nextState, builder);
                modeStack.push(nextState);
                int delta = getDelta(nextState);
                if (delta == 0)
                {
                    builder.append(c);
                    i++;
                }
                else
                {
                    i += delta;
                }
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
