package controllers.Binary;

import controllers.Helper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import java.net.URL;


public class Erode {
    public static ObjectNode processing(JsonNode json, String PATH) {

        ObjectNode respJSON = Json.newObject();

        int[][] filter;

        // convert json to matrix
        filter = Helper.convertBinaryJsonToMatrix(json);

        String id = json.findPath("id").toString();
        String uploadPath = PATH + id + ".png";

        try {
            BufferedImage im = ImageIO.read(new URL(uploadPath));

            // check if 8-bit image
            if (im.getType() == 10) {
                // convert to binary image by threshold-value 127
                im = Helper.getBinaryImage(127, im);
            }

            int w = im.getWidth();
            int h = im.getHeight();
            int newValue;

            // border extension
            BufferedImage copy;
            copy = Helper.copyBinaryImage(im, "CONTINUE");

            // filter operation
            for (int i = 1; i <= h; i++) {
                for (int j = 1; j <= w; j++) {
                    newValue = 255;

                    // minimum filter
                    for (int k = -1; k <= 1; k++) {
                        for (int l = -1; l <= 1; l++) {
                            int filterVal = filter[k + 1][l + 1];
                            if (filterVal == 1) {
                                filterVal = -255;
                            }
                            int bufferVal = Helper.getPix(copy, j + l, i + k);
                            bufferVal -= filterVal;

                            // check min
                            if (bufferVal < newValue) {
                                newValue = bufferVal;
                            }
                        }
                    }
                    // check pixel bounds
                    newValue = Helper.checkPixel(newValue);
                    Helper.setPix(im, j - 1, i - 1, newValue);
                }
            }
            Helper.uploadBufferedImageToAws(im, id + ".png");

            // generate histogram as JSON
            respJSON = Helper.generateBinaryHisto(id + ".png");
        } catch (IOException ioe) {
            respJSON.put("error", "Error on processing erosion...");
        }
        return respJSON;
    }
}