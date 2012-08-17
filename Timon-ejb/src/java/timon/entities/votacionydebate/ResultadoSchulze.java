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

package timon.entities.votacionydebate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Alfonso Tames
 * 
 */
@Entity
public class ResultadoSchulze implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    @ManyToOne
    private Votacion votacion;
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Score> scores;
    private long[][] pref;
    private long[][] sp;
    private long exetime;
    private int avance=0;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fechaConteo;
    private int miembros;
    private float quorum;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ResultadoSchulze)) {
            return false;
        }
        ResultadoSchulze other = (ResultadoSchulze) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.votacionydebate.ResultadoSchulze[ id=" + id + " ]";
    }

    /**
     * @return the scores
     */
    public List<Score> getScores() {
        return scores;
    }

    /**
     * @param scores the scores to set
     */
    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    /**
     * @return the pref
     */
    public long[][] getPref() {
        return pref;
    }

    /**
     * @param pref the pref to set
     */
    public void setPref(long[][] pref) {
        this.pref = pref;
    }

    /**
     * @return the sp
     */
    public long[][] getSp() {
        return sp;
    }

    /**
     * @param sp the sp to set
     */
    public void setSp(long[][] sp) {
        this.sp = sp;
    }


    /**
     * @return the exetime
     */
    public long getExetime() {
        return exetime;
    }

    /**
     * @param exetime the exetime to set
     */
    public void setExetime(long exetime) {
        this.exetime = exetime;
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
     * @return the fechaConteo
     */
    public Date getFechaConteo() {
        return fechaConteo;
    }

    /**
     * @param fechaConteo the fechaConteo to set
     */
    public void setFechaConteo(Date fechaConteo) {
        this.fechaConteo = fechaConteo;
    }

    /**
     * @return the avance
     */
    public int getAvance() {
        return avance;
    }

    /**
     * @param avance the avance to set
     */
    public void setAvance(int avance) {
        this.avance = avance;
    }

    /**
     * @return the miembros
     */
    public int getMiembros() {
        return miembros;
    }

    /**
     * @param miembros the miembros to set
     */
    public void setMiembros(int miembros) {
        this.miembros = miembros;
    }

    /**
     * @return the quorum
     */
    public float getQuorum() {
        return quorum;
    }

    /**
     * @param quorum the quorum to set
     */
    public void setQuorum(float quorum) {
        this.quorum = quorum;
    }
    
}
