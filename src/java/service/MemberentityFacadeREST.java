package service;

import Entity.Itementity;
import Entity.Lineitementity;
import Entity.Member;
import Entity.Memberentity;
import Entity.Qrphonesyncentity;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless
@Path("entity.memberentity")
public class MemberentityFacadeREST extends AbstractFacade<Memberentity> {

    @PersistenceContext(unitName = "WebService")
    private EntityManager em;

    public MemberentityFacadeREST() {
        super(Memberentity.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Memberentity entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Memberentity entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("members")
    @Produces({"application/json"})
    public List<Memberentity> listAllMembers() {
        Query q = em.createQuery("Select s from Memberentity s where s.isdeleted=FALSE");
        List<Memberentity> list = q.getResultList();
        for (Memberentity m : list) {
            em.detach(m);
            m.setCountryId(null);
            m.setLoyaltytierId(null);
            m.setLineitementityList(null);
            m.setWishlistId(null);
        }
        List<Memberentity> list2 = new ArrayList();
        list2.add(list.get(0));
        return list;
    }

    //this function is used by ECommerce_MemberLoginServlet
    @GET
    @Path("login")
    @Produces("application/json")
    public Response loginMember(@QueryParam("email") String email, @QueryParam("password") String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String passwordSalt = rs.getString("PASSWORDSALT");
            String passwordHash = generatePasswordHash(passwordSalt, password);
            if (passwordHash.equals(rs.getString("PASSWORDHASH"))) {
                return Response.ok(email, MediaType.APPLICATION_JSON).build();
            } else {
                System.out.println("Login credentials provided were incorrect, password wrong.");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    public String generatePasswordSalt() {
        byte[] salt = new byte[16];
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(salt);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("\nServer failed to generate password salt.\n" + ex);
        }
        return Arrays.toString(salt);
    }

    public String generatePasswordHash(String salt, String password) {
        String passwordHash = null;
        try {
            password = salt + password;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            passwordHash = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("\nServer failed to hash password.\n" + ex);
        }
        return passwordHash;
    }

    // This GET() method is called by ECommerce_GetMember
    @GET
    @Path("member")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMember(@QueryParam("memberEmail") String memberEmail) {
        try {
            String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
            Connection conn = DriverManager.getConnection(connURL);
            String sqlStr = "SELECT * FROM memberentity m WHERE m.EMAIL=? and m.ISDELETED=?";
            PreparedStatement pstmt = conn.prepareStatement(sqlStr);
            pstmt.setString(1, memberEmail);
            pstmt.setInt(2, 0);
            ResultSet rs = pstmt.executeQuery();

            Member m = null;

            if (rs.next()) {
                long id = rs.getLong("ID");
                String address = rs.getString("ADDRESS");
                String name = rs.getString("NAME");
                String email = rs.getString("EMAIL");
                String phone = rs.getString("PHONE");
                String city = rs.getString("CITY"); // Labled as "Country" in IslandFurniture User Profile
                int securityQuestion = rs.getInt("SECURITYQUESTION");
                String securityAnswer = rs.getString("SECURITYANSWER");
                int age = rs.getInt("AGE");
                int income = rs.getInt("INCOME");
                m = new Member(id, name, email, phone, city, address, securityQuestion, securityAnswer, age, income);
            }

            GenericEntity<Member> myEntity = new GenericEntity<Member>(m){};
            conn.close();
            return Response
                    .status(200)
                    .entity(myEntity)
                    .build();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    // When this method is active and not commented out, it causes errors with GlassFish during WebService deployment:
    // "WARNING: No injection source found for a parameter of type public javax.ws.rs.core.Response service.MemberentityFacadeREST.updateMember(Entity.Member,java.lang.String) at index 0."
    // An attempt to test WebService using Postman only returned a Status 403 Forbidden (shown by screenshot of Postman)
    // Method called by ECommerce.MemberEditProfileServlet
    @PUT
    @Path("updateMember")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMember(@QueryParam("name") String name, @QueryParam("email") String email, @QueryParam("phone") String phone, 
            @QueryParam("city") String city, @QueryParam("address") String address, 
            @QueryParam("securityQuestion") Integer securityQuestion, @QueryParam("securityAnswer") String securityAnswer, 
            @QueryParam("age") Integer age, @QueryParam("income") Integer income, @QueryParam("password") String password) {
        
        try {
            // Passing each info into variables before pushing them to mySQL database
//            String name = member.getName();
//            String email = member.getEmail();
//            String phone = member.getPhone();
//            String city = member.getCity();
//            String address = member.getAddress();
//            int securityQuestion = member.getSecurityQuestion();
//            String securityAnswer = member.getSecurityAnswer();
//            int age = member.getAge();
//            int income = member.getIncome();
            
            String passwordSalt = "";
            String passwordHash = "";
            
            if (!password.equals("")) {
                // Generate passwordSalt and passwordHash
                passwordSalt = generatePasswordSalt();
                passwordHash = generatePasswordHash(passwordSalt, password);
            }
        
            String result = "";
            
            // Checking if change password field is empty or not, then the corresponding block of code will be executed:



            if (passwordSalt.equals("") || passwordHash.equals("")) {
                // Update the SQL database
                String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
                Connection conn = DriverManager.getConnection(connURL);
                String sqlStr = ("UPDATE memberentity m SET m.NAME=?, m.PHONE=?, m.CITY=?, m.ADDRESS=?,"
                        + " m.SECURITYQUESTION=?, m.SECURITYANSWER=?, m.AGE=?, m.INCOME=? WHERE m.EMAIL=?;");
                // PreparedStatements
                PreparedStatement pstmt = conn.prepareStatement(sqlStr);
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, city);
                pstmt.setString(4, address);
                pstmt.setInt(5, securityQuestion);
                pstmt.setString(6, securityAnswer);
                pstmt.setInt(7, age);
                pstmt.setInt(8, income);
                pstmt.setString(9, email);
            
                int rec = pstmt.executeUpdate();
                
                if (rec > 0) {
                    result = "True";
                }
                else {
                    result = "False";
                }
                conn.close();
                
            } else {
                // Update the SQL database
                String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
                Connection conn = DriverManager.getConnection(connURL);
                String sqlStr = ("UPDATE memberentity m SET m.NAME=?, m.PHONE=?, m.CITY=?, m.ADDRESS=?,"
                        + " m.SECURITYQUESTION=?, m.SECURITYANSWER=?, m.AGE=?, m.INCOME=?, m.PASSWORDSALT=?, m.PASSWORDHASH=? "
                        + "WHERE m.EMAIL=?;");
                // PreparedStatements
                PreparedStatement pstmt = conn.prepareStatement(sqlStr);
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, city);
                pstmt.setString(4, address);
                pstmt.setInt(5, securityQuestion);
                pstmt.setString(6, securityAnswer);
                pstmt.setInt(7, age);
                pstmt.setInt(8, income);
                pstmt.setString(9, passwordSalt);
                pstmt.setString(10, passwordHash);
                pstmt.setString(11, email);
            
                int rec = pstmt.executeUpdate();
                
                if (rec > 0) {
                    result = "True";
                }
                else {
                    result = "False";
                }
                conn.close();
            
            }
            return Response.status(200).entity("" + result).build();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    @GET
    @Path("uploadShoppingList")
    @Produces({"application/json"})
    public String uploadShoppingList(@QueryParam("email") String email, @QueryParam("shoppingList") String shoppingList) {
        System.out.println("webservice: uploadShoppingList called");
        System.out.println(shoppingList);
        try {
            Query q = em.createQuery("select m from Memberentity m where m.email=:email and m.isdeleted=false");
            q.setParameter("email", email);
            Memberentity m = (Memberentity) q.getSingleResult();
            List<Lineitementity> list = m.getLineitementityList();
            if (!list.isEmpty()) {
                for (Lineitementity lineItem : list) {
                    em.refresh(lineItem);
                    em.flush();
                    em.remove(lineItem);
                }
            }
            m.setLineitementityList(new ArrayList<Lineitementity>());
            em.flush();

            Scanner sc = new Scanner(shoppingList);
            sc.useDelimiter(",");
            while (sc.hasNext()) {
                String SKU = sc.next();
                Integer quantity = Integer.parseInt(sc.next());
                if (quantity != 0) {
                    q = em.createQuery("select i from Itementity i where i.sku=:SKU and i.isdeleted=false");
                    q.setParameter("SKU", SKU);
                    Itementity item = (Itementity) q.getSingleResult();

                    Lineitementity lineItem = new Lineitementity();

                    lineItem.setItemId(item);
                    lineItem.setQuantity(quantity);
                    System.out.println("Item: " + item.getSku());
                    System.out.println("Quantity: " + quantity);
                    m.getLineitementityList().add(lineItem);
                }
            }
            return "success";
            //return s;
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @GET
    @Path("syncWithPOS")
    @Produces({"application/json"})
    public String tieMemberToSyncRequest(@QueryParam("email") String email, @QueryParam("qrCode") String qrCode) {
        System.out.println("tieMemberToSyncRequest() called");
        try {
            Query q = em.createQuery("SELECT p from Qrphonesyncentity p where p.qrcode=:qrCode");
            q.setParameter("qrCode", qrCode);
            Qrphonesyncentity phoneSyncEntity = (Qrphonesyncentity) q.getSingleResult();
            if (phoneSyncEntity == null) {
                return "fail";
            } else {
                phoneSyncEntity.setMemberemail(email);
                em.merge(phoneSyncEntity);
                em.flush();
                return "success";
            }
        } catch (Exception ex) {
            System.out.println("tieMemberToSyncRequest(): Error");
            ex.printStackTrace();
            return "fail";
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
