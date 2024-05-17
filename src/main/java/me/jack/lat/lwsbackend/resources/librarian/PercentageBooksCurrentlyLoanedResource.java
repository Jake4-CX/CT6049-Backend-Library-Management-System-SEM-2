package me.jack.lat.lwsbackend.resources.librarian;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.jack.lat.lwsbackend.annotations.RestrictedRoles;
import me.jack.lat.lwsbackend.entities.User;
import me.jack.lat.lwsbackend.service.dataWarehouse.LibrarianService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/librarian/percentage/currently-loaned/")
public class PercentageBooksCurrentlyLoanedResource {

    @GET
    @RestrictedRoles({User.Role.LIBRARIAN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPercentageBooksCurrentlyLoaned() {
        LibrarianService service = new LibrarianService();
        Map<String, Object> response = new HashMap<>();

        try {
            double result = service.getPercentageBooksCurrentlyLoaned();
            response.put("message", "success");
            response.put("data", result);
            return Response.ok(response).type(MediaType.APPLICATION_JSON).build();

        } catch (SQLException e) {
            response.put("message", "error");
            response.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON).build();
        }
    }
}