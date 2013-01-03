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
package timon.sessionbeans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import timon.entities.showcase.Slide;
import timon.entities.showcase.SlideImage;

/**
 *
 * @author Alfonso Tames
 */
@Stateless
public class SlideLogic implements Serializable {

    private static final Logger mrlog = Logger.getLogger(SlideLogic.class.getName());
    @PersistenceContext(unitName = "Timon-ejbPU")
    private EntityManager em;

    public byte[] getImagenDeSlide(long id) {
        SlideImage si = em.find(SlideImage.class, id);
        return si.getFile();
    }    
    @PermitAll
    public List<Slide> getActiveSlides() {
        Date hoy = new Date();
        return em.createQuery("select s from Slide s where s.activa = true and :hoy < s.expira order by s.orden").setParameter("hoy", hoy).getResultList();
    }
    
}
