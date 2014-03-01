package controllers;

import controllers.Helper;
import controllers.Binary.*;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import play.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class Image extends Controller {
    // constant upload path
    public static final String PATH = Play.application().path().getAbsolutePath() + "/public/uploads";

    // upload image
    public static Result upload(String id) {
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart picture = body.getFile("picture");
        File theDir = new File(PATH);

        // create directory if not exists
        if (!theDir.exists()) {
            theDir.mkdir();
        }


        if (picture != null) {
            File file = picture.getFile();

            String myUploadPath = PATH + "/" + id;
            file.renameTo(new File(myUploadPath));

            try {
                // read image
                BufferedImage input = ImageIO.read(new File(myUploadPath));

                // 8-bit image
                BufferedImage im = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g2d = im.createGraphics();

                // convert image to 8-bit image
                g2d.drawImage(input, 0, 0, null);

                // write image to filesystem
                ImageIO.write(im, "PNG", new File(myUploadPath));

            } catch (IOException ioe) {
            }
            return ok(views.html.image.render(id));
        } else {
            flash("error", "Missing file");

            // redirect to home
            return redirect(routes.Application.index());
        }
    }

    public static Result smoothing() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in smoothing-filter...");
        } else {
            double[][] filter;
            String id = json.findPath("id").toString();

            // convert filter-json to filter matrix
            filter = Helper.convertJsonToMatrix(json);
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

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
                ImageIO.write(im, "PNG", new File(uploadPath));

                // generate histogram as json
                respJSON = Helper.generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing smoothing-filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result median() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in median-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // create histogram
                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = Helper.copyImage(im, "CONTINUE");

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
                        // sort the pixel vector and take middle element
                        Arrays.sort(P);
                        Helper.setPix(im, u - 1, v - 1, P[4]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // generate histogram as json
                respJSON = Helper.generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing median filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result weightedMedian() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        // convert json to matrix
        double[][] filter;
        filter = Helper.convertJsonToMatrix(json);

        if (json == null) {
            return badRequest("Expecting Json data in weighted median-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = Helper.copyImage(im, "CONTINUE");

                // filter operation
                int[] P = new int[9];

                for (int v = 1; v <= h; v++) {
                    for (int u = 1; u <= w; u++) {

                        // fill the pixel vector P for filter position (u,v)
                        int k = 0;
                        for (int j = -1; j <= 1; j++) {
                            for (int i = -1; i <= 1; i++) {
                                double c = filter[j + 1][i + 1];
                                P[k] = Helper.getPix(copy, u + i, v + j) * (int) c;
                                k++;
                            }
                        }
                        // sort the pixel vector and take the middle element
                        Arrays.sort(P);
                        Helper.setPix(im, u - 1, v - 1, P[4]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = Helper.generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing weighted median-filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result toBinary() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in toBinary()...");
        } else {
            String id = json.findPath("id").toString();

            // threshold value for manually binary conversion
            int threshold = Integer.parseInt(json.findPath("threshold").toString());
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // check if 8-bit image
                if (im.getType() == 10) {
                    // convert to binary by the threshold-value
                    im = Helper.getBinaryImage(threshold, im);
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = Helper.generateBinaryHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing to convert to binary...");
            }
            return ok(respJSON);
        }
    }

    public static Result minimum() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in minimum-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

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
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = Helper.generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing minimum filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result maximum() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in maximum-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // create histogram
                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = Helper.copyImage(im, "BLACK");

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
                        // sort the pixel vector and take the last element
                        Arrays.sort(P);
                        Helper.setPix(im, u - 1, v - 1, P[8]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = Helper.generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing maximum filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result region() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(RegionLabeling.processing(json, PATH));
        }
    }

    public static Result dilate() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(Dilate.processing(json, PATH));
        }
    }

    public static Result erode() {
        // catch post-request as json
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(Erode.processing(json, PATH));
        }
    }

    // function to show histogram as json-object
    public static Result showHist(String id) {
        ObjectNode respJSON = Json.newObject();

        // generate the json from image id
        respJSON = Helper.generateHisto(id);

        return ok(respJSON);
    }

    // function to show a binary histogram as json-object
    public static Result showBinaryHist(String id) {
        ObjectNode respJSON = Json.newObject();

        // generate binary histogram from image-id
        respJSON = Helper.generateBinaryHisto(id);

        return ok(respJSON);
    }

    // function to count regions in a labeled image
    public static Result showLabels(String id) {

        ObjectNode respJSON = Json.newObject();

        try {
            // read image
            BufferedImage im = ImageIO.read(new File(PATH + "/" + id));

            // create histogram
            int w = im.getWidth();
            int h = im.getHeight();

            // hashset for distinct selection
            HashSet<Integer> distinct = new HashSet<Integer>();

            // iterate trough image
            for (int v = 0; v < h; v++) {
                for (int u = 0; u < w; u++) {
                    distinct.add(Helper.getPix(im, u, v));
                }
            }

            // - 1: dont count the 0 value
            respJSON.put("labels", distinct.size() - 1);

        } catch (IOException ioe) {
            respJSON.put("error", "Error in showLabels()...");
        }
        return ok(respJSON);
    }
}