/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionbeans;

import entities.registro.Miembro;
import entities.votacionydebate.Opcion;
import entities.votacionydebate.Voto;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Alfonso Tames
 */
@Stateless
public class VotoYDebateLogic implements Serializable {

    @PersistenceContext(unitName = "Timon-ejbPU")
    private EntityManager em;

    public void cuentaConSchulze() {
        System.out.println("Conteo con Schulze...\n\n");
        System.out.println("Opciones en la votacion:");
        List<Opcion> opciones = em.createQuery("select o from Opcion o where o.votacion.id=1").getResultList();
        for (Opcion o : opciones) {
            System.out.println("Opcion: " + o.getNombre());
        }
        System.out.println("Son " + opciones.size() + " opciones.");
        // Prueba



        System.out.println("Votaron así:");
        List<Miembro> miembros = em.createQuery("select v.miembro from Voto v group by v.miembro").getResultList();
        for (Miembro m : miembros) {
            System.out.println("Miembro: " + m.getNombre() + " " + m.getApellidoPaterno());
            List<Voto> votos = em.createQuery("select v from Voto v where v.opcion.votacion.id=1 and v.miembro=:m order by v.rank").setParameter("m", m).getResultList();
            for (Voto v : votos) {
                System.out.println("|----: " + v.getRank() + " " + v.getOpcion().getNombre());
            }
        }

        // Algoritmo para el Metodo Schulze (Thanks http://wiki.electorama.com/wiki/Schulze_method !)
        Map<DoubleKey,Long> p = new HashMap<DoubleKey,Long>();
        
        
        for (Opcion i : opciones) {
            for (Opcion j : opciones) {
                if (!i.equals(j)) {
                    if (cuantosPrefieren(i,j) > cuantosPrefieren(j,i)) {
                        p.put(new DoubleKey(i,j), cuantosPrefieren(i,j));
                    } else {
                        p.put(new DoubleKey(i,j), 0L);
                    }
                }
            }
        }
        String matriz = "";
        // Ver la matriz de preferencia
        for (Opcion i : opciones) {
            matriz += i.getNombre()+" ";
            for (Opcion j : opciones) {
                matriz += p.get(new DoubleKey(i,j))+" ";                               
            }
            matriz += "\n";
        }
        System.out.println(matriz);

        long c = cuantosPrefieren(opciones.get(1), opciones.get(2));


    }

    public long cuantosPrefieren(Opcion i, Opcion j) {
        long c = 0;
        System.out.println("La pregunta es cuantos prefieren " + i.getNombre() + " sobre " + j.getNombre());
        List<Miembro> miembros = em.createQuery("select v.miembro from Voto v group by v.miembro").getResultList();
        for (Miembro m : miembros) {
            long ri = 0;
            long rj = 0;

            System.out.println("Probando con " + m.getNombre());
            try {
                ri = (Long) em.createQuery("select v.rank from Voto v where v.miembro=:m and v.opcion=:i").setParameter("m", m).setParameter("i", i).getSingleResult();
            } catch (Exception e) {
            }
            try {
                rj = (Long) em.createQuery("select v.rank from Voto v where v.miembro=:m and v.opcion=:j").setParameter("m", m).setParameter("j", j).getSingleResult();

            } catch (Exception e) {
            }

            if (ri > 0) {
                if (ri < rj) {
                    c++;
                }
            }
            if (ri > 0 && rj < 1) {
                c++;
            }
        }
        System.out.println(c + " lo prefieren");
        return 0L;
    }
}

class DoubleKey {   
    private Opcion i;
    private Opcion j;
    
    public DoubleKey(Opcion i, Opcion j) {
        this.i = i;
        this.j = j;
    }

    /**
     * @return the i
     */
    public Opcion getI() {
        return i;
    }

    /**
     * @param i the i to set
     */
    public void setI(Opcion i) {
        this.i = i;
    }

    /**
     * @return the j
     */
    public Opcion getJ() {
        return j;
    }

    /**
     * @param j the j to set
     */
    public void setJ(Opcion j) {
        this.j = j;
    }
    
}