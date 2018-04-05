package zvi.ImageProcessing;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Histogram {
    HashMap<String, Integer> imageHistogram;

    public Histogram(BufferedImage image){
        this.imageHistogram = new HashMap<String, Integer>();
        createHistogram(image);
    }

    public HashMap getImageHistogram(){
        return this.imageHistogram;
    }

    public void createHistogram(BufferedImage image){
        convertToGrayScale(image);
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


}
