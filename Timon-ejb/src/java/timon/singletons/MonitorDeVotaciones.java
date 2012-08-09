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
package timon.singletons;

import java.util.HashMap;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author alfonso
 */
@Singleton
@LocalBean
public class MonitorDeVotaciones {

    /*
     * La idea del Monitor de Votaciones es llevar un registro de las votaciones
     * que se estan contando en el momento para poder alimentar las barras de
     * progreso
     *
     * No estamos borrando los conteos, lo dejarémos para mañana ;)
     *
     */
    private Map<Long, Integer> conteos;

    public MonitorDeVotaciones() {        
        conteos = new HashMap<Long,Integer>();
    }

    /**
     * @return the conteos
     */
    public Map<Long, Integer> getConteos() {
        return conteos;
    }

    /**
     * @param conteos the conteos to set
     */
    public void setConteos(Map<Long, Integer> conteos) {
        this.conteos = conteos;
    }
}
