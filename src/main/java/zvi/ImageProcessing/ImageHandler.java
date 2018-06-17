package zvi.ImageProcessing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageHandler {

    public static BufferedImage getGrayScaleImage(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();
                int grayLevel = (r + g + b) / 3;
                int gray = (grayLevel << 16) | (grayLevel << 8) | grayLevel;
                image.setRGB(x, y, gray);
            }
        }
        return image;
    }

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
