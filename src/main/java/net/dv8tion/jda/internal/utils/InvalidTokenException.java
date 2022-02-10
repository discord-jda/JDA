package net.dv8tion.jda.internal.utils;

/**
 * This class represents an exception which is thrown when the token provided by the user is invalid
 * Replaces {@link javax.security.auth.login.LoginException}
 *
 * @author java-coding-prodigy
 * @since 5.0.0
 * */
public class InvalidTokenException extends RuntimeException
{

    /**
     * Constructs an <code>InvalidTokenException</code> with no detail message.
     * */
    public InvalidTokenException()
    {
        super();
    }
    /**
     * Constructs a <code>InvalidTokenException</code> with the
     * specified detail message.
     *
     * @param   message   the detail message.
     */
    public InvalidTokenException(String message)
    {
        super(message);
    }

    

    public InvalidTokenException(String message, Throwable rootCause)
    {
        super(message, rootCause);
    }


}
