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
@Named(value = "dc")
@SessionScoped
public class DelegacionController implements Serializable {
    @Inject
    UserManager um;
    @Inject
    VotoYDebateLogic vydl;
    
    private Miembro selecMiembro;
    
    public List<Miembro> completarMiembro(String query) {
        return vydl.completarMiembro(query);
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
    
    
}
