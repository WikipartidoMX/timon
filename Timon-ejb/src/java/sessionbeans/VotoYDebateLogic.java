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

import entities.registro.Miembro;
import entities.votacionydebate.Opcion;
import entities.votacionydebate.Tema;
import entities.votacionydebate.Voto;
import java.io.Serializable;
import java.util.List;
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
    
    
    public List<Tema> getTemas() {
        return em.createQuery("select t from Tema t").getResultList();
    }
    
    public Tema getTema(long id) {
        return em.find(Tema.class, id);
    }
    

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

        // Implementacion del Metodo Schulze (Thanks http://wiki.electorama.com/wiki/Schulze_method !)


        int i = 0;
        int j = 0;
        int k = 0;

        int c = opciones.size();

        for (i = 0; i < c; i++) {
            System.out.println("Opcion " + i + ".- " + opciones.get(i).getNombre());
        }

        boolean[] winner = new boolean[c];
        long[][] p = new long[c][c];

        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
                    if (cuantosPrefieren(opciones.get(i), opciones.get(j)) > cuantosPrefieren(opciones.get(j), opciones.get(i))) {
                        p[i][j] = cuantosPrefieren(opciones.get(i), opciones.get(j));
                    } else {
                        p[i][j] = 0;
                    }
                }
            }
        }
        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
                    for (k = 0; k < c; k++) {
                        if (!opciones.get(i).equals(opciones.get(k))) {
                            if (!opciones.get(j).equals(opciones.get(k))) {
                                p[j][k] = Math.max(p[j][k], Math.min(p[j][i], p[i][k]));
                            }
                        }
                    }
                }
            }
        }

        for (i = 0; i < c; i++) {
            winner[i] = true;
        }

        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
                    if (p[j][i] > p[i][j]) {
                        winner[i] = false;
                    }
                }
            }
        }

        for (i = 0; i < c; i++) {
            if (winner[i]) {
                System.out.println("GANO "+opciones.get(i).getNombre());
            }
        }


        // Ver la matriz de preferencia
        String matriz = "";
        for (int x = 0; x < c; x++) {
            matriz += opciones.get(x).getNombre() + " ";
            for (int y = 0; y < opciones.size(); y++) {
                matriz += p[x][y] + " ";
            }
            matriz += "\n";
        }
        System.out.println("Matriz de Preferencia: \n\n" + matriz);
        //****


    }

    public long cuantosPrefieren(Opcion i, Opcion j) {
        long c = 0;
        System.out.println("La pregunta es cuantos prefieren " + i.getNombre() + " sobre " + j.getNombre());
        List<Miembro> miembros = em.createQuery("select v.miembro from Voto v group by v.miembro").getResultList();
        for (Miembro m : miembros) {
            long ri = 0;
            long rj = 0;

            //System.out.println("Probando con " + m.getNombre());
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
        return c;
    }
}
