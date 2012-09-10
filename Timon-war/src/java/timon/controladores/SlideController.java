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
package timon.controladores;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import timon.entities.showcase.Slide;
import timon.sessionbeans.SlideLogic;

/**
 *
 * @author Alfonso Tamés
 */

@Named(value = "sc")
@SessionScoped
public class SlideController implements Serializable {
    
    @Inject
    SlideLogic sl;
    private List<Slide> slides;

    /**
     * @return the slides
     */
    public List<Slide> getSlides() {
        if (slides == null) {
            slides = sl.getActiveSlides();
        }
        return slides;
    }

    /**
     * @param slides the slides to set
     */
    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }
    
}
