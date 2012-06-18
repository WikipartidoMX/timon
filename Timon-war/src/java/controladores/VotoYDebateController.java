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
import entities.votacionydebate.Tema;
import entities.votacionydebate.Votacion;
import java.io.Serializable;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import sessionbeans.TimonLogic;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "vydc")
@SessionScoped
public class VotoYDebateController implements Serializable {

    private TreeNode selectedTema;
    private List<SelectItem> estados;
    private long estadoid;
    private Votacion nuevaVotacion;
    private TreeNode raiz = null;
    private List<Estado> selecEstados;
    private List<Tema> selecTemas;
    @Inject
    VotoYDebateLogic vydl;
    @Inject
    private TimonLogic tl;

    public VotoYDebateController() {

        nuevaVotacion = new Votacion();
        nuevaVotacion.setEstados(new LinkedList<Estado>());
        nuevaVotacion.setTemas(new LinkedList<Tema>());
        selecEstados = new LinkedList<Estado>();
        selecTemas = new LinkedList<Tema>();

    }

    public void prueba() {
        vydl.cuentaConSchulze();

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
                if (((Tema) mapa.get(tid).getData()).getPater() != 0) {
                    mapa.get(tid).setParent(mapa.get(((Tema) mapa.get(tid).getData()).getPater()));
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
}
