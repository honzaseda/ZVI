package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageHandler {

    public static BufferedImage getGrayScaleImage(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                int grayLevel = (r + g + b) / 3;
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                image.setRGB(x, y, gray);
            }
        }
        return image;
    }

    public static int[][] getGrayscaleMap(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int[][] pixels = new int[imageWidth][imageHeight];

        DataBufferByte db = (DataBufferByte) image.getRaster().getDataBuffer();
        byte[] pixelarray = db.getData();
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                pixels[x][y] = pixelarray[x + y * imageWidth] & 0xFF;
            }
        }
        return pixels;
    }

}
