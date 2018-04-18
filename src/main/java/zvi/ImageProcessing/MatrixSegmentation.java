package zvi.ImageProcessing;

public class MatrixSegmentation {
    private int[][] adjacencyMatrix;
    private int numberOfSegments;
    private ImageHandler loadedImage;
    public ImageHandler segmentedImage;


    public MatrixSegmentation(ImageHandler imageHandler, boolean useDiagNeighbours) {
        adjacencyMatrix = new int[256][256];
        loadedImage = imageHandler;
        segmentedImage = imageHandler;
        numberOfSegments = 2;
        createAdjacencyMatrix(useDiagNeighbours);
        recoloringSegmentation();
        segmentedImage.updateImageFromMap();
    }

    private void createAdjacencyMatrix(boolean useDiagNeighbours) {
        int imageWidth = loadedImage.getImage().getWidth();
        int imageHeight = loadedImage.getImage().getHeight();
        int[][] imagePixels = loadedImage.getGrayscaleMap();

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

    void recoloringSegmentation() {
        int minIndex = findDiagMin();
        int maxNeighbour = getMaxNeighbour(minIndex);
//        System.out.println("Min: " + minIndex + ", max neighbour: " + maxNeighbour);
        for (int i = 0; i < 256; i++) {
            adjacencyMatrix[i][maxNeighbour] += adjacencyMatrix[i][minIndex];
            adjacencyMatrix[i][minIndex] = 0;

            adjacencyMatrix[maxNeighbour][i] += adjacencyMatrix[minIndex][i];
            adjacencyMatrix[minIndex][i] = 0;
        }

        segmentedImage.updateGrayscaleMap(minIndex, maxNeighbour);
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
}
