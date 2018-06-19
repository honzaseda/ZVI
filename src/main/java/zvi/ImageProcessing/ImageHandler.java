package zvi.ImageProcessing;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageHandler {

    /**
     * @return Integer
     */
    public static int getEpsilon() {
        return epsilon;
    }

    /**
     * @param epsilon
     */
    public static void setEpsilon(int epsilon) {
        ImageHandler.epsilon = epsilon;
    }

    private static int epsilon = 15;

    /**
     * Converts passed image to grayscale
     * @param image
     * @return BufferedImage
     */
    public static BufferedImage getGrayScaleImage(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();
                int grayLevel = (r + g + b) / 3;
                Color grayPixel = new Color(grayLevel, grayLevel, grayLevel);
                image.setRGB(x, y, grayPixel.getRGB());
            }
        }
        return image;
    }

    /**
     * Creates 2D array of grayscale values of image
     * @param image
     * @return int[][]
     */
    public static int[][] getGrayscaleMap(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int[][] pixels = new int[imageWidth][imageHeight];

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();
                pixels[x][y] = (r + g + b) / 3;
            }
        }

        return pixels;
    }

}
