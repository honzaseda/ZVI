package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThresholdSegmentation {
    private int[] imageHistogram;
    private List<Integer> thresholds = new ArrayList<>();
    private ImageHandler loadedImage;
    public ImageHandler segmentedImage;

    public ThresholdSegmentation(ImageHandler imageHandler, boolean filtering) {
        this.imageHistogram = new int[256];
        this.loadedImage = imageHandler;
        createHistogram(imageHandler);
        if(filtering){
            filterHistogram();
        }
    }

    public int[] getImageHistogram() {
        return this.imageHistogram;
    }

    private void createHistogram(ImageHandler imageHandler) {
        BufferedImage image = imageHandler.getImage();
        int imageWidth = image.getWidth();
        int imageHeigth = image.getHeight();
        int[][] pixelsBrightness = imageHandler.getGrayscaleMap();

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeigth; y++) {
                this.imageHistogram[pixelsBrightness[x][y]]++;
            }
        }
    }

    public int findThreshold(int distance) {
        return 120;
    }

    public void addThreshold(int level){
        this.thresholds.add(level);
    }

    public BufferedImage segmentation(){
        segmentedImage = new ImageHandler(this.loadedImage.getGrayScaleImage());
        Collections.sort(this.thresholds);
        this.thresholds.size();
        System.out.print("Použité prahy:");
        for (Integer threshold : this.thresholds) {
            System.out.print(" " + threshold);
        }
        System.out.println("");

        for (int x = 0; x < segmentedImage.getImage().getWidth(); ++x) {
            for (int y = 0; y < segmentedImage.getImage().getHeight(); ++y) {
                int segmentedLevel;
                int rgb = segmentedImage.getImage().getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                int grayLevel = (r + g + b) / 3;
                if(grayLevel <= this.thresholds.get(0)){
                    segmentedLevel = 0;
                }
                else {
                    segmentedLevel = 255;
                }
                int gray = (segmentedLevel << 16) + (segmentedLevel << 8) + segmentedLevel;
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
