package controllers;

import models.S3File;
import plugins.S3Plugin;
import play.db.ebean.Model;
import controllers.Helper;
import controllers.Binary.*;
import controllers.Grayscale.*;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import play.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

//for buffered image
import java.net.URL;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class Image extends Controller {
    // constant upload path
    public static final String PATH = "http://imgproc.s3.amazonaws.com/";

    // upload image
    public static Result upload(String id) {
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart picture = body.getFile("picture");
        
        if (picture != null) {

            //here starts the aws fun
            S3File s3file = new S3File();
            s3file.name = id;
            s3file.file = picture.getFile();
            String imgUrl = null;
            
            try {

                // read image
                BufferedImage input = ImageIO.read(s3file.file);

                // 8-bit image
                BufferedImage im = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g2d = im.createGraphics();

                // convert image to 8-bit image
                g2d.drawImage(input, 0, 0, null);

                imgUrl = Helper.uploadBufferedImageToAws(im, id);
            } catch (IOException ioe) {
            }

            return ok(views.html.image.render(imgUrl));
        } else {
            flash("error", "Missing file");

            // redirect to home
            return redirect(routes.Application.index());
        }
    }

    public static Result smoothing() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(Configurable.processing(json, PATH));
        }
    }

    public static Result median() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(Median.processing(json, PATH));
        }
    }

    public static Result weightedMedian() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(WeightedMedian.processing(json, PATH));
        }
    }

    public static Result toBinary() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(ConvertToBinary.processing(json, PATH));
        }
    }

    public static Result minimum() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(Minimum.processing(json, PATH));
        }
    }

    public static Result maximum() {
        // catch post-request as json object
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            return ok(Maximum.processing(json, PATH));
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
            BufferedImage im = ImageIO.read(new URL(PATH + id));

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

