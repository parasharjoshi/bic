/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.common;

import com.oracle.bits.bic.to.Credential;
import com.oracle.bits.bic.to.InceptionModelTO;
import com.oracle.bits.bic.to.RequestTO;
import com.oracle.bits.bic.to.RestoreModelTO;
import com.oracle.bits.bic.to.TrainRequestTO;
import com.oracle.bits.bic.to.TrainingImageTO;
import com.oracle.bits.bic.to.TrainingModelTO;
import com.oracle.bits.bic.to.UserTO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
@Path("/")
public class BicServiceRestController {

    public static final String VERSION = "1.0";
    public final BicServiceWrapper bicServiceWrapper = new BicServiceWrapper();

    @GET
    @Path("/getdata")
    public Response getMsg() {
        String output = "{\"message\":\"Welcome to BIC (Basic Image Classifier)\"}";
        return Response.status(200).entity(output).header("Content-Type", "application/json").build();
    }
    
    @GET
    @Path("/uniqueId")
    public Response getUniqueId() {
        String output = "{\"UUID\":\""+bicServiceWrapper.getUniqueToken()+"\"}";
        return Response.status(200).entity(output).header("Content-Type", "application/json").build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogin(Credential creds) {
        System.out.println("Got request to DoLogin....");
        return Response.status(200).entity(bicServiceWrapper.doLogin(creds)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }

    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signUp(UserTO user) {
        System.out.println("Got request to DoLogin....");
        return Response.status(200).entity(bicServiceWrapper.signUp(user)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/canrecognize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response canRecognize() {
        System.out.println("Got request to canRecognize....");
        return Response.status(200).entity(bicServiceWrapper.canRecognize()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/recogrequests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response recogStats() {
        System.out.println("Got request to recogStats....");
        return Response.status(200).entity(bicServiceWrapper.getRecogStats()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }

    @POST
    @Path("/recognize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recognize(RequestTO req) {
        System.out.println("Got request to Recognize....");
        return Response.status(200).entity(bicServiceWrapper.recognizeImage(req)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @POST
    @Path("/admin/upload/trainingimg")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadTrainingImage(TrainingImageTO req) {
        System.out.println("Got request to uploadTrainingInage....");
        return Response.status(200).entity(bicServiceWrapper.uploadTrainingImage(req)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/admin/labelToTrain")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLabelsToTrain() {
        System.out.println("Got request to uploadTrainingInage....");
        return Response.status(200).entity(bicServiceWrapper.getLabelsToTrain()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/admin/isTraining")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isTrainingInProgress() {
        System.out.println("Got request to isTrainingInProgress....");
        String output = "{\"isTraining\":"+bicServiceWrapper.isTrainingInProgress()+"}";
        return Response.status(200).entity(output).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @POST
    @Path("/admin/startTraining")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response trainModel(TrainingModelTO req) {
        System.out.println("Got request to uploadTrainingInage....");
        return Response.status(200).entity(bicServiceWrapper.trainModel(req)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @POST
    @Path("/admin/restoreModel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreModel(RestoreModelTO req) {
        System.out.println("Got request to uploadTrainingInage....");
        return Response.status(200).entity(bicServiceWrapper.restoreModel(req)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @POST
    @Path("/trainrequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newTrainingRequest(TrainRequestTO req) {
        System.out.println("Got request to newTrainingRequest....");
        return Response.status(200).entity(bicServiceWrapper.newTrainRequest(req)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }

    @POST
    @Path("/recogFeedback")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recogFeedback(RequestTO req) {
        System.out.println("Got request to Recognize....");
        return Response.status(200).entity(bicServiceWrapper.recogFeedback(req)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @POST
    @Path("/downloadInitModel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadInitModel(InceptionModelTO modelTo) {
        System.out.println("Got request to downloadModel....");
        return Response.status(200).entity(bicServiceWrapper.downloadInitModel(modelTo)).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/modelinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentModelInfo() {
        System.out.println("Got request to getCurrentModelInfo....");
        return Response.status(200).entity(bicServiceWrapper.getCurrentModel()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/admin/modelinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentModelInfoForAdmin() {
        System.out.println("Got request to getCurrentModelInfoForAdmin....");
        return Response.status(200).entity(bicServiceWrapper.getCurrentModelInfoForAdmin()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/admin/allmodel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllModelForAdmin() {
        System.out.println("Got request to getCurrentModelInfoForAdmin....");
        return Response.status(200).entity(bicServiceWrapper.getAllModelForAdmin()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/admin/trainreq")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTrainingRequestForAdmin() {
        System.out.println("Got request to getCurrentModelInfoForAdmin....");
        return Response.status(200).entity(bicServiceWrapper.getAllTrainingRequestForAdmin()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/admin/light/trainreq")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTrainingRequestForAdminNoContent() {
        System.out.println("Got request to getCurrentModelInfoForAdmin....");
        return Response.status(200).entity(bicServiceWrapper.getAllTrainingRequestForAdminNoContent()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/activity")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllActivity() {
        System.out.println("Got request to getAllActivity....");
        return Response.status(200).entity(bicServiceWrapper.getAllActivity()).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
    
    @GET
    @Path("/checkUsername/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isUsernameExists(@PathParam("username") String username) {
        System.out.println("Got request to isUsernameExists....");
        Boolean result = bicServiceWrapper.isUserNameExists(username);
        String returnString = "{ \"available\" : "+result.toString()+"}";
        return Response.status(200).entity(returnString).header("Content-Type", "application/json")
                //.header("Access-Control-Allow-Origin","*")
                .build();
    }
}
