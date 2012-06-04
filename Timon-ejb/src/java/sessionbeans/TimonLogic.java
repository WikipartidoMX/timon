/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionbeans;

import java.io.Serializable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 *
 * @author alfonso
 */
@Stateless
public class TimonLogic implements Serializable {
    @PersistenceContext(unitName = "Timon-ejbPU")
    private EntityManager em;

    public void persist(Object object) {
        em.persist(object);
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    public void echo(String echo) {
        System.out.println("Echo "+echo);
    }
    
    
    
}
