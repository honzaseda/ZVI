package zvi.ImageProcessing;

import java.awt.image.BufferedImage;

public class ImageHandler {
    BufferedImage image;

    public ImageHandler(BufferedImage img) {
        this.image = img;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage convertToGrayScale(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                int grayLevel = (r + g + b) / 3;
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                image.setRGB(x, y, gray);
            }
        }
        return image;
    }

}
