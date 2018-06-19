package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ThresholdSegmentation {
    private int[] imageHistogram;
    private BufferedImage loadedImage;
    private Set<Integer> thresholds;

    /**
     * ThresholdSegmentation Constructor
     * @param imageHandler
     * @param filtering
     */
    public ThresholdSegmentation(BufferedImage imageHandler, boolean filtering) {
        imageHistogram = new int[256];
        loadedImage = imageHandler;
        thresholds = new HashSet<Integer>();
        createHistogram(imageHandler);
        if (filtering) {
            filterHistogram();
        }
    }

    /**
     * @param value
     */
    public void addThreshold(int value) {
        thresholds.add(value);
    }

    /**
     * @param value
     */
    public void removeThreshold(int value) {
        thresholds.remove(value);
    }

    /**
     * @return Set<Integer>
     */
    public Set<Integer> getThresholds() {
        return thresholds;
    }

    /**
     * @return int[]
     */
    public int[] getImageHistogram() {
        return imageHistogram;
    }

    /**
     * Creates histogram from input image
     * @param image
     */
    private void createHistogram(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int[][] pixelsBrightness = ImageHandler.getGrayscaleMap(loadedImage);

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                imageHistogram[pixelsBrightness[x][y]]++;
            }
        }
    }

    /**
     * Threshold segmentation using found thresholds
     * @return BufferedImage segmented image
     */
    public BufferedImage segmentation() {
        BufferedImage segmentedImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        int[] sortedThresholds = new int[thresholds.size()];
        int c = 0;
        for (int x : thresholds) sortedThresholds[c++] = x;
        Arrays.sort(sortedThresholds);
        System.out.println("Probíhá segmentace. Použité prahy: " + thresholds);

//        BufferedImage loadedGrayscale = ImageHandler.getGrayScaleImage(loadedImage);
        int[][] grayMap = ImageHandler.getGrayscaleMap(loadedImage);

        for (int x = 0; x < loadedImage.getWidth(); ++x) {
            for (int y = 0; y < loadedImage.getHeight(); ++y) {
                int segmentedLevel = 0;
                for(int i = 0; i < thresholds.size(); i++){
                    if (grayMap[x][y] > sortedThresholds[i]){
                        if (i == thresholds.size() - 1){
                            segmentedLevel = 255;
                        }
                        else {
                            segmentedLevel = sortedThresholds[i];
                        }
                    }
                }
                int gray = (segmentedLevel << 16) + (segmentedLevel << 8) + segmentedLevel;
                segmentedImage.setRGB(x, y, gray);
            }
        }

        return segmentedImage;
    }

    /**
     * Filtering using simple Mean filter with size 5
     */
    private void filterHistogram() {
        int length = imageHistogram.length;
        int[] filteredHistogram = new int[length];
        filteredHistogram[0] = (imageHistogram[0] + imageHistogram[1] + imageHistogram[2]) / 3;
        filteredHistogram[1] = (imageHistogram[3] + imageHistogram[1] + imageHistogram[2] + imageHistogram[0]) / 4;

        filteredHistogram[length - 1] = (imageHistogram[length - 1] + imageHistogram[length - 2] + imageHistogram[length - 3]) / 3;
        filteredHistogram[length - 2] = (imageHistogram[length - 3] + imageHistogram[length - 2] + imageHistogram[length - 3] + imageHistogram[length - 1]) / 4;

        for (int i = 2; i < length - 2; i++) {
            filteredHistogram[i] = (imageHistogram[i - 2] + imageHistogram[i - 1] + imageHistogram[i + 1] + imageHistogram[i + 2] + imageHistogram[i]) / 5;
        }
        filteredHistogram[length - 1] = imageHistogram[length - 1];
        imageHistogram = filteredHistogram;
    }

    /**
     * Finds possible thresholds for input parameters
     * @param epsilon
     * @param distance
     * @return ArrayList
     */
    public ArrayList<Integer> findThresholds(int epsilon, int distance) {
        this.thresholds.clear();
        ArrayList<Integer> maxima = findHistogramLocalMaxima(epsilon, distance);
        ArrayList<Integer> thresholds = new ArrayList<>();

        System.out.println("Hledání lokálních maxim v histogramu pro epsilon " + epsilon + " se vzdáleností " + distance + ". Nalezeno: " + maxima);

        //TODO co když jenom jedno max

        for (Integer max : maxima) {

            if (maxima.indexOf(max) == maxima.size() - 1) {
                break;
            }
            int min = max;
            int center = ((max + maxima.get(maxima.indexOf(max) + 1)) / 2);
            if (imageHistogram[center] < imageHistogram[min]) {
                min = center;
            }
            for (int i = 0; i < (maxima.get(maxima.indexOf(max) + 1) - max) / 2; i++) {
                if (imageHistogram[center + i] < imageHistogram[min]) {
                    min = center + i;
                }

                if (imageHistogram[center - i] < imageHistogram[min]) {
                    min = center - i;
                }
            }
            thresholds.add(min);
        }
        this.thresholds.addAll(thresholds);

        return thresholds;
    }

    /**
     * Finds all local maxima in histogram
     * @param epsilon
     * @param distance
     * @return ArrayList
     */
    private ArrayList<Integer> findHistogramLocalMaxima(int epsilon, int distance) {
        ArrayList<Integer> max = new ArrayList<>();
        int length = imageHistogram.length;
        for (int i = 0; i < length; i++) {
            boolean isLocalMaxima = true;
            if (imageHistogram[i] > 0) {
                for (int j = 1; j <= epsilon; j++) {
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
                    //TODO asi lepší hlídat vzdálenost u minim než u maxim
                    boolean noAdjacent = true;
                    for (Integer m : max) {
                        if(Math.abs(m - i) < distance){
                            noAdjacent = false;
                        }
                    }

                    if(noAdjacent) {
                        max.add(i);
                    }
                }
            }
        }

        return max;
    }
}
