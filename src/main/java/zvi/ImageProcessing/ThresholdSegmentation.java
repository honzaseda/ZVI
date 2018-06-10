package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

public class ThresholdSegmentation {
    private int[] imageHistogram;
    private int threshold;
    private BufferedImage loadedImage;
    private HashSet<Integer> thresholds;

    public ThresholdSegmentation(BufferedImage imageHandler, boolean filtering) {
        imageHistogram = new int[256];
        loadedImage = imageHandler;
        thresholds = new HashSet<>();
        createHistogram(imageHandler);
        if (filtering) {
            filterHistogram();
        }
    }

    public void addThreshold(int value){
        thresholds.add(value);
    }

    public void removeThreshold(int value){
        thresholds.remove(value);
    }

    public HashSet<Integer> getThresholds(){
        return thresholds;
    }

    public int[] getImageHistogram() {
        return imageHistogram;
    }

    private void createHistogram(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeigth = image.getHeight();
        int[][] pixelsBrightness = ImageHandler.getGrayscaleMap(loadedImage);

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeigth; y++) {
                imageHistogram[pixelsBrightness[x][y]]++;
            }
        }
    }

    public void setThreshold(int level) {
        threshold = level;
    }

    public BufferedImage segmentation() {
        BufferedImage segmentedImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        System.out.println("Probíhá segmentace s ručním prahováním. Použitý práh: " + threshold);

        findHistogramLocalMaxima(50);
        BufferedImage loadedGrayscale = ImageHandler.getGrayScaleImage(loadedImage);

        for (int x = 0; x < loadedImage.getWidth(); ++x) {
            for (int y = 0; y < loadedImage.getHeight(); ++y) {
                int segmentedLevel;
                int rgb = loadedGrayscale.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                int grayLevel = (r + g + b) / 3;
                if (grayLevel <= threshold) {
                    segmentedLevel = 0;
                } else {
                    segmentedLevel = 255;
                }
                int gray = (segmentedLevel << 16) + (segmentedLevel << 8) + segmentedLevel;
                segmentedImage.setRGB(x, y, gray);
            }
        }

        return segmentedImage;
    }

    /*
    Filtering using simple FIR filter with [1,2,1] mask
     */
    private void filterHistogram() {
        int length = imageHistogram.length;
        int[] filteredHistogram = new int[length];
        filteredHistogram[0] = imageHistogram[0];
        for (int i = 2; i < length - 2; i++) {
            filteredHistogram[i] = (imageHistogram[i - 2] + imageHistogram[i - 1] + imageHistogram[i + 1] + imageHistogram[i + 2] + 2 * imageHistogram[i]) / 6;
        }
        filteredHistogram[length - 1] = imageHistogram[length - 1];
        imageHistogram = filteredHistogram;
    }

    public ArrayList<Integer> findThresholds(int vicinity, int distance){
        ArrayList<Integer> maxima = findHistogramLocalMaxima(vicinity);
        ArrayList<Integer> thresholds = new ArrayList<>();

        for (Integer max : maxima) {

            if(maxima.indexOf(max) == maxima.size() - 1) break;
            int min = max;
            for(int i = max; i < maxima.get(maxima.indexOf(max) + 1);  i++){
                if(imageHistogram[i] < imageHistogram[min]){
                    min = i;
                }
            }
            thresholds.add(min);
        }

        return thresholds;
    }

    private ArrayList<Integer> findHistogramLocalMaxima(int vicinity){
        ArrayList<Integer> max = new ArrayList<>();
        int length = imageHistogram.length;
        for (int i = 0; i < length; i++){
            boolean isLocalMaxima = true;
            if(imageHistogram[i] > 0) {
                for (int j = 1; j <= vicinity / 2; j++) {
                    if (i - j >= 0) {
                        if (imageHistogram[i] < imageHistogram[i - j]) {
                            isLocalMaxima = false;
                            break;
                        }
                    }

                    if (i + j <= 255) {
                        if (imageHistogram[i] < imageHistogram[i + j]) {
                            isLocalMaxima = false;
                            break;
                        }
                    }
                }
                if (isLocalMaxima) {
                    //TODO
                    max.add(i);
                }
            }
        }

        return max;
    }
}
