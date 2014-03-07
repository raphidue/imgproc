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


public class Minimum {
    public static ObjectNode processing(JsonNode json, String PATH) {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();

        String id = json.findPath("id").toString();
        String uploadPath = PATH + id + ".png";

        try {
            BufferedImage im = ImageIO.read(new URL(uploadPath));

            // create histogram
            int w = im.getWidth();
            int h = im.getHeight();

            // border extension
            BufferedImage copy;
            copy = Helper.copyImage(im, "WHITE");

            // filter operation
            int[] P = new int[9];

            for (int v = 1; v <= h; v++) {
                for (int u = 1; u <= w; u++) {

                    // fill the pixel vector P for filter position (u,v)
                    int k = 0;
                    for (int j = -1; j <= 1; j++) {
                        for (int i = -1; i <= 1; i++) {
                            P[k] = Helper.getPix(copy, u + i, v + j);
                            k++;
                        }
                    }
                    // sort the pixel vector and take 1 element
                    Arrays.sort(P);
                    Helper.setPix(im, u - 1, v - 1, P[0]);
                }
            }
            Helper.uploadBufferedImageToAws(im, id + ".png");

            // create histogram
            respJSON = Helper.generateHisto(id + ".png");
        } catch (IOException ioe) {
            respJSON.put("error", "Error on processing minimum filter...");
        }
        return respJSON;
    }
}