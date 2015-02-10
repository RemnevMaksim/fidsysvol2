package ru.mremne.resources;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import ru.mremne.model.IdResult;
import ru.mremne.model.Result;
import ru.mremne.model.ResultPoints;
import ru.mremne.model.Status;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static ru.mremne.service.ServiceConnection.getService;

/**
 * autor:maksim
 * date: 14.01.15
 * time: 20:31.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class IdentifyResource {
    private static final Logger log=Logger.getLogger(IndexResources.class);
    private static Map<String,Result> resultMap=new HashMap<>();
    @POST
    @Path("/identifier/identify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response identify(String input) {
        resultMap.clear();
        ObjectMapper mapper = new ObjectMapper();
        try{
            JsonNode inputJson=mapper.readTree(input);
            JsonNode idJson=inputJson.path("id");
            JsonNode pointsJSON=inputJson.path("points");
            log.info("points: "+pointsJSON.toString());
            int k = -1;
            ArrayList<Integer> dotsX = new ArrayList<>(), dotsY = new ArrayList<>();
            ResultPoints resultPoints=new ResultPoints();
            for (JsonNode point : pointsJSON) {
                dotsX.add(point.get("x").asInt());
                dotsY.add(point.get("y").asInt());
                ++k;
                resultPoints.putInResultPoints(dotsX.get(k), dotsY.get(k));
            }
            TreeSet<Double> angles=ResultPoints.getAngleValue(resultPoints.getPointList());
            double[] ang=new double[angles.size()];
            int i=0;
            for(Double d:angles){
                ang[i]=d;
                i++;
            }
            Result identiResult=new Result();
            if(getService().checkAngles(ang).getStatus()==Response.ok().build().getStatus()){
                identiResult.setId(idJson.toString());
                identiResult.setStatus(Status.READY);
                identiResult.setResult(IdResult.ORIGIN);
                resultMap.put(idJson.toString(),identiResult);
               // getService().saveStatus(identiResult);
            }else{
                identiResult.setId(idJson.toString());
                identiResult.setStatus(Status.READY);
                identiResult.setResult(IdResult.UNKNOWN);
              //  getService().saveStatus(identiResult);
                resultMap.put(idJson.toString(),identiResult);
            }
            return getService().checkAngles(ang);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return status(Response.Status.BAD_REQUEST).build();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
            return status(Response.Status.BAD_REQUEST).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok().build();
    }
    @GET
    @Path("/identifier/status/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response statusToResponse(@PathParam("id") String id){
       // log.info("status info: "+getService().getStatus(id));
        log.info("id: "+id);
        log.info("resultMap size: "+resultMap.size());
        List<Object> list=Arrays.asList(resultMap.values().toArray());
        log.info("result list- "+list);
        log.info("In status method");
        ObjectMapper mapper=new ObjectMapper();
        String output = null;
        if(resultMap.size()!=0) {
            try {
                output = mapper.writeValueAsString(list);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(output);
            resultMap.clear();
        }
        return Response.ok("{\"results\":" + output + "}").build();
    }
}
