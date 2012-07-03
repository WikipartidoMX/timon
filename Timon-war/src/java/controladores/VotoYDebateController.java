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
import java.io.Serializable;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
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

            votacion = vydl.getVotacion(vid);

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
            System.out.println("Construyendo el treenode");
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
        System.out.println("Agregando " + estadoid);
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
}
