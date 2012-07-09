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
package controladores;

import entities.registro.Estado;
import entities.votacionydebate.ImagenVotacion;
import entities.votacionydebate.Opcion;
import entities.votacionydebate.Tema;
import entities.votacionydebate.Votacion;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import sessionbeans.TimonLogic;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "vydc")
@SessionScoped
public class VotoYDebateController implements Serializable {

    private Votacion votacion;
    private long vid;
    private long vact = 0;
    private List<Opcion> opciones;
    private TreeNode selectedTema;
    private List<SelectItem> estados;
    private long estadoid;
    private Votacion nuevaVotacion;
    private Opcion nuevaOpcion;
    private TreeNode raiz = null;
    private List<Estado> selecEstados;
    private List<Tema> selecTemas;
    private UploadedFile imagen;
    @Inject
    UserManager um;
    @Inject
    VotoYDebateLogic vydl;
    @Inject
    private TimonLogic tl;
    private String wikiDescVotacion;

    public VotoYDebateController() {
        resetVotacion();
    }

    public void resetVotacion() {
        nuevaVotacion = new Votacion();

        nuevaVotacion.setEstados(new LinkedList<Estado>());
        nuevaVotacion.setTemas(new LinkedList<Tema>());
        selecEstados = new LinkedList<Estado>();
        selecTemas = new LinkedList<Tema>();
        nuevaOpcion = new Opcion();
        opciones = new LinkedList<Opcion>();
        imagen = null;
    }

    public void prueba() {
        System.out.println("Prueba " + vid);
    }

    public void verVotacion() {
        System.out.println("vact " + vact);
        System.out.println("vid " + vid);
        if (vact != vid || vact==0) {
            votacion = vydl.getVotacion(vid);

            try {
                wikiDescVotacion = getContentFromURL(votacion.getUrl());
            } catch (IOException ex) {
                Logger.getLogger(VotoYDebateController.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Desc: "+wikiDescVotacion);
            vact = vid;
        }

    }

    public List<Votacion> getVotaciones() {
        return vydl.getVotaciones(0, 100);
    }

    public void handleFileUpload(FileUploadEvent event) {
        imagen = event.getFile();
    }

    public String guardarVotacion() {
        if (um.getUser() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Para guardar la votación debe ingresar a la plataforma como miembro.", null));
            return "";
        }
        try {
            System.out.println("Guardando");
            if (opciones.size() < 2) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "La votación debe contener al menos dos opciones", null));
                return "";
            }

            if (nuevaVotacion.getNombre() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "La votación necesita un título", null));
                return "";
            }
            if (nuevaVotacion.getFechaCierre() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "La votación requiere una fecha de cierre", null));
                return "";
            }

            nuevaVotacion.setOpciones(new LinkedList<Opcion>());
            for (Opcion o : opciones) {
                o.setVotacion(nuevaVotacion);
                nuevaVotacion.getOpciones().add(o);
            }
            nuevaVotacion.setEstados(selecEstados);
            nuevaVotacion.setTemas(selecTemas);
            // El merge se hace en caso de edicion, pero no estamos seguros de querer editar una votacion
            // a menos de que se active en una fecha futura.
            if (nuevaVotacion.getId() != null) {
                System.out.println("Mergeando la votacion...");
                nuevaVotacion = (Votacion) tl.merge(nuevaVotacion);
            } else {
                nuevaVotacion.setMiembro(um.getUser());
                nuevaVotacion.setFechaCreacion(new Date());
                tl.persist(nuevaVotacion);
                if (imagen != null) {
                    ImagenVotacion iv = new ImagenVotacion();
                    iv.setFile(imagen.getContents());
                    iv.setVotacion(nuevaVotacion);
                    tl.persist(iv);
                }
            }
            resetVotacion();

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "La votación ha sido guardada", null));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No es posible guardar la votación", null));
        }
        return "/votacionydebate/votaciones.xhtml?faces-redirect=true";
    }

    public void agregarOpcion() {
        opciones.add(nuevaOpcion);
        System.out.println("Queda asi:");
        for (Opcion o : opciones) {
            System.out.println(o.getNombre());
        }
        nuevaOpcion = new Opcion();
    }

    public void borrarOpcion(int o) {
        System.out.println("Borrando " + o);

        opciones.remove(o);
    }

    /**
     * @return the nuevaVotacion
     */
    public Votacion getNuevaVotacion() {
        return nuevaVotacion;
    }

    /**
     * @param nuevaVotacion the nuevaVotacion to set
     */
    public void setNuevaVotacion(Votacion nuevaVotacion) {
        this.nuevaVotacion = nuevaVotacion;
    }

    /**
     * @return the raiz
     */
    public TreeNode getRaiz() {
        if (raiz == null) {
            Map<Long, TreeNode> mapa = new HashMap<Long, TreeNode>();
            raiz = new DefaultTreeNode(new Tema(), null);
            List<Tema> temas = vydl.getTemas();
            for (Tema t : temas) {
                TreeNode tn = new DefaultTreeNode(t, raiz);
                mapa.put(t.getId(), tn);
            }
            for (Long tid : mapa.keySet()) {
                try {
                    if (((Tema) mapa.get(tid).getData()).getPater() != 0) {
                        mapa.get(tid).setParent(mapa.get(((Tema) mapa.get(tid).getData()).getPater()));
                    }
                } catch (Exception e) {
                }
            }
        }
        return raiz;
    }

    public void agregarEstado() {
        //System.out.println("Agregando " + estadoid);
        if (!selecEstados.contains(tl.getEstado(estadoid))) {
            selecEstados.add(tl.getEstado(estadoid));
        }
    }

    public void borrarEstado(Estado e) {
        selecEstados.remove(e);
    }

    public void borrarTema(Tema t) {
        selecTemas.remove(t);
    }

    public void agregarTema() {
        System.out.println("El tema es " + ((Tema) selectedTema.getData()).getNombre());
        if (!selecTemas.contains((Tema) selectedTema.getData())) {
            selecTemas.add((Tema) selectedTema.getData());
        }
    }

    public String getContentFromURL(String url) throws IOException {
        StringBuilder resp = new StringBuilder();
        HttpEntity entity = null;
        HttpClient httpclient = null;
        HttpGet httpget = null;
        try {
            System.out.println("Llamando al wiki " + url);
            httpclient = new DefaultHttpClient();
            httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            System.out.println(response.getStatusLine());
            entity = response.getEntity();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        InputStream instream = null;
        if (entity != null) {

            try {
                instream = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(instream));
                // do something useful with the response
                String buffer;
                while ((buffer = reader.readLine()) != null) {
                    
                    resp.append(buffer);
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
     * @param raiz the raiz to set
     */
    public void setRaiz(TreeNode raiz) {
        this.raiz = raiz;
    }

    /**
     * @return the selecEstados
     */
    public List<Estado> getSelecEstados() {
        return selecEstados;
    }

    /**
     * @param selecEstados the selecEstados to set
     */
    public void setSelecEstados(List<Estado> selecEstados) {
        this.selecEstados = selecEstados;
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
     * @return the selecTemas
     */
    public List<Tema> getSelecTemas() {
        return selecTemas;
    }

    /**
     * @param selecTemas the selecTemas to set
     */
    public void setSelecTemas(List<Tema> selecTemas) {
        this.selecTemas = selecTemas;
    }

    /**
     * @return the selectedTema
     */
    public TreeNode getSelectedTema() {
        return selectedTema;
    }

    /**
     * @param selectedTema the selectedTema to set
     */
    public void setSelectedTema(TreeNode selectedTema) {
        this.selectedTema = selectedTema;
    }

    /**
     * @return the nuevaOpcion
     */
    public Opcion getNuevaOpcion() {
        return nuevaOpcion;
    }

    /**
     * @param nuevaOpcion the nuevaOpcion to set
     */
    public void setNuevaOpcion(Opcion nuevaOpcion) {
        this.nuevaOpcion = nuevaOpcion;
    }

    /**
     * @return the opciones
     */
    public List<Opcion> getOpciones() {
        return opciones;
    }

    /**
     * @param opciones the opciones to set
     */
    public void setOpciones(List<Opcion> opciones) {
        this.opciones = opciones;
    }

    /**
     * @return the imagen
     */
    public UploadedFile getImagen() {
        return imagen;
    }

    /**
     * @param imagen the imagen to set
     */
    public void setImagen(UploadedFile imagen) {
        this.imagen = imagen;
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
}
