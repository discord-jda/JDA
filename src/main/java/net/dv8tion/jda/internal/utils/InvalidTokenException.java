package net.dv8tion.jda.internal.utils;

public class InvalidTokenException extends RuntimeException
{


    public InvalidTokenException()
    {
        super();
    }

    public InvalidTokenException(String message)
    {
        super(message);
    }

    

    public InvalidTokenException(String message, Throwable rootCause)
    {
        super(message, rootCause);
    }


}
