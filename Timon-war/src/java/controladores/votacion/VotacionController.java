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
import entities.votacionydebate.Opcion;
import entities.votacionydebate.Votacion;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.primefaces.model.DualListModel;
import sessionbeans.VotoYDebateLogic;

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
    
    
    public List<Votacion> getVotaciones() {
        return vl.getVotaciones(0, 100);
    }    

    public void verVotacion() {

        List<Opcion> opcionesDisponibles = new ArrayList<Opcion>();
        List<Opcion> opcionesVotadas = new ArrayList<Opcion>();
        System.out.println("vact " + getVact());
        System.out.println("vid " + getVid());
        if (getVact() != getVid() || getVact() == 0) {
            votacion = vl.getVotacion(getVid());
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
            System.out.println("Sacando la opcion con id "+id);
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

    public void guardarVotacion() {
        List<Opcion> votos = opciones.getTarget();
        int n = 1;
        for (Opcion o : votos) {
            System.out.println("Voto " + n + " " + o.getNombre());
            n++;
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
            httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            //System.out.println(response.getStatusLine());
            entity = response.getEntity();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        InputStream instream = null;
        if (entity != null) {

            try {
                instream = entity.getContent();
                // do something useful with the response
                byte[] buffer = new byte[1024];
                int read;
                while ((read = instream.read(buffer)) != -1) {
                    resp.append(new String(Arrays.copyOfRange(buffer, 0, read),"UTF8"));
                    
                }


            } catch (IOException ex) {

                // In case of an IOException the connection will be released
                // back to the connection manager automatically
                throw ex;

            } catch (RuntimeException ex) {

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
}