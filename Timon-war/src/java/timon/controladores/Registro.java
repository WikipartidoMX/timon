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
package timon.controladores;

import timon.entities.registro.Avatar;
import timon.entities.registro.Estado;
import timon.entities.registro.Miembro;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import timon.sessionbeans.TimonLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "registro")
@SessionScoped
public class Registro implements Serializable {

    private Miembro miembro;
    private List<SelectItem> estados;
    private long estadoid;
    private UploadedFile ufile; // Para inclusion de avatar
    @Inject
    private UserManager um;
    @Inject
    private TimonLogic tl;
    @Resource
    private javax.transaction.UserTransaction utx;

    /**
     * Creates a new instance of Registro
     */
    public Registro() {
        miembro = new Miembro();
    }

    public String handleFileUpload(FileUploadEvent event) {
        System.out.println("El evento...");
        ufile = event.getFile();
        Avatar a = tl.getAvatar(miembro.getId());
        if (a == null) {
            System.out.println("El avatar regresa nulo");
            a = new Avatar();
            System.out.println(ufile.getFileName());
            a.setFile(ufile.getContents());
            a.setMiembro(miembro);
            try {
                utx.begin();
                System.out.println("Persistiendo...");
                tl.persist(a);
                utx.commit();
                return "index.xhtml?faces-redirect=true";
            } catch (Exception e) {
                System.out.println(e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "No es posible cambiar el avatar.", null));
            }

        } else {
            a.setMiembro(miembro);
            System.out.println("El archivo mide" + ufile.getSize());
            a.setFile(ufile.getContents());
            try {
                utx.begin();
                System.out.println("Actualizando...");
                tl.merge(a);
                utx.commit();
                return "index.xhtml?faces-redirect=true";
            } catch (Exception e) {
                System.out.println(e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "No es posible cambiar el avatar.", null));
            }
        }
        return "index.xhtml?faces-redirect=true";
    }

    public String actualizarAvatar() {


        return "";
    }

    public String agregarAlWiki() {
        // Revisa si el mail ya está registrado
        Miembro existe = null;
        existe = tl.getMiembroFromEmail(miembro.getEmail());
        if (existe != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ya está registrado tu mail en el registro del Wiki,", null));
            return "";
        }
        existe = tl.getMiembroFromClavedeusuario(miembro.getClavedeusuario());
        if (existe != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ya está registrado tu clave de usuario en el registro del Wiki.", null));
            return "";
        }


        //httppost al Wiki
        long codigo = 0;
        String resp = "";
        // Hay que asegurarnos que la primera letra del nombre de usuario sea mayúscula
        // así lo pide el wiki:
        miembro.setClavedeusuario(miembro.getClavedeusuario().substring(0, 1).toUpperCase()
                + miembro.getClavedeusuario().substring(1, miembro.getClavedeusuario().length()));
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            HttpPost httppost = new HttpPost("http://wiki.wikipartido.mx/au.php");
            StringEntity se = new StringEntity("{\"username\":\"" + miembro.getClavedeusuario()
                    + "\",\"password\":\"" + miembro.getPassword() + "\",\"email\":\"" + miembro.getEmail()
                    + "\"}");
            httppost.setEntity(se);
            System.out.print(se);
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");
            response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                long len = entity.getContentLength();
                if (len != -1 && len < 2048) {
                    resp += EntityUtils.toString(entity);
                } else {
                    // Stream content out
                }
            }
            codigo = response.getStatusLine().getStatusCode();
            System.out.println(codigo);
            System.out.println("Respuesta:");
            System.out.println(resp);
        } catch (Exception e) {
            System.out.print("Cannot establish connection!");
            System.out.println(e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No es posible contactar al servidor de wiki, intente más tarde.", null));
        }



        if (codigo != 200) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ocurrió un error en el servidor de wiki, intente más tarde (código: " + codigo + " )", null));
            return "";
        }
        if (!resp.equals("agregado")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "El servidor de Wiki rechazó la clave de usuario (" + resp + "), intente usar otra clave de usuario.", null));
            return "";
        }
        try {
            miembro.setFechaRegistro(new Date());
            HttpServletRequest hsr = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String h = hsr.getHeader("x-forwarded-for");
            InetAddress addr = InetAddress.getByName(h);
            if (addr.getHostName() != null) {
                miembro.setIp(addr.getHostName());
            } else {
                miembro.setIp(h);
            }

            miembro.setPaso(1L);
            System.out.println("Presistiendo...");
            persist(miembro);

            um.setUser(miembro);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Usuario Agregado.", null));
            return "";
        } catch (Exception e) {

            System.out.println(e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ocurrió un error al tratar de ingresar el registro.", null));
        }

        return "";
    }

    public Number cuantos() {
        Number c = tl.cuantosMiembros();
        return c;
    }

    public String nombreDeEstado(Long id) {
        Estado estado = tl.getEstado(id);
        return estado.getNombre();
    }

    public String registrar() {

        miembro.setUrl(miembro.getUrl().replace("http://", ""));
        System.out.println("Afiliando a " + miembro.getNombre() + " " + miembro.getApellidoPaterno());
        Miembro existe = null;
        existe = tl.getMiembroFromEmail(miembro.getEmail());
        if (existe != null) {

            try {
                miembro.setPaso(2L);
                miembro.setEstado(tl.getEstado(estadoid));
                utx.begin();
                tl.merge(miembro);
                utx.commit();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "¡Se ha ingresado tu afiliación al sistema!", null));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return "registro.xhtml?faces-redirect=true";
        }
        return "";
    }

    /**
     * @return the miembro
     */
    public Miembro getMiembro() {
        return miembro;
    }

    /**
     * @param miembro the miembro to set
     */
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }

    public void persist(Object object) {
        try {
            utx.begin();
            tl.persist(object);
            utx.commit();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
            throw new RuntimeException(e);
        }
    }

    public void merge(Object object) {
        try {
            utx.begin();
            tl.merge(object);
            utx.commit();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the estados
     */
    public List<SelectItem> getEstados() {
        if (estados == null) {
            estados = new ArrayList<SelectItem>();
            List<Estado> eds = tl.getEstados();
            for (Estado e : eds) {
                estados.add(new SelectItem(e.getId(), e.getNombre()));
            }
        }
        return estados;
    }

    /**
     * @param estados the estados to set
     */
    public void setEstados(List<SelectItem> estados) {
        this.estados = estados;
    }

    /**
     * @return the ufile
     */
    public UploadedFile getUfile() {
        return ufile;
    }

    /**
     * @param ufile the ufile to set
     */
    public void setUfile(UploadedFile ufile) {
        this.ufile = ufile;
    }

    /**
     * @return the estadoid
     */
    public long getEstadoid() {
        return estadoid;
    }

    /**
     * @param estadoid the estadoid to set
     */
    public void setEstadoid(long estadoid) {
        this.estadoid = estadoid;
    }
    /**
     * @return the barcode
     */
    /**
     * @param barcode the barcode to set
     */
}
