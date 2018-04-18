package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageHandler {
    private BufferedImage image;
    private int[][] grayscaleMap;

    public ImageHandler(BufferedImage img) {
        this.image = img;
        createGrayscaleMap(getGrayScaleImage());
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage getGrayScaleImage() {
        BufferedImage image = this.getImage();
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
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

    private void createGrayscaleMap(BufferedImage image) {
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
        grayscaleMap = pixels;
    }

    public void updateGrayscaleMap(int oldValue, int newValue) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                if (grayscaleMap[x][y] == oldValue) {
                    grayscaleMap[x][y] = newValue;
                }
            }
        }
    }

    public int[][] getGrayscaleMap() {
        return this.grayscaleMap;
    }

    public void updateImageFromMap() {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int gray = (grayscaleMap[x][y] << 16) + (grayscaleMap[x][y] << 8) + grayscaleMap[x][y];
                image.setRGB(x, y, gray);
            }
        }
    }

}
