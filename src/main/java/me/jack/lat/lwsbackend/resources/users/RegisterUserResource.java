package me.jack.lat.lwsbackend.resources.users;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.jack.lat.lwsbackend.annotations.UnprotectedRoute;
import me.jack.lat.lwsbackend.model.NewUser;
import me.jack.lat.lwsbackend.service.oracleDB.UserService;
import me.jack.lat.lwsbackend.util.EnvVariableUtil;

import java.util.HashMap;
import java.util.Map;

@Path("/users/register")
public class RegisterUserResource {

    @POST
    @UnprotectedRoute
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@Valid NewUser newUser) {
        System.out.println("OracleDB Username: " + EnvVariableUtil.getVariable("ORACLE_DB_USERNAME"));
        return createUserSQL(newUser);

    }

    public Response createUserSQL(NewUser newUser) {
        Map<String, Object> response = new HashMap<>();

        UserService userService = new UserService();
        Error userCreated = userService.createUser(newUser);

        if (userCreated == null) {
            response.put("message", "success");
            return Response.status(Response.Status.CREATED).entity(response).type(MediaType.APPLICATION_JSON).build();
        } else {
            response.put("message", userCreated.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(response).type(MediaType.APPLICATION_JSON).build();
        }

    }
}
