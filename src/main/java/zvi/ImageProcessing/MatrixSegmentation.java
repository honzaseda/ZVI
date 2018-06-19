package zvi.ImageProcessing;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MatrixSegmentation {
    private int[][] adjacencyMatrix;
    private int numberOfSegments;
    private BufferedImage loadedImage;
    private BufferedImage segmentedImage;
    private int[][] grayscaleMap;

    public boolean isBrighten() {
        return brighten;
    }

    public void setBrighten(boolean brighten) {
        this.brighten = brighten;
    }

    public boolean brighten = true;


    public MatrixSegmentation(BufferedImage image, int segments) {
        loadedImage = image;
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();

        adjacencyMatrix = new int[256][256];
        numberOfSegments = segments;

        segmentedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        grayscaleMap = ImageHandler.getGrayscaleMap(loadedImage);


    }

    public BufferedImage getSegmentedImage(){
        updateImageFromMap();
        return segmentedImage;
    }

    public void createAdjacencyMatrix(boolean useDiagNeighbours) {
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();
        int[][] imagePixels = grayscaleMap;

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                if (x != 0) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y]]++;
                    if (useDiagNeighbours) {
                        if (y != 0) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y - 1]]++;
                        }
                        if (y != imageHeight - 1) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y + 1]]++;
                        }
                    }
                }
                if (x != imageWidth - 1) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y]]++;
                    if (useDiagNeighbours) {
                        if (y != 0) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y - 1]]++;
                        }
                        if (y != imageHeight - 1) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y + 1]]++;
                        }
                    }
                }
                if (y != 0) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x][y - 1]]++;
                }
                if (y != imageHeight - 1) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x][y + 1]]++;
                }
            }
        }
    }

    public void recoloringSegmentation() {
        int c = 0;
        while(numberOfRegions() > numberOfSegments) {
            int minIndex = findDiagMin();
            int maxNeighbour = getMaxNeighbour(minIndex);
            for (int i = 0; i < 256; i++) {
                adjacencyMatrix[i][maxNeighbour] += adjacencyMatrix[i][minIndex];
                adjacencyMatrix[i][minIndex] = 0;

                adjacencyMatrix[maxNeighbour][i] += adjacencyMatrix[minIndex][i];
                adjacencyMatrix[minIndex][i] = 0;
            }
            System.out.println(c + ". Přebarvuji jas " + minIndex + " na " + maxNeighbour);
            updateGrayscaleMap(minIndex, maxNeighbour);
            c++;
        }
//        if (numberOfRegions() > numberOfSegments) {
//            recoloringSegmentation();
//        }
    }

    private int numberOfRegions() {
        int count = 0;
        for (int i = 0; i < 256; i++) {
            if (adjacencyMatrix[i][i] > 0) {
                count++;
            }
        }
        return count;
    }

    private int findDiagMin() {
        int min = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int j = 0; j < 256; j++) {
            if (adjacencyMatrix[j][j] <= min) {
                if (isValidBrightness(j)) {
                    min = adjacencyMatrix[j][j];
                    minIndex = j;
                }
            }
        }
        return minIndex;
    }

    private int getMaxNeighbour(int rowIndex) {
        int currentMax = adjacencyMatrix[rowIndex][0];
        int maxIndex = 0;
        if (rowIndex == 0) {
            currentMax = adjacencyMatrix[rowIndex][1];
            maxIndex = 1;
        }

        for (int i = 0; i < 256; i++) {
            if (i == rowIndex) {
                continue;
            }
            if (adjacencyMatrix[rowIndex][i] > currentMax) {
                currentMax = adjacencyMatrix[rowIndex][i];
                maxIndex = i;
            } else if (adjacencyMatrix[rowIndex][i] == currentMax && currentMax != 0) {
                if (Math.abs(rowIndex - i) < Math.abs(rowIndex - maxIndex)) {
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    private boolean isValidBrightness(int index) {
        int sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += adjacencyMatrix[index][i];
        }
        return (sum > 0);
    }

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

    private void updateImageFromMap() {
        for (int x = 0; x < segmentedImage.getWidth(); x++) {
            for (int y = 0; y < segmentedImage.getHeight(); y++) {
                int gray = (grayscaleMap[x][y] << 16) | (grayscaleMap[x][y] << 8) | grayscaleMap[x][y];
                segmentedImage.setRGB(x, y, gray);
            }
        }
    }

    public int getReferenceBrightness(int index){
        int r = 255;
        for(int i = 0; i < segmentedImage.getWidth(); i++){
            if(grayscaleMap[i][index] < r){
                r = grayscaleMap[i][index];
            }
        }
        return r;
    }

    public void sequentialRecoloringSegmentation(boolean bothDirections, int r, int k, boolean findR){
        for(int y = 0; y < segmentedImage.getHeight(); y++){
            if(findR){
                r = getReferenceBrightness(y);
            }
            for (int x = 0; x < segmentedImage.getWidth(); x++){
                if(bothDirections){
                    if(Math.abs(grayscaleMap[x][y] - r) <= k){
                        grayscaleMap[x][y] = r;
                    }
                }
                else {
                    if(this.isBrighten()){
                        if(grayscaleMap[x][y] - r <= 0 && grayscaleMap[x][y] >= -k){
                            grayscaleMap[x][y] = r;
                        }
                    }
                    else{
                        if(grayscaleMap[x][y] - r >= 0 && grayscaleMap[x][y] <= k){
                            grayscaleMap[x][y] = r;
                        }
                    }
                }
            }
        }
        updateImageFromMap();
    }
}
