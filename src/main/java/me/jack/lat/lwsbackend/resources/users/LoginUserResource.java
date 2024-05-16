package me.jack.lat.lwsbackend.resources.users;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.jack.lat.lwsbackend.annotations.UnprotectedRoute;
import me.jack.lat.lwsbackend.model.LoginUser;
import me.jack.lat.lwsbackend.model.NewUser;
import me.jack.lat.lwsbackend.service.oracleDB.UserService;

import java.util.HashMap;
import java.util.Map;

@Path("/users/login")
public class LoginUserResource {

    @POST
    @UnprotectedRoute
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@Valid LoginUser loginUser) {
        return loginUserSQL(loginUser);

    }

    public Response loginUserSQL(LoginUser loginUser) {
        Map<String, Object> response = new HashMap<>();

        UserService userService = new UserService();
        HashMap<String, Object> returnEntity = userService.loginValidation(loginUser.getUserEmail(), loginUser.getUserPassword());

        if (returnEntity != null) {
            response.put("message", "success");
            response.put("data", returnEntity);
            return Response.status(Response.Status.OK).entity(response).type(MediaType.APPLICATION_JSON).build();
        } else {
            response.put("message", "Incorrect email or password.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).type(MediaType.APPLICATION_JSON).build();
        }

    }
}
