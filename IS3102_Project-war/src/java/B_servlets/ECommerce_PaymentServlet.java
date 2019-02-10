/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import EntityManager.ItemEntity;
import HelperClasses.RetailProduct;
import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author icars98
 */
@WebServlet(name = "ECommerce_PaymentServlet", urlPatterns = {"/ECommerce_PaymentServlet"})
public class ECommerce_PaymentServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        try (PrintWriter out = response.getWriter()) {
            Long countryID = (Long) session.getAttribute("countryID");
            ArrayList<ShoppingCartLineItem> shoppingCart = (ArrayList<ShoppingCartLineItem>) (session.getAttribute("shoppingCart"));
            String email = "";
            Long memberID = null;
            Cookie[] memberCookie = request.getCookies();
            for(int i=0;i<memberCookie.length;i++) {
                if (memberCookie[i].getName().equals("memberId")){
                    email = memberCookie[i].getValue();
                }
            }
            memberID = getMemberIDRESTful(email);
            
            Long salesRecordID = null;
            double price = 0;
            double amountPaid = 0;
            if (shoppingCart != null && shoppingCart.size() > 0) {
                for (ShoppingCartLineItem item : shoppingCart) {
                    price = item.getPrice();
                    double subtotal = item.getPrice() * item.getQuantity();
                    amountPaid += subtotal;
                }
            }
            salesRecordID = createECommerceTransactionRecord(memberID, amountPaid, countryID);
            
            int status = 0;
            Long itemEntityID = null;
            Long itemID = null;
            int count = 0;
            Integer quantity = 0;
            String result = "";
            if (salesRecordID != null) {
                if (shoppingCart != null && shoppingCart.size() > 0) {
                    for (ShoppingCartLineItem item : shoppingCart) {
                        price = item.getPrice();
                        double subtotal = item.getPrice() * item.getQuantity();
                        amountPaid += subtotal;
                        
                        itemID = Long.parseLong(item.getId());
                        itemEntityID = getItemEntityIDRESTful(itemID);
                        
                        quantity = item.getQuantity();
                        
                        status = createECommerceLineItemRecord(salesRecordID, itemEntityID, quantity, countryID);
                    }
                }
            }
            
            if (status > 0) {
                shoppingCart.clear();
                
                result = "Thank you for shopping at Island Furniture. Checkout successful!";
                session.setAttribute("shoppingCart", shoppingCart);
                response.sendRedirect("../IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + result);
            } else {
                result = "Due to an error, checkut unsuccessful.";
                session.setAttribute("shoppingCart", shoppingCart);
                response.sendRedirect("../IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
            }
            
        }
    }
    
    public Long getItemEntityIDRESTful(Long itemID) {
        try {
           Long itemEntityID = null;
        
            Client client = ClientBuilder.newClient();
            WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("getItemEntityID")
                .queryParam("itemID", itemID);
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            Response res = invocationBuilder.get();
            System.out.println("status: " + res.getStatus());

            if (res.getStatus() != 200) {
                return null;
            }

            itemEntityID = Long.parseLong(res.readEntity(String.class));

            return itemEntityID;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
        
    public Long getMemberIDRESTful(String email) {
        try {
            Long memberID = null;
        
            Client client = ClientBuilder.newClient();
            WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity")
                .path("getMemberID")
                .queryParam("email", email);
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            Response res = invocationBuilder.get();
            System.out.println("status: " + res.getStatus());

            if (res.getStatus() != 200) {
                return null;
            }

            memberID = Long.parseLong(res.readEntity(String.class));

            return memberID;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public Long createECommerceTransactionRecord(Long memberID, Double amountPaid, Long countryID) {
        Long salesRecordID = null;
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("createECommerceTransactionRecord")
                .queryParam("memberID", memberID)
                .queryParam("amountPaid", amountPaid)
                .queryParam("countryID", countryID);
        Invocation.Builder invocationBuilder = target.request();
        Response response = invocationBuilder.put(Entity.entity("", "application/json"));
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            return null;
        }

        salesRecordID = Long.parseLong(response.readEntity(String.class));
        

        return salesRecordID;
    }
    
    public Integer createECommerceLineItemRecord(Long salesRecordID, Long itemEntityID, Integer quantity, Long countryID) {
        int status = 0;
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("createECommerceLineItemRecord")
                .queryParam("salesRecordID", salesRecordID)
                .queryParam("itemEntityID", itemEntityID)
                .queryParam("quantity", quantity)
                .queryParam("countryID", countryID);
        Invocation.Builder invocationBuilder = target.request();
        Response response = invocationBuilder.put(Entity.entity("", "application/json"));
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            return null;
        }

        status = Integer.parseInt(response.readEntity(String.class));
        

        return status;
    }
    
    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
