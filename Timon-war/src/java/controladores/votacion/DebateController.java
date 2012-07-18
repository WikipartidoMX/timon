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
import entities.votacionydebate.Argumento;
import entities.votacionydebate.Opcion;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "debc")
@SessionScoped
public class DebateController implements Serializable {
    
    @Inject
    UserManager um;
    
    @Inject
    VotoYDebateLogic vl;
    
    @Inject
    VotacionController vc;
    
    private Argumento newArg;

    
    public List<Opcion> getOpciones() {
        List<Opcion> ops = vl.getOpcionesParaVotacion(vc.getVotacion());
        Opcion nop = new Opcion();
        nop.setNombre("Ninguna Opción");
        ops.add(0, nop);
        return ops;
    }

    /**
     * @return the newARg
     */
    public Argumento getNewArg() {
        return newArg;
    }

    /**
     * @param newARg the newARg to set
     */
    public void setNewARg(Argumento newARg) {
        this.newArg = newARg;
    }
    
    
}
