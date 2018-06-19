package zvi.ImageProcessing;

import java.awt.image.BufferedImage;

public class MatrixSegmentation {
    private int[][] coocurencyMatrix;
    private int numberOfSegments;
    private BufferedImage loadedImage;
    private BufferedImage segmentedImage;
    private int[][] grayscaleMap;

    /**
     * @return Boolean
     */
    public boolean isBrighten() {
        return brighten;
    }

    /**
     * @param brighten
     */
    public void setBrighten(boolean brighten) {
        this.brighten = brighten;
    }

    public boolean brighten = true;

    /**
     * MatrixSegmentation Constructor
     * @param image
     * @param segments
     */
    public MatrixSegmentation(BufferedImage image, int segments) {
        loadedImage = image;
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();

        coocurencyMatrix = new int[256][256];
        numberOfSegments = segments;

        segmentedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        grayscaleMap = ImageHandler.getGrayscaleMap(loadedImage);

    }

    /**
     * @return BufferedImage
     */
    public BufferedImage getSegmentedImage(){
        updateImageFromMap();
        return segmentedImage;
    }

    /**
     * Creates co-ocurency matrix of input image
     * @param useDiagNeighbours
     */
    public void createCoocurencyMatrix(boolean useDiagNeighbours) {
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();
        int[][] imagePixels = grayscaleMap;

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                if (x != 0) {
                    this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y]]++;
                    if (useDiagNeighbours) {
                        if (y != 0) {
                            this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y - 1]]++;
                        }
                        if (y != imageHeight - 1) {
                            this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y + 1]]++;
                        }
                    }
                }
                if (x != imageWidth - 1) {
                    this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y]]++;
                    if (useDiagNeighbours) {
                        if (y != 0) {
                            this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y - 1]]++;
                        }
                        if (y != imageHeight - 1) {
                            this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y + 1]]++;
                        }
                    }
                }
                if (y != 0) {
                    this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x][y - 1]]++;
                }
                if (y != imageHeight - 1) {
                    this.coocurencyMatrix[imagePixels[x][y]][imagePixels[x][y + 1]]++;
                }
            }
        }
    }

    /**
     * Recoloring segmentation
     */
    public void recoloringSegmentation() {
        int c = 0;
        while(numberOfRegions() > numberOfSegments) {
            int minIndex = findDiagMin();
            int maxNeighbour = getMaxNeighbour(minIndex);
            for (int i = 0; i < 256; i++) {
                coocurencyMatrix[i][maxNeighbour] += coocurencyMatrix[i][minIndex];
                coocurencyMatrix[i][minIndex] = 0;
            }

            for (int i = 0; i < 256; i++) {
                coocurencyMatrix[maxNeighbour][i] += coocurencyMatrix[minIndex][i];
                coocurencyMatrix[minIndex][i] = 0;
            }
            System.out.println(c + ". Přebarvuji jas " + minIndex + " na " + maxNeighbour);
            updateGrayscaleMap(minIndex, maxNeighbour);
            c++;
        }
    }

    /**
     * @return Integer Number of different brightness
     */
    private int numberOfRegions() {
        int count = 0;
        for (int i = 0; i < 256; i++) {
            if (coocurencyMatrix[i][i] > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Finds minimal value on co-ocurency matrix diagonal
     * @return Integer
     */
    private int findDiagMin() {
        int min = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int j = 0; j < 256; j++) {
            if (coocurencyMatrix[j][j] <= min) {
                if (isValidBrightness(j)) {
                    min = coocurencyMatrix[j][j];
                    minIndex = j;
                }
            }
        }
        return minIndex;
    }

    /**
     * Finds maximal value for index row
     * @param rowIndex
     * @return Integer
     */
    private int getMaxNeighbour(int rowIndex) {
        int currentMax = coocurencyMatrix[rowIndex][0];
        int maxIndex = 0;
        if (rowIndex == 0) {
            currentMax = coocurencyMatrix[rowIndex][1];
            maxIndex = 1;
        }

        for (int i = 0; i < 256; i++) {
            if (i == rowIndex) {
                continue;
            }
            if (coocurencyMatrix[rowIndex][i] > currentMax) {
                currentMax = coocurencyMatrix[rowIndex][i];
                maxIndex = i;
            } else if (coocurencyMatrix[rowIndex][i] == currentMax && currentMax != 0) {
                if (Math.abs(rowIndex - i) < Math.abs(rowIndex - maxIndex)) {
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    /**
     * Checks if sum of row is greater than zero
     * @param index
     * @return Boolean
     */
    private boolean isValidBrightness(int index) {
        int sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += coocurencyMatrix[index][i];
        }
        return (sum > 0);
    }

    /**
     * Updates 2D image array by passed recolorizing arguments
     * @param oldValue
     * @param newValue
     */
    private void updateGrayscaleMap(int oldValue, int newValue) {
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                if (grayscaleMap[x][y] == oldValue) {
                    grayscaleMap[x][y] = newValue;
                }
            }
        }
    }

    /**
     * Updates BufferedImage from 2D image array
     */
    private void updateImageFromMap() {
        for (int x = 0; x < segmentedImage.getWidth(); x++) {
            for (int y = 0; y < segmentedImage.getHeight(); y++) {
                int gray = (grayscaleMap[x][y] << 16) | (grayscaleMap[x][y] << 8) | grayscaleMap[x][y];
                segmentedImage.setRGB(x, y, gray);
            }
        }
    }

    /**
     * Finds reference brightness as local minimum on row
     * @param index
     * @return Integer
     */
    private int getReferenceBrightness(int index, boolean reverse){
        int r = 255;
        if(reverse) {
            r = 0;
        }
        for(int i = 0; i < segmentedImage.getWidth(); i++){
            if(reverse) {
                if (grayscaleMap[i][index] > r) {
                    r = grayscaleMap[i][index];
                }
            }
            else {
                if (grayscaleMap[i][index] < r) {
                    r = grayscaleMap[i][index];
                }
            }
        }
        return r;
    }

    /**
     * Sequential recoloring
     * @param bothDirections
     * @param r
     * @param k
     * @param findR
     */
    public void sequentialRecoloringSegmentation(boolean bothDirections, int r, int k, boolean findR){
        for(int y = 0; y < segmentedImage.getHeight(); y++){
            if(findR){
                if(!bothDirections && this.isBrighten()){
                    r = getReferenceBrightness(y, true);
                }
                else {
                    r = getReferenceBrightness(y, false);
                }
            }
            for (int x = 0; x < segmentedImage.getWidth(); x++){
                if(bothDirections){
                    if(Math.abs(grayscaleMap[x][y] - r) <= k){
                        grayscaleMap[x][y] = r;
                    }
                }
                else {
                    if(this.isBrighten()){
                        if(grayscaleMap[x][y] - r <= 0 && grayscaleMap[x][y] - r >= -k){
                            grayscaleMap[x][y] = r;
                        }
                    }
                    else{
                        if(grayscaleMap[x][y] - r >= 0 && grayscaleMap[x][y] - r <= k){
                            grayscaleMap[x][y] = r;
                        }
                    }
                }
            }
        }
        updateImageFromMap();
    }
}
