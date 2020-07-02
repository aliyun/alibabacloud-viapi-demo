package com.example.api.demo;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.junit.Test;

/**
 * @author long.qul
 * @date 2020/07/02
 */
public class ImageTest {

    /**
     * 裁切透明像素
     * @throws IOException
     */
    @Test
    public void testCropTransparentPixels() throws IOException {
        URL resource = ImageTest.class.getClassLoader().getResource("transparent.png");
        File imageFile = new File(resource.getFile());
        String directoryPath = imageFile.getParentFile().getPath();

        BufferedImage source = ImageIO.read(imageFile);
        BufferedImage trimmed = trimImage(source);
        String outputPath = directoryPath + "/transparent.trimmed.png";
        ImageIO.write(trimmed, "png", new File(outputPath));
    }

    /**
     * credit to:
     * https://stackoverflow.com/questions/3224561/crop-image-to-smallest-size-by-removing-transparent-pixels-in-java
     * @param image
     * @return
     */
    private BufferedImage trimImage(BufferedImage image) {
        WritableRaster raster = image.getAlphaRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int left = 0;
        int top = 0;
        int right = width - 1;
        int bottom = height - 1;
        int minRight = width - 1;
        int minBottom = height - 1;

        top:
        for (;top < bottom; top++){
            for (int x = 0; x < width; x++){
                if (raster.getSample(x, top, 0) != 0){
                    minRight = x;
                    minBottom = top;
                    break top;
                }
            }
        }

        left:
        for (;left < minRight; left++){
            for (int y = height - 1; y > top; y--){
                if (raster.getSample(left, y, 0) != 0){
                    minBottom = y;
                    break left;
                }
            }
        }

        bottom:
        for (;bottom > minBottom; bottom--){
            for (int x = width - 1; x >= left; x--){
                if (raster.getSample(x, bottom, 0) != 0){
                    minRight = x;
                    break bottom;
                }
            }
        }

        right:
        for (;right > minRight; right--){
            for (int y = bottom; y >= top; y--){
                if (raster.getSample(right, y, 0) != 0){
                    break right;
                }
            }
        }

        return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}
