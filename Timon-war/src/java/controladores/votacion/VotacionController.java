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
package controladores.votacion;

import controladores.UserManager;
import entities.registro.Miembro;
import entities.votacionydebate.LogVotacion;
import entities.votacionydebate.Opcion;
import entities.votacionydebate.Votacion;
import entities.votacionydebate.ResultadoSchulze;
import entities.votacionydebate.Score;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.primefaces.model.DualListModel;
import sessionbeans.VotoYDebateLogic;
import org.apache.http.params.CoreConnectionPNames;

/**
 *
 * @author Alfonso Tamés
 */
@Named(value = "vc")
@SessionScoped
public class VotacionController implements Serializable {

    @Inject
    UserManager um;
    @Inject
    VotoYDebateLogic vl;
    private long vid;
    private long vact = 0;
    private Votacion votacion;
    private String wikiDescVotacion;
    private DualListModel<Opcion> opciones;
    private String wikiDescOpcion;
    private LogVotacion logvot = new LogVotacion();
    private ResultadoSchulze rs;

    public VotacionController() {
        logvot = new LogVotacion();
        rs = null;
    }

    public ResultadoSchulze getRs() {
        if (rs == null) {
            rs = vl.getUltimoResultado(votacion);
        } else {
            vl.getResultadoSchulze(rs.getId());
        }

        return rs;
    }

    public List<Votacion> getVotaciones() {
        return vl.getVotaciones(0, 100);
    }

    // Que tan pesado es un delegado para cierta votacion
    public long numeroAtomico(Miembro delegado) {
        return vl.numeroAtomico(votacion, delegado);
    }

    public void verVotacion() {

        List<Opcion> opcionesDisponibles = new ArrayList<Opcion>();
        List<Opcion> opcionesVotadas = new ArrayList<Opcion>();
        //System.out.println("vact " + getVact());
        //System.out.println("vid " + getVid());
        if (getVact() != getVid() || getVact() == 0) {
            votacion = vl.getVotacion(getVid());
            rs = vl.getUltimoResultado(votacion);
            try {
                setWikiDescVotacion(getContentFromURL(votacion.getUrl()));
                opcionesDisponibles = vl.getOpcionesParaVotacion(votacion);
                opciones = new DualListModel<Opcion>(opcionesDisponibles, opcionesVotadas);
            } catch (Exception ex) {
                Logger.getLogger(NuevaVotacionController.class.getName()).log(Level.SEVERE, null, ex);
            }

            //System.out.println("Desc: " + getWikiDescVotacion());
            setVact(getVid());
        }
    }

    public void verWikiDescOpcion(long id) {
        System.out.println("Invocando verWikiDescOpcion...");
        try {
            System.out.println("Sacando la opcion con id " + id);
            Opcion op = vl.getOpcion(id);
            wikiDescOpcion = getContentFromURL(op.getUrl());
            System.out.println("Contiene:");
            System.out.println(wikiDescOpcion);
        } catch (Exception ex) {
            System.out.println("Error !!!!!!!");
            Logger.getLogger(VotacionController.class.getName()).log(Level.SEVERE, "Error al sacar contenido del wiki", ex);
        }

    }

    public boolean tieneImagenLaOpcion(long id) {
        return vl.tieneImagenLaOpcion(id);
    }

    public String guardarVotacion() {
        if (um.getUser() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Para votar debe ingresar a la plataforma como miembro.", null));
        }

        List<Opcion> votos = opciones.getTarget();
        logvot.setVotacion(votacion);
        logvot.setMiembro(um.getUser());
        logvot.setFecha(new Date());
        rs = vl.guardarVotacion(logvot, votos);
        vl.cuentaConSchulze(rs);
        logvot = new LogVotacion();
        return "votResultados.xhtml?vid=" + vid + "&amp;faces-redirect=true";
    }

    public Integer getAvance() {
        
        if (rs == null) {
            System.out.println("Aqui el rs es nulo");
            return 0;
        } else {
            System.out.println("Soy getAvance... "+rs.getId()+" "+rs.getAvance());
            return vl.getAvanceParaRS(rs.getId());
        }
    }

    public String getContentFromURL(String url) throws IOException {
        StringBuilder resp = new StringBuilder();
        HttpEntity entity = null;
        HttpClient httpclient = null;
        HttpGet httpget = null;
        try {
            //System.out.println("Cargando la pagina de descripcion " + url);
            httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1);
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
            httpclient.getParams().setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
            httpclient.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY, true);

            httpget = new HttpGet(url);

            HttpResponse response = httpclient.execute(httpget);
            //System.out.println(response.getStatusLine());
            entity = response.getEntity();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            resp.append(e.getMessage());
        }
        InputStream instream = null;
        if (entity != null) {

            try {
                instream = entity.getContent();
                // do something useful with the response
                byte[] buffer = new byte[1024];
                int read;
                while ((read = instream.read(buffer)) != -1) {
                    resp.append(new String(Arrays.copyOfRange(buffer, 0, read), "UTF8"));

                }


            } catch (IOException ex) {
                System.out.println("Error IO conectando al wiki!!!");
                // In case of an IOException the connection will be released
                // back to the connection manager automatically
                throw ex;

            } catch (RuntimeException ex) {

                System.out.println("Error Runtime conectando al wiki!!!");
                // In case of an unexpected exception you may want to abort
                // the HTTP request in order to shut down the underlying
                // connection and release it back to the connection manager.
                httpget.abort();
                throw ex;

            } finally {

                // Closing the input stream will trigger connection release
                instream.close();

            }

            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }

        return resp.toString();
    }

    /**
     * @return the votacion
     */
    public Votacion getVotacion() {
        return votacion;
    }

    /**
     * @param votacion the votacion to set
     */
    public void setVotacion(Votacion votacion) {
        this.votacion = votacion;
    }

    /**
     * @return the wikiDescVotacion
     */
    public String getWikiDescVotacion() {
        return wikiDescVotacion;
    }

    /**
     * @param wikiDescVotacion the wikiDescVotacion to set
     */
    public void setWikiDescVotacion(String wikiDescVotacion) {
        this.wikiDescVotacion = wikiDescVotacion;
    }

    /**
     * @return the vid
     */
    public long getVid() {
        return vid;
    }

    /**
     * @param vid the vid to set
     */
    public void setVid(long vid) {
        this.vid = vid;
    }

    /**
     * @return the vact
     */
    public long getVact() {
        return vact;
    }

    /**
     * @param vact the vact to set
     */
    public void setVact(long vact) {
        this.vact = vact;
    }

    /**
     * @return the opciones
     */
    public DualListModel<Opcion> getOpciones() {
        return opciones;
    }

    /**
     * @param opciones the opciones to set
     */
    public void setOpciones(DualListModel<Opcion> opciones) {
        this.opciones = opciones;
    }

    /**
     * @return the wikiDescOpcion
     */
    public String getWikiDescOpcion() {
        return wikiDescOpcion;
    }

    /**
     * @param wikiDescOpcion the wikiDescOpcion to set
     */
    public void setWikiDescOpcion(String wikiDescOpcion) {
        this.wikiDescOpcion = wikiDescOpcion;
    }

    /**
     * @return the logvot
     */
    public LogVotacion getLogvot() {
        return logvot;
    }

    /**
     * @param logvot the logvot to set
     */
    public void setLogvot(LogVotacion logvot) {
        this.logvot = logvot;
    }
}
