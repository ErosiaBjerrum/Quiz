package bybjerrum.routes;

import bybjerrum.controller.SecurityController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SecurityRoutes {

    public static EndpointGroup getRoutes(SecurityController controller) {

        return () -> {
            path("auth", () -> {

                post("register", controller::register);
                post("login", controller::login);
                get("me", controller::getCurrentUser);
            });
        };
    }

}