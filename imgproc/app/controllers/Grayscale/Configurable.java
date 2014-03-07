package controllers.Grayscale;

import controllers.Helper;

import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.net.URL;


public class Configurable {
    public static ObjectNode processing(JsonNode json, String PATH) {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();

        double[][] filter;
        String id = json.findPath("id").toString();

        // convert filter-json to filter matrix
        filter = Helper.convertJsonToMatrix(json);
        String uploadPath = PATH + id + ".png";

        try {
            BufferedImage im = ImageIO.read(new URL(uploadPath));

            // create histogram
            int w = im.getWidth();
            int h = im.getHeight();

            // border extenstion
            BufferedImage copy;
            copy = Helper.copyImage(im, "BLACK");

            // apply filter
            for (int v = 1; v <= h; v++) {
                for (int u = 1; u <= w; u++) {
                    double sum = 0;
                    for (int j = -1; j <= 1; j++) {
                        for (int i = -1; i <= 1; i++) {
                            // get pixel
                            int p = Helper.getPix(copy, u + i, v + j);
                            // get the corresponding filter coefficient:
                            double c = filter[j + 1][i + 1];
                            sum = sum + c * p;
                        }
                    }

                    int q = (int) Math.round(sum);

                    // check pixel bounds
                    q = Helper.checkPixel(q);

                    // set Pixel
                    Helper.setPix(im, u - 1, v - 1, q);
                }
            }
            Helper.uploadBufferedImageToAws(im, id + ".png");

            // generate histogram as json
            respJSON = Helper.generateHisto(id + ".png");
        } catch (IOException ioe) {
            respJSON.put("error", "Error on processing smoothing-filter...");
        }
        return respJSON;

    }
}