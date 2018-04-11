package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThresholdSegmentation {
    private int[] imageHistogram;
    private List<Integer> thresholds = new ArrayList<>();
    private ImageHandler imageHandler;

    public ThresholdSegmentation(ImageHandler imageHandler, boolean filtering) {
        this.imageHistogram = new int[256];
        this.imageHandler = imageHandler;
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

    private int[][] getImageMap(BufferedImage image, int imageWidth, int imageHeigth) {
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

    public int findThreshold(int distance) {
        return 120;
    }

    public void addThreshold(int level){
        this.thresholds.add(level);
    }

    public BufferedImage segmentation(){
        ImageHandler segmentedImage = new ImageHandler(this.imageHandler.convertToGrayScale(this.imageHandler.getImage()));
        Collections.sort(this.thresholds);
        this.thresholds.size();
        System.out.print("Použité prahy:");
        for (Integer threshold : this.thresholds) {
            System.out.print(" " + threshold);
        }
        System.out.println("");

        for (int x = 0; x < segmentedImage.getImage().getWidth(); ++x) {
            for (int y = 0; y < segmentedImage.getImage().getHeight(); ++y) {
                int segmentedlevel;
                int rgb = segmentedImage.getImage().getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                int grayLevel = (r + g + b) / 3;
                if(grayLevel <= this.thresholds.get(0)){
                    segmentedlevel = 0;
                }
                else {
                    segmentedlevel = 255;
                }
                int gray = (segmentedlevel << 16) + (segmentedlevel << 8) + segmentedlevel;
                segmentedImage.getImage().setRGB(x, y, gray);
            }
        }

        return segmentedImage.getImage();
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
