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

import entities.registro.Avatar;
import entities.registro.Estado;
import entities.registro.Miembro;
import java.io.Serializable;
import java.util.List;
import javax.annotation.security.PermitAll;
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
    
    public Object merge(Object object) {
        return em.merge(object);
    }

    public Miembro login(String email, String passwd) {
        try {
            return (Miembro) em.createQuery("select m from Miembro m "
                    + "where m.email=:email and m.password=:passwd").setParameter("email", email).setParameter("passwd", passwd).getSingleResult();
        } catch (Exception e) {            
            return null;
        }
    }
    
    public Number cuantosMiembros() {
        try {
            return (Number) em.createQuery("select count(m) from Miembro m").getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Estado> getEstados() {
        return em.createQuery("select e from Estado e").getResultList();
    }
    
    public Estado getEstado(long id) {
        try {
            return em.find(Estado.class, id);
        } catch (Exception e) {
            return null;
        }
    }
    

    public Miembro getMiembro(long id) {
        try {
            Miembro m = em.find(Miembro.class, id);
            //System.out.println("TimonLogic :: getMiembro :: Regresandoa a :"+m.getNombre());
            return m;
        } catch (Exception e) {
            System.out.println("TimonLogic :: getMiembro :: Error ");
            return null;
        }
        
    }    
    
    public Miembro getMiembroFromEmail(String email) {
        try {
            return (Miembro) em.createQuery("select m from Miembro m where m.email=:email").setParameter("email", email).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    public Miembro getMiembroFromClavedeusuario(String clavedeusuario) {
        try {
            return (Miembro) em.createQuery("select m from Miembro m where m.clavedeusuario=:clavedeusuario").setParameter("clavedeusuario", clavedeusuario).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Avatar getAvatar(long mid) {
        try {
            return (Avatar)em.createQuery("select a from Avatar a where a.miembro.id=:mid").setParameter("mid", mid).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] getAvatarFile(long mid) {
        try {
            Avatar a = (Avatar)em.createQuery("select a from Avatar a where a.miembro.id=:mid").setParameter("mid", mid).getSingleResult();
            return a.getFile();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            return null;
        }

    }

}
