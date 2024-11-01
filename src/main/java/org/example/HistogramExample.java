package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HistogramExample {

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("kostka.png"));
        BufferedImage stretchedImage = stretchHistogram(image);
        BufferedImage equalizedImage = equalizeHistogram(image);

        BufferedImage binaryzationImage = binaryzationManual(image,150);
        BufferedImage precentBlackImage = binaryzationPercentBlack(image,50);
        BufferedImage meanIterativeImage = binaryzationMeanIterative(image);

        ImageIO.write(stretchedImage, "png", new File("stretched.png"));
        ImageIO.write(equalizedImage, "png", new File("equalized.png"));

        ImageIO.write(binaryzationImage, "png", new File("binaryzationImage.png"));
        ImageIO.write(precentBlackImage, "png", new File("precentBlackImage.png"));
        ImageIO.write(meanIterativeImage, "png", new File("meanIterativeImage.png"));
    }

    public static BufferedImage stretchHistogram(BufferedImage image) {
        // Find min and max pixel values
        int min = 255, max = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Assuming grayscale
                if (gray < min) min = gray;
                if (gray > max) max = gray;
            }
        }

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Assuming grayscale
                int stretched = (int) (((gray - min) / (double) (max - min)) * 255);
                int newRgb = (stretched << 16) | (stretched << 8) | stretched; // Grayscale
                newImage.setRGB(x, y, newRgb);
            }
        }
        return newImage;
    }

    public static BufferedImage equalizeHistogram(BufferedImage image) {
        // Implement histogram equalization
        int[] histogram = new int[256];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Assuming grayscale
                histogram[gray]++;
            }
        }

        // Compute cumulative distribution function (CDF)
        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        // Normalize the CDF
        int cdfMin = cdf[0];
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Assuming grayscale
                int equalized = (int) (((cdf[gray] - cdfMin) / (double) (image.getWidth() * image.getHeight() - cdfMin)) * 255);
                int newRgb = (equalized << 16) | (equalized << 8) | equalized; // Grayscale
                newImage.setRGB(x, y, newRgb);
            }
        }
        return newImage;
    }


    public static BufferedImage binaryzationManual(BufferedImage image, int threshold) {
        BufferedImage binaryImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Assuming grayscale from red channel
                int newColor = gray < threshold ? 0 : 255; // Black or white
                int newRgb = (newColor << 16) | (newColor << 8) | newColor; // Grayscale
                binaryImage.setRGB(x, y, newRgb);
            }
        }
        return binaryImage;
    }

    public static BufferedImage binaryzationPercentBlack(BufferedImage image, double percent) {
        int totalPixels = image.getWidth() * image.getHeight();
        int blackThreshold = (int) (percent * totalPixels);
        int[] histogram = new int[256];

        // Create histogram
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Assuming grayscale
                histogram[gray]++;
            }
        }

        // Find the threshold where the number of black pixels reaches the specified percentage
        int cumulative = 0;
        int threshold = 0;
        for (int i = 0; i < histogram.length; i++) {
            cumulative += histogram[i];
            if (cumulative >= blackThreshold) {
                threshold = i;
                break;
            }
        }

        return binaryzationManual(image, threshold);
    }

    public static BufferedImage binaryzationMeanIterative(BufferedImage image) {
        int threshold = 128; // Initial threshold
        int newThreshold;
        do {
            int sumBelow = 0, countBelow = 0;
            int sumAbove = 0, countAbove = 0;

            // Calculate mean for each side of the threshold
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    int gray = (rgb >> 16) & 0xFF; // Assuming grayscale
                    if (gray < threshold) {
                        sumBelow += gray;
                        countBelow++;
                    } else {
                        sumAbove += gray;
                        countAbove++;
                    }
                }
            }

            // Calculate new threshold
            newThreshold = (countBelow == 0 ? 0 : sumBelow / countBelow) + (countAbove == 0 ? 255 : sumAbove / countAbove);
            newThreshold /= 2;

            if (newThreshold == threshold) {
                break; // No change in threshold
            }
            threshold = newThreshold;
        } while (true);

        return binaryzationManual(image, threshold);
    }

}
