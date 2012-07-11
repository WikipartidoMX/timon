/*
 *   __    __ _ _     _   ___           _   _     _             __  __
 *  / / /\ \ (_) | __(_) / _ \__ _ _ __| |_(_) __| | ___   /\/\ \ \/ /
 *  \ \/  \/ / | |/ /| |/ /_)/ _` | '__| __| |/ _` |/ _ \ /    \ \  / 
 *   \  /\  /| |   < | / ___/ (_| | |  | |_| | (_| | (_) / /\/\ \/  \ 
 *    \/  \/ |_|_|\_\|_\/    \__,_|_|   \__|_|\__,_|\___/\/    \/_/\_\
 *                                              
 *                                              
 *  
 * Wikipartido de Mexico
 * VER ARCHIVO DE LiCENCIA
 * 
 *
 */
package servlets;

import controladores.VotoYDebateController;
import java.io.*;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alfonso
 */
@WebServlet(name = "ImagenDeNuevaVotacion", urlPatterns = {"/ImagenDeNuevaVotacion"})
public class ImagenDeNuevaVotacion extends HttpServlet {

    @Inject
    VotoYDebateController vydc;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        byte[] f = null;
        try {
            f = vydc.getImagen().getContents();
        } catch (Exception e) {
            String path = request.getServletContext().getRealPath("/images/fondoVotacion.png");
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
