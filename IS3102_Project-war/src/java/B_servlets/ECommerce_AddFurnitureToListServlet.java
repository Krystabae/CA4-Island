/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import HelperClasses.RetailProduct;
import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author icars98
 */
@WebServlet(name = "ECommerce_AddFurnitureToListServlet", urlPatterns = {"/ECommerce_AddFurnitureToListServlet"})
public class ECommerce_AddFurnitureToListServlet extends HttpServlet {

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
        try (PrintWriter out = response.getWriter()) {
            HttpSession session = request.getSession();
            
            Long countryID = (Long) session.getAttribute("countryID");
            
            String id = request.getParameter("id");
            String SKU = request.getParameter("SKU");
            Double price = Double.parseDouble(request.getParameter("price"));
            String name = request.getParameter("name");
            String imageURL = request.getParameter("imageURL");
            Integer quantity = 1;
            
            int getQuantity = getLineItemAvailabilityListRESTful(countryID, SKU);
            String result = "";
            ArrayList<ShoppingCartLineItem> shoppingCart = (ArrayList<ShoppingCartLineItem>) (session.getAttribute("shoppingCart"));
            
            if (shoppingCart == null || shoppingCart.size() < 1) {
                shoppingCart = new ArrayList();
            
                int size = shoppingCart.size();
                if ((size + quantity) <= getQuantity) {
                    ShoppingCartLineItem s = new ShoppingCartLineItem(id, SKU, name, imageURL, price, quantity, countryID);
                    shoppingCart.add(s);
                
                    session.setAttribute("shoppingCart", shoppingCart);
                
                    result = "Item successfully added into the cart!";
                    response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + result);
                }
                else {
                    result = "There is insufficient quantity of item ''"+ name +"'' in the storage bin. Item is not added to cart.";
                    response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
                }
            } else {
                int size = shoppingCart.size();
                if ((size + quantity) <= getQuantity) {
                    ShoppingCartLineItem s = new ShoppingCartLineItem(id, SKU, name, imageURL, price, quantity, countryID);
                    shoppingCart.add(s);
                
                    session.setAttribute("shoppingCart", shoppingCart);
                
                    result = "Item successfully added into the cart!";
                    response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + result);
                }
                else {
                    result = "There is insufficient quantity of item ''"+ name +"'' in the storage bin. Item is not added to cart.";
                    response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
                }
            }
            
            
            
            
            
            
        } catch(Exception ex) {
            ex.printStackTrace();
            String result = "Due to an error, item is not added to cart.";
            response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
        }
    }
    
    public int getLineItemAvailabilityListRESTful(Long countryID, String SKU) {
        try {
            Integer getQuantity = 0;
        
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.countryentity")
                .path("getQuantity")
                .queryParam("countryID", countryID)
                .queryParam("SKU", SKU);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        Response res = invocationBuilder.get();
        System.out.println("status: " + res.getStatus());

        if (res.getStatus() != 200) {
            return 0;
        }

        getQuantity = Integer.parseInt(res.readEntity(String.class));

        return getQuantity;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
        
        
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
