package com.dnablue2112;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
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

        System.out.print("Should we check album art? [Y/n] ");
        String albumArtResponse = System.console().readLine();
        checkAlbumArt = albumArtResponse.isEmpty() || albumArtResponse.equalsIgnoreCase("Y");

        System.out.print("Should we check lyrics? [Y/n] ");
        String lyricsResponse = System.console().readLine();
        checkLyrics = lyricsResponse.isEmpty() || lyricsResponse.equalsIgnoreCase("Y");

        //TODO: Make a report text file to store the report in so that it can be marked off as we go
        //Make some lists for paths that need to be reported
        List<String> albumArtPaths = new ArrayList<>();

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
                        albumArtPaths.add(f.getPath());
                    } else {
                        //Check if the art is tiny
                        int lowerBound = 400;
                        Artwork art = albumArt.getFirst();
                        byte[] imageData = art.getBinaryData();
                        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                        BufferedImage bimg = ImageIO.read(bais);

                        if (bimg.getHeight() < lowerBound || bimg.getWidth() < 400) {
                            System.out.println(f.getName() + " w:" + bimg.getWidth() + " h:" + bimg.getHeight());
                        }

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

        System.out.print("Should we save a report for album art? [y/N] ");
        String albumArtReportResponse = System.console().readLine();
        boolean reportAlbumArt = albumArtReportResponse.equalsIgnoreCase("Y");
        if (reportAlbumArt) {
            albumArtPaths.sort(String.CASE_INSENSITIVE_ORDER);
            File report = new File(directory, "AlbumArtReport.txt");
            if (!report.exists()) {
                try {
                    report.createNewFile();
                } catch (IOException e) {
                    System.out.println("Report failed to create");
                    return;
                }
            }
            try {
                FileWriter fileWriter = new FileWriter(report);
                for (String path : albumArtPaths) {
                    //Write to text file
                    fileWriter.write(path + "\n");
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to write report");
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
