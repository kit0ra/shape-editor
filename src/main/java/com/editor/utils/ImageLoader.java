package com.editor.utils;

import java.awt.Image;
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
        // Check cache first
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        // Try to load the image
        try (InputStream is = ImageLoader.class.getResourceAsStream("/" + path)) {
            if (is == null) {
                System.err.println("Image not found: " + path);
                return null;
            }

            Image image = ImageIO.read(is);
            if (image != null) {
                imageCache.put(path, image); // Cache the image
            }
            return image;
        } catch (IOException e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
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
