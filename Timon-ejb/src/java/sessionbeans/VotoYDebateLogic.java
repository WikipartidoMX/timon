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

import entities.registro.Estado;
import entities.registro.Miembro;
import entities.votacionydebate.*;
import java.io.Serializable;
import java.util.*;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author Alfonso Tames
 */
@Stateless
public class VotoYDebateLogic implements Serializable {

    
    @PersistenceContext(unitName = "Timon-ejbPU")
    private EntityManager em;
    @Resource
    protected UserTransaction utx;

    public void persist(Object object) {
        em.persist(object);
    }

    public Object merge(Object object) {
        return em.merge(object);
    }

    public void remove(Object obj) {
        em.remove(obj);
    }

    public ResultadoSchulze guardarVotacion(LogVotacion logvot, List<Opcion> opciones) {
        System.out.println("Registrando Votos de " + logvot.getMiembro().getNombre());
        System.out.println("Para Votacion: " + logvot.getVotacion());
        long i = 1;
        List<Miembro> delegan = miembrosQueDeleganA(logvot.getMiembro(), logvot.getVotacion());
        for (Opcion op : opciones) {
            // Registro para todas las personas que representa
            for (Miembro m : delegan) {
                System.out.println("Registrando voto " + i + " " + op.getNombre() + " para " + m.getNombre());
                Voto v = new Voto();
                v.setDelegado(logvot.getMiembro());
                v.setMiembro(m);
                v.setOpcion(op);
                v.setRank(i);
                persist(v);
            }
            // Registro para la persona en cuestion
            Voto v = new Voto();
            v.setMiembro(logvot.getMiembro());
            v.setOpcion(op);
            v.setRank(i);
            v.setDelegado(null);
            persist(v);
            i++;
        }
        persist(logvot);
        ResultadoSchulze rs = new ResultadoSchulze();
        rs.setFechaConteo(new Date());
        rs.setVotacion(logvot.getVotacion());
        List<ResultadoSchulze> pasados = em.createQuery("select r from ResultadoSchulze r where "
                + "r.fechaConteo < :fc and r.votacion = :vot").setParameter("fc", rs.getFechaConteo()).setParameter("vot", logvot.getVotacion()).getResultList();
        for (ResultadoSchulze r : pasados) {
            em.remove(r);
        }
        System.out.println("Persistiendo el nuevo RS...");
        persist(rs);
        return rs;
    }

    public ResultadoSchulze getResultadoSchulze(long id) {
        return em.find(ResultadoSchulze.class, id);
    }

    public List<Miembro> miembrosQueDeleganA(Miembro delegado, Votacion vot) {
        List<Miembro> r = new LinkedList<Miembro>();
        for (Tema t : vot.getTemas()) {
            r.addAll(em.createQuery("select d.miembro from Delegacion d "
                    + "where d.delegado = :delegado and d.tema = :t").setParameter("delegado", delegado).setParameter("t", t).getResultList());
        }
        return r;

    }

    public long numeroAtomico(Votacion vot, Miembro delegado) {
        long na = 0;
        for (Tema t : vot.getTemas()) {
            na += (Long) em.createQuery("select count(d) from Delegacion"
                    + " d where d.delegado = :delegado and d.tema = :tema").setParameter("delegado", delegado).setParameter("tema", t).getSingleResult();
        }
        return na;
    }

    public List<Argumento> getArgumentosParaVotacion(Votacion vot) {
        return em.createQuery("select a from Argumento a where a.votacion = :votacion order by a.fecha desc").setParameter("votacion", vot).getResultList();
    }

    public List<Opcion> getOpcionesParaVotacion(Votacion vot) {
        return em.createQuery("select o from Opcion o where o.votacion = :votacion").setParameter("votacion", vot).getResultList();
    }

    public Opcion getOpcion(long id) {
        return em.find(Opcion.class, id);
    }

    public boolean tieneImagenLaOpcion(long id) {
        try {
            em.createQuery("select i from ImagenOpcion i where i.opcion.id = :id").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public byte[] getImagenDeOpcion(long id) {
        Opcion o = em.find(Opcion.class, id);
        ImagenOpcion io = (ImagenOpcion) em.createQuery("select i from ImagenOpcion i where i.opcion = :opcion").setParameter("opcion", o).getSingleResult();
        return io.getFile();
    }

    public void guardarDelegacion(Delegacion d) throws Exception {
        Delegacion existe = null;
        try {
            existe = (Delegacion) em.createQuery("select d from Delegacion d where "
                    + "d.miembro = :miembro and "
                    + "d.tema = :tema").setParameter("miembro", d.getMiembro()).setParameter("tema", d.getTema()).getSingleResult();
        } catch (Exception e) {
        }
        if (existe != null) {
            String dsex = "delegado";
            if (existe.getDelegado().getSexo().equals("M")) {
                dsex = "delegada";
            }
            throw new Exception(existe.getDelegado().getNombreCompleto()
                    + " ya es " + dsex + " para el tema " + d.getTema().getNombre() + ", "
                    + "elimine la delegacion actual para reasignar.");
        }
        persist(d);
    }

    // TODO: Revisar permisos en EJB
    @PermitAll
    public List<Tema> getTemas() {
        return em.createQuery("select t from Tema t").getResultList();
    }

    public Tema getTema(long id) {
        return em.find(Tema.class, id);
    }

    @PermitAll
    public List<Votacion> getVotaciones(int start, int max) {
        return getVotaciones(start, max, null, null);
    }

    public Votacion getVotacion(long id) {
        return em.find(Votacion.class, id);
    }

    public void borrarDelegacion(long id) {
        em.remove(em.find(Delegacion.class, id));
    }

    public List<Miembro> completarMiembro(String query) {
        String q = "select m from Miembro m where ";
        String[] palabs = query.split("\\s");
        int i = 1;
        for (String p : palabs) {
            q += "(m.nombre like :p" + i + " or "
                    + "m.apellidoPaterno like :p" + i + " or "
                    + "m.apellidoMaterno like :p" + i + ") ";
            if (i < palabs.length) {
                q += " and ";
            }
            i++;
        }
        q += " and m.paso = 2";
        Query ejq = em.createQuery(q);
        i = 1;
        for (String p : palabs) {
            ejq.setParameter("p" + Integer.toString(i), "%" + p + "%");
            i++;
        }
        return ejq.getResultList();
    }

    public List<Delegacion> getDelegacionesPara(Miembro m) {
        return em.createQuery("select d from Delegacion d where d.miembro = :m").setParameter("m", m).getResultList();
    }

    public List<Votacion> getVotaciones(int start, int max, List<Tema> temas, Estado estado) {
        String ejbql = "select v from Votacion v";
        /*
         * Falta implementar filtros (si llegamos a tener muchas votaciones) for
         * (Tema t : temas) {
         *
         * }
         *
         */
        Query query = em.createQuery(ejbql);
        query.setFirstResult(start);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public byte[] getImagenVotacion(long vid) {
        try {
            ImagenVotacion a = (ImagenVotacion) em.createQuery("select i from ImagenVotacion i where i.votacion.id=:vid").setParameter("vid", vid).getSingleResult();
            return a.getFile();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            return null;
        }

    }

    public ResultadoSchulze getUltimoResultado(Votacion vot) {
        try {
            return (ResultadoSchulze) em.createQuery("select r from ResultadoSchulze r "
                    + "where r.votacion=:vot order by r.fechaConteo").setParameter("vot", vot).getSingleResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Integer getAvanceParaRS(long id) {
        int a;       
        em.flush();
        a = (Integer)em.createQuery("select r.avance from ResultadoSchulze r where r.id = :id").setParameter("id", id).getSingleResult();
        System.out.println("Soy el sessionbean!!!! "+a);
        return a;
    }



    @Asynchronous
    public void cuentaConSchulze(ResultadoSchulze rs) {
        rs = em.merge(rs);

        Votacion vot = rs.getVotacion();

        long startTime = System.currentTimeMillis();
        System.out.println("Conteo con Schulze para la votacion " + vot.getNombre());
        List<Opcion> opciones = em.createQuery("select o from Opcion o "
                + "where o.votacion = :vot").setParameter("vot", vot).getResultList();
        //for (Opcion o : opciones) {
        //System.out.println("Opcion: " + o.getNombre());
        //}
        //System.out.println("Son " + opciones.size() + " opciones.");
        // Prueba

        System.out.println("Votaron así:");
        List<Miembro> miembros = em.createQuery("select v.miembro from Voto v "
                + "where v.opcion.votacion = :vot group by v.miembro").setParameter("vot", vot).getResultList();
        for (Miembro m : miembros) {
            //System.out.println("Miembro: " + m.getNombre() + " " + m.getApellidoPaterno());
            List<Voto> votos = em.createQuery("select v from Voto v "
                    + "where v.opcion.votacion = :vot and v.miembro=:m order by v.rank").setParameter("vot", vot).setParameter("m", m).getResultList();
            for (Voto v : votos) {
                //System.out.println("|----: " + v.getRank() + " " + v.getOpcion().getNombre());
            }
        }

        // Implementacion del Metodo Schulze (Thanks http://wiki.electorama.com/wiki/Schulze_method !)
        List<Score> scores = new ArrayList<Score>();
        int i, j, k;
        int c = opciones.size();
        boolean[] winner = new boolean[c];
        long[][] p = new long[c][c];

        long[][] prefe = new long[c][c];

        long st;
        // Primero calculamos la matriz de preferencias
        st = System.currentTimeMillis();
        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
                    long a = cuantosPrefieren(opciones.get(i), opciones.get(j));
                    long b = cuantosPrefieren(opciones.get(j), opciones.get(i));
                    prefe[i][j] = a;
                    prefe[j][i] = b;
                    if (a > b) {
                        p[i][j] = a;
                    } else {
                        p[i][j] = 0;
                    }
                }
            }
            if (i == 0) {
                long t = System.currentTimeMillis() - st;
                System.out.println("Tomó a la primera iteración: " + t);
            }
            rs.setAvance((i * 100) / c);
            System.out.println("Avance: "+rs.getAvance());
        }
        // Luego calculamos el strongest path de una preferencia a otra
        st = System.currentTimeMillis();
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
            if (i == 0) {
                long t = System.currentTimeMillis() - st;
                System.out.println("Tomó a la primera iteración SP: " + t);
            }



        }
        for (i = 0; i < c; i++) {
            winner[i] = true;
        }
        int[] score = new int[c];
        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
                    //System.out.println("p[j][i] : p[i][j]"+" "+p[j][i]+ " : "+p[i][j]);
                    if (p[j][i] > p[i][j]) {
                        winner[i] = false;
                    } else {
                        score[i]++;
                    }
                }
            }
        }
        for (i = 0; i < c; i++) {
            if (winner[i]) {
                System.out.println("GANO " + opciones.get(i).getNombre());
            }
            //System.out.println("Score "+opciones.get(i).getNombre()+ " "+score[i]);
            scores.add(new Score(opciones.get(i), score[i]));
        }

        Collections.sort(scores);

        // Ver la matriz de preferencia
        /*
         * String matriz = ""; for (int x = 0; x < c; x++) { matriz +=
         * opciones.get(x).getNombre() + " "; for (int y = 0; y <
         * opciones.size(); y++) { matriz += p[x][y] + " "; } matriz += "\n"; }
         * System.out.println("Matriz de Preferencia: \n\n" + matriz);
         *
         */
        rs.setScores(scores);
        rs.setPref(prefe);
        rs.setSp(p);
        rs.setExetime(System.currentTimeMillis() - startTime);
        rs.setAvance(100);

    }

    public long cuantosPrefieren(Opcion i, Opcion j) {
        long c = 0;
        //System.out.println("La pregunta es cuantos prefieren " + i.getNombre() + " sobre " + j.getNombre());
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
        //System.out.println(c + " prefieren " + i.getNombre() + " sobre " + j.getNombre());
        return c;
    }
}
