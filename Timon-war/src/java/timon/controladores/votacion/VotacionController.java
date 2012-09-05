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
package timon.controladores.votacion;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.datagrid.DataGrid;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import timon.controladores.UserManager;
import timon.entities.registro.Estado;
import timon.entities.registro.Miembro;
import timon.entities.votacionydebate.LogVotacion;
import timon.entities.votacionydebate.Opcion;
import timon.entities.votacionydebate.ResultadoSchulze;
import timon.entities.votacionydebate.Tema;
import timon.entities.votacionydebate.Votacion;
import timon.entities.votacionydebate.Voto;
import timon.sessionbeans.VotoYDebateLogic;
import timon.sessionbeans.VotoYDebateLogic.LazyResult;
import timon.singletons.MonitorDeVotaciones;

/**
 *
 * @author Alfonso Tamés
 */
@Named(value = "vc")
@SessionScoped
public class VotacionController implements Serializable {

    private static final Logger mrlog = Logger.getLogger(VotacionController.class.getName());
    @Inject
    UserManager um;
    @Inject
    VotoYDebateLogic vl;
    @Inject
    MonitorDeVotaciones mv;
    private long vid;
    private long vact = 0;
    private Votacion votacion;
    private String wikiDescVotacion;
    private DualListModel<Opcion> opciones;
    private LogVotacion logvot = new LogVotacion();
    private ResultadoSchulze rs;
    private LazyDataModel<Votacion> votaciones;
    private List<SelectItem> temas;
    private Tema filtroTema;
    private String filtroTexto;
    private Estado filtroEstado = null;
    private boolean filtroAbiertas = false;
    private Date filtroFechaCierreDesde = null;
    private Date filtroFechaCierreHasta = null;
    private DataGrid dataGrid;
    private Date hoy;
    

    public VotacionController() {
        logvot = new LogVotacion();
        rs = null;
        hoy = new Date();
        votaciones = new LazyDataModel() {
            @Override
            public List<Votacion> load(int start, int max, String string, SortOrder so, Map map) {
                mrlog.log(Level.FINE, "Filtro Tema: {0}", getFiltroTema());
                LazyResult vots = vl.getVotaciones(start, max, filtroTema, filtroTexto, filtroEstado, filtroAbiertas, filtroFechaCierreDesde, filtroFechaCierreHasta);
                this.setRowCount(vots.getSize());
                return vots.getSet();
            }
        };
        mrlog.log(Level.FINE, "votaciones Row Count: {0} Page Size: {1}", new Object[]{votaciones.getRowCount(), votaciones.getPageSize()});
        
    }
    
    public void reset() {
        dataGrid.setFirst(0);
    }
    
    public long getParticipacion() {
        return vl.getParticipacion(votacion);
    }
    public long getQuorum() {
        return vl.getQuorum(vl.getPoblacion(votacion));
    }
    public long getPoblacion() {
        return vl.getPoblacion(votacion);
    }
    
    public boolean isPerteneceAEstadosDeVotacion() {
        return vl.perteneceAEstadosDeVotacion(um.getUser(), votacion);
    }

    public ResultadoSchulze getRs() throws Exception {
        if (rs == null) {
            rs = vl.getUltimoResultado(votacion);

            if (rs == null) {
                // Verifica si hay una votacion en proceso
                Integer avance = mv.getConteos().get(votacion.getId());
                if (avance == null) {
                    try {
                        vl.cuentaConSchulze(votacion);

                    } catch (Exception ex) {
                        mrlog.log(Level.SEVERE, "Ocurrió un error al tratar de iniciar el conteo de la votacion {0}", ex.getMessage());
                        throw new Exception("Ocurrió un error al tratar de iniciar el conteo de la votacion" + ex.getMessage());
                    }
                } else {
                    if (avance == 100) {
                        try {
                            vl.cuentaConSchulze(votacion);
                        } catch (Exception ex) {
                            mrlog.log(Level.SEVERE, "Ocurrió un error al tratar de iniciar el conteo de la votacion {0}", ex.getMessage());
                            throw new Exception("Ocurrió un error al tratar de iniciar el conteo de la votacion" + ex.getMessage());
                        }
                    }
                }
            }

            if (rs != null) {
                Collections.sort(rs.getScores());
            }
        }
        return rs;
    }
    
    public String getStatus() {
        return getStatus(votacion);
    }
    
    public String getStatus(Votacion vot) {
        String status = "";
        ResultadoSchulze rs= vl.getUltimoResultado(vot);
        if (rs == null) {
            return "";
        }
        if (rs.isHayQuorum() && hoy.after(vot.getFechaCierre())) {
            return "efectiva";
        }
        if (rs.isHayQuorum() && hoy.before(vot.getFechaCierre())) {
            return "conquorum";
        }
        if (!rs.isHayQuorum() && hoy.after(vot.getFechaCierre())) {
            return "sinquorum";
        }
        return status;
    }
    

    // Que tan pesado es un delegado para cierta votacion
    public long numeroAtomico(Miembro delegado) {
        return vl.numeroAtomico(votacion, delegado);
    }

    public void verVotacion() {

        List<Opcion> opcionesDisponibles;
        List<Opcion> opcionesVotadas = new LinkedList<>();
        mrlog.log(Level.FINEST, "vact {0}", getVact());
        mrlog.log(Level.FINEST, "vid {0}", getVid());
        if (getVact() != getVid() || getVact() == 0) {
            votacion = vl.getVotacion(getVid());
            setVact(getVid());
            wikiDescVotacion = "No hay documento en el Wiki asociado a esta votación";
            rs = null;
            if (votacion.getUrl() != null) {
                if (!votacion.getUrl().equals("")) {
                    System.out.println("Se supone que esto no es nulo:-" + votacion.getUrl() + "-");

                    try {
                        wikiDescVotacion = vl.getContentFromWiki(votacion);
                    } catch (Exception ex) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No es posible extraer el contenido del documento " + votacion.getUrl() + " del Wiki.", null));
                    }
                }
            }
            try {
                rs = getRs();
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ocurrio un error al tratar de obtener el resultado de la eleccion.", null));
            }
            try {
                opcionesDisponibles = vl.getOpcionesParaVotacion(votacion);
                opciones = new DualListModel<>(opcionesDisponibles, opcionesVotadas);
            } catch (Exception ex) {
                mrlog.log(Level.SEVERE, null, ex);
            }

        }
    }

    public void onCompleteProgressBar() {
        rs = null;
    }

    public List<Voto> getVotos() {
        return vl.getVotosPara(um.getUser(), votacion);
    }

    public LogVotacion getLogVotacion() {
        return vl.getLogVotacion(um.getUser(), votacion);
    }

    public boolean tieneImagenLaOpcion(long id) {
        return vl.tieneImagenLaOpcion(id);
    }

    public String guardarVotacion() {
        if (um.getUser() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Para votar debe ingresar a la plataforma como miembro.", null));
            return "";
        }

        List<Opcion> votos = opciones.getTarget();
        logvot.setVotacion(votacion);
        logvot.setMiembro(um.getUser());
        logvot.setFecha(new Date());
        try {
            vl.guardarVotacion(logvot, votos);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡Gracias por Votar! Tus votos se han guardado con éxito", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al tratar de guardar la votación: " + e.getMessage(), null));
            mrlog.log(Level.SEVERE, "Error al tratar de guardar la votacion!!! {0}", e.getMessage());
            return "";
        }
        mrlog.log(Level.FINE, "Acabe de guardar los votos de {0} ahora invoco el conteo de forma asíncrona...", um.getUser());
        try {
            mv.getConteos().put(votacion.getId(), 0);
            vl.cuentaConSchulze(votacion);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al tratar de invocar la votacion!!!" + e.getMessage(), null));
            mrlog.log(Level.SEVERE, "Error al tratar de invocar la votacion!!! {0}", e.getMessage());
        }
        logvot = new LogVotacion();
        return "votResultados.xhtml?vid=" + vid + "&amp;faces-redirect=true";
    }

    public Integer getAvance() {
        if (mv.getConteos().get(votacion.getId()) == null) {
            return 100;
        }
        int a;
        a = mv.getConteos().get(votacion.getId());
        mrlog.log(Level.FINE, "Reportando un avance de... {0} para {1}", new Object[]{a, votacion.getId()});
        return a;
    }

    /**
     * @return the votacion
     */
    public Votacion getVotacion() {
        return votacion;
    }

    /**
     * @param votacion the votacion to set
     */
    public void setVotacion(Votacion votacion) {
        this.votacion = votacion;
    }

    /**
     * @return the wikiDescVotacion
     */
    public String getWikiDescVotacion() {
        return wikiDescVotacion;
    }

    /**
     * @param wikiDescVotacion the wikiDescVotacion to set
     */
    public void setWikiDescVotacion(String wikiDescVotacion) {
        this.wikiDescVotacion = wikiDescVotacion;
    }

    /**
     * @return the vid
     */
    public long getVid() {
        return vid;
    }

    /**
     * @param vid the vid to set
     */
    public void setVid(long vid) {
        this.vid = vid;
    }

    /**
     * @return the vact
     */
    public long getVact() {
        return vact;
    }

    /**
     * @param vact the vact to set
     */
    public void setVact(long vact) {
        this.vact = vact;
    }

    /**
     * @return the opciones
     */
    public DualListModel<Opcion> getOpciones() {
        return opciones;
    }

    /**
     * @param opciones the opciones to set
     */
    public void setOpciones(DualListModel<Opcion> opciones) {
        this.opciones = opciones;
    }

    /**
     * @return the logvot
     */
    public LogVotacion getLogvot() {
        return logvot;
    }

    /**
     * @param logvot the logvot to set
     */
    public void setLogvot(LogVotacion logvot) {
        this.logvot = logvot;
    }

    /**
     * @return the votaciones
     */
    public LazyDataModel getVotaciones() {
        mrlog.log(Level.FINE, "VotacionController::getVotaciones");
        mrlog.log(Level.FINE, "VotacionController::getVotaciones Votaciones tiene {0}", votaciones.getRowCount());

        return votaciones;
    }

    /**
     * @param votaciones the votaciones to set
     */
    public void setVotaciones(LazyDataModel votaciones) {
        this.votaciones = votaciones;
    }

    /**
     * @return the filtroAbiertas
     */
    public boolean isFiltroAbiertas() {
        return filtroAbiertas;
    }

    /**
     * @param filtroAbiertas the filtroAbiertas to set
     */
    public void setFiltroAbiertas(boolean filtroAbiertas) {
        this.filtroAbiertas = filtroAbiertas;
    }

    /**
     * @return the filtroFechaCierreDesde
     */
    public Date getFiltroFechaCierreDesde() {
        return filtroFechaCierreDesde;
    }

    /**
     * @param filtroFechaCierreDesde the filtroFechaCierreDesde to set
     */
    public void setFiltroFechaCierreDesde(Date filtroFechaCierreDesde) {
        this.filtroFechaCierreDesde = filtroFechaCierreDesde;
    }

    /**
     * @return the filtroFechaCierreHasta
     */
    public Date getFiltroFechaCierreHasta() {
        return filtroFechaCierreHasta;
    }

    /**
     * @param filtroFechaCierreHasta the filtroFechaCierreHasta to set
     */
    public void setFiltroFechaCierreHasta(Date filtroFechaCierreHasta) {
        this.filtroFechaCierreHasta = filtroFechaCierreHasta;
    }

    /**
     * @return the filtroEstado
     */
    public Estado getFiltroEstado() {
        return filtroEstado;
    }

    /**
     * @param filtroEstado the filtroEstado to set
     */
    public void setFiltroEstado(Estado filtroEstado) {
        this.filtroEstado = filtroEstado;
    }



    /**
     * @return the temas
     */
    public List<SelectItem> getTemas() {
        if (temas == null) {
            temas = new ArrayList<>();

            temas.add(new SelectItem(null,"Todos los Temas"));
            for (Tema t : vl.getTemas()) {
                temas.add(new SelectItem(t, t.getNombre()));
            }
        }
        return temas;
    }

    /**
     * @param temas the temas to set
     */
    public void setTemas(List<SelectItem> temas) {
        this.temas = temas;
    }

    /**
     * @return the filtroTema
     */
    public Tema getFiltroTema() {
        return filtroTema;
    }

    /**
     * @param filtroTema the filtroTema to set
     */
    public void setFiltroTema(Tema filtroTema) {
        this.filtroTema = filtroTema;
    }

    /**
     * @return the dataGrid
     */
    public DataGrid getDataGrid() {
        return dataGrid;
    }

    /**
     * @param dataGrid the dataGrid to set
     */
    public void setDataGrid(DataGrid dataGrid) {
        this.dataGrid = dataGrid;
    }

    /**
     * @return the filtroTexto
     */
    public String getFiltroTexto() {
        return filtroTexto;
    }

    /**
     * @param filtroTexto the filtroTexto to set
     */
    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    /**
     * @return the hoy
     */
    public Date getHoy() {
        return hoy;
    }

    /**
     * @param hoy the hoy to set
     */
    public void setHoy(Date hoy) {
        this.hoy = hoy;
    }
}
