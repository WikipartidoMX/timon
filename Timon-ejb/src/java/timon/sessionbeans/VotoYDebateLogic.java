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

    /*
     * La lógica de guardar la votación y contar los votos está separada
     *
     */
    public void guardarVotacion(LogVotacion logvot, List<Opcion> opciones) throws Exception {
        // TODO: Verificar que el delegado/miembro sea del estado o estados        
        if (logvot.getMiembro().getPaso() < 2) {
            throw new Exception("Aún no eres miembro con derecho a voto :( ¡Afíliate hoy!");
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
        rs.setVotacion(vot);
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
        List<Score> scores = new ArrayList<Score>();
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
        rs.setScores(scores);
        rs.setPref(prefe);
        rs.setSp(p);
        rs.setExetime(System.currentTimeMillis() - st);
        rs.setAvance(100);
        mv.getConteos().put(vot.getId(), 100);
    }

    /**
     * Generación de la matriz de preferencia de esta elección.
     * Este es el método rápido y no reporta el progreso de la operación porque
     * la base de datos hace en un solo paso la generación de la matriz.
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
        StringBuilder q = new StringBuilder("select count(*), v1.opcion_id, v2.opcion_id from voto as v1, ").append("(select miembro_id, opcion_id, rank from voto where votacion_id=").append(vot.getId()).append(" union select v.miembro_id, o.id as opcion_id, null as rank from ").append("voto as v, opcion as o where v.votacion_id=").append(vot.getId()).append(" and o.votacion_id=").append(vot.getId()).append(" and o.id != v.opcion_id and o.id not in (select vt.opcion_id from ").append("voto as vt where vt.miembro_id=v.miembro_id and vt.votacion_id=").append(vot.getId()).append(")) ").append("as v2 where v1.miembro_id=v2.miembro_id and ((v1.rank < v2.rank) or ").append("(v2.rank is null)) and v2.opcion_id != v1.opcion_id and v1.votacion_id=").append(vot.getId()).append(" group by v1.opcion_id, v2.opcion_id");
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
 * Método lento para la generación de la matriz de preferencias de la votación.
 * No estamos usando este método porque es muy lento aunque sí reporta el avance del conteo.
 * La idea es tener un método que reporte el avance del conteo.
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
        mrlog.log(Level.FINE,"Determinando el tipo de clase...");
        if (para instanceof Opcion) {
            mrlog.log(Level.FINE,"La clase es Opcion...");
            Opcion op = (Opcion)para;
            if (op.getUrl() != null) {
                url = op.getUrl();
                System.out.println("El URL no es nulo: "+url);
            }
            titulo = op.getNombre();
        }
        if (para instanceof Votacion) {
            mrlog.log(Level.FINE,"La clase es Votacion...");
            Votacion vot = (Votacion)para;
            if (vot.getUrl() != null) {
                url = vot.getUrl();
            }
            titulo = vot.getNombre();
        }        
                url = "http://wiki.wikipartido.mx/wiki/index.php/" + url + "?action=render";
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
                    html.append(new String(buffer,0,read,"UTF-8"));
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
        res = html.toString().replace("src=\"/wiki", "src=\"http://wiki.wikipartido.mx/wiki");
        
        return res;
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
