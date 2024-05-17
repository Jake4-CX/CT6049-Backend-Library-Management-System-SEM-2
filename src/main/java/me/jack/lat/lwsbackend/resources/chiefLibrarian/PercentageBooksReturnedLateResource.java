package me.jack.lat.lwsbackend.resources.chiefLibrarian;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.jack.lat.lwsbackend.annotations.RestrictedRoles;
import me.jack.lat.lwsbackend.entities.User;
import me.jack.lat.lwsbackend.service.dataWarehouse.ChiefLibrarianService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/chief-librarian/percentage/books-late")
public class PercentageBooksReturnedLateResource {

    @GET
    @RestrictedRoles({User.Role.CHIEF_LIBRARIAN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPercentageBooksReturnedLate() {
        ChiefLibrarianService service = new ChiefLibrarianService();
        Map<String, Object> response = new HashMap<>();

        try {
            double result = service.getPercentageBooksReturnedLate();
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