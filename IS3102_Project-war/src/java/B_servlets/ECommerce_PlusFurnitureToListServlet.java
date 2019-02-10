/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author icars98
 */
@WebServlet(name = "ECommerce_PlusFurnitureToListServlet", urlPatterns = {"/ECommerce_PlusFurnitureToListServlet"})
public class ECommerce_PlusFurnitureToListServlet extends HttpServlet {

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
            /* TODO output your page here. You may use following sample code. */
            ArrayList<ShoppingCartLineItem> shoppingCart = (ArrayList<ShoppingCartLineItem>) (session.getAttribute("shoppingCart"));
            String SKU = request.getParameter("SKU");
            String result;
            int arrIndex = 0;
            int quantity = 0;
            
            if (shoppingCart != null && shoppingCart.size() > 0) {
                for (ShoppingCartLineItem item : shoppingCart) {
                    quantity = item.getQuantity();
                    
                    arrIndex = shoppingCart.indexOf(SKU);
                }
            }
            quantity++;
            if (arrIndex >= 0) {
                shoppingCart.get(arrIndex).setQuantity(quantity);
                session.setAttribute("shoppingCart", shoppingCart);
                
                result = "Item quantity increased successfully!";
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + result);
            } else {
                result = "Due to an error, item quantity is not increased.";
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            String result = "Due to an error, item quantity is not increased.";
            response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
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
