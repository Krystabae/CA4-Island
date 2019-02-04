/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import HelperClasses.Member;
import static HelperClasses.Member.member;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author icars98
 */
@WebServlet(name = "ECommerce_MemberEditProfileServlet", urlPatterns = {"/ECommerce_MemberEditProfileServlet"})
public class ECommerce_MemberEditProfileServlet extends HttpServlet {

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
        String result = "";
        try (PrintWriter out = response.getWriter()) {
            HttpSession session = request.getSession();
            
            Member member = null;
            // Passing all the form values into variables
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String city = request.getParameter("country");
            String address = request.getParameter("address");
            int securityQuestion = Integer.parseInt(request.getParameter("securityQuestion"));
            String securityAnswer = request.getParameter("securityAnswer");
            int age = Integer.parseInt(request.getParameter("age"));
            int income = Integer.parseInt(request.getParameter("income"));
            
            member = new Member(name, email, phone, city, address, securityQuestion, securityAnswer, age, income);
                    
            String password = request.getParameter("repassword");
            
            String status = updateMember(name, email, phone, city, address, securityQuestion, securityAnswer, age, income, password); // Pass info into Servlet and vice versa
            
            if(status.equals("True")) {
                result = "Account updated successfully.";
                session.setAttribute("member", member);
                response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?goodMsg=" + result);
            } else {
                result = "Failed to update account. Please try again.";
                session.setAttribute("member", member);
                response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);
            }
        }
    }
    
    public String updateMember(String name, String email, String phone, String city, String address, Integer securityQuestion, 
            String securityAnswer, Integer age, Integer income, String password) {
        
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity")
                .path("updateMember")
                .queryParam("name", name)
                .queryParam("email", email)
                .queryParam("phone", phone)
                .queryParam("city", city)
                .queryParam("address", address)
                .queryParam("securityQuestion", securityQuestion)
                .queryParam("securityAnswer", securityAnswer)
                .queryParam("age", age)
                .queryParam("income", income)
                .queryParam("password", password);
        
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        Response res = invocationBuilder.get();
        
        String s=res.readEntity(String.class);
        
        
        return s;
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
