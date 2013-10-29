package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._include.*;
import views.html.*;

public class Application extends Controller {
    
    public static Result index() {
        return ok(views.html.index.render(scripts.render(), navigation.render("index"), footer.render()));
    }
    
    public static Result processing() {
        return ok(views.html.processing.render(scripts.render(), navigation.render("processing"), footer.render()));
    }
    
    public static Result contact() {        
        return ok(views.html.contact.render(scripts.render(), navigation.render("contact"), footer.render()));
    }
    
    public static Result about() {
        return ok(views.html.about.render(scripts.render(), navigation.render("about"), footer.render()));
    }
    
    public static Result documentation() {
        return ok(views.html.documentation.render(scripts.render(), 
            navigation.render("documentation"), footer.render()));
    }
}
