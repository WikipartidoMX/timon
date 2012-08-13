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
package timon.servlets;

import timon.controladores.UserManager;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import timon.sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@WebServlet(name = "ShowImagenOpcion", urlPatterns = {"/ShowImagenOpcion"})
public class ShowImagenOpcion extends HttpServlet {

    @Inject
    VotoYDebateLogic vl;
    @Inject
    UserManager um;
    byte[] png = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A};
    byte[] jpg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    byte[] gif = new byte[]{(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38};

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long oid = 0;
        try {
            oid = Integer.parseInt(request.getParameter("oid"));
        } catch (Exception e) {
        }
        byte[] f = null;
        f = vl.getImagenDeOpcion(oid);

        if (f != null) {
            
            String mime = "image/xyz";
            if (Arrays.equals(Arrays.copyOfRange(f, 0, 8), png)) {
                mime = "image/png";
            }
            if (Arrays.equals(Arrays.copyOfRange(f, 0, 3), jpg)) {
                mime = "image/jpg";
            }
            if (Arrays.equals(Arrays.copyOfRange(f, 0, 3), gif)) {
                mime = "image/gif";
            }
            //System.out.println(mime);
            //response.setContentType(mime);
            //response.setContentLength(f.length);
            
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
