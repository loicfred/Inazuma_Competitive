package iecompbot.interaction.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static iecompbot.L10N.TL;
import static iecompbot.interaction.Automation.Wait;

public class PlayerManager {
    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(InteractionHook M, String trackUrl, String name, OptionMapping repeattimes) {
        final GuildMusicManager musicManager = this.getMusicManager(M.getInteraction().getGuild());
        EmbedBuilder Embed = new EmbedBuilder();
        Embed.setColor(Color.magenta);
        Embed.setTitle(TL(M,"Music-Manager"));
        int times = 1;

        if (repeattimes != null) {
            times = repeattimes.getAsInt();
        }
        final boolean[] done = {false};
        for (int i = 0; i != times; i++) {
            Wait(20);
            int finalTimes = times;
            this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.scheduler.queue(track);
                    if (!done[0]) {
                        long totalSeconds = track.getDuration() / 1000;
                        // Calculate the minutes and seconds
                        long minutes = totalSeconds / 60;
                        long seconds = totalSeconds % 60;

                        // Return the result as a formatted string
                        Embed.setDescription(":musical_note: " + TL(M,"music-manager-load-success", name) + "\n" +
                                TL(M,"Duration") + ": `" + String.format("%d:%02d", minutes, seconds) + "`\n" +
                                TL(M,"Repeat") + ": `" + finalTimes + "`");
                        M.editOriginalEmbeds(Embed.build()).queue();
                        done[0] = true;
                    }
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    if (!done[0]) {
                        M.editOriginal("Loaded the playlist.").queue();
                        done[0] = true;
                    }
                }

                @Override
                public void noMatches() {
                    if (!done[0]) {
                        Embed.setDescription(TL(M,"music-manager-load-no-match", name));
                        M.editOriginalEmbeds(Embed.build()).queue();
                        done[0] = true;
                    }
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    if (!done[0]) {
                        Embed.setDescription(TL(M,"music-manager-load-fail", name));
                        M.editOriginalEmbeds(Embed.build()).queue();
                        done[0] = true;
                    }
                }
            });
        }
    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }

}