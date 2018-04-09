package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Histogram {
    private int[] imageHistogram;

    public Histogram(ImageHandler imageHandler, boolean filtering) {
        this.imageHistogram = new int[256];
        createHistogram(imageHandler.convertToGrayScale(imageHandler.getImage()));
        if(filtering){
            filterHistogram();
        }
    }

    public int[] getImageHistogram() {
        return this.imageHistogram;
    }

    private void createHistogram(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeigth = image.getHeight();
        int[][] pixelsBrightness;
        pixelsBrightness = getImageMap(image, imageWidth, imageHeigth);

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeigth; y++) {
                this.imageHistogram[pixelsBrightness[x][y]]++;
            }
        }
    }

    private static int[][] getImageMap(BufferedImage image, int imageWidth, int imageHeigth) {
        int[][] pixels = new int[imageWidth][imageHeigth];

        DataBufferByte db = (DataBufferByte) image.getRaster().getDataBuffer();
        byte[] pixelarray = db.getData();
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeigth; y++) {
                pixels[x][y] = pixelarray[x + y * imageWidth] & 0xFF;
            }
        }
        return pixels;
    }

    public static int findThreshold(int distance) {
        return 120;
    }

    /*
    Filtering using simple FIR filter with [1,2,1] mask
     */
    private void filterHistogram() {
        int length = this.imageHistogram.length;
        int[] filteredHistogram = new int[length];
        filteredHistogram[0] = this.imageHistogram[0];
        for (int i = 1; i < length - 2; i++) {
            filteredHistogram[i] = (this.imageHistogram[i - 1] + this.imageHistogram[i + 1] + 2 * this.imageHistogram[i]) / 4;
        }
        filteredHistogram[length - 1] = this.imageHistogram[length - 1];
        this.imageHistogram = filteredHistogram;
    }
}
