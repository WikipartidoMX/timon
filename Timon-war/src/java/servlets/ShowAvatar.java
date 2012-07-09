/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import controladores.UserManager;
import entities.registro.Miembro;
import java.io.*;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sessionbeans.TimonLogic;

/**
 *
 * @author alfonso
 */
@WebServlet(name = "ShowAvatar", urlPatterns = {"/ShowAvatar"})
public class ShowAvatar extends HttpServlet {

    @Inject
    TimonLogic tl;
    @Inject
    UserManager um;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long mid = 0;
        try {
            mid = Integer.parseInt(request.getParameter("mid"));
        } catch (Exception e) {
        }
        byte[] f = null;
        String filename = "camafeoh.png";
        Miembro m = tl.getMiembro(mid);
        f = tl.getAvatarFile(mid);
        if (f == null) {
            String path = request.getServletContext().getRealPath("/images/" + filename);
            try {
                if (m.getSexo().equals("M")) {
                    filename = "camafeom.png";
                    path = request.getServletContext().getRealPath("/images/" + filename);
                }
            } catch (Exception e) {
            }
            File file = new File(path);
            f = org.apache.commons.io.FileUtils.readFileToByteArray(file);
        }
        if (f != null) {
            ByteArrayInputStream input = new ByteArrayInputStream(f);
            BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream());
            try {
                int b;
                byte[] buffer = new byte[10240]; // 10kb buffer
                while ((b = input.read(buffer, 0, 10240)) != -1) {
                    output.write(buffer, 0, b);
                }
            } finally {
                output.close();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
