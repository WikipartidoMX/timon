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

import entities.registro.Miembro;
import entities.votacionydebate.Delegacion;
import entities.votacionydebate.Tema;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "dc")
@SessionScoped
public class DelegacionController implements Serializable {
    
    @Inject
    UserManager um;
    @Inject
    VotoYDebateLogic vydl;
    
    private TreeNode raiz = null;
    private Miembro selecMiembro;
    private TreeNode selTemaTreeNode;
    
    public void agregarDelegacion() {
        System.out.println("Agregando Delegación");
        if (um.getUser() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe ingresar al sistema y estar afiliado para delegar su voto. ", null));
            return;
        }
        if (um.getUser().getPaso() < 2) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe estar afiliado para delegar su voto. ", null));
            return;            
        }
        if (selecMiembro == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar un miembro para delegar su voto. ", null));
            return;            
        }
        if (selTemaTreeNode == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar un tema para delegar su voto. ", null));
            return;            
        }           
        
        System.out.println("Tema es: "+((Tema)selTemaTreeNode.getData()).getNombre());
        Delegacion del = new Delegacion();
        del.setMiembro(um.getUser());
        del.setDelegado(selecMiembro);
        del.setTema((Tema)selTemaTreeNode.getData());
        try {
            vydl.guardarDelegacion(del);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        
        selecMiembro = null;
    }
    
    public void borrarDelegacion(long id) {
        vydl.borrarDelegacion(id);
    }
    
    public List<Miembro> completarMiembro(String query) {
        return vydl.completarMiembro(query);
    }
    
    public void asignaMiembro(SelectEvent event) {
        System.out.println("Asignando!!!");
        Miembro m = (Miembro)event.getObject();
        System.out.println("asignaMiembro: "+m.getNombre());
    }
    
    public List<Delegacion> getDelegaciones() {
        return vydl.getDelegacionesPara(um.getUser());
    }
    
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
    
    /**
     * @param raiz the raiz to set
     */
    public void setRaiz(TreeNode raiz) {
        this.raiz = raiz;
    }    

    /**
     * @return the selecMiembro
     */
    public Miembro getSelecMiembro() {
        return selecMiembro;
    }

    /**
     * @param selecMiembro the selecMiembro to set
     */
    public void setSelecMiembro(Miembro selecMiembro) {
        this.selecMiembro = selecMiembro;
    }

    /**
     * @return the selTemaTreeNode
     */
    public TreeNode getSelTemaTreeNode() {
        return selTemaTreeNode;
    }

    /**
     * @param selTemaTreeNode the selTemaTreeNode to set
     */
    public void setSelTemaTreeNode(TreeNode selTemaTreeNode) {
        this.selTemaTreeNode = selTemaTreeNode;
    }        
}
