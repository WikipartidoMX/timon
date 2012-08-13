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

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import timon.controladores.votacion.VotacionController;
import timon.entities.votacionydebate.Opcion;
import timon.entities.votacionydebate.ResultadoSchulze;
import timon.sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames. Gracias a Dr. John B. Matthews por la solucion para
 * calcular las dimensiones
 */
@WebServlet(name = "DirectedGraph", urlPatterns = {"/DirectedGraph"})
public class DirectedGraph extends HttpServlet {

    private static final Logger mrlog = Logger.getLogger(DirectedGraph.class.getName());
    @Inject
    VotacionController vc;
    @Inject
    VotoYDebateLogic vl;
    double phi;
    int barb;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/png");
        ResultadoSchulze rs = null;
        try {
            rs = vc.getRs();
        } catch (Exception e) {
            mrlog.log(Level.FINE, "Error al recuperar el RS '{'0'}'{0}", e.getMessage());
        }

        if (rs == null) {
            // TODO: Regresa una simpatica imagen que diga que estamos esperando el ultimo resultado
            return;
        }
        List<Opcion> opciones = rs.getVotacion().getOpciones();
        int n = opciones.size();
        long[][] prefs = rs.getPref();
        // Tamano de la imagen y radios
        int w = 800;
        int h = 600;
        int mw = w / 2;
        int mh = h / 2;
        int r = (4 * mh) / 5;
        int rt = r + 50;
        int ra = r;
        int r2 = Math.abs((mh - r) / 2);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setComposite(AlphaComposite.Src);
        FontRenderContext frc = g.getFontRenderContext();
        Font f = new Font("Helvetica", Font.PLAIN, 12);
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (opciones.size() > 20) {
            Font fail = new Font("Helvetica", Font.PLAIN, 20);
            TextLayout tl = new TextLayout("¡No es posible hacer una gráfica con tantas opciones!", fail, frc);
            g.setColor(Color.black);
            tl.draw(g, 200, 300);
            escupeBytes(img, g, response);
            return;
        }
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
            i++;
        }


        g.setColor(Color.gray);
        double t = 40;
        int oi = 0;
        for (Opcion opi : opciones) {
            double x, y;
            x = cords.get(opi).x;
            y = cords.get(opi).y;
            Line2D[] lados = {
                new Line2D.Double(x - t, y - t, x + t, y - t),
                new Line2D.Double(x + t, y - t, x + t, y + t),
                new Line2D.Double(x + t, y + t, x - t, y + t),
                new Line2D.Double(x - t, y + t, x - t, y - t)
            };

            g.setColor(Color.gray);
            /*
             * for (Line2D lado : lados) { g.draw(lado); }
             */
            int oj = 0;
            for (Opcion opj : opciones) {

                Line2D l = new Line2D.Double(acords.get(opi).x, acords.get(opi).y, acords.get(opj).x, acords.get(opj).y);
                g.setColor(Color.gray);
                //g.draw(l);
                for (Line2D lado : lados) {
                    if (lado.intersectsLine(l)) {
                        Point2D inter = getLineLineIntersection(lado, l);
                        if (inter != null) {

                            g.setColor(Color.gray);
                            if (prefs[oi][oj] < prefs[oj][oi]) {
                                drawArrow(g, l, inter);
                                Line2D li = new Line2D.Double(inter.getX(), inter.getY(), cords.get(opj).x, cords.get(opj).y);
                                g.draw(li);
                                Point2D mita = new Point2D.Double((inter.getX() + cords.get(opj).x) / 2, (inter.getY() + cords.get(opj).y) / 2);

                                TextLayout tl = new TextLayout(Long.toString(prefs[oj][oi]), f, frc);
                                Rectangle2D rect = new Rectangle2D.Double(-10, -14,
                                        tl.getBounds().getWidth() + 25,
                                        tl.getBounds().getHeight() + 5);
                                AffineTransform bat = g.getTransform();
                                AffineTransform at = new AffineTransform();
                                at.translate(mita.getX(), mita.getY());
                                double teta = Math.atan2(li.getY2() - li.getY1(), li.getX2() - li.getX1());
                                at.rotate(teta);
                                g.setTransform(at);
                                g.setPaint(Color.white);
                                g.fill(rect);
                                g.setColor(Color.gray);
                                g.draw(rect);
                                tl.draw(g, 0, -2);
                                g.setTransform(bat);

                            }
                        }
                    }
                }

                oj++;
            }
            oi++;
        }

        g.setStroke(new BasicStroke(2));

        int nu = 0;
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
            nu++;
        }

        escupeBytes(img, g, response);
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

    // by CommanderKeith
    public Point2D getLineLineIntersection(Line2D l1, Line2D l2) {
        double x1 = l1.getX1();
        double x2 = l1.getX2();
        double y1 = l1.getY1();
        double y2 = l1.getY2();
        double x3 = l2.getX1();
        double x4 = l2.getX2();
        double y3 = l2.getY1();
        double y4 = l2.getY2();
        double det1And2 = det(x1, y1, x2, y2);
        double det3And4 = det(x3, y3, x4, y4);
        double x1LessX2 = x1 - x2;
        double y1LessY2 = y1 - y2;
        double x3LessX4 = x3 - x4;
        double y3LessY4 = y3 - y4;
        double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
        if (det1Less2And3Less4 == 0) {
            // the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the lines overlap) so return null.
            return null;
        }
        double x = (det(det1And2, x1LessX2,
                det3And4, x3LessX4)
                / det1Less2And3Less4);
        double y = (det(det1And2, y1LessY2,
                det3And4, y3LessY4)
                / det1Less2And3Less4);
        return new Point2D.Double(x, y);
    }

    public Polygon drawArrow(Graphics2D g, Line2D l, Point2D punta) {
        AffineTransform otx = g.getTransform();
        AffineTransform tx = new AffineTransform();
        Polygon ah = new Polygon();
        ah.addPoint(0, 7);
        ah.addPoint(-7, -7);
        ah.addPoint(7, -7);
        double teta = Math.atan2(l.getY2() - punta.getY(), l.getX2() - punta.getX());
        tx.translate(punta.getX(), punta.getY());
        tx.rotate((teta - 3 * Math.PI / 2d));

        g.setTransform(tx);
        g.fill(ah);
        g.setTransform(otx);
        return ah;
    }

    protected static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    public void escupeBytes(BufferedImage img, Graphics2D g, HttpServletResponse response) {
        OutputStream output = null;
        try {
            output = response.getOutputStream();
            response.setContentType("image/png");
            javax.imageio.ImageIO.write(img, "png", output);
        } catch (Exception e) {
            mrlog.log(Level.SEVERE, "No es posible escupir los bytes");
        } finally {
            try {
            output.close();
            g.dispose();
            } catch (Exception e) {
                mrlog.log(Level.SEVERE, "No es posible cerrar el output");
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
