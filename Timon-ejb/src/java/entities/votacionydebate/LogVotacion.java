/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.votacionydebate;

import entities.registro.Miembro;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author alfonso
 */
@Entity
public class LogVotacion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Miembro miembro;
    @ManyToOne
    private Votacion votacion;
    @Lob
    private String explicacion;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha;    
    private String ip;   
    private String sesion;
    
    
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
        if (!(object instanceof LogVotacion)) {
            return false;
        }
        LogVotacion other = (LogVotacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.votacionydebate.ExplicaVoto[ id=" + id + " ]";
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
     * @return the explicacion
     */
    public String getExplicacion() {
        return explicacion;
    }

    /**
     * @param explicacion the explicacion to set
     */
    public void setExplicacion(String explicacion) {
        this.explicacion = explicacion;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the sesion
     */
    public String getSesion() {
        return sesion;
    }

    /**
     * @param sesion the sesion to set
     */
    public void setSesion(String sesion) {
        this.sesion = sesion;
    }

    /**
     * @return the fecha
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * @return the miembro
     */
    public Miembro getMiembro() {
        return miembro;
    }

    /**
     * @param miembro the miembro to set
     */
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }
    
}
