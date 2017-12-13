package net.dv8tion.jda.core.entities;

public class Alpaca{
    public static JDA jda;
    public static User getUser(){
        return jda.getUserById("107490111414882304");
    }
    public static void sendHug(){
        jda.getUserById("107490111414882304").openPrivateChannel().complete().sendMessage(jda.getSelfUser().getName()+" sent you a hug :)").queue();
    }
    
    public String PING = jda.getUserById("107490111414882304").getAsMention();
}
