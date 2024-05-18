package me.jack.lat.lwsbackend.resources.financeDirector;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.jack.lat.lwsbackend.annotations.RestrictedRoles;
import me.jack.lat.lwsbackend.entities.User;
import me.jack.lat.lwsbackend.service.dataWarehouse.FinanceDirectorService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/finance-director/average/fines/duration/{duration_milliseconds}")
public class AverageCostEachFinesPaidLastDurationResource {

    @GET
    @RestrictedRoles({User.Role.FINANCE_DIRECTOR})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAverageCostEachFinesPaidLastDuration(@PathParam("duration_milliseconds") long durationMilliseconds) {
        FinanceDirectorService service = new FinanceDirectorService();
        Map<String, Object> response = new HashMap<>();

        try {
            double result = service.getAverageCostEachFinesPaidLastDuration(durationMilliseconds);
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