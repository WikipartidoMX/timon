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
import timon.entities.votacionydebate.Argumento;
import timon.entities.votacionydebate.Opcion;
import timon.entities.votacionydebate.Tema;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import timon.sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "deco")
@SessionScoped
public class DebateController implements Serializable {

    @Inject
    UserManager um;
    @Inject
    VotoYDebateLogic vl;
    @Inject
    VotacionController vc;
    private Argumento newArg;
    private Opcion ningunaop;
    private List<Opcion> ops;
    private TreeNode raiz;

    public DebateController() {
        newArg = new Argumento();
        ningunaop = new Opcion();
        ningunaop.setNombre("Ninguna opcion en particular");

    }

    public List<Opcion> getOpciones() {
        ops = vl.getOpcionesParaVotacion(vc.getVotacion());
        ops.add(0, ningunaop);
        return ops;
    }

    public String guardarArgumento() {
        if (um.getUser() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe ingresar primero al sistema antes de ingresar un argumento.", null));
            return "";
        }
        System.out.println("Guardando Argumento");
        System.out.println("El argumento es: " + newArg.getDiscurso());
        if (newArg.getOpcion() == null) {
            System.out.println("Ninguna opción !!!");
        }
        System.out.println("Se le asigna la votación " + vc.getVotacion().getNombre());
        System.out.println("Miembro: " + um.getUser().getNombre());

        newArg.setVotacion(vc.getVotacion());
        newArg.setMiembro(um.getUser());
        newArg.setFecha(new Date());

        try {
            vl.persist(newArg);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No es posible ingresar su argumento.", null));
            return "";
        }
        return "votDebate.xhtml?faces-redirect=true";
    }

    public List<Argumento> getArgumentos() {
        return vl.getArgumentosParaVotacion(vc.getVotacion());
    }

    /**
     * @return the newArg
     */
    public Argumento getNewArg() {
        return newArg;
    }

    /**
     * @param newArg the newArg to set
     */
    public void setNewArg(Argumento newArg) {
        this.newArg = newArg;
    }

    /**
     * @return the ningunaop
     */
    public Opcion getNingunaop() {
        return ningunaop;
    }

    /**
     * @param ningunaop the ningunaop to set
     */
    public void setNingunaop(Opcion ningunaop) {
        this.ningunaop = ningunaop;
    }

    /**
     * @return the raiz
     */
    
    public String responderA(long argid) {
        newArg = new Argumento();
        if (argid!=0) {
            newArg.setPater(argid);
        }                
        return "nuevoArgumento.xhtml?vid="+vc.getVotacion().getId()+"&amp;faces-redirect=true";
    }
    public TreeNode getRaiz() {

        Map<Long, TreeNode> mapa = new HashMap<Long, TreeNode>();
        raiz = new DefaultTreeNode(new Argumento(), null);
        List<Argumento> argus = vl.getArgumentosParaVotacion(vc.getVotacion());
        for (Argumento a : argus) {
            TreeNode tn = new DefaultTreeNode(a, raiz);
            tn.setExpanded(true);
            mapa.put(a.getId(), tn);
        }
        for (Long tid : mapa.keySet()) {
            try {
                if (((Argumento) mapa.get(tid).getData()).getPater() != 0) {
                    mapa.get(tid).setParent(mapa.get(((Argumento) mapa.get(tid).getData()).getPater()));
                }
            } catch (Exception e) {
            }
        }
        raiz.setExpanded(true);
        
        return raiz;
    }

    /**
     * @param raiz the raiz to set
     */
    public void setRaiz(TreeNode raiz) {
        this.raiz = raiz;
    }
}
