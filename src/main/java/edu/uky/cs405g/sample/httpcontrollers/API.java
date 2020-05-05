// API.java
// Group Name:
//         499Team
// Authors:
//         Tom North
//         Abrar Sajeel
//         Kay Guerschom

// Provided code
package edu.uky.cs405g.sample.httpcontrollers;
//
// Sample code used with permission from Dr. Bumgardner
//
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }

    //curl http://localhost:9990/api/status
    //{"status_code":1}
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        String responseString = "{\"status_code\":0}";
        try {
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // healthcheck()

    //curl http://localhost:9998/api/listusers
    //{"1":"@paul","2":"@chuck"}
    @GET
    @Path("/listusers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        String responseString = "{}";
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getUsers();
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // listUsers()


    //curl -d '{"foo":"silly1","bar":"silly2"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/exampleJSON
    //
    //{"status_code":1, "foo":silly1, "bar":silly2}
    @POST
    @Path("/exampleJSON")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exampleJSON(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String fooval = myMap.get("foo");
            String barval = myMap.get("bar");
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1, "
                    + "\"foo\":\"" + fooval + "\", "
                    + "\"bar\":\"" + barval + "\"}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleJSON()

    //curl http://localhost:9990/api/exampleGETBDATE/2
    //{"bdate":"1968-01-26"}
    @GET
    @Path("/exampleGETBDATE/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exampleBDATE(@PathParam("idnum") String idnum) {
        String responseString = "{\"status_code\":0}";
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getBDATE(idnum);
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleBDATE


    //              *******CODE OUR TEAM, 499TEAM, WROTE*******
    //                    *******TOM NORTH'S CODE *********

    // Create user API to insert a user's: handle, password, full name, location, email, and bdate into database
    // Returns one of the following outputs depending on if the code is successful or not
    // Output: {"status":"4"} // positive number is the Identity.idnum created.--- User was created
    // Output: {"status":"-2", "error":"SQL Constraint Exception"}. ---- User was not created, handle already exists
    @POST
    @Path("/createuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    // Function to create user
    public Response createuser(InputStream input) {

        String responseString = "{\"status\":0}";

        // Read input from curl call
        StringBuilder crunchifyBuilder = new StringBuilder();

        // Try statement to create map, store string values from input, and call
        // DBEngine function to input into database table, Identity
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;

            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            // Create a map
            Map<String, String> createuserMap = gson.fromJson(jsonString, mapType);

            // Values of input
            String handle = createuserMap.get("handle");
            String pass = createuserMap.get("password");
            String fullname = createuserMap.get("fullname");
            String location = createuserMap.get("location");
            String email = createuserMap.get("xmail");
            String bdate = createuserMap.get("bdate");

            // Call the function in DBEngine the will add input to database if handle does not already exist
            Map<String, String> UserMap = Launcher.dbEngine.createuser(handle, pass, fullname, location, email, bdate);

            // Output
            responseString = Launcher.gson.toJson(UserMap);

        }

        // Catch statement for exceptions found in code
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }

        // Return
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // createuser()


    // seeuser api to find user based on handle and password
    // If user is found output their information found in Identity table
    // Example input
    // Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/seeuser/2 (Links to an external site.)
    // 2 = Identity.idnum
    // Output: {"status":"1", "handle":"@carlos", "fullname":"Carlos Mize", "location":"Kentucky", "email":carlos@notgmail.com", "bdate":"1970-01-26","joined":"2020-04-01"}
    // Output: {}. // no match found, could be blocked, user doesn't know.
    @POST
    @Path("/seeuser/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    // Function to find user
    public Response seeuser(InputStream inputData, @PathParam("idnum") String idnum) {
        String responseString = "{\"status_code\":0}";

        //Read the input from the curl call
        StringBuilder crunchifyBuilder = new StringBuilder();

        // Try function to find user based on given info
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;

            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();

            // Create a map
            Map<String, String> seeuserMap = gson.fromJson(jsonString, mapType);

            // Create strings to get values from the map
            String handle = seeuserMap.get("handle");
            String pass = seeuserMap.get("password");

            // Call DBEngine function to find user
            Map<String, String> UserMap = Launcher.dbEngine.seeuser(handle, pass, idnum);
            // Output
            responseString = Launcher.gson.toJson(UserMap);
        }

        // Catch statement to find exceptions
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }

        // Return
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // seeuser


    // suggestions api
    // recommend (4) followers based on other followers
    // Query should be give idnum, handle of at most 4 (Hint: LIMIT 4)
    // idnum and handles of people followed by people that are followed
    // by you BUT not you and not anyone you
    // already follow.
    // Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/suggestions (Links to an external site.)
    // Output, status > 0 is the number of suggested people returned
    // Output: {"status":"3", "idnums":"1,2,4", "handles":"@paul,@carlos","@fake"}
    // Output: {"status":"0", "error":"no suggestions"}
    @POST
    @Path("/suggestions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    // Function to recommend followers of mutual followers
    public Response suggestions(InputStream inputData) {
        String responseString = "{\"status_code\":0}";

        //Read input from curl call
        StringBuilder crunchifyBuilder = new StringBuilder();

        // Try statement to find followers
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();

            // Create a map
            Map<String, String> suggestionsMap = gson.fromJson(jsonString, mapType);

            // Get values for map
            String handle = suggestionsMap.get("handle");
            String password = suggestionsMap.get("password");

            // Call function in DBEngine to find followers to suggest
            Map<String, String> SuggestMap = Launcher.dbEngine.suggestions(handle, password);
            // Output
            responseString = Launcher.gson.toJson(SuggestMap);
        }

        // Catch statement to catch exceptions
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }

        // Return
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // suggestions


    // ******* Kay Guerschom's Code ********

    // curl -d '{"handle":"@cooldude42", "password":"mysecret!", "chapter":"I ate at Mario's!", "url":"http://imagesite.dne/marios.jpg"}'
    // -H "Content-Type: application/json" -X POST http://localhost:9990/api/poststory
    @POST
    @Path("/poststory")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    public Response poststory(InputStream inputData)
    {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();

        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;

            while ((line = in.readLine()) != null)
            {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();

            Map<String, String> storyMap = gson.fromJson(jsonString, mapType);
            String handle = storyMap.get("handle");
            String password = storyMap.get("password");
            String chapter = storyMap.get("chapter");
            String url = storyMap.get("url");
            String expires = storyMap.get("expires");

            Map<String, String> teamMap = Launcher.dbEngine.poststory(handle, password, chapter, url, expires);
            responseString = Launcher.gson.toJson(teamMap);
        }
        catch (Exception ex)
        {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } //poststory


    //curl -d '{"handle":"@cooldude42", "password":"mysecret!", "likeit":true}'
    // -H "Content-Type: application/json" -X POST http://localhost:9990/api/reprint/45
    @POST
    @Path("/reprint/{sidnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    public Response reprint(InputStream inputData, @PathParam("sidnum") String sidnum)
    {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();

        try
        {
            // Here its is parsing the input into a JSon string
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null)
            {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> reprintMap = gson.fromJson(jsonString, mapType);
            String handle = reprintMap.get("handle");
            String password = reprintMap.get("password");
            String likeit = reprintMap.get("likeit");
            Map<String, String> rMap = Launcher.dbEngine.reprint(handle, password, likeit, sidnum);
            responseString = Launcher.gson.toJson(rMap);

        }
        catch (Exception ex)
        {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }

        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // reprint


    //curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json"
    // -X POST http://localhost:9990/api/follow/2 (Links to an external site.)
    @POST
    @Path("/follow/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    public Response follow(InputStream inputData, @PathParam("idnum") String idnum)
    {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();

        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;

            while ((line = in.readLine()) != null)
            {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> followMap = gson.fromJson(jsonString, mapType);
            String handle = followMap.get("handle");
            String password = followMap.get("password");
            Map<String, String> teamMap = Launcher.dbEngine.follow(handle, password, idnum);
            responseString = Launcher.gson.toJson(teamMap);
        }
        catch (Exception ex)
        {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }

        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }//follow


                        // ********* Abrar Majda's Code ************

    //unfollow api
    @POST
    @Path("/unfollow/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unfollow(InputStream input, @PathParam("idnum") String idnum)
    {
        String response = "{\"status_code\":0}";
        StringBuilder builder = new StringBuilder();
        try
        {
            // parse input
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while ((line = in.readLine()) != null)
            {
                builder.append(line);
            }
            String jString = builder.toString();

            // create a map of jString
            Map<String, String> map = gson.fromJson(jString, mapType);

            // variables to store data required
            String handle = map.get("handle");
            String password = map.get("password");

            Map<String, String> teamMap = Launcher.dbEngine.unfollow(handle, password, idnum);
            response = Launcher.gson.toJson(teamMap);
        }
        catch (Exception ex)
        {
            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            String exceptionAsString = stringWriter.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(response)
                .header("Access-Control-Allow-Origin", "*").build();
    }


    //block API
    @POST
    @Path("/block/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response block(InputStream input, @PathParam("idnum") String idnum)
    {
        String response = "{\"status_code\":0}";
        StringBuilder builder = new StringBuilder();
        try
        {

            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while ((line = in.readLine()) != null)
            {
                builder.append(line);
            }
            String jString = builder.toString();

            // Create a map of jString
            Map<String, String> map = gson.fromJson(jString, mapType);

            // variables to hold required values
            String handle = map.get("handle");
            String password = map.get("password");

            Map<String, String> team = Launcher.dbEngine.block(handle, password, idnum);
            response = Launcher.gson.toJson(team);
        }
        catch (Exception ex)
        {
            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            String exceptionAsString = stringWriter.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(response)
                .header("Access-Control-Allow-Origin", "*").build();
    }

    //timeline API
    @POST
    @Path("/timeline")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response timeline(InputStream input)
    {
        String response = "{\"status_code\":0}";
        StringBuilder builder = new StringBuilder();
        try
        {
            // input gets parsed into JSON
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while ((line = in.readLine()) != null)
            {
                builder.append(line);
            }
            String jString = builder.toString();

            // Create a map of the jString
            Map<String, String> map = gson.fromJson(jString, mapType);

            // variables to hold necessary data
            String handle = map.get("handle");
            String password = map.get("password");
            String newest = map.get("newest");
            String oldest = map.get("oldest");

            Map<String, String> teamMap = Launcher.dbEngine.timeline(handle, password, newest, oldest);
            response = Launcher.gson.toJson(teamMap);
        }

        catch (Exception ex)
        {
            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            String exception = stringWriter.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exception).build();
        }
        return Response.ok(response)
                .header("Access-Control-Allow-Origin", "*").build();
    }
} // API class

// API.java
