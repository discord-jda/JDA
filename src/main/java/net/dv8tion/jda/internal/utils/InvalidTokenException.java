package net.dv8tion.jda.internal.utils;

/**
 * Indicates that an invalid token was given when trying to login the Discord API
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
     * Constructs an <code>InvalidTokenException</code> with the
     * specified detail message.
     *
     * @param  message   
               The detail message.
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
