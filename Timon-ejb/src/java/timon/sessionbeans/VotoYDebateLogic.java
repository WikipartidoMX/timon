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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import timon.entities.log.Actividad;
import timon.entities.registro.Estado;
import timon.entities.registro.Miembro;
import timon.entities.votacionydebate.*;
import timon.singletons.MonitorDeVotaciones;

/**
 *
 * @author Alfonso Tames
 */

/*
 * TODO: Más adelante necesitaremos programar los conteos con JMS y definir un ancho
 * de banda limitado para que sólo puedan ser computadas al mismo tiempo cierta cantidad 
 * de votaciones.
 * 
 * Por supuesto la cantidad de votaciones procesadas puede aumentar con más nodos
 * 
 */
@Stateless
public class VotoYDebateLogic implements Serializable {

    private static final Logger mrlog = Logger.getLogger(VotoYDebateLogic.class.getName());
    @PersistenceContext(unitName = "Timon-ejbPU")
    private EntityManager em;
    @Inject
    MonitorDeVotaciones mv;

    public void persist(Object object) {
        em.persist(object);
    }

    public Object merge(Object object) {
        return em.merge(object);
    }

    public void remove(Object obj) {
        em.remove(obj);
    }
    
    public boolean isIPsaturado(String ip, Votacion vot) {
        boolean saturado = false;
        Date ahora = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(ahora);
        cal.add(Calendar.HOUR, -24);
        List<LogVotacion> logs = em.createQuery("select l from LogVotacion l where l.votacion = :vot and l.ip = :ip and l.fecha > :lapso")
                .setParameter("lapso", cal.getTime())
                .setParameter("vot", vot)
                .setParameter("ip", ip)
                .getResultList();
        if (logs != null) {
            if (logs.size() > 2) {
                saturado = true;
            }
        } else {
            saturado = false;
        }
        return saturado;
    }

    /*
     * La lógica de guardar la votación y contar los votos está separada
     *
     */
    public void guardarVotacion(LogVotacion logvot, List<Opcion> opciones) throws Exception {
        
        // TODO: Verificar que el delegado/miembro sea del estado o estados        
        if (logvot.getMiembro().getPaso() < 2) {
            throw new Exception("Aún no eres miembro con derecho a voto :( ¡Afíliate hoy!");
        }
        if (!perteneceAEstadosDeVotacion(logvot.getMiembro(),logvot.getVotacion())) {
            throw new Exception("¡No perteneces a los estados limitados a la votación!");
        }
        Date hoy = new Date();
        if (hoy.after(logvot.getVotacion().getFechaCierre())) {
            throw new Exception("¡La votación ya está cerrada! No es posible registrar más votos.");
        }
        
        if (isIPsaturado(logvot.getIp(),logvot.getVotacion())) {
            Actividad act = new Actividad();
            act.setMiembro(logvot.getMiembro());
            act.setIp(logvot.getIp());
            act.setFecha(new Date());
            act.setTipo("votacion");
            act.setDescripcion("Rebase del límite de votacion :"+logvot.getVotacion().getId()+": "+logvot.getVotacion().getNombre());
            persist(act);
            throw new Exception("Se ha alcanzado el límite de votos.");
        }
        
        mrlog.log(Level.FINE, "Registrando Votos de {0} para votacion: {1}",
                new Object[]{logvot.getMiembro().getNombre(), logvot.getVotacion()});
        mrlog.log(Level.FINE, "Verificando si {0} ya votó en esta votación...", logvot.getMiembro().getNombre());
        if (getLogVotacion(logvot.getMiembro(), logvot.getVotacion()) != null) {
            throw new Exception("¡Ya votaste en la elección <" + logvot.getVotacion().getNombre() + ">!");
        }
        long i = 1;
        List<Miembro> delegan = miembrosQueDeleganA(logvot.getMiembro(), logvot.getVotacion());
        mrlog.log(Level.FINE, "{0} miembros le han delegado su voto, empezando iteraciones para guardar los votos...", delegan.size());
        // Antes de empezar a guardar los votos hay que extraer de la lista delegan
        // a los miembros que ya votaron (no falla que votan y luego delegan)
        // Para remover con seguridad se necesita un iterator:
        Iterator<Miembro> ite = delegan.iterator();
        while (ite.hasNext()) {
            Miembro m = ite.next();
            if (getLogVotacion(m, logvot.getVotacion()) != null) {
                mrlog.log(Level.FINE, "¡El miembro {0} ya voto! se removerá de la lista de delegaciones...", m.getNombreCompleto());
                ite.remove();
            }
        }
        mrlog.log(Level.FINE, "Empezando a iterar para guardar los votos...");
        try {
            for (Opcion op : opciones) {
                for (Miembro m : delegan) {
                    mrlog.log(Level.FINEST, "Registrando voto {0} {1} para {2}", new Object[]{i, op.getNombre(), m.getNombre()});
                    Voto v = new Voto();
                    v.setDelegado(logvot.getMiembro());
                    v.setMiembro(m);
                    v.setOpcion(op);
                    v.setRank(i);
                    v.setVotacion(logvot.getVotacion());
                    persist(v);
                }
                // Guardar el voto de la persona que esta votando
                Voto v = new Voto();
                v.setMiembro(logvot.getMiembro());
                v.setOpcion(op);
                v.setRank(i);
                v.setVotacion(logvot.getVotacion());
                v.setDelegado(null);
                persist(v);
                i++;
            }
            persist(logvot);
        } catch (Exception e) {
            throw new Exception("Ocurrió un error al tratar de guardar la votación: " + e.getMessage());
        }
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

    // La cantidad de miembros que han delegado su voto en un delegado para una votación
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
        return em.createQuery("select o from Opcion o where o.votacion = :votacion order by o.orden").setParameter("votacion", vot).getResultList();
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
        // TODO: hay que restringir la delegación del voto por IP, probablemente poner captcha

        if (d.getMiembro().getPaso() < 2) {
            throw new Exception("No eres miembro con derecho a voto. ¡Afíliate hoy!");
        }
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

    public Votacion getVotacion(long id) {
        return em.find(Votacion.class, id);
    }

    public void borrarDelegacion(long id) {
        em.remove(em.find(Delegacion.class, id));
    }

    public List<Miembro> completarMiembro(String query) {
        mrlog.log(Level.FINE,"completando el miembro...");
        List<Miembro> miembros;
        StringBuilder q = new StringBuilder();
        q.append("select m from Miembro m where ");
        String[] palabs = query.split("\\s");
        int i = 1;
        for (String p : palabs) {
            q.append("(m.nombre like :p").append(i).append(" or m.apellidoPaterno like :p").append(i)
                    .append(" or m.apellidoMaterno like :p").append(i).append(") ");
            if (i < palabs.length) {
                q.append(" and ");
            }
            i++;
        }
        q.append(" and m.paso = 2");
        Query ejq = em.createQuery(q.toString());
        i = 1;
        for (String p : palabs) {
            ejq.setParameter("p" + Integer.toString(i), "%" + p + "%");
            i++;
        }
        mrlog.log(Level.FINE,"ejecutando el query...");
        miembros = ejq.getResultList();
        mrlog.log(Level.FINE,"regresando una lista de {0} miembros.",miembros.size());
        return miembros;
    }

    public List<Delegacion> getDelegacionesPara(Miembro m) {
        return em.createQuery("select d from Delegacion d where d.miembro = :m").setParameter("m", m).getResultList();
    }

    @PermitAll
    public LazyResult getVotaciones(int start, int max) {
        return getVotaciones(start, max, null, null, null, false, null, null);
    }

    public LazyResult getVotaciones(int start, int max, Tema filtroTema, String filtroTexto,
            Estado filtroEstado, boolean filtroAbiertas,
            Date filtroFechaCierreDesde, Date filtroFechaCierreHasta) {
        mrlog.log(Level.FINE, "Buscando votaciones de {0} a {1}", new Object[]{start, max});
        StringBuilder ejbql = new StringBuilder();
        StringBuilder ejbqlCount = new StringBuilder();
        StringBuilder filtros = new StringBuilder();
        StringBuilder froms = new StringBuilder();
        ejbql.append("select v from Votacion v ");
        ejbqlCount.append("select count(v) from Votacion v ");
        boolean filtrar = false;
        if (filtroTema != null) {
            froms.append(", Tema t ");
            filtros.append(" t member of v.temas and t = :tema ");
            filtrar = true;
        }
        if (filtroTexto != null) {
            if (filtroTexto.matches(".*\\w.*")) {
                String[] palabs = filtroTexto.split("\\s");
                int i = 1;
                if (palabs.length > 0 && filtrar) {
                    filtros.append(" and ");
                }
                filtros.append("(");
                for (String p : palabs) {

                    filtros.append(" (v.nombre like :p").append(i).append(" or v.descripcion like :p").append(i).append(") ");
                    if (i < palabs.length) {
                        filtros.append(" and ");
                    }
                    i++;
                }
                filtros.append(")");
                filtrar = true;
            }
        }
        if (filtroAbiertas) {
            if (filtrar) {
                filtros.append(" and ");
            }
            filtros.append(" v.fechaCierre >= :hoy ");
            filtrar = true;
        }
        if (filtrar) {
            ejbql.append(froms).append(" where ").append(filtros);
            ejbqlCount.append(froms).append(" where ").append(filtros);
        }
        // Orden
        ejbql.append(" order by v.fechaCierre desc ");
        ejbqlCount.append(" order by v.fechaCierre desc ");
        mrlog.log(Level.FINE, ejbql.toString());
        mrlog.log(Level.FINE, ejbqlCount.toString());


        Query query = em.createQuery(ejbql.toString());
        Query queryCount = em.createQuery(ejbqlCount.toString());
        if (filtrar) {
            if (filtroTema != null) {
                query.setParameter("tema", filtroTema);
                queryCount.setParameter("tema", filtroTema);
            }
            if (filtroTexto != null) {
                if (filtroTexto.matches(".*\\w.*")) {
                    int i = 1;
                    String[] palabs = filtroTexto.split("\\s");
                    for (String p : palabs) {
                        query.setParameter("p" + Integer.toString(i), "%" + p + "%");
                        queryCount.setParameter("p" + Integer.toString(i), "%" + p + "%");
                        mrlog.log(Level.FINE, "Agregando parámetros: {0}", i);
                        i++;
                    }
                }
            }
            if (filtroAbiertas) {
                Date hoy = new Date();
                query.setParameter("hoy", hoy);
                queryCount.setParameter("hoy", hoy);
            }
        }
        mrlog.log(Level.FINE, query.toString());
        long sl = (Long) queryCount.getSingleResult();
        int s = (int) sl;
        query.setFirstResult(start);
        query.setMaxResults(max);
        mrlog.log(Level.FINE, "Regresando {0} resgistros", query.getResultList().size());
        List<Votacion> votas = query.getResultList();
        for (Votacion v : votas) {
            mrlog.log(Level.FINE, "Votacion: {0}", v.getNombre());
        }
        return new LazyResult(votas, s);
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
                    + "where r.votacion=:vot order by r.fechaConteo").setParameter("vot", vot).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            mrlog.log(Level.FINE, "No hay resultados de conteos previos para esta eleccion: {0}", e.getMessage());
            return null;
        }
    }

    public LogVotacion getLogVotacion(Miembro m, Votacion vot) {
        try {
            return (LogVotacion) em.createQuery("select l from LogVotacion l where l.miembro = :m and l.votacion = :vot").setParameter("m", m).setParameter("vot", vot).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<Voto> getVotosPara(Miembro m, Votacion vot) {
        return em.createQuery("select v from Voto v "
                + "where v.votacion = :vot and v.miembro=:m order by v.rank").setParameter("vot", vot).setParameter("m", m).getResultList();
    }

    @Asynchronous
    public void cuentaConSchulze(Votacion vot) throws Exception {
        mv.getConteos().put(vot.getId(), 0);
        mrlog.log(Level.FINE, "Iniciando el conteo con Schulze...");
        ResultadoSchulze rs = new ResultadoSchulze(); // aqui guardaremos los resultados del conteo
        rs.setFechaConteo(new Date());
        // Cuantos miembros votaron
        long participacion = getParticipacion(vot);
        // Cuantos miembros pueden votar
        long poblacion = getPoblacion(vot);
        mrlog.log(Level.FINE,"Han participado {0} miembros",participacion);
        mrlog.log(Level.FINE,"Total de Población para esta votación: {0} miembros.",poblacion);
        
        rs.setVotacion(vot);        
        rs.setParticipacion(participacion);
        rs.setPoblacion(poblacion);
        long quorum = getQuorum(poblacion);
        rs.setQuorum(quorum);
        mrlog.log(Level.FINE,"El quorum es de {0} miembros",quorum);
        if (participacion >= quorum) {
            rs.setHayQuorum(true);
        } else {
            rs.setHayQuorum(false);
        }
        try {
            List<ResultadoSchulze> pasados = em.createQuery("select r from ResultadoSchulze r where "
                    + "r.fechaConteo < :fc and r.votacion = :vot").setParameter("fc", rs.getFechaConteo()).setParameter("vot", vot).getResultList();
            mrlog.log(Level.FINE, "Había otros {0} resultados anteriores a este que serán eliminados...", pasados.size());
            for (ResultadoSchulze r : pasados) {
                em.remove(r);
            }
        } catch (Exception e) {
            throw new Exception("Ocurrió un error al tratar de actualizar la votación" + e.getMessage());
        }
        mrlog.log(Level.FINE, "Persistiendo el ResuladoSchulze...");
        try {
            persist(rs);
        } catch (Exception e) {
            throw new Exception("Error: ¡No es posible guardar el resultado de la votación! " + e.getMessage());
        }
        long st = System.currentTimeMillis();
        mrlog.log(Level.FINE, "Conteo con Schulze para la votacion {0}", vot.getNombre());
        // Implementacion del Metodo Schulze (El algoritmo se puede encontrar en el sitio de Markus Schulze)
        List<Opcion> opciones = vot.getOpciones();
        for (Opcion o : opciones) {
            mrlog.log(Level.FINEST, "Opcion:{0} {1}", new Object[]{o.getId(), o.getNombre()});
        }
        List<Score> scores = new ArrayList<>();
        int i, j, k;
        int c = opciones.size();
        boolean[] winner = new boolean[c];
        mrlog.log(Level.FINE, "Calculando Matriz de Preferencias de tamaño {0}...", Math.pow(c, 2));
        long[][] prefe = getMatrizDePreferenciaRapido(vot);
        long[][] p = new long[c][c];
        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                p[i][j] = prefe[i][j];
            }
        }
        StringBuilder matriz = new StringBuilder();
        for (int x = 0; x < c; x++) {
            matriz.append(opciones.get(x).getNombre()).append(" ");
            for (int y = 0; y < c; y++) {
                matriz.append(prefe[x][y]).append(" ");
            }
            matriz.append("\n");
        }
        mrlog.log(Level.FINE, "Matriz: \n{0}", matriz.toString());
        mrlog.log(Level.FINE, "Claculando Strongest Paths, se iterará {0} veces en total...", Math.pow(c, 3));
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
        mrlog.log(Level.FINE, "Definiendo al/los ganadores...");
        for (i = 0; i < c; i++) {
            winner[i] = true;
        }
        int[] score = new int[c];
        for (i = 0; i < c; i++) {
            for (j = 0; j < c; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
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
                mrlog.log(Level.FINE, "GANO {0}", opciones.get(i).getNombre());
            }
            mrlog.log(Level.FINEST, "La opción {0} se prefiere sobre otras {1} opciones", new Object[]{opciones.get(i).getNombre(), score[i]});
            scores.add(new Score(opciones.get(i), score[i]));
        }
        Collections.sort(scores);
        int lugar=1;
        for (i=0; i < scores.size()-1; i++) {
            scores.get(i).setLugar(lugar);
            if (scores.get(i).getScore() > scores.get(i+1).getScore()) {
                lugar++;
            }
        }
        scores.get(i).setLugar(lugar);
        rs.setScores(scores);
        rs.setPref(prefe);
        rs.setSp(p);
        rs.setExetime(System.currentTimeMillis() - st);
        mv.getConteos().put(vot.getId(), 100);
    }

    /**
     * Generación de la matriz de preferencia de esta elección. Este es el
     * método rápido y no reporta el progreso de la operación porque la base de
     * datos hace en un solo paso la generación de la matriz.
     *
     * MariaDB tiene contemplado reportar el progreso de los queries así que
     * esperaremos a su implementación.
     *
     * ¡MariaDB es hasta 10 veces más rápida en este query que MySQL!
     *
     * @param vot La votación que se va a resolver.
     * @return La matriz de preferencia completa de esta votacion
     */
    private long[][] getMatrizDePreferenciaRapido(Votacion vot) {
        List<Opcion> opciones = vot.getOpciones();
        int t = opciones.size();
        long[][] m = new long[t][t];
        StringBuilder q = new StringBuilder("select count(*), v1.OPCION_ID, v2.OPCION_ID from VOTO as v1, ").append("(select MIEMBRO_ID, OPCION_ID, RANK from VOTO where VOTACION_ID=").append(vot.getId()).append(" union select v.MIEMBRO_ID, o.ID as OPCION_ID, null as RANK from ").append("VOTO as v, OPCION as o where v.VOTACION_ID=").append(vot.getId()).append(" and o.VOTACION_ID=").append(vot.getId()).append(" and o.ID != v.OPCION_ID and o.ID not in (select vt.OPCION_ID from ").append("VOTO as vt where vt.MIEMBRO_ID=v.MIEMBRO_ID and vt.VOTACION_ID=").append(vot.getId()).append(")) ").append("as v2 where v1.MIEMBRO_ID=v2.MIEMBRO_ID and ((v1.RANK < v2.RANK) or ").append("(v2.RANK is null)) and v2.OPCION_ID != v1.OPCION_ID and v1.VOTACION_ID=").append(vot.getId()).append(" group by v1.OPCION_ID, v2.OPCION_ID");
        mrlog.log(Level.FINE, q.toString());
        List<Object> objs = em.createNativeQuery(q.toString()).getResultList();
        Map vals = new HashMap<Preferencia, Long>();
        Preferencia p;
        for (Object ob : objs) {
            Object[] oa = (Object[]) ob;
            long i = (Long) oa[1];
            long j = (Long) oa[2];
            long c = (Long) oa[0];
            p = new Preferencia(i, j);
            vals.put(p, c);
        }
        mrlog.log(Level.FINE, "El query regresó con {0} valores...", vals.size());
        for (int i = 0; i < t; i++) {
            for (int j = 0; j < t; j++) {
                if (!opciones.get(i).equals(opciones.get(j))) {
                    long a;
                    try {
                        a = (Long) vals.get(new Preferencia(opciones.get(i).getId(), opciones.get(j).getId()));
                    } catch (Exception e) {
                        a = 0;
                    }
                    m[i][j] = a;
                }
            }
            mv.getConteos().put(vot.getId(), Integer.valueOf((i * 100) / t));
        }
        return m;
    }

    /**
     * Método lento para la generación de la matriz de preferencias de la
     * votación. No estamos usando este método porque es muy lento aunque sí
     * reporta el avance del conteo. La idea es tener un método que reporte el
     * avance del conteo.
     *
     * @param vot La votación en cuestión
     * @return La matriz de preferencia.
     */
    private long[][] getMatrizDePreferenciaLento(Votacion vot) {
        // Método más lento pero que reporta avance a la barra de progreso...
        List<Opcion> ops = vot.getOpciones();
        int t = ops.size();
        long[][] m = new long[t][t];
        StringBuilder q = new StringBuilder();
        for (int i = 0; i < t; i++) {
            for (int j = 0; j < t; j++) {
                if (!ops.get(i).equals(ops.get(j))) {
                    long a;
                    try {
                        q.setLength(0);
                        q.append("select count(*) from voto as v1, ");
                        q.append("(select miembro_id, opcion_id, rank from voto where votacion_id=").append(vot.getId())
                                .append(" and opcion_id=").append(ops.get(j).getId())
                                .append(" union select v.miembro_id, o.id as opcion_id, null as rank from voto as v, opcion as o where v.votacion_id=").append(vot.getId())
                                .append(" and o.votacion_id=").append(vot.getId())
                                .append(" and o.id=").append(ops.get(j).getId())
                                .append(" and o.id not in (select vt.opcion_id from voto as vt where vt.opcion_id=").append(ops.get(j).getId())
                                .append(" and vt.miembro_id=v.miembro_id and vt.votacion_id=").append(vot.getId())
                                .append(")) as v2 where v1.miembro_id=v2.miembro_id and ((v1.rank < v2.rank) or (v2.rank is null)) and v1.opcion_id=").append(ops.get(i).getId())
                                .append(" and v1.votacion_id=").append(vot.getId())
                                .append(" group by v1.opcion_id, v2.opcion_id");
                        //mrlog.log(Level.FINEST, q.toString());
                        a = (Long) em.createNativeQuery(q.toString()).getSingleResult();
                    } catch (Exception e) {
                        a = 0;
                    }
                    m[i][j] = a;
                }
            }
            mv.getConteos().put(vot.getId(), Integer.valueOf((i * 100) / t));
        }
        mrlog.log(Level.FINE, q.toString());
        return m;
    }

    public String getContentFromWiki(Object para) throws Exception {
        String res = null;
        String url = null;
        String titulo = null;
        mrlog.log(Level.FINE, "Determinando el tipo de clase...");
        if (para instanceof Opcion) {
            mrlog.log(Level.FINE, "La clase es Opcion...");
            Opcion op = (Opcion) para;
            if (op.getUrl() != null) {
                url = op.getUrl();
                System.out.println("El URL no es nulo: " + url);
            }
            titulo = op.getNombre();
        }
        if (para instanceof Votacion) {
            mrlog.log(Level.FINE, "La clase es Votacion...");
            Votacion vot = (Votacion) para;
            if (vot.getUrl() != null) {
                url = vot.getUrl();
            }
            titulo = vot.getNombre();
        }
        url = url.replace("?", "");
        url = url.replace("=", "");
        url = url.replace("/", "");
        url = url.replace("\\", "");
        url = url.replace("&", "");

        url = "http://wiki.wikipartido.mx/index.php/" + url + "?action=render";
        titulo = "<h1>" + titulo + "</h1>";
        System.out.println("URL: " + url);


        HttpEntity entity = null;
        HttpClient httpclient = null;
        HttpGet httpget = null;
        StringBuilder html = new StringBuilder();
        html.append(titulo);
        try {
            httpclient = new DefaultHttpClient();
            httpget = new HttpGet(url);
            HttpResponse r = httpclient.execute(httpget);
            if (r.getStatusLine().getStatusCode() == 404) {
                throw new Exception("No existe el documento en el Wiki");
            }
            entity = r.getEntity();

        } catch (Exception e) {
            throw e;
        }

        InputStream instream = null;
        if (entity != null) {

            try {
                instream = entity.getContent();
                byte[] buffer = new byte[1024];
                int read;
                while ((read = instream.read(buffer)) != -1) {
                    //out.write(buffer, 0, read);
                    html.append(new String(buffer, 0, read, "UTF-8"));
                }
            } catch (IOException ex) {
                throw ex;

            } catch (RuntimeException ex) {
                httpget.abort();
                throw ex;
            } finally {

                instream.close();

            }
            httpclient.getConnectionManager().shutdown();
        }
        // Repara las imagenes que vienen del wiki ¬¬
        res = html.toString().replace("src=\"/", "src=\"http://wiki.wikipartido.mx/");

        return res;
    }
    

    public long getParticipacion(Votacion vot) {
       return  (long) em.createQuery("select v.miembro.id from Voto v where v.votacion = :vot group by v.miembro.id").setParameter("vot", vot).getResultList().size();       
    }

    public long getPoblacion(Votacion vot) {
        long p = 0;
        if (vot.getEstados().size() > 0) {
            for (Estado e : vot.getEstados()) {
                p += cuantosMiembrosActivos(e);
            }
        } else {
            p = cuantosMiembrosActivos();
        }
        return p;
    }

    public long cuantosMiembrosActivos() {
        return cuantosMiembrosActivos(null);
    }

    public long cuantosMiembrosActivos(Estado estado) {
        if (estado == null) {
            return (long) em.createQuery("select count(m) from Miembro m where m.paso > 1").getSingleResult();
        } else {
            return (long) em.createQuery("select count(m) from Miembro m where m.estado = :estado and m.paso > 1").setParameter("estado", estado).getSingleResult();
        }
    }

    public long getQuorum(long poblacion) {
        double q = Math.exp(-.25*(Math.log(poblacion)/Math.log(2)))*100;
        return (long)(q*poblacion/100);
    }
    
    public boolean perteneceAEstadosDeVotacion(Miembro m, Votacion v) {
        boolean pertenece = true;
        if (v.getEstados().size() < 1) {
            return pertenece;
        }
        try {
        em.createQuery("select v from Votacion v, Estado e where e member of v.estados and e = :e and v=:v")
                .setParameter("e", m.getEstado())
                .setParameter("v", v).getSingleResult();
        } catch (Exception e) {
            mrlog.log(Level.FINE,e.getMessage());
            return false;
        }
        return pertenece;
    }

    /**
     * El LazyResult nos es útil en resultados que requieren paginación.
     */
    public class LazyResult {

        private int size;
        private List set;

        public LazyResult(List set, int totalSize) {
            this.set = set;
            this.size = totalSize;
        }

        /**
         * @return the set
         */
        public List getSet() {
            return set;
        }

        /**
         * @param set the set to set
         */
        public void setSet(List set) {
            this.set = set;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }

        /**
         * @param size the size to set
         */
        public void setSize(int size) {
            this.size = size;
        }
    }

    class Preferencia {

        private long i;
        private long j;

        public Preferencia(long i, long j) {
            this.i = i;
            this.j = j;
        }

        @Override
        public boolean equals(Object o) {
            if (getClass() == o.getClass()) {
                if ((((Preferencia) o).getI() == this.i) && (((Preferencia) o).getJ() == this.j)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        /**
         * @return the i
         */
        public long getI() {
            return i;
        }

        /**
         * @param i the i to set
         */
        public void setI(long i) {
            this.i = i;
        }

        /**
         * @return the j
         */
        public long getJ() {
            return j;
        }

        /**
         * @param j the j to set
         */
        public void setJ(long j) {
            this.j = j;
        }
    }
}
