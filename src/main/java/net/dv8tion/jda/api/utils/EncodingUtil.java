package net.dv8tion.jda.api.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class EncodingUtil {
    /**
     * URL-Encodes the given String to UTF-8 after
     * form-data specifications (space {@literal ->} +)
     *
     * @param  chars
     *         The characters to encode
     *
     * @return The encoded String
     */
    public static String encodeUTF8(String chars)
    {
        try
        {
            return URLEncoder.encode(chars, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError(e); // thanks JDK 1.4
        }
    }

    public static String encodeCodepointsUTF8(String input)
    {
        if (!input.startsWith("U+"))
            throw new IllegalArgumentException("Invalid format");
        String[] codePoints = input.substring(2).split("\\s*U\\+\\s*");
        StringBuilder encoded = new StringBuilder();
        for (String part : codePoints)
        {
            String utf16 = decodeCodepoint(part);
            String urlEncoded = encodeUTF8(utf16);
            encoded.append(urlEncoded);
        }
        return encoded.toString();
    }

    public static String decodeCodepoint(String codepoint)
    {
        if (!codepoint.startsWith("U+"))
            throw new IllegalArgumentException("Invalid format");
        int codePoint = Integer.parseUnsignedInt(codepoint.substring(2), 16);
        char[] chars = Character.toChars(codePoint);
        return String.valueOf(chars);
    }

    public static String encodeCodepoints(String unicode)
    {
        return unicode.codePoints()
               .mapToObj(code -> "U+" + Integer.toHexString(code))
               .collect(Collectors.joining());
    }
}
