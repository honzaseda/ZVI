package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;

public class Histogram {
    int[] imageHistogram;

    public Histogram(BufferedImage image){
        this.imageHistogram = new int[256];
        createHistogram(image);
    }

    public int[] getImageHistogram(){
        return this.imageHistogram;
    }

    private void createHistogram(BufferedImage image){
        convertToGrayScale(image);
        int imageWidth = image.getWidth();
        int imageHeigth = image.getHeight();
        int[][] pixelsBrightness;
        pixelsBrightness = getImageMap(image, imageWidth, imageHeigth);

        for (int x = 0; x < imageWidth; x++){
            for (int y = 0; y < imageHeigth; y++){
                this.imageHistogram[pixelsBrightness[x][y]]++;
            }
        }
    }

    private static void convertToGrayScale(BufferedImage image)
    {
        for (int x = 0; x < image.getWidth(); ++x)
            for (int y = 0; y < image.getHeight(); ++y)
            {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                int grayLevel = (r + g + b) / 3;
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                image.setRGB(x, y, gray);
            }
    }

    private static int[][] getImageMap(BufferedImage image, int imageWidth, int imageHeigth){
        int[][] pixels = new int[imageWidth][imageHeigth];

        DataBufferByte db = (DataBufferByte)image.getRaster().getDataBuffer();
        byte[] pixelarray = db.getData();
        for (int x = 0; x < imageWidth; x++ ) {
            for (int y = 0; y < imageHeigth; y++ ) {
                pixels[x][y] = pixelarray[x + y * imageWidth] &0xFF;
            }
        }

        return pixels;
    }

}
