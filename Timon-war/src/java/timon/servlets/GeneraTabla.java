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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import timon.entities.votacionydebate.Opcion;
import timon.entities.votacionydebate.ResultadoSchulze;
import timon.sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tamés
 */
@WebServlet(name = "GeneraTabla", urlPatterns = {"/GeneraTabla"})
public class GeneraTabla extends HttpServlet {

    @Inject
    VotoYDebateLogic vl;
    long rsid;
    String tipo;
    String cual;
    long[][] m;
    int c;
    List<Opcion> ops;
    ResultadoSchulze rs;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            rsid = Integer.parseInt(request.getParameter("rsid")); // ResultadoSchulze
            tipo = request.getParameter("tipo"); // Tipo de archivo
            cual = request.getParameter("cual"); // Mat. de Preferencia o SP            
        } catch (Exception e) {
            throw new ServletException("Faltan parámetros de ejecución.");
        }

        rs = vl.getResultadoSchulze(rsid);
        if (rs == null) {
            throw new ServletException("Al parecer no existe un resultado con esa clave.");
        }
        c = rs.getVotacion().getOpciones().size();
        ops = rs.getVotacion().getOpciones();
        // Vamos a estrenar Java 7:
        switch (cual) {
            case "pref":
                m = rs.getPref();
                break;
            case "sp":
                m = rs.getSp();
                break;
            default:
                throw new ServletException("No entiendo cual matriz generar.");
        }
        switch (tipo) {
            case "xlsx":
                regresaExcel(response);
                break;
            case "csv":
                regresaCsv(response);
                break;
            default:
                throw new ServletException("No entiendo el tipo de archivo.");
        }



    }

    private int getPictureType(byte[] f) {
        byte[] png = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A};
        byte[] jpg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        if (Arrays.equals(Arrays.copyOfRange(f, 0, 8), png)) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        if (Arrays.equals(Arrays.copyOfRange(f, 0, 3), jpg)) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        return Workbook.PICTURE_TYPE_PNG; //default
    }

    private void regresaCsv(HttpServletResponse response) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append(",");
        for (int i = 0; i < c; i++) {
            out.append(ops.get(i).getNombre());
            if (i != (c - 1)) {
                out.append(",");
            }
        }
        out.append("\n");
        for (int i = 0; i < c; i++) {
            out.append(ops.get(i).getNombre()).append(",");
            for (int j = 0; j < c; j++) {
                if (!ops.get(i).equals(ops.get(j))) {
                    out.append(m[i][j]);
                    if (j != (c - 1)) {
                        out.append(",");
                    }
                } else {
                    out.append(",");
                }
            }
            out.append("\n");
        }
        String mat="";
        if (cual.equals("pref")) {
            mat = " - Matriz de Preferencia.csv";
        }
        if (cual.equals("sp")) {
            mat = " - Rutas Más Fuertes.csv";
        }
        
        response.setHeader("Content-Disposition", "attachment; filename=\"Resultados " + rs.getVotacion().getNombre() + mat+"\"");
        response.setContentType("application/csv");        
        PrintWriter o = response.getWriter();
        o.write(out.toString());
    }

    private void regresaExcel(HttpServletResponse response) throws ServletException, IOException {

        XSSFColor rojo = new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xDD, (byte) 0xDD});
        XSSFColor verde = new XSSFColor(new byte[]{(byte) 0xDD, (byte) 0xFF, (byte) 0xDD});
        XSSFColor gris = new XSSFColor(new byte[]{(byte) 0xDD, (byte) 0xDD, (byte) 0xDD});
        Workbook wb = new SXSSFWorkbook(c);
        XSSFFont bold = (XSSFFont) wb.createFont();
        bold.setBold(true);
        XSSFFont vals = (XSSFFont) wb.createFont();
        vals.setFontHeight(24);
        XSSFCellStyle headstyle = (XSSFCellStyle) wb.createCellStyle();
        headstyle.setFont(bold);
        headstyle.setAlignment(HorizontalAlignment.CENTER);
        XSSFCellStyle headstyler = (XSSFCellStyle) wb.createCellStyle();
        headstyler.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
        headstyler.setFont(bold);
        headstyler.setAlignment(HorizontalAlignment.CENTER);
        headstyler.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFCellStyle celrojo = (XSSFCellStyle) wb.createCellStyle();
        celrojo.setFillForegroundColor(rojo);
        celrojo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        celrojo.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        celrojo.setVerticalAlignment(VerticalAlignment.CENTER);
        celrojo.setBorderTop(XSSFCellStyle.BORDER_THIN);
        celrojo.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        celrojo.setBorderRight(XSSFCellStyle.BORDER_THIN);
        celrojo.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        celrojo.setTopBorderColor(gris);
        celrojo.setLeftBorderColor(gris);
        celrojo.setRightBorderColor(gris);
        celrojo.setBottomBorderColor(gris);
        celrojo.setFont(vals);
        XSSFCellStyle celverde = (XSSFCellStyle) wb.createCellStyle();
        celverde.setFillForegroundColor(verde);
        celverde.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        celverde.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        celverde.setVerticalAlignment(VerticalAlignment.CENTER);
        celverde.setBorderTop(XSSFCellStyle.BORDER_THIN);
        celverde.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        celverde.setBorderRight(XSSFCellStyle.BORDER_THIN);
        celverde.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        celverde.setTopBorderColor(gris);
        celverde.setLeftBorderColor(gris);
        celverde.setRightBorderColor(gris);
        celverde.setBottomBorderColor(gris);
        celverde.setFont(vals);
        CreationHelper helper = wb.getCreationHelper();
        try {
        Sheet sh = wb.createSheet("Matriz de Preferencia");
        sh.setDisplayGridlines(false);
        Row head = sh.createRow(5);

        for (int i = 0; i < c; i++) {
            Cell cell = head.createCell(i + 2);

            cell.setCellStyle(headstyle);
            cell.setCellValue(ops.get(i).getNombre());
            sh.setColumnWidth(i + 2, 3500);
            if (vl.tieneImagenLaOpcion(ops.get(i).getId())) {
                byte[] ima = vl.getImagenDeOpcion(ops.get(i).getId());
                int pictureid = wb.addPicture(ima, getPictureType(ima));
                Drawing drawing = sh.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(i + 2);
                anchor.setRow1(0);
                Picture pict = drawing.createPicture(anchor, pictureid);
                ByteArrayInputStream in = new ByteArrayInputStream(ima);
                BufferedImage bima = ImageIO.read(in);
                int w = bima.getWidth();
                float factor = 80f / w;
                pict.resize(factor);
            }
        }

        for (int i = 0; i < c; i++) {
            Row row = sh.createRow(i + 6);
            row.setHeightInPoints((4 * sh.getDefaultRowHeightInPoints()));
            Cell nom = row.createCell(1);
            nom.setCellValue(ops.get(i).getNombre());
            nom.setCellStyle(headstyler);
            for (int j = 0; j < c; j++) {
                Cell cell = row.createCell(j + 2);
                if (!ops.get(i).equals(ops.get(j))) {
                    if (m[i][j] > m[j][i]) {
                        cell.setCellStyle(celverde);
                    } else {
                        cell.setCellStyle(celrojo);
                    }
                    cell.setCellValue(m[i][j]);
                }
            }
        }
        sh.autoSizeColumn(1);
        sh.setColumnWidth(0, 5500);

        } catch (Exception e) {
            throw new ServletException("No es posible generar la hoja de preferencias.");
        }
        try {
        Sheet sh = wb.createSheet("Rutas más Fuertes");
        m = rs.getSp();
        sh.setDisplayGridlines(false);
        Row head = sh.createRow(5);

        for (int i = 0; i < c; i++) {
            Cell cell = head.createCell(i + 2);

            cell.setCellStyle(headstyle);
            cell.setCellValue(ops.get(i).getNombre());
            sh.setColumnWidth(i + 2, 3500);
            if (vl.tieneImagenLaOpcion(ops.get(i).getId())) {
                byte[] ima = vl.getImagenDeOpcion(ops.get(i).getId());
                int pictureid = wb.addPicture(ima, getPictureType(ima));
                Drawing drawing = sh.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(i + 2);
                anchor.setRow1(0);
                Picture pict = drawing.createPicture(anchor, pictureid);
                ByteArrayInputStream in = new ByteArrayInputStream(ima);
                BufferedImage bima = ImageIO.read(in);
                int w = bima.getWidth();
                float factor = 80f / w;
                pict.resize(factor);
            }
        }

        for (int i = 0; i < c; i++) {
            Row row = sh.createRow(i + 6);
            row.setHeightInPoints((4 * sh.getDefaultRowHeightInPoints()));
            Cell nom = row.createCell(1);
            nom.setCellValue(ops.get(i).getNombre());
            nom.setCellStyle(headstyler);
            for (int j = 0; j < c; j++) {
                Cell cell = row.createCell(j + 2);
                if (!ops.get(i).equals(ops.get(j))) {
                    if (m[i][j] > m[j][i]) {
                        cell.setCellStyle(celverde);
                    } else {
                        cell.setCellStyle(celrojo);
                    }
                    cell.setCellValue(m[i][j]);
                }
            }
        }
        sh.autoSizeColumn(1);
        sh.setColumnWidth(0, 5500);

        } catch (Exception e) {
            throw new ServletException("No es posible generar la hoja de preferencias.");
        }
        
        response.setHeader("Content-Disposition", "attachment; filename=\"Resultados " + rs.getVotacion().getNombre() + ".xlsx\"");
        response.setContentType("application/xlsx");
        wb.write(response.getOutputStream());

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
