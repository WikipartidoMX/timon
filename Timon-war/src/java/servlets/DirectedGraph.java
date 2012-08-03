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

import controladores.votacion.VotacionController;
import entities.votacionydebate.Opcion;
import entities.votacionydebate.ResultadoSchulze;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames Gracias a Dr. John B. Matthews por la solucion para
 * calcular las dimensiones
 */
@WebServlet(name = "DirectedGraph", urlPatterns = {"/DirectedGraph"})
public class DirectedGraph extends HttpServlet {

    @Inject
    VotacionController vc;
    @Inject
    VotoYDebateLogic vl;
    double phi;
    int barb;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ResultadoSchulze rs = vc.getRs();
        if (rs == null) {
            System.out.println("RS es nulo!!!");
        }
        List<Opcion> opciones = rs.getVotacion().getOpciones();
        System.out.println("Creando la Grafica para " + rs.getVotacion().getNombre() + "...");
        int n = opciones.size();

        // Tamano de la imagen y radios
        int w = 800;
        int h = 600;
        int mw = w / 2;
        int mh = h / 2;
        int r = (4 * mh) / 5;
        int rt = r + 50;
        int ra = r;
        int r2 = Math.abs((mh - r) / 2);

        phi = Math.toRadians(40);
        barb = 20;

        Map<Opcion, Point> cords = new HashMap<Opcion, Point>();
        Map<Opcion, Point> tcords = new HashMap<Opcion, Point>();
        Map<Opcion, Point> acords = new HashMap<Opcion, Point>();        
        int i = 0;
        for (Opcion op : opciones) {
            double t = 2 * Math.PI * i / n;
            int x = (int) Math.round(mw + r * Math.cos(t));
            int y = (int) Math.round(mh + r * Math.sin(t));
            int tx = (int) Math.round(mw + rt * Math.cos(t));
            int ty = (int) Math.round(mh + rt * Math.sin(t));
            int ax = (int) Math.round(mw + ra * Math.cos(t));
            int ay = (int) Math.round(mh + ra * Math.sin(t));            
            cords.put(op, new Point(x, y));
            tcords.put(op, new Point(tx, ty));
            acords.put(op, new Point(ax, ay));            
            System.out.println(x + "," + y);
            i++;
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setComposite(AlphaComposite.Src);
        FontRenderContext frc = g.getFontRenderContext();
        Font f = new Font("Helvetica", Font.PLAIN, 12);
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (Opcion opi : opciones) {
            for (Opcion opj : opciones) {
                g.setColor(Color.gray);
                g.drawLine(acords.get(opi).x, acords.get(opi).y, acords.get(opj).x, acords.get(opj).y);
                drawArrowHead(g,new Point(10,20),new Point(15,25),Color.gray);
            }
        }

        g.setStroke(new BasicStroke(2));

        for (Opcion op : opciones) {
            if (vl.tieneImagenLaOpcion(op.getId())) {
                InputStream in = new ByteArrayInputStream(vl.getImagenDeOpcion(op.getId()));
                BufferedImage ima = ImageIO.read(in);
                BufferedImage out = createResizedCopy(ima, 65, 65, false);
                g.setComposite(AlphaComposite.Src);
                g.drawImage(out, null, cords.get(op).x - r2, cords.get(op).y - r2);
            } else {
                g.setPaint(Color.lightGray);
                Ellipse2D e = new Ellipse2D.Double(cords.get(op).x - r2, cords.get(op).y - r2, 2 * r2, 2 * r2);
                g.fill(e);
                g.setColor(Color.black);
                g.draw(e);
            }

            TextLayout tl = new TextLayout(op.getNombre(), f, frc);
            Rectangle rect = tl.getBounds().getBounds();
            rect.setLocation(tcords.get(op).x, tcords.get(op).y - rect.height);
            Rectangle rectc = new Rectangle(cords.get(op).x - r2, cords.get(op).y - r2, 2 * r2, 2 * r2);
            int tx = tcords.get(op).x;
            int ty = tcords.get(op).y;
            if (rect.intersects(rectc)) {
                //System.out.println("Intersecta con " + op.getNombre());
                tx = tx - rect.width;
            }
            g.setColor(Color.black);
            tl.draw(g, tx, ty);
        }

        OutputStream output = response.getOutputStream();
        response.setContentType("image/png");
        try {
            javax.imageio.ImageIO.write(img, "png", output);
        } finally {
            output.close();
            g.dispose();
        }
    }

    BufferedImage createResizedCopy(Image originalImage,
            int scaledWidth, int scaledHeight,
            boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    // ArrowHead by Craig Wood 
    private void drawArrowHead(Graphics2D g2, Point tip, Point tail, Color color) {
        g2.setPaint(color);
        double dy = tip.y - tail.y;
        double dx = tip.x - tail.x;
        double theta = Math.atan2(dy, dx);
        //System.out.println("theta = " + Math.toDegrees(theta));
        double x, y, rho = theta + phi;
        for (int j = 0; j < 2; j++) {
            x = tip.x - barb * Math.cos(rho);
            y = tip.y - barb * Math.sin(rho);
            g2.draw(new Line2D.Double(tip.x, tip.y, x, y));
            rho = theta - phi;
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
