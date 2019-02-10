package service;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("commerce")
public class ECommerceFacadeREST {

    @Context
    private UriInfo context;

    public ECommerceFacadeREST() {
    }

    @GET
    @Produces("application/json")
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }
    
    @GET
    @Path("getItemEntityID")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemEntityID(@QueryParam("itemID") String itemID) {
        try {
            String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
            Connection conn = DriverManager.getConnection(connURL);
            String sqlStr = "select ID from lineitementity where ITEM_ID=? limit 1;";
            
            PreparedStatement pstmt = conn.prepareStatement(sqlStr);
            pstmt.setString(1, itemID);
            
            ResultSet rs = pstmt.executeQuery();
            
            String memberID = "";
            if (rs.next()) {
                memberID = rs.getString("ID");
            }
            conn.close();
            
            return Response.status(200).entity("" + memberID).build();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * PUT method for updating or creating an instance of ECommerce
     *
     * @param memberID
     * @param amountPaid
     * @param countryID
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Path("createECommerceTransactionRecord")
    @Consumes({"application/json"})
    public Response createECommerceTransactionRecord (@QueryParam("memberID") Long memberID, @QueryParam("amountPaid") Double amountPaid, 
            @QueryParam("countryID") Long countryID) {
        try {
            String currency = "";
            if (countryID == 25) {
                currency = "SGD";
            }
            
            String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
            Connection conn = DriverManager.getConnection(connURL);
            String sqlStr = "insert into salesrecordentity (AMOUNTDUE, AMOUNTPAID, CREATEDDATE, CURRENCY, MEMBER_ID) \n" +
                "VALUES (?,?,date(now()),?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
            pstmt.setDouble(1, amountPaid);
            pstmt.setDouble(2, amountPaid);
            pstmt.setString(3, currency);
            pstmt.setLong(4, memberID);
            
            int result = pstmt.executeUpdate();
            
            String generatedKey = "";
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                generatedKey = rs.getString(1);
            }
            
            return Response.status(Response.Status.CREATED).entity("" + generatedKey).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @PUT
    @Path("createECommerceLineItemRecord")
    @Consumes({"application/json"})
    public Response createECommerceLineItemRecord (@QueryParam("salesRecordID") Long salesRecordID, 
            @QueryParam("itemEntityID") Long itemEntityID, @QueryParam("quantity") Integer quantity, 
            @QueryParam("countryID") Long countryID) {
        try {
            String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
            Connection conn = DriverManager.getConnection(connURL);
            String sqlStr = "insert into salesrecordentity_lineitementity VALUES(?,?);";
            
            PreparedStatement pstmt = conn.prepareStatement(sqlStr);
            pstmt.setLong(1, salesRecordID);
            pstmt.setLong(2, itemEntityID);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                return Response.status(Response.Status.CREATED).entity("" + result).build();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
}
