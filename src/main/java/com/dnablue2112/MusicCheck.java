package com.dnablue2112;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicCheck {

    List<File> musicFiles = new ArrayList<>();
    boolean checkLyrics, checkAlbumArt;

    public MusicCheck() {
        File directory = new File("./");
        System.out.println("Searching for music files...");
        //Search the directory for music files to add to our list
        searchDirectory(directory);
        //Now we have a list of music files to check
        System.out.println("Found " + musicFiles.size() + " music files to check. Starting now...");
        //Ask what to check

        System.out.print("Should we check album art? [Y/n]");
        String albumArtResponse = System.console().readLine();
        checkAlbumArt = albumArtResponse.isEmpty() || albumArtResponse.equalsIgnoreCase("Y");

        System.out.print("Should we check lyrics? [Y/n]");
        String lyricsResponse = System.console().readLine();
        checkLyrics = lyricsResponse.isEmpty() || lyricsResponse.equalsIgnoreCase("Y");

        //Loop over each file and check if there is an issue to report
        for (File f : musicFiles) {
            if (checkAlbumArt) {
                try {
                    AudioFile audioFile = AudioFileIO.read(f);
                    Tag tag = audioFile.getTag();
                    //Check for album art
                    List<Artwork> albumArt = tag.getArtworkList();
                    if (albumArt.isEmpty()) {
                        System.out.println("No Album Art for " + f.getPath());
                    }
                } catch (IOException | CannotReadException | TagException | ReadOnlyFileException |
                         InvalidAudioFrameException e) {
                    System.out.println("Failed to read file " + f.getPath());
                    System.out.println(e.getMessage());
                }
            }
            if (checkLyrics) {
                //Check for lyrics
                File syncedLyrics = new File(f.getParentFile(), getFileNameWithoutExtension(f) + ".lrc");
                File plainLyrics = new File(f.getParentFile(), getFileNameWithoutExtension(f) + ".txt");
                if (syncedLyrics.exists()) {
                    continue;
                } else if (plainLyrics.exists()) {
                    System.out.println("Only plain lyrics for " + f.getPath());
                } else {
                    System.out.println("No lyrics for " + f.getPath());
                }
            }
        }
    }

    private void searchDirectory(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                searchDirectory(f);
            else if (f.getName().endsWith(".mp3"))
                musicFiles.add(f);
            else if (f.getName().endsWith(".lrc"))
                continue;
            else if (f.getName().endsWith(".txt"))
                continue;
            else if (f.getName().equalsIgnoreCase("_MusicCheck.jar"))
                continue;
            else if (f.getName().equalsIgnoreCase("desktop.ini"))
                continue;
            else
                //Other file found, report it
                System.out.println("Found an erroneous file: \n" + f.getPath());
        }
    }

    private String getFileNameWithoutExtension(File f) {
        return f.getName().substring(0, f.getName().lastIndexOf("."));
    }

}
