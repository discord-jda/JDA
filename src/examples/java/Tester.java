import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Region;
import net.dv8tion.jda.audio.player.Player;
import net.dv8tion.jda.audio.player.URLPlayer;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.VoiceStatus;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.AvatarUtil;
import net.dv8tion.jda.utils.InviteUtil;
import org.apache.http.HttpHost;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Austin on 1/4/2016.
 */
public class Tester extends ListenerAdapter
{
    Player player;

    public static void main(String[] args) throws LoginException, InterruptedException
    {
        JSONObject config = ExampleUtils.getConfig();
        JDABuilder builder = new JDABuilder()
                .setEmail(config.getString("email"))
                .setPassword(config.getString("password"))
                .addListener(new Tester())
                .setDebug(true);

//        if (!config.getString("proxyHost").isEmpty())
//            builder.setProxy(config.getString("proxyHost"), config.getInt("proxyPort"));

        JDA api = builder.buildBlocking();

        if (api.getGlobalProxy() != null)
        {
            HttpHost proxy = api.getGlobalProxy();
            System.setProperty("http.proxyHost", proxy.getHostName());
            System.setProperty("http.proxyPort", "" + proxy.getPort());
            System.setProperty("https.proxyHost", proxy.getHostName());
            System.setProperty("https.proxyPort", "" + proxy.getPort());
        }
    }

    @Override
    public void onVoiceJoin(VoiceJoinEvent event)
    {
        event.getChannel().getGuild().getTextChannels().stream()
                .filter(chan -> chan.getName().equalsIgnoreCase("testing"))
                .findFirst()
                .orElse(event.getChannel().getGuild().getTextChannels().get(0))
                .sendMessage(new MessageBuilder()
                        .appendString(event.getUser().getUsername())
                        .appendString(" just joined the voice channel: ")
                        .appendString(event.getChannel().getName())
                        .build());
    }

    @Override
    public void onVoiceLeave(VoiceLeaveEvent event)
    {
        event.getOldChannel().getGuild().getTextChannels().stream()
                .filter(chan -> chan.getName().equalsIgnoreCase("testing") || chan.getName().equalsIgnoreCase("bot-testing"))
                .findFirst()
                .orElse(event.getOldChannel().getGuild().getTextChannels().get(0))
                .sendMessage(new MessageBuilder()
                        .appendString(event.getUser().getUsername())
                        .appendString(" just left the voice channel: ")
                        .appendString(event.getOldChannel().getName())
                        .build());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        System.out.printf("[%s]<%s> %s\n",
                event.getChannel().getName(),
                event.getAuthor().getUsername(),
                event.getMessage().getContent());
        if (event.getAuthor().getId().equals(event.getJDA().getSelfInfo().getId()))
            return;
        if (!event.getAuthor().getUsername().equals("DV8FromTheWorld")
                && !event.getAuthor().getUsername().equals("Kantenkugel")
                && !event.getAuthor().getUsername().equals("Almighty Alpaca"))
            return;

        VoiceChannel voiceChannel = event.getChannel().getGuild().getVoiceChannels().stream()
                .filter(chan -> chan.getName().equalsIgnoreCase("testing") || chan.getName().equalsIgnoreCase("bot-testing"))
                .findFirst()
                .orElse(event.getChannel().getGuild().getVoiceChannels().get(0));

        if (event.getMessage().getContent().startsWith("list"))
        {
            List<VoiceStatus> statuses = event.getGuild().getVoiceStatuses().stream().filter(status -> status.inVoiceChannel()).collect(Collectors.toList());
            MessageBuilder builder = new MessageBuilder();
            builder.appendString("Guild has **")
                    .appendString("" + statuses.size())
                    .appendString("** users a VoiceChannels.\n")
                    .appendString("__The following users are in a VoiceChannel:__\n\n");
            for (VoiceStatus status : statuses)
            {
                builder.appendString(status.getUser().getUsername()).appendString("\n");
            }
            event.getChannel().sendMessage(builder.build());

        }
        if (event.getMessage().getContent().startsWith("join"))
        {
            event.getJDA().getAudioManager().openAudioConnection(voiceChannel);
        }
        if (event.getMessage().getContent().startsWith("leave"))
        {
            event.getJDA().getAudioManager().closeAudioConnection();
        }
        if (event.getMessage().getContent().startsWith("play"))
        {
            try
            {
                System.out.println("starting talkings");
                if (player == null)
                {
//                    player = new FilePlayer();
//                    player.setAudioFile(new File("anime-48000.mp3"));
                    URL url = new URL("https://dl.dropboxusercontent.com/u/41124983/anime-48000.mp3?dl=1");
                    player = new URLPlayer(event.getJDA(), url);
                    event.getJDA().getAudioManager().setSendingHandler(player);
                }

                player.play();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (UnsupportedAudioFileException e)
            {
                e.printStackTrace();
            }
        }
        if (event.getMessage().getContent().startsWith("pause"))
            player.pause();
        if (event.getMessage().getContent().startsWith("restart"))
            player.restart();
        if (event.getMessage().getContent().startsWith("stop"))
            player.stop();
//        if (event.getMessage().getContent().startsWith("guilds"))
//        {
//            MessageBuilder builder = new MessageBuilder();
//            builder.appendString("I am in the following guilds: \n");
//            for (Guild guild : event.getJDA().getGuilds())
//            {
//                builder.appendString(guild.getName() + "\n");
//            }
//            event.getChannel().sendMessage(builder.build());
//        }

//        int permVal = PermissionUtil.getEffectivePermission(event.getAuthor(), event.getChannel());
//        List<Permission> perms = Permission.getPermissions(permVal);
//        MessageBuilder builder = new MessageBuilder();
//            builder.appendString(event.getAuthor().getUsername()).appendString(" has the following effective perms.\n");
//            for (Permission perm : perms)
//            {
//                builder.appendString(perm.toString()).appendString("\n");
//            }
//            event.getChannel().sendMessage(builder.build());

//        if (command(event, "allow"))
//        {
//            PermissionOverride over = event.getChannel().getOverrideForUser(event.getAuthor());
//            if (over == null)
//            {
//                event.getChannel().sendMessage("User has no overrides in this channel");
//                return;
//            }
//            MessageBuilder builder = new MessageBuilder();
//            builder.appendString(event.getAuthor().getUsername()).appendString(" has the following perms set to Allow.\n");
//            for (Permission perm : over.getAllowed())
//            {
//                builder.appendString(perm.toString()).appendString("\n");
//            }
//            event.getChannel().sendMessage(builder.build());
//        }
//        if (command(event, "deny"))
//        {
//            PermissionOverride over = ((TextChannelImpl) event.getChannel()).getOverrideForUser(event.getAuthor());
//            if (over == null)
//            {
//                event.getChannel().sendMessage("User has no overrides in this channel");
//                return;
//            }
//            MessageBuilder builder = new MessageBuilder();
//            builder.appendString(event.getAuthor().getUsername()).appendString(" has the following perms set to Deny.\n");
//            for (Permission perm : over.getDenied())
//            {
//                builder.appendString(perm.toString()).appendString("\n");
//            }
//            event.getChannel().sendMessage(builder.build());
//        }
//        if (command(event, "inherit"))
//        {
//            PermissionOverride over = ((TextChannelImpl) event.getChannel()).getOverrideForUser(event.getAuthor());
//            if (over == null)
//            {
//                event.getChannel().sendMessage("User has no overrides in this channel");
//                return;
//            }
//            MessageBuilder builder = new MessageBuilder();
//            builder.appendString(event.getAuthor().getUsername()).appendString(" has the following perms set to Inherit.\n");
//            for (Permission perm : over.getInherit())
//            {
//                builder.appendString(perm.toString()).appendString("\n");
//            }
//            event.getChannel().sendMessage(builder.build());
//        }


//        if (event.getMessage().getContent().startsWith("add"))
//        {
//            event.getGuild().getManager().addRoleToUser(
//                    event.getAuthor(),
//                    event.getGuild().getRoles().stream().filter(r -> r.getName().equals("TmpTests")).findFirst().get());
//        }
//        if (event.getMessage().getContent().startsWith("remove"))
//        {
//            event.getGuild().getManager().removeRoleFromUser(
//                    event.getAuthor(),
//                    event.getGuild().getRoles().stream().filter(r -> r.getName().equals("TmpTests")).findFirst().get());
//        }

//        if (!event.getMessage().getAttachments().isEmpty())
//        {
//            event.getChannel().sendMessage(
//                    new MessageBuilder()
//                            .appendString(event.getAuthor().getUsername())
//                            .appendString(" sent a message with an attachment!\nFilename: ")
//                            .appendString(event.getMessage().getAttachments().get(0).getFileName())
//                            .appendString("\nIs image? ")
//                            .appendString(event.getMessage().getAttachments().get(0).isImage() ? "Yes" : "No")
//                            .build());
//        }

//        File f = new File("Ho-Kago Teastep - AMV.mp3");
//        if (!f.exists()) System.out.println("Doesn't Exist!");
//        ((TextChannelImpl)event.getChannel()).sendFileAsync(f, null);
//        System.out.println("rawr");

//        event.getChannel().sendTyping();
//        event.getChannel().sendMessage(new MessageBuilder().appendString("cats").build());

//        if (event.getAuthor().getUsername().equalsIgnoreCase("dv8fromtheworld")
//            || event.getAuthor().getUsername().equalsIgnoreCase("kantenkugel"))
//        {
//            if (event.getMessage().getContent().startsWith(".guild "))
//                this.makeGuild(event);
//            if (event.getMessage().getContent().startsWith(".channel "))
//                this.makeChannel(event);
//        }
    }

    private void makeChannel(GuildMessageReceivedEvent event)
    {

        String name = event.getMessage().getContent().replace(".guild ","");
        event.getGuild().createTextChannel(name);
        event.getChannel().sendMessage("Created channel!");
    }

    private void makeGuild(GuildMessageReceivedEvent event)
    {
        File f = Paths.get("./icon.jpg").toFile();
        HttpURLConnection conn;
        try
        {
            conn = (HttpURLConnection) new URL(event.getAuthor().getAvatarUrl()).openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
            conn.setRequestProperty("authorization", event.getJDA().getAuthToken());
            conn.connect();
            Files.copy(conn.getInputStream(), f.toPath());
        }
        catch (ProtocolException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String name = event.getMessage().getContent().replace(".guild ","");
        AvatarUtil.Avatar icon = null;
        try
        {
            icon = AvatarUtil.getAvatar(f);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        final AvatarUtil.Avatar fIcon = icon;
        event.getJDA().createGuildAsync(name,
                manager ->
                {
                    manager.setRegion(Region.AMSTERDAM).setIcon(fIcon).update();
                    InviteUtil.Invite invite = InviteUtil.createInvite(manager.getGuild().getTextChannels().get(0), event.getJDA());
                    event.getChannel().sendMessage("A new guild was created! Invite url: " + invite.getUrl());
                });
    }

    private boolean command(GuildMessageReceivedEvent e, String command)
    {
        return e.getMessage().getContent().startsWith(command);
    }
}
