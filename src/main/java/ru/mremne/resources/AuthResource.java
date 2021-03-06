package ru.mremne.resources;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.mvc.Template;
import ru.mremne.model.mongo.dao.User;
import ru.mremne.service.MongoService;
import ru.mremne.view.ViewUserData;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

/**
 * autor:maksim
 * date: 27.04.15
 * time: 23:12.
 */

@Path("/auth")
public class AuthResource {
    private static final Logger LOG = Logger.getLogger(AuthResource.class);
    @Inject
    private MongoService mongoService;
    final static String USER_ID_ATTRIBUTE = "userId";

    @Context
    SecurityContext securityContext;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String addUser(@FormParam("login") String login,
                          @FormParam("email") String email,
                          @FormParam("name") String name,
                          @FormParam("password") String password) throws IOException {
        if (mongoService.getUserByLogin(login) != null) {
            response.sendRedirect("/auth/register/error");
        }
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setPassword(password);
        mongoService.saveUser(user);
        response.sendRedirect("/");
        return "ok";

    }

    @POST
    @Path("/signin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String processLogin(@FormParam("signin-login") String login,
                               @FormParam("signin-pass") String hash) throws IOException {

        User user = mongoService.getUserByLoginAndPass(login, hash);
        if (user == null) {
            System.out.println("processLogin | name or password is wrong");
            response.sendRedirect("/auth/signin/error");
            return "not found";
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID_ATTRIBUTE, user.getId());

        String referer = request.getHeader("referer");
        response.sendRedirect(referer);
        return "ok!";
    }

    @GET
    @Path("/signin/error")
    @Template(name = "/templates/login/error.ftl")
    public ViewUserData showLoginError() throws IOException {
        return new ViewUserData();
    }

    @GET
    @Path("/signout")
    public String processLogout() throws IOException {
        HttpSession session = request.getSession(true);
        session.removeAttribute(USER_ID_ATTRIBUTE);
        response.sendRedirect("/");
        return "";
    }

    @GET
    @Path("/register/error")
    @Template(name = "/templates/register/exist.ftl")
    public ViewUserData showRegisterError() {
        return new ViewUserData();
    }

}
