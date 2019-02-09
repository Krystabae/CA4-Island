package service;

import Entity.Countryentity;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("entity.countryentity")
public class CountryentityFacadeREST extends AbstractFacade<Countryentity> {

    @PersistenceContext(unitName = "WebService")
    private EntityManager em;

    public CountryentityFacadeREST() {
        super(Countryentity.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Countryentity entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Countryentity entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Countryentity find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Countryentity> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Countryentity> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @GET
    @Path("country")
    @Produces({"application/json"})
    public List<Countryentity> listAllCountries() {
        Query q = em.createQuery("Select c from Countryentity c");
        List<Countryentity> list = q.getResultList();
        List<Countryentity> countryList = new ArrayList();
        for (Countryentity country : list) {
            em.detach(country);
            country.setItemCountryentityList(null);
            country.setMemberentityList(null);
            country.setStoreentityList(null);
            country.setWarehouseentityList(null);
            countryList.add(country);
        }
        return countryList;
    }
    
    @GET
    @Path("getQuantity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuantity(@QueryParam("countryID") Long countryID, @QueryParam("SKU") String SKU) {
        try {
            String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
            Connection conn = DriverManager.getConnection(connURL);
            String sqlStr = "select lineitementity.QUANTITY from lineitementity\n" +
                "inner join item_countryentity on lineitementity.ITEM_ID = item_countryentity.ITEM_ID\n" +
                "inner join itementity on lineitementity.ITEM_ID = itementity.ID\n" +
                "where  item_countryentity.COUNTRY_ID=? and itementity.SKU=?;";
            
            PreparedStatement pstmt = conn.prepareStatement(sqlStr);
            pstmt.setLong(1, countryID);
            pstmt.setString(2, SKU);
            
            ResultSet rs = pstmt.executeQuery();
            
            String getQuantity = "";
            if (rs.next()) {
                getQuantity = rs.getString("QUANTITY");
            }
            //GenericEntity<Integer> myEntity = new GenericEntity<Integer>(getQuantity){};
            conn.close();
            
            return Response.status(200).entity("" + getQuantity).build();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
