package com.editor.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageLoader {
    private static final Map<String, Image> imageCache = new HashMap<>();

    /**
     * Loads an image from the resources folder
     *
     * @param path Path relative to resources folder (e.g., "icons/save.png")
     * @return The loaded Image, or null if failed to load
     */
    public static Image loadImage(String path) {
        System.out.println("[ImageLoader] Attempting to load image: " + path);

        
        if (imageCache.containsKey(path)) {
            System.out.println("[ImageLoader] Image found in cache: " + path);
            return imageCache.get(path);
        }

        
        System.out.println("[ImageLoader] Trying to load from classpath: /" + path);
        try (InputStream is = ImageLoader.class.getResourceAsStream("/" + path)) {
            if (is != null) {
                System.out.println("[ImageLoader] Found in classpath: /" + path);
                Image image = ImageIO.read(is);
                if (image != null) {
                    System.out.println("[ImageLoader] Successfully loaded from classpath: /" + path);
                    imageCache.put(path, image); 
                    return image;
                } else {
                    System.err.println("[ImageLoader] Failed to read image from classpath stream: /" + path);
                }
            } else {
                System.err.println("[ImageLoader] Resource stream is null for: /" + path);
            }
        } catch (IOException e) {
            System.err.println("[ImageLoader] Error loading image from classpath: " + path);
            System.err.println("[ImageLoader] Exception: " + e.getMessage());
        }

        
        try {
            
            File resourceFile = new File("src/main/resources/" + path);
            System.out.println("[ImageLoader] Trying file system path: " + resourceFile.getAbsolutePath());
            if (resourceFile.exists()) {
                System.out.println("[ImageLoader] File exists at: " + resourceFile.getAbsolutePath());
                Image image = ImageIO.read(resourceFile);
                if (image != null) {
                    System.out.println(
                            "[ImageLoader] Successfully loaded from file system: " + resourceFile.getAbsolutePath());
                    imageCache.put(path, image); 
                    return image;
                } else {
                    System.err
                            .println("[ImageLoader] Failed to read image from file: " + resourceFile.getAbsolutePath());
                }
            } else {
                System.err.println("[ImageLoader] File does not exist: " + resourceFile.getAbsolutePath());
            }

            
            File binFile = new File("bin/" + path);
            System.out.println("[ImageLoader] Trying bin path: " + binFile.getAbsolutePath());
            if (binFile.exists()) {
                System.out.println("[ImageLoader] File exists at: " + binFile.getAbsolutePath());
                Image image = ImageIO.read(binFile);
                if (image != null) {
                    System.out.println("[ImageLoader] Successfully loaded from bin path: " + binFile.getAbsolutePath());
                    imageCache.put(path, image); 
                    return image;
                } else {
                    System.err
                            .println("[ImageLoader] Failed to read image from bin file: " + binFile.getAbsolutePath());
                }
            } else {
                System.err.println("[ImageLoader] Bin file does not exist: " + binFile.getAbsolutePath());
            }

            
            File directFile = new File(path);
            System.out.println("[ImageLoader] Trying direct path: " + directFile.getAbsolutePath());
            if (directFile.exists()) {
                System.out.println("[ImageLoader] File exists at: " + directFile.getAbsolutePath());
                Image image = ImageIO.read(directFile);
                if (image != null) {
                    System.out.println(
                            "[ImageLoader] Successfully loaded from direct path: " + directFile.getAbsolutePath());
                    imageCache.put(path, image); 
                    return image;
                } else {
                    System.err.println(
                            "[ImageLoader] Failed to read image from direct file: " + directFile.getAbsolutePath());
                }
            } else {
                System.err.println("[ImageLoader] Direct file does not exist: " + directFile.getAbsolutePath());
            }

            System.err.println("Image not found: " + path);
            return null;
        } catch (IOException e) {
            System.err.println("Error loading image from file system: " + path);
            System.err.println("Stack trace: " + e);
            return null;
        }
    }

    /**
     * Clears the image cache to free memory
     */
    public static void clearCache() {
        imageCache.clear();
    }

    /**
     * Preloads multiple images at once
     *
     * @param paths Array of image paths to preload
     */
    public static void preloadImages(String... paths) {
        for (String path : paths) {
            loadImage(path);
        }
    }
}
