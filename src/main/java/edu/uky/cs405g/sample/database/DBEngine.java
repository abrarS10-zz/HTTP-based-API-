// DBEngine.java
// Group Name:
//         499Team
// Authors:
//         Tom North
//         Abrar Sajeel
//         Kay Guerschom

// Provided code
package edu.uky.cs405g.sample.database;

// Used with permission from Dr. Bumgardner

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;


public class DBEngine {
    private DataSource ds;
    public boolean isInit = false;

    public DBEngine(String host, String database, String login,
                    String password) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String dbConnectionString = null;
            if (database == null) {
                dbConnectionString = "jdbc:mysql://" + host + "?"
                        + "user=" + login + "&password=" + password
                        + "&useUnicode=true&useJDBCCompliantTimezoneShift=true"
                        + "&useLegacyDatetimeCode=false&serverTimezone=UTC";
            } else {
                dbConnectionString = "jdbc:mysql://" + host + "/" + database
                        + "?" + "user=" + login + "&password=" + password
                        + "&useUnicode=true&useJDBCCompliantTimezoneShift=true"
                        + "&useLegacyDatetimeCode=false&serverTimezone=UTC";
            }
            ds = setupDataSource(dbConnectionString);
            isInit = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } // DBEngine()

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = null;
        connectionFactory =
                new DriverManagerConnectionFactory(connectURI, null);
        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    } // setupDataSource()

    public Map<String, String> getUsers() {
        Map<String, String> userIdMap = new LinkedHashMap<>();

        PreparedStatement stmt = null;
        try {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT * FROM Identity";
            stmt = conn.prepareStatement(queryString);
            // No parameters, so no binding needed.
            ResultSet rs = stmt.executeQuery();

            ArrayList<String> userIds = new ArrayList<String>();
            ArrayList<String> userHandles = new ArrayList<String>();

            while (rs.next()) {
                userIds.add(Integer.toString(rs.getInt("idnum")));
                userHandles.add(rs.getString("handle"));
            }
            String uIds = String.join(",", userIds);
            String uHnds = String.join(",", userHandles);

            userIdMap.put("idnums", uIds);
            userIdMap.put("handles", uHnds);
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(userIdMap);
        return userIdMap;
    } // getUsers()

    public Map<String, String> getBDATE(String idnum) {
        Map<String, String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        int id = Integer.parseInt(idnum);
        try {
            Connection conn = ds.getConnection();
            String queryString = null;
// Here is a statement, but we want a prepared statement.
//            queryString = "SELECT bdate FROM Identity WHERE idnum = "+id;
//
            queryString = "SELECT bdate FROM Identity WHERE idnum = ?";
// ? is a parameter placeholder
            stmt = conn.prepareStatement(queryString);
            stmt.setInt(1, id);
// 1 here is to denote the first parameter.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bdate = rs.getString("bdate");
                userIdMap.put("bdate", bdate);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getBDATE()


                        //*******CODE OUR TEAM, 499TEAM, WROTE*******
                       // *******TOM NORTH'S CODE *********

    // Insert a user's: handle, password, full name, location, email, and bdate into database
    // Returns one of the following outputs depending on if the code is successful or not
    // Output: {"status":"4"} // positive number is the Identity.idnum created.--- User was created
    // Output: {"status":"-2", "error":"SQL Constraint Exception"}. ---- User was not created, handle already exists
    // Function to insert user into database
    public Map<String, String> createuser(String handle, String pass, String fullname, String location, String email, String bdate)
    {
        // Create map
        Map<String, String> UserMap = new LinkedHashMap<>();

        PreparedStatement stmt = null;

        // Try statement to insert values into Identity table
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            // Insert command
            queryString = "INSERT INTO Identity (handle, pass, fullname, location, email, bdate) VALUES(?, ?, ?, ?, ?, ?)";

            // Store values
            stmt = conn.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, handle);
            stmt.setString(2, pass);
            stmt.setString(3, fullname);
            stmt.setString(4, location);
            stmt.setString(5, email);
            stmt.setString(6, bdate);

            // Update table
            int result = stmt.executeUpdate();

            // Try statement to create new user if one does not already exist with given handle
            try (ResultSet userId = stmt.getGeneratedKeys())
            {
                // Output if user was successfully created with their idnum
                if (userId.next())
                {
                    String idNum = String.valueOf(userId.getInt(1));
                    UserMap.put("status", idNum);
                }
            }

            // Close
            stmt.close();
            conn.close();
        }

        // Catch statement if user handle already exists
        catch (Exception ex)
        {
            ex.printStackTrace();
            UserMap.put("status", "-2");
            UserMap.put("error", "SQL Constraint Exception");
        }

        // Return map
        return UserMap;
    } // createuser()


    // seeuser api to find user based on handle and password
    // If user is found output their information found in Identity table
    // Example input
    // Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/seeuser/2 (Links to an external site.)
    // 2 = Identity.idnum
    // Output: {"status":"1", "handle":"@carlos", "fullname":"Carlos Mize", "location":"Kentucky", "email":carlos@notgmail.com", "bdate":"1970-01-26","joined":"2020-04-01"}
    // Output: {}. // no match found, could be blocked, user doesn't know.

    // Function to find data in Identity table based on handle and password
    public Map<String, String> seeuser(String handle, String pass, String idnum)
    {
        // Create map
        Map<String, String> UserMap = new LinkedHashMap<>();

        // See if user exists
        int User = correctCred(handle, pass);

        // If user does not exist, return the error
        if (User == -10)
        {
            UserMap.put("status_code", Integer.toString(User));
            UserMap.put("error", "invalid credentials");
        }

        // Else attempt to get info
        else
            {
            PreparedStatement stmt = null;

            // Try statement to find information for user
            try
            {
                Connection conn = ds.getConnection();
                String queryString = null;
                queryString = "SELECT handle, fullname, location, email, bdate, joined FROM Identity WHERE idnum=? and" +
                        " idnum not in (select idnum from Block where idnum = ? and blocked = ?);";

                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, idnum);
                stmt.setString(2, idnum);
                stmt.setInt(3, User);

                // Get query
                ResultSet rs = stmt.executeQuery();

                // Place all user info into Map to return to API
                while (rs.next())
                {
                    handle = rs.getString("handle");
                    String fullname = rs.getString("fullname");
                    String location = rs.getString("location");
                    String email = rs.getString("email");
                    String bdate = rs.getString("bdate");
                    String joined = rs.getString("joined");

                    UserMap.put("status", "1");
                    UserMap.put("handle", handle);
                    UserMap.put("fullname", fullname);
                    UserMap.put("location", location);
                    UserMap.put("email", email);
                    UserMap.put("bdate", bdate);
                    UserMap.put("joined", joined);
                }

                // Close
                rs.close();
                stmt.close();
                conn.close();
            }

            // Catch statements for exceptions
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        // Return map
        return UserMap;
    } // seeuser()


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

    // Function to recommend followers of mutual followers
    public Map<String, String> suggestions(String handle, String password)
    {
        // Create map
        Map<String, String> UserMap = new LinkedHashMap<>();

        // See if current user exists
        int User = correctCred(handle, password);

        // If user does not exist, return the error
        if (User == -10)
        {
            UserMap.put("status_code", Integer.toString(User));
            UserMap.put("error", "invalid credentials");
            return UserMap;
        }
        // Else user is verified
        else
            {
            PreparedStatement stmt = null;

            // Try statement to look for suggested users
            try
            {
                Connection conn = ds.getConnection();

                String queryString = null;

                // Query to find suggested followers
                queryString = "select idnum, handle from Identity inner join (select followedList.followed from Follows " +
                        "followedList inner join (select User.followed from Follows User where " +
                        "User.follower = ?) as UserFollowList on followedList.follower = " +
                        "UserFollowList.followed where followedList.followed NOT IN (select followed from " +
                        "Follows where follower=? union select blocked from Block where idnum = ?) and " +
                        "followedList.followed != ? LIMIT 4) as suggestions on Identity.idnum = suggestions.followed;";

                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, Integer.toString(User));
                stmt.setString(2, Integer.toString(User));
                stmt.setString(3, Integer.toString(User));
                stmt.setString(4, Integer.toString(User));

                // Execute query
                ResultSet rs = stmt.executeQuery();

                rs.last();

                // Find total results
                int total = rs.getRow();
                rs.beforeFirst();
                UserMap.put("status", Integer.toString(total));

                // No suggestions
                if (total == 0)
                {
                    UserMap.put("status", "0");
                    UserMap.put("error", "no suggestions");
                }

                // Yes, suggestions
                else
                    {
                    ArrayList<String> ID = new ArrayList<String>();
                    ArrayList<String> handles = new ArrayList<String>();

                    // Add suggestions
                    while (rs.next())
                    {
                        ID.add(Integer.toString(rs.getInt("idnum")));
                        handles.add(rs.getString("handle"));
                    }

                    String userIds = String.join(",", ID);
                    String userHandles = String.join(",", handles);

                    UserMap.put("idnums", userIds);
                    UserMap.put("handles", userHandles);
                }
                rs.close();
                stmt.close();
                conn.close();
            }

            // Catch statement to find exceptions
            // Failed to find any suggestions
            // Or unable to get suggestions
            catch (Exception ex)
            {
                ex.printStackTrace();
                UserMap.put("status", "0");
                UserMap.put("error", "unable to get suggestions");
            }
        }

        // Return map
        return UserMap;
    } // suggestions()


                // ******* Kay Guerschom's Code ********

    //doesStoryExist
    public int StoryCheck(String sidnum)
    {
        int Id = 0;
        PreparedStatement stmt = null;

        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT idnum FROM Story where sidnum = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,sidnum);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                Id = rs.getInt("idnum");
            }

            rs.close();
            stmt.close();
            conn.close();
        }

        catch(Exception ex)
        {
            ex.printStackTrace();
            //System.out.println("Story does not exist...");
        }
        return Id;
    } // Does story exist


    public Map<String,String> poststory(String handle, String password, String chapter, String url, String expires)
    {
        Map<String,String> storyidMap = new LinkedHashMap<>();
        int userID = correctCred(handle, password);

        if (userID == -10)
        {
            storyidMap.put("status_code", Integer.toString(userID));
            storyidMap.put("error", "invalid credentials");
            return storyidMap;
        }

        else
            {
            PreparedStatement stmt = null;
            try
            {
                Connection conn = ds.getConnection();
                String queryString = null;
                queryString = "INSERT INTO Story (idnum, chapter, url, expires) VALUES(?, ?, ?, ?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, Integer.toString(userID));
                stmt.setString(2, chapter);
                stmt.setString(3, url);
                stmt.setString(4, expires);
                int result = stmt.executeUpdate();
                storyidMap.put("status", Integer.toString(result));
                stmt.close();
                conn.close();
            }

            catch(Exception ex)
            {
                ex.printStackTrace();
                if (ex.getMessage().contains("chapter"))
                {
                    //System.out.println("Failed to post story...");
                    storyidMap.put("status", "0");
                    storyidMap.put("error", "missing chapter");
                }
                else if (ex.getMessage().contains("expires"))
                {
                    //System.out.println("Failed to post story...");
                    storyidMap.put("status", "0");
                    storyidMap.put("error", "invalid expires date");
                }
                else
                    {
                    storyidMap.put("status", "-2");
                    storyidMap.put("error", "SQL Constraint Exception");
                }
            }
        }

        return storyidMap;
    } //poststory


    public Map<String,String> reprint(String handle, String password, String likeit, String sidnum)
    {
        Map<String,String> reprintIdnumMap = new LinkedHashMap<>();
        int reprintUser = correctCred(handle, password);

        if (reprintUser == -10)
        {
            reprintIdnumMap.put("status_code", Integer.toString(reprintUser));
            reprintIdnumMap.put("error", "invalid credentials");
            return reprintIdnumMap;
        }

        int userId = StoryCheck(sidnum);

        if (userId == 0)
        {
            reprintIdnumMap.put("status_code", "0");
            reprintIdnumMap.put("error", "story not found");
            return reprintIdnumMap;
        }

        int Blocked = Blocked(userId, reprintUser);

        if (Blocked == 1)
        {
            reprintIdnumMap.put("status_code", "0");
            reprintIdnumMap.put("error", "blocked");
            return reprintIdnumMap;
        }

        else
            {
            int likes = 0;

            if (likeit.equals("true"))
            {
                likes = 1;
            }

            PreparedStatement stmt = null;

            try
            {
                Connection conn = ds.getConnection();
                String queryString = null;
                queryString = "INSERT INTO Reprint (idnum, sidnum, likeit, newstory) VALUES(?, ?, ?, ?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, Integer.toString(reprintUser));
                stmt.setString(2, sidnum);
                stmt.setString(3, Integer.toString(likes));
                stmt.setString(4, sidnum);
                int result = stmt.executeUpdate();
                //System.out.println("Successfully reprinted story!");
                reprintIdnumMap.put("status", Integer.toString(result));
                stmt.close();
                conn.close();
            }

            catch(Exception ex)
            {
                ex.printStackTrace();
                reprintIdnumMap.put("status", "-2");
                reprintIdnumMap.put("error", "SQL Constraint Exception");
            }
        }

        return reprintIdnumMap;
    } // reprint()


    public int userFollowed(Integer Userid, Integer follow) {
        //this will check if the user is already being followed
        int follows = 0;
        PreparedStatement stmt = null;

        try {
            Connection conn = ds.getConnection();
            String queryString = null;
            // Select query to retrieve Userid
            queryString = "SELECT * FROM Follows where follower = ? and followed = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1, Integer.toString(Userid));
            stmt.setString(2, Integer.toString(follow));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                //if the user is already being followed
                follows = 1;
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return follows;
    }


    public Map<String,String> follow(String handle, String password, String idnum)
    {
        Map<String,String> followMap = new LinkedHashMap<>();
        int idUser = correctCred(handle, password);

        if (idUser == -10)
        {
            followMap.put("status_code", Integer.toString(idUser));
            followMap.put("error", "invalid credentials");
            return followMap;
        }

        int isBlocked = Blocked(Integer.parseInt(idnum), idUser);

        if (isBlocked == 1)
        {
            followMap.put("status_code", "0");
            followMap.put("error", "blocked");
            return followMap;
        }

        int follows = userFollowed(idUser, Integer.parseInt(idnum));

        if (follows == 1)
        {
            followMap.put("status_code", "0");
            followMap.put("error", "user already followed");
        }

        else
            {
            PreparedStatement stmt = null;

            try
            {
                Connection conn = ds.getConnection();
                String queryString = null;
                queryString = "INSERT INTO Follows (follower, followed) VALUES(?, ?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, Integer.toString(idUser));
                stmt.setString(2, idnum);
                int FinalResult = stmt.executeUpdate();
                followMap.put("status", Integer.toString(FinalResult));
                stmt.close();
                conn.close();
            }

            catch(Exception ex)
            {
                ex.printStackTrace();
                if (ex.getMessage().contains("foreign key")) {
                    followMap.put("status", "0");
                    followMap.put("error", "user does not exist");
                }
                else
                    {
                    followMap.put("status", "0");
                    followMap.put("error", "User already being followed");
                    }
            }
        }

        return followMap;
    } // follow


                 // ********* Abrar Majda's Code *************


    // function for unfollow api
    public Map<String,String> unfollow(String handle, String password, String idnum)
    {
        Map<String,String> userMap = new LinkedHashMap<>();

        // Does user exist?
        int user = correctCred(handle, password);

        if (user == -10)
        {
            userMap.put("status_code", Integer.toString(user));
            userMap.put("error", "invalid credentials");
            return userMap;
        }

        else
            {
            PreparedStatement statement = null;
            try
            {
                Connection conn = ds.getConnection();
                String query = null;
                query = "DELETE FROM Follows where follower = ? and followed = ?";

                statement = conn.prepareStatement(query);
                statement.setString(1, Integer.toString(user));
                statement.setString(2, idnum);

                int res = statement.executeUpdate();

                if (res == 0)
                {
                    userMap.put("status", "0");
                    userMap.put("error", "Not currently followed");
                }
                else
                    {
                    userMap.put("status", "1");
                    }

                statement.close();
                conn.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                userMap.put("status", "-2");
                userMap.put("error", "SQL Constraint Exception");
            }
        }
        return userMap;
    }


    // This will return the 1 if the current user is blocked and returns 0 if not blocked
    public int Blocked(Integer blocker, Integer blocking)
    {
        int Blocked = 0;
        PreparedStatement stmt = null;

        // See if user is blocked
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            // Query the DB
            queryString = "SELECT blknum FROM Block where idnum = ? and blocked = ?";
            stmt = conn.prepareStatement(queryString);

            stmt.setString(1, Integer.toString(blocker));
            stmt.setString(2, Integer.toString(blocking));

            ResultSet rs = stmt.executeQuery();

            // User is blocked
            while (rs.next())
            {
                Blocked = 1;
            }

            // Close
            rs.close();
            stmt.close();
            conn.close();
        }

        // User is not blocked
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // return
        return Blocked;
    }


    // function for block api
    public Map<String,String> block(String handle, String password, String idnum)
    {
        Map<String,String> userMap = new LinkedHashMap<>();

        // Does the user exist in the database?
        int user = correctCred(handle, password);

        if (user == -10)
        {
            userMap.put("status_code", Integer.toString(user));
            userMap.put("error", "invalid credentials");
            return userMap;
        }
        //check if this user has been blocked by the current user
        int isBlck = Blocked(user, Integer.parseInt(idnum));
        // if the user is blocked, return the error
        if (isBlck == 1)
        {
            userMap.put("status_code", "0");
            userMap.put("error", "already blocked user");
            return userMap;
        }
        //user has been verified
        else
            {
            PreparedStatement statement = null;
            try
            {
                Connection conn = ds.getConnection();
                String query = null;
                query = "INSERT INTO Block (idnum, blocked) VALUES (?, ?)";

                statement = conn.prepareStatement(query);
                statement.setString(1, Integer.toString(user));
                statement.setString(2, idnum);

                int res = statement.executeUpdate();

                if (res == 0)
                {
                    userMap.put("status", "0");
                    userMap.put("error", "DNE");
                }
                else
                    {
                    userMap.put("status", "1");
                    }

                statement.close();
                conn.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                userMap.put("status", "-2");
                userMap.put("error", "SQL Constraint Exception");
            }
        }
        return userMap;
    }

    //function for api timeline
    public Map<String,String> timeline(String handle, String password, String newest, String oldest)
    {
        Map<String,String> userMap = new LinkedHashMap<>();

        // Does the current user exist in the database
        int user = correctCred(handle, password);

        if (user == -10)
        {
            userMap.put("status_code", Integer.toString(user));
            userMap.put("error", "invalid credentials");
            return userMap;
        }

        else
            {
            PreparedStatement statement = null;
            try
            {
                Connection conn = ds.getConnection();
                String query = null;

                query = "select \"Story\" as type, i1.handle author, s1.sidnum, s1.chapter, s1.tstamp posted from" +
                        " Story as s1, Identity as i1 where ((s1.tstamp < ?) and (s1.tstamp > ?)) and s1.idnum in" +
                        "  (select f1.followed from Follows as f1" +
                        "    where f1.follower = ?) and i1.idnum = s1.idnum" +
                        "  union" +
                        "  select \"Reprint\" as type, i2.handle author, s2.sidnum, s2.chapter, s2.tstamp posted from Story as s2, Identity as i2" +
                        "    where s2.sidnum in " +
                        "    (select r1.sidnum from Reprint as r1 where r1.likeit = 0 and " +
                        "    ((r1.tstamp < ?) and (r1.tstamp > ?))" +
                        "    and idnum in (select f2.followed from Follows as f2 where f2.follower = 9)) and s2.idnum not in" +
                        "    (select b1.blocked from Block as b1 where b1.idnum = ?) and i2.idnum = s2.idnum;";

                statement = conn.prepareStatement(query);
                statement.setString(1, newest);
                statement.setString(2, oldest);
                statement.setInt(3, user);
                statement.setString(4, newest);
                statement.setString(5, oldest);
                statement.setInt(6, user);

                ResultSet results = statement.executeQuery();

                results.last();
                int total = results.getRow();
                results.beforeFirst();

                while (results.next())
                {
                    ArrayList<String> tl = new ArrayList<String>();

                    tl.add("{\"type\":\"" + results.getString("type") + "\"");
                    tl.add("\"author\":\"" + results.getString("author") + "\"");
                    tl.add("\"sidnum\":\"" + Integer.toString(results.getInt("sidnum")) + "\"");
                    tl.add("\"chapter\":\"" + results.getString("chapter") + "\"");
                    tl.add("\"posted\":\"" + results.getString("posted") + "\"}");

                    String s = String.join(",", tl);

                    userMap.put(Integer.toString(results.getRow() - 1), s);
                }

                userMap.put("status", Integer.toString(total));

                results.close();
                statement.close();
                conn.close();

                userMap.put("status", Integer.toString(total));

            }
            catch(Exception e)
            {
                e.printStackTrace();
                userMap.put("status", "0");
                userMap.put("error", "failed to read timeline");
            }
        }
        return userMap;
    }


                // ******** Other Code ***********
               //  ******** Worked on as a team ***********

    // This will return idnum of the current user
    // Returns -10 if invalid credentials
    public int correctCred(String handle, String password)
    {
        int userId = -10;
        PreparedStatement stmt = null;

        // Verify user credentials
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            // Query the DB
            queryString = "SELECT idnum FROM Identity where handle = ? and pass = ?";
            stmt = conn.prepareStatement(queryString);

            stmt.setString(1, handle);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // Correct credentials
            while (rs.next())
            {
                userId = rs.getInt("idnum");
            }

            // Close
            rs.close();
            stmt.close();
            conn.close();
        }

        // Exceptions
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // Return
        return userId;
    }

} // DBEngine class

// DBEngine.java
