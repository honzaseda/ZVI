package zvi.ImageProcessing;

import java.awt.image.BufferedImage;

public class MatrixSegmentation {
    private int[][] adjacencyMatrix;
    private int numberOfSegments;
    private BufferedImage loadedImage;
    private BufferedImage segmentedImage;
    private int[][] grayscaleMap;


    public MatrixSegmentation(BufferedImage image, boolean useDiagNeighbours, int segments) {
        loadedImage = image;
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();

        adjacencyMatrix = new int[256][256];
        numberOfSegments = segments;

        segmentedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        grayscaleMap = ImageHandler.getGrayscaleMap(loadedImage);

        createAdjacencyMatrix(useDiagNeighbours);
        recoloringSegmentation();
    }

    public BufferedImage getSegmentedImage(){
        updateImageFromMap();
        return segmentedImage;
    }

    private void createAdjacencyMatrix(boolean useDiagNeighbours) {
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();
        int[][] imagePixels = ImageHandler.getGrayscaleMap(loadedImage);

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                if (x != 0) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y]]++;
                    if (useDiagNeighbours) {
                        if (y != 0) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y - 1]]++;
                        }
                        if (y != imageWidth - 1) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x - 1][y + 1]]++;
                        }
                    }
                }
                if (x != imageHeight - 1) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y]]++;
                    if (useDiagNeighbours) {
                        if (y != 0) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y - 1]]++;
                        }
                        if (y != imageWidth - 1) {
                            this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x + 1][y + 1]]++;
                        }
                    }
                }
                if (y != 0) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x][y - 1]]++;
                }
                if (y != imageWidth - 1) {
                    this.adjacencyMatrix[imagePixels[x][y]][imagePixels[x][y + 1]]++;
                }
            }
        }
    }

    private void recoloringSegmentation() {
        int minIndex = findDiagMin();
        int maxNeighbour = getMaxNeighbour(minIndex);
//        System.out.println("Min: " + minIndex + ", max neighbour: " + maxNeighbour);
        for (int i = 0; i < 256; i++) {
            adjacencyMatrix[i][maxNeighbour] += adjacencyMatrix[i][minIndex];
            adjacencyMatrix[i][minIndex] = 0;

            adjacencyMatrix[maxNeighbour][i] += adjacencyMatrix[minIndex][i];
            adjacencyMatrix[minIndex][i] = 0;
        }

        updateGrayscaleMap(minIndex, maxNeighbour);
        if (numberOfRegions() > numberOfSegments) {
            recoloringSegmentation();
        }
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

    public void updateGrayscaleMap(int oldValue, int newValue) {
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

    public void updateImageFromMap() {
        for (int x = 0; x < segmentedImage.getWidth(); x++) {
            for (int y = 0; y < segmentedImage.getHeight(); y++) {
                int gray = (grayscaleMap[x][y] << 16) + (grayscaleMap[x][y] << 8) + grayscaleMap[x][y];
                segmentedImage.setRGB(x, y, gray);
            }
        }
    }
}
