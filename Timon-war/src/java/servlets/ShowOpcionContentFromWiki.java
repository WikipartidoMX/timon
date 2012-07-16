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

import entities.votacionydebate.Opcion;
import entities.votacionydebate.Votacion;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@WebServlet(name = "ShowOpcionContentFromWiki", urlPatterns = {"/ShowOpcionContentFromWiki"})
public class ShowOpcionContentFromWiki extends HttpServlet {

    @Inject
    VotoYDebateLogic vl;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long oid = 0;
        long vid = 0;
        String url = null;
        String titulo = null;
        try {
            oid = Integer.parseInt(request.getParameter("oid"));
        } catch (Exception e) {
        }
        try {
            vid = Integer.parseInt(request.getParameter("vid"));
        } catch (Exception e) {
        }        
        if (vid != 0) {
            Votacion v = vl.getVotacion(vid);
            url = v.getUrl();
            titulo = new String(v.getNombre());
        } else
        if (oid != 0) {
            Opcion o = vl.getOpcion(oid);
            url = o.getUrl();
            titulo = o.getNombre();
        }
        if (url == null) {
            response.setStatus(500);
            return;
        }
        titulo = "<h1>"+titulo+"</h1>";
        System.out.println("URL: "+url);
        response.setContentType("text/html; charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();
        HttpEntity entity = null;
        HttpClient httpclient = null;
        HttpGet httpget = null;
        try {
            httpclient = new DefaultHttpClient();
            httpget = new HttpGet(url);
            HttpResponse r = httpclient.execute(httpget);
            entity = r.getEntity();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        InputStream instream = null;
        if (entity != null) {
            out.write(titulo.getBytes());
            try {
                instream = entity.getContent();
                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = instream.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } catch (IOException ex) {
                throw ex;

            } catch (RuntimeException ex) {
                httpget.abort();
                throw ex;
            } finally {

                instream.close();
                out.flush();
                out.close();
            }
            httpclient.getConnectionManager().shutdown();
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
