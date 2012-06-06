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
package sessionbeans;

import entities.Avatar;
import entities.Miembro;
import java.io.Serializable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Alfonso Tames
 */
@Stateless
public class TimonLogic implements Serializable {

    @PersistenceContext(unitName = "Timon-ejbPU")
    private EntityManager em;

    public void persist(Object object) {
        em.persist(object);
    }

    public Miembro login(String email, String passwd) {
        try {
            return (Miembro) em.createQuery("select m from Miembro m "
                    + "where m.email=:email and m.password=:passwd").setParameter("email", email).setParameter("passwd", passwd).getSingleResult();
        } catch (Exception e) {            
            return null;
        }
    }

    public byte[] getAvatarFile(long mid) {
        try {
            Avatar a = (Avatar)em.createQuery("select a from Avatar a where a.miembro.id=:mid").setParameter("mid", mid).getSingleResult();
            System.out.println("Regresando el AVATAR" +a.getId());
            return a.getFile();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            return null;
        }

    }

}
