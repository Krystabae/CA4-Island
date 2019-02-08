package service;

import Entity.RetailProduct;
import Entity.Retailproductentity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("entity.retailproductentity")
public class RetailproductentityFacadeREST extends AbstractFacade<Retailproductentity> {

    @PersistenceContext(unitName = "WebService")
    private EntityManager em;

    public RetailproductentityFacadeREST() {
        super(Retailproductentity.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Retailproductentity entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Retailproductentity entity) {
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
    public Retailproductentity find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Retailproductentity> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Retailproductentity> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("getRetailProductList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRetailProductsList(@QueryParam("countryID") Long countryID) {
        try {
            String connURL = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
            Connection conn = DriverManager.getConnection(connURL);
            String sqlStr = "select * from itementity\n" +
                "inner join item_countryentity on itementity.ID = item_countryentity.ITEM_ID\n" +
                "inner join retailproductentity on itementity.ID = retailproductentity.ID\n" +
                "where item_countryentity.COUNTRY_ID=? and itementity.DTYPE='RetailProductEntity';";
            PreparedStatement pstmt = conn.prepareStatement(sqlStr);
            pstmt.setLong(1,countryID);
            
            ResultSet rs = pstmt.executeQuery();
            List<RetailProduct> list = new ArrayList();
            while (rs.next()) {
                RetailProduct rp;
                rp = new RetailProduct(
                        rs.getLong("ID"),
                        rs.getString("NAME"),
                        rs.getString("IMAGEURL"),
                        rs.getString("SKU"),
                        rs.getString("DESCRIPTION"),
                        rs.getString("TYPE"),
                        rs.getString("CATEGORY"),
                        rs.getDouble("RETAILPRICE"));
                        
                list.add(rp);
            }
            
            GenericEntity<List<RetailProduct>> myEntity = new GenericEntity<List<RetailProduct>>(list){};
            conn.close();
            return Response.status(200).entity(myEntity).build();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
