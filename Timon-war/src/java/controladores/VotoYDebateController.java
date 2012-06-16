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
import entities.votacionydebate.Votacion;
import java.io.Serializable;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import sessionbeans.TimonLogic;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "vydc")
@SessionScoped
public class VotoYDebateController implements Serializable {
    
    private Votacion nuevaVotacion = new Votacion();
    
    @Inject
    VotoYDebateLogic vydl;

    public VotoYDebateController() {
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
    
}
