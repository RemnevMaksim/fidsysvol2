package ru.mremne.service;

import org.apache.log4j.Logger;
import org.neo4j.helpers.collection.IteratorUtil;
import ru.mremne.executor.CypherExecutor;
import ru.mremne.executor.JdbcCypherExecutor;
import ru.mremne.model.Labels;
import ru.mremne.model.Relationships;
import ru.mremne.model.Result;
import ru.mremne.model.ResultPoints;
import ru.mremne.util.Util;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * @author maksim
 * date: 20.12.14
 * time: 21:30.
 */
@Resource
@ManagedBean
public class FidService {

    private final CypherExecutor cypher=createCypherExecutor(Util.getNeo4jUrl());
    private static final Logger LOG =Logger.getLogger(FidService.class);
    public static final int CONSTR=3;
    private CypherExecutor createCypherExecutor(String uri) {
        try {
            String auth = new URL(uri).getUserInfo();
            if (auth != null) {
                String[] parts = auth.split(":");
                return new JdbcCypherExecutor(uri,parts[0],parts[1]);
            }
            return new JdbcCypherExecutor(uri);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Neo4j-ServerURL " + uri);
        }
    }

    public boolean addAngles(double[] angles){
       LOG.info("add angles..");
        if(angles.length!=0){
            int level=0;
            for(int i=0;i<angles.length-1;i++){
                cypher.query("MATCH (n:"+ Labels.INTERVALS +"{value: "+angles[i]+" })," +
                        "(m:"+Labels.INTERVALS +"{value: "+angles[i+1]+"}) " +
                        "CREATE UNIQUE (n)-[:"+ Relationships.LEVEL+"{level: "+level+" }]->(m) " +
                        "return m",map("1",null));
                level++;
            }
          return true;
        }else{
            LOG.error("nothing to add!");
            return false;
        }
    }
    public Response checkAngles(double[] angles){
        int levelExpected=angles.length-1;
        int levelActual=0;
        LOG.info("checking angles...");
        SortedSet<String> identityList=new TreeSet<>();
        if(angles.length!=0){
            for(int i=0;i<angles.length-1;i++){
                identityList.add(IteratorUtil.asCollection(cypher.query("START n = node(*)\n" +
                        "MATCH n-[r:"+Relationships.LEVEL+"]->c\n" +
                        "WHERE HAS(n.value) AND HAS(c.value) " +
                        "AND n.value>(" + (angles[i] - CONSTR) + ") AND n.value<(" + (angles[i] + CONSTR) + ")\n" +
                        "AND c.value>(" + (angles[i + 1] - CONSTR) + ") AND c.value<(" + (angles[i + 1] + CONSTR) + ")\n" +
                        "RETURN n, r,c", map("1", i))).toString());
            }
            LOG.info("map.size =" + identityList.size());
            for(String m:identityList){
                int tmp=ResultPoints.extractMaxLevel(m);
                if(tmp>levelActual) {
                    LOG.info("founded angles: " + m);
                    levelActual=tmp;
                }
            }
        }else{
            LOG.error("nothing to search!!");
            return Response.noContent().build();
        }
        LOG.info("expected level was : " + levelExpected + " ,but actual is : " + levelActual);
        if(Math.abs(levelExpected-levelActual)<=levelActual) {
            LOG.info("everything is ok!!");
            return Response.ok().build();
        }
        return Response.noContent().build();
    }
    public void saveStatus(Result result){
        LOG.info("Saving status info...");
        if(result!=null) {
            cypher.query("CREATE (r: " + Labels.STATUS + "{" + result.toString() + "}) return r", map("1", null));
            LOG.info("ok!");
        }else{
            LOG.warn("no results to save!");
        }
    }
    public Map<String,Object> getStatus(String id){
        LOG.info("Get results by id..");
        Map<String, Object> params = new HashMap<>();
        if(id!=null){
             params=cypher.query("MATCH (r: "+Labels.STATUS +"{id: \""+id+"\"}) return r.id AS id, r.status AS status, r.result AS result  "
                                ,map("1",null)).next();
            LOG.info("ok");
        }
        return params;
    }

    public void deleteCurrentStatus(String id){
        cypher.query("MATCH (r: "+Labels.STATUS +"{id: \""+id+"\"}) DELETE r",map(null,null));
    }
}
